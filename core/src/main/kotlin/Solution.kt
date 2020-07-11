@file:Suppress("TooManyFunctions", "MemberVisibilityCanBePrivate")

package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.ParameterGeneratorFactory
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
        solution.declaredFields.filter { it.isStatic() && !it.isAnswerable() }.also {
            check(it.isEmpty()) { "No support for testing classes with static fields yet" }
        }
    }

    val publicMethods = (solution.declaredMethods.toList() + solution.declaredConstructors.toList()).map {
        it as Executable
    }.filter { !it.isPrivate() && !it.isAnswerable() }

    val solutionMethods = publicMethods.filterIsInstance<Method>().also {
        check(it.isNotEmpty()) { "Answerable found no methods to test in ${solution.name}" }
    }.toSet()
    val onlyStatic = solutionMethods.all { it.isStatic() }

    val solutionConstructors = if (!onlyStatic) {
        publicMethods.filterIsInstance<Constructor<*>>().also {
            check(it.isNotEmpty()) { "Answerable found no available constructors in ${solution.name}" }
        }.toSet()
    } else {
        setOf()
    }
    val solutionExecutables = solutionConstructors + solutionMethods

    val emptyConstructor = solutionConstructors.size == 1 && solutionConstructors.first().parameters.isEmpty()
    val emptyMethod = solutionMethods.size == 1 && solutionMethods.first().parameters.isEmpty()

    val initializer: Executable? = if (emptyConstructor) {
        solution.superclass.declaredMethods.filter { it.isInitializer() }.also {
            check(it.size <= 1) { "Solution parent class ${solution.superclass.name} has multiple initializers" }
        }.firstOrNull()?.let {
            Initializer.validate(it)
            it
        }
    } else {
        null
    }
    val emptyInitializer = initializer?.parameters?.isEmpty() ?: true

    private val initializers = if (initializer != null) {
        setOf(initializer)
    } else {
        setOf()
    }
    val parameterGeneratorFactory: ParameterGeneratorFactory =
        ParameterGeneratorFactory(solutionMethods + solutionConstructors + initializers, solution)

    // These calculations should be improved to create a better test balance
    @Suppress("MagicNumber")
    val receiverEntropy = when {
        onlyStatic -> 0
        emptyConstructor && emptyInitializer -> 2
        else -> 5
    }

    @Suppress("MagicNumber")
    val methodEntropy = when {
        emptyMethod -> 0
        else -> 5
    }

    val defaultReceiverCount = 2.0.pow(receiverEntropy.toDouble()).toInt()
    val defaultMethodCount = 2.0.pow(methodEntropy.toDouble()).toInt()
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
        check(solutionMethods.size == 1) { "No support yet for multiple verifiers" }
        Verify.validate(it, solutionMethods.first().genericReturnType)
    }

    fun verify(result: TestResult<*>) {
        verifier?.also { customVerifier ->
            @Suppress("TooGenericExceptionCaught")
            try {
                unwrapMethodInvocationException { customVerifier.invoke(null, result) }
            } catch (e: Throwable) {
                result.differs.add(TestResult.Differs.VERIFIER_THREW)
                result.verifierThrew = e
            }
        } ?: defaultVerify(result)
    }

    fun defaultVerify(result: TestResult<*>) {
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
        if (solution.threw != submission.threw) {
            result.differs.add(TestResult.Differs.THREW)
        }
        if (!compare(solution.parameters, submission.parameters)) {
            result.differs.add(TestResult.Differs.PARAMETERS)
        }
    }

    fun compare(solution: Any?, submission: Any?) = when {
        solution == null -> submission == null
        solution::class.java.isArray -> submission?.asArray()?.let { solution.asArray().contentDeepEquals(it) } ?: false
        else -> solution == submission
    }

    fun submission(submission: Class<*>) = Submission(this, submission)
}

fun Set<Method>.cycle() = sequence {
    yield(shuffled().first())
}

class RandomGroup(seed: Long = Random.nextLong()) {
    val solution = java.util.Random().also { it.setSeed(seed) }
    val submission = java.util.Random().also { it.setSeed(seed) }
    val reference = java.util.Random().also { it.setSeed(seed) }
    val synced: Boolean
        get() = setOf(solution.nextLong(), submission.nextLong(), reference.nextLong()).size == 1
}

class ClassDesignError(klass: Class<*>, executable: Executable) : Exception(
    "Submission class ${klass.name} didn't provide ${if (executable is Method) {
        "method"
    } else {
        "constructor"
    }} ${executable.fullName()}"
)

class Submission(val solution: Solution, val submission: Class<*>) {
    val submissionExecutables = solution.solutionExecutables.map { solutionExecutable ->
        when (solutionExecutable) {
            is Constructor<*> -> submission.findConstructor(solutionExecutable)
            is Method -> submission.findMethod(solutionExecutable)
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

    fun test(passedSettings: Solution.Settings = Solution.Settings()): List<TestResult<*>> {
        val settings = solution.setCounts(Solution.Settings.DEFAULTS merge passedSettings)

        val random = if (passedSettings.seed == -1) {
            Random
        } else {
            Random(passedSettings.seed.toLong())
        }

        val methodGenerators = solution.parameterGeneratorFactory.get(random, settings)
        val constructors = sequence {
            while (true) {
                yieldAll(solution.solutionConstructors.toList().shuffled(random))
            }
        }

        val totalTests = settings.receiverCount * (settings.methodCount + 1)
        val runners: MutableList<TestRunner> = mutableListOf()
        for (i in 0 until totalTests) {
            if (runners.readyCount() < settings.receiverCount) {
                TestRunner(runners.size, this, methodGenerators, constructors).also { runner ->
                    runner.next(i)
                    runners.add(runner)
                }
            } else {
                runners.shuffled(random).first().next(i)
            }
        }
        return runners.map { it.testResults }.flatten()
    }
}

fun solution(klass: Class<*>, captureOutput: CaptureOutput = ::defaultCaptureOutput) = Solution(klass, captureOutput)

fun Executable.isStatic() = Modifier.isStatic(modifiers)
fun Executable.isPrivate() = Modifier.isPrivate(modifiers)
fun Executable.fullName() = "$name(${parameters.joinToString(", ") { it.type.name }})"

fun Class<*>.findMethod(method: Method) = this.declaredMethods.find {
    (it.name == method.name) &&
        (it?.parameterTypes?.contentEquals(method.parameterTypes) ?: false) &&
        (it?.returnType?.equals(method.returnType) ?: false)
}

fun Class<*>.findConstructor(constructor: Constructor<*>) = this.declaredConstructors.find {
    it?.parameterTypes?.contentEquals(constructor.parameterTypes) ?: false
}

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

fun unwrapMethodInvocationException(run: () -> Any?): Any? = try {
    run()
} catch (e: InvocationTargetException) {
    throw e.cause ?: error("InvocationTargetException should have a cause")
}
