@file:Suppress("TooManyFunctions", "MemberVisibilityCanBePrivate")

package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.GeneratorFactory
import edu.illinois.cs.cs125.jenisol.core.generators.ReceiverGenerator
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.pow
import kotlin.random.Random
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

class Solution(val solution: Class<*>, val captureOutput: CaptureOutput = ::defaultCaptureOutput) {
    data class Settings(
        val methodCount: Int = -1,
        val receiverCount: Int = -1,
        val seed: Int = -1,
        val simpleCount: Int = -1,
        val edgeCount: Int = -1,
        val mixedCount: Int = -1,
        val fixedCount: Int = -1
    ) {
        companion object {
            val DEFAULTS = Settings(
                simpleCount = Int.MAX_VALUE,
                edgeCount = Int.MAX_VALUE,
                mixedCount = Int.MAX_VALUE,
                fixedCount = Int.MAX_VALUE
            )
        }
    }

    init {
        solution.declaredFields.filter { it.isStatic() && !it.isJenisol() }.also {
            check(it.isEmpty()) { "No support for testing classes with static fields yet" }
        }
    }

    val allExecutables =
        (solution.declaredMethods.toSet() + solution.declaredConstructors.toSet())
            .filterNotNull()
            .filter {
                !it.isPrivate() && !it.isJenisol()
            }.toSet().also {
                check(it.isNotEmpty()) { "Found no methods to test" }
            }

    fun Executable.receiverParameter() = parameterTypes.any { it == solution }

    val receiverGenerators = allExecutables.filter { executable ->
        !executable.receiverParameter()
    }.filter { executable ->
        when (executable) {
            is Constructor<*> -> true
            is Method -> executable.isStatic() && executable.returnType == solution
            else -> error("Unexpected executable type")
        }
    }.toSet()
    val methodsToTest = (allExecutables - receiverGenerators).also {
        check(it.isNotEmpty()) { "Found methods that generate receivers but no ways to test them" }
    }
    val needsReceiver = methodsToTest.filter { executable ->
        executable.receiverParameter() || (executable is Method && !executable.isStatic())
    }.toSet()
    val receiverTransformers = methodsToTest.filterIsInstance<Method>().filter { method ->
        method.returnType != solution
    }.filter { method ->
        !method.isStatic() || method.receiverParameter()
    }.toSet()

    val skipReceiver = needsReceiver.isEmpty() && receiverTransformers.isEmpty() &&
        (receiverGenerators.isEmpty() ||
            (receiverGenerators.size == 1 && receiverGenerators.first().parameters.isEmpty()))

    init {
        if (needsReceiver.isNotEmpty()) {
            check(receiverGenerators.isNotEmpty()) { "No way to generate needed receivers" }
        }
        if (!skipReceiver && receiverGenerators.isNotEmpty()) {
            check(receiverTransformers.isNotEmpty()) { "No way to verify generated receivers" }
        }
    }

    val initializer: Executable? = solution.superclass.declaredMethods.filter {
        it.isInitializer()
    }.also {
        check(it.size <= 1) { "Solution parent class ${solution.superclass.name} has multiple initializers" }
    }.firstOrNull()?.also {
        Initializer.validate(it)
    }
    private val initializers = initializer?.let { setOf(it) } ?: setOf()
    val receiversAndInitializers = receiverGenerators + initializers

    val generatorFactory: GeneratorFactory = GeneratorFactory(allExecutables + initializers, this)

    /*
    val proxyInterface = solution.interfaces.filter { it.isCompare() }.also {
        check(it.size <= 1) { "Can only declare one compare interface" }
    }.firstOrNull()

    fun createProxy(submission: Any) = proxyInterface?.let {
        Proxy.newProxyInstance(submission::class.java.classLoader, listOf(it).toTypedArray()) { _, method, args ->
            method.invoke(submission, *args)
        }
    } ?: error("No interface to proxy to")
     */

    val receiverEntropy: Int
    val methodEntropy: Int

    // These calculations should be improved to create a better test balance
    init {
        val emptyInitializers =
            (receiverGenerators.size == 1 && receiverGenerators.first().parameters.isEmpty()) &&
                (initializer?.parameters?.isEmpty() ?: true)

        @Suppress("MagicNumber")
        receiverEntropy = when {
            skipReceiver -> 0
            emptyInitializers -> 2
            else -> 5
        }
        @Suppress("MagicNumber")
        methodEntropy = when {
            methodsToTest.size == 1 && methodsToTest.first().parameters.isEmpty() -> 0
            else -> 5
        }
    }

    val defaultReceiverCount = 2.0.pow(receiverEntropy.toDouble()).toInt()
    val defaultMethodCount = 2.0.pow(methodEntropy.toDouble()).toInt()

    @Suppress("unused")
    val defaultTotalTests = defaultReceiverCount * (defaultMethodCount + 1)

    fun setCounts(settings: Settings): Settings {
        val receiverCount = if (settings.receiverCount != -1) {
            settings.receiverCount
        } else {
            defaultReceiverCount
        }
        val testCount = if (settings.methodCount != -1) {
            settings.methodCount
        } else {
            defaultMethodCount
        }
        return settings.copy(
            receiverCount = receiverCount, methodCount = testCount
        ).also {
            check(it.receiverCount >= 0) { "Invalid receiver count" }
            check(it.methodCount > 0) { "Invalid test count" }
        }
    }

    val verifier: Method? = solution.declaredMethods.filter { it.isVerify() }.also {
        check(it.size <= 1) { "No support yet for multiple verifiers" }
    }.firstOrNull()?.also {
        check(methodsToTest.size == 1) { "No support yet for multiple verifiers" }
        val methodToTest = methodsToTest.first()
        val returnType = when (methodToTest) {
            is Constructor<*> -> solution
            is Method -> methodToTest.genericReturnType
            else -> error("Unexpected executable type")
        }
        Verify.validate(it, returnType, methodToTest.parameterTypes)
    }

    val receiverCompare = object : Comparator {
        override val descendants = true
        override fun compare(solution: Any, submission: Any): Boolean = true
    }

    fun submission(submission: Class<*>) = Submission(this, submission)
}

fun Set<Executable>.cycle() = sequence {
    yield(shuffled().first())
}

class RandomGroup(seed: Long = Random.nextLong()) {
    val solution = java.util.Random().also { it.setSeed(seed) }
    val submission = java.util.Random().also { it.setSeed(seed) }
    val solutionCopy = java.util.Random().also { it.setSeed(seed) }
    val submissionCopy = java.util.Random().also { it.setSeed(seed) }
    val synced: Boolean
        get() = setOf(
            solution.nextLong(), solutionCopy.nextLong(), submission.nextLong(), submissionCopy.nextLong()
        ).size == 1
}

class ClassDesignError(klass: Class<*>, executable: Executable) : Exception(
    "Submission class ${klass.name} didn't provide ${if (executable is Method) {
        "method"
    } else {
        "constructor"
    }} ${executable.fullName()}"
)

class Submission(val solution: Solution, val submission: Class<*>) {
    val submissionExecutables = solution.allExecutables.map { solutionExecutable ->
        when (solutionExecutable) {
            is Constructor<*> -> submission.findConstructor(solutionExecutable, solution.solution)
            is Method -> submission.findMethod(solutionExecutable, solution.solution)
            else -> error("Encountered unexpected executable type: $solutionExecutable")
        }?.let { executable ->
            solutionExecutable to executable
        } ?: throw ClassDesignError(submission, solutionExecutable)
    }.toMap().toMutableMap().also {
        if (solution.initializer != null) {
            it[solution.initializer] = solution.initializer
        }
    }.toMap()

    fun MutableList<TestRunner>.readyCount() = filter { it.ready }.count()

    val comparators = Comparators(
        mutableMapOf(solution.solution to solution.receiverCompare, submission to solution.receiverCompare)
    )

    fun compare(solution: Any?, submission: Any?) = when (solution) {
        null -> submission == null
        else -> solution.deepEquals(submission, comparators)
    }

    fun verify(result: TestResult<*, *>) {
        solution.verifier?.also { customVerifier ->
            @Suppress("TooGenericExceptionCaught")
            try {
                unwrap { customVerifier.invoke(null, result) }
            } catch (e: Throwable) {
                result.differs.add(TestResult.Differs.VERIFIER_THREW)
                result.verifierThrew = e
            }
        } ?: defaultVerify(result)
    }

    fun defaultVerify(result: TestResult<*, *>) {
        val solution = result.solution
        val submission = result.submission

        if (solution.stdout.isNotBlank() && solution.stdout != submission.stdout) {
            result.differs.add(TestResult.Differs.STDOUT)
        }
        if (solution.stderr.isNotBlank() && solution.stderr != submission.stderr) {
            result.differs.add(TestResult.Differs.STDERR)
        }

        if (result.type == TestResult.Type.METHOD && !compare(solution.returned, submission.returned)) {
            result.differs.add(TestResult.Differs.RETURN)
        }
        if (!compare(solution.threw, submission.threw)) {
            result.differs.add(TestResult.Differs.THREW)
        }
        if (!compare(solution.parameters, submission.parameters)) {
            result.differs.add(TestResult.Differs.PARAMETERS)
        }
    }

    fun test(passedSettings: Solution.Settings = Solution.Settings()): TestResults {
        val settings = solution.setCounts(Solution.Settings.DEFAULTS merge passedSettings)

        val random = if (passedSettings.seed == -1) {
            Random
        } else {
            Random(passedSettings.seed.toLong())
        }

        val runners: MutableList<TestRunner> = mutableListOf()
        var stepCount = 0
        var totalCount = 0

        val receiverGenerators = sequence {
            while (true) {
                yieldAll(solution.receiverGenerators.toList().shuffled(random))
            }
        }

        val (receiverGenerator, initialGenerators) = if (!solution.skipReceiver) {
            check(settings.receiverCount > 1) { "Incorrect receiver count" }

            val generators = solution.generatorFactory.get(
                random, settings, null, solution.receiversAndInitializers
            )
            while (runners.readyCount() < settings.receiverCount) {
                TestRunner(runners.size, this, generators, receiverGenerators).also { runner ->
                    runner.next(stepCount++)
                    runners.add(runner)
                }
            }
            Pair(ReceiverGenerator(random, runners.filter { it.ready }.toMutableList()), generators)
        } else {
            Pair(null, null)
        }

        val generators = solution.generatorFactory.get(random, settings, receiverGenerator, from = initialGenerators)
        runners.filter { it.ready }.forEach {
            it.generators = generators
        }

        val totalTests = settings.receiverCount * settings.methodCount
        while (true) {
            val usedRunner = if (runners.readyCount() < settings.receiverCount) {
                TestRunner(runners.size, this, generators, receiverGenerators).also { runner ->
                    runner.next(stepCount++)
                    runners.add(runner)
                    receiverGenerator?.runners?.add(runner)
                }
            } else {
                runners.filter { it.ready }.shuffled(random).first().also {
                    it.next(stepCount++)
                }
            }

            if (usedRunner.returnedReceivers != null) {
                runners.add(
                    TestRunner(
                        runners.size,
                        this,
                        generators,
                        receiverGenerators,
                        usedRunner.returnedReceivers
                    )
                )
                usedRunner.returnedReceivers = null
            }

            totalCount++
            if (totalCount == totalTests) {
                break
            }
        }
        @Suppress("UNCHECKED_CAST")
        return TestResults(runners.map { it.testResults as List<TestResult<Any, ParameterGroup>> }.flatten())
    }
}

fun solution(klass: Class<*>, captureOutput: CaptureOutput = ::defaultCaptureOutput) = Solution(klass, captureOutput)

fun Executable.isStatic() = Modifier.isStatic(modifiers)
fun Executable.isPrivate() = Modifier.isPrivate(modifiers)
fun Executable.isPublic() = Modifier.isPublic(modifiers)
fun Executable.fullName() = "$name(${parameters.joinToString(", ") { it.type.name }})"

fun Class<*>.findMethod(method: Method, solution: Class<*>) = this.declaredMethods.find {
    it.isPublic() &&
        it != null &&
        it.name == method.name &&
        it.parameterTypes.fixReceivers(this, solution).contentEquals(method.parameterTypes) &&
        (it.returnType == method.returnType || (it.returnType == this && method.returnType == solution)) &&
        it.isStatic() == method.isStatic()
}

fun Class<*>.findConstructor(constructor: Constructor<*>, solution: Class<*>) = this.declaredConstructors.find {
    it.isPublic() &&
        it?.parameterTypes?.fixReceivers(this, solution)?.contentEquals(constructor.parameterTypes) ?: false
}

fun Array<Class<*>>.fixReceivers(from: Class<*>, to: Class<*>) = map {
    when (it) {
        from -> to
        else -> it
    }
}.toTypedArray()

inline infix fun <reified T : Any> T.merge(other: T): T {
    val nameToProperty = T::class.declaredMemberProperties.associateBy { it.name }
    val primaryConstructor = T::class.primaryConstructor!!
    val args = primaryConstructor.parameters.associateWith { parameter ->
        val property = nameToProperty[parameter.name]!!
        val value = if (property.get(other) != -1) {
            property.get(other)
        } else {
            property.get(this)
        }
        value
    }
    return primaryConstructor.callBy(args)
}

typealias CaptureOutput = (run: () -> Any?) -> CapturedResult

data class CapturedResult(val returned: Any?, val threw: Throwable?, val stdout: String, val stderr: String)

private val outputLock = ReentrantLock()
fun defaultCaptureOutput(run: () -> Any?): CapturedResult = outputLock.withLock {
    val original = Pair(System.out, System.err)
    val diverted = Pair(ByteArrayOutputStream(), ByteArrayOutputStream()).also {
        System.setOut(PrintStream(it.first))
        System.setErr(PrintStream(it.second))
    }

    @Suppress("TooGenericExceptionCaught")
    val result: Pair<Any?, Throwable?> = try {
        Pair(run(), null)
    } catch (e: Throwable) {
        Pair(null, e)
    }
    System.setOut(original.first)
    System.setErr(original.second)
    return CapturedResult(result.first, result.second, diverted.first.toString(), diverted.second.toString())
}

fun unwrap(run: () -> Any?): Any? = try {
    run()
} catch (e: InvocationTargetException) {
    throw e.cause ?: error("InvocationTargetException should have a cause")
}
