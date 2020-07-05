@file:Suppress("TooManyFunctions", "MemberVisibilityCanBePrivate")

package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.MethodGenerators
import edu.illinois.cs.cs125.jenisol.core.generators.ParameterGeneratorFactory
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
import kotlin.random.Random
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

class Solution(val solution: Class<*>, val captureOutput: CaptureOutput = ::defaultCaptureOutput) {
    data class Settings(
        val testCount: Int = -1,
        val receiverCount: Int = 32,
        val seed: Int = -1,
        val simpleCount: Int = -1,
        val edgeCount: Int = -1,
        val mixedCount: Int = -1,
        val fixedCount: Int = -1
    ) {
        companion object {
            val DEFAULTS = Settings(
                testCount = 1024,
                receiverCount = 32,
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

    val emptyConstructor = solutionConstructors.size == 1 && solutionConstructors.first().parameters.isEmpty()
    val emptyMethod = solutionMethods.size == 1 && solutionMethods.first().parameters.isEmpty()

    val initializer: Set<Method> = if (emptyConstructor) {
        solution.superclass.declaredMethods.filter { it.isInitializer() }.also {
            check(it.size <= 1) { "Solution parent class ${solution.superclass.name} has multiple initializers" }
        }.firstOrNull()?.let {
            Initializer.validate(it)
            setOf(it)
        } ?: setOf()
    } else {
        setOf()
    }
    val hasInitializer = initializer.isNotEmpty()

    val parameterGeneratorFactory: ParameterGeneratorFactory =
        ParameterGeneratorFactory(solutionMethods + solutionConstructors + initializer, solution)

    fun receiverCount(settings: Settings) = if (onlyStatic || (emptyConstructor && !emptyMethod)) {
        1
    } else if (!emptyConstructor && emptyMethod) {
        settings.testCount / 2
    } else {
        settings.receiverCount
    }

    fun compare(step: PairRunner.Step) {
        val solution = step.solution
        val submission = step.submission

        if (solution.stdout.isNotBlank() && solution.stdout != submission.stdout) {
            step.differs.add(PairRunner.Step.Differs.STDOUT)
        }
        if (solution.stderr.isNotBlank() && solution.stderr != submission.stderr) {
            step.differs.add(PairRunner.Step.Differs.STDERR)
        }

        if (step.type == PairRunner.Step.Type.METHOD) {
            if (solution.returned != null && submission.returned != null && solution.returned::class.java.isArray) {
                if (!solution.returned.asArray().contentDeepEquals(submission.returned.asArray())) {
                    step.differs.add(PairRunner.Step.Differs.RETURN)
                }
            } else if (solution.returned != submission.returned) {
                step.differs.add(PairRunner.Step.Differs.RETURN)
            }
        }
        if (solution.threw != submission.threw) {
            step.differs.add(PairRunner.Step.Differs.THREW)
        }
    }

    fun submission(submission: Class<*>) = Submission(this, submission)
}

fun Set<Method>.cycle() = sequence {
    yield(shuffled().first())
}

class RandomPair(seed: Long = Random.nextLong()) {
    val solution = java.util.Random().also { it.setSeed(seed) }
    val submission = java.util.Random().also { it.setSeed(seed) }
    val synced: Boolean
        get() = solution.nextLong() == submission.nextLong()
}

class ClassDesignError(klass: Class<*>, executable: Executable) : Exception(
    "Submission class ${klass.name} didn't provide ${if (executable is Method) {
        "method"
    } else {
        "constructor"
    }} ${executable.fullName()}"
)

class Submission(val solution: Solution, val submission: Class<*>) {
    val submissionConstructors = solution.solutionConstructors.map { constructor ->
        constructor to (submission.findConstructor(constructor) ?: throw ClassDesignError(submission, constructor))
    }.toMap()
    val submissionMethods = solution.solutionMethods.map { method ->
        method to (submission.findMethod(method) ?: throw ClassDesignError(submission, method))
    }.toMap()

    fun MutableList<PairRunner>.readyCount() = filter { it.ready }.count()

    fun test(settings: Solution.Settings = Solution.Settings()): List<PairRunner.Step> {
        val testSettings = Solution.Settings.DEFAULTS merge settings

        val random = if (settings.seed == -1) {
            Random
        } else {
            Random(settings.seed.toLong())
        }

        val receiverCount = solution.receiverCount(settings)
        val methodGenerators = solution.parameterGeneratorFactory.get(random, testSettings)
        val receiverGenerator = ReceiverGenerator(this, methodGenerators)

        val runners: MutableList<PairRunner> = mutableListOf()
        for (i in 0 until testSettings.testCount) {
            if (runners.readyCount() < receiverCount) {
                PairRunner(runners.size, this, methodGenerators, receiverGenerator).also { runner ->
                    runner.next()
                    runners.add(runner)
                }
            } else {
                runners.shuffled(random).first().next()
            }
        }
        return runners.map { it.steps }.flatten()
    }
}

data class Result(val returned: Any?, val threw: Throwable?, val stdout: String, val stderr: String)

class PairRunner(
    val runnerID: Int,
    val submission: Submission,
    val methodGenerators: MethodGenerators,
    val receiverGenerator: ReceiverGenerator
) {

    data class ReceiverPair(var solution: Any?, var submission: Any?)
    data class Step(val runnerID: Int, val solution: Result, val submission: Result, val type: Type) {
        enum class Type { CONSTRUCTOR, INITIALIZER, METHOD }
        enum class Differs { STDOUT, STDERR, RETURN, THREW }

        val differs: MutableSet<Differs> = mutableSetOf()
        val succeeded: Boolean
            get() = differs.isEmpty()
        val failed: Boolean
            get() = !succeeded
    }

    val methodIterator = submission.solution.solutionMethods.cycle()
    val steps: MutableList<Step> = mutableListOf()

    val ready: Boolean
        get() = steps.isEmpty() || steps.all { it.succeeded }

    val receivers: ReceiverPair by lazy {
        if (submission.solution.onlyStatic) {
            ReceiverPair(null, null)
        } else {
            steps.find { it.type == Step.Type.CONSTRUCTOR && it.succeeded }?.let {
                check(it.solution.returned != null) { "Constructor returned null" }
                check(it.submission.returned != null) { "Constructor returned null" }
                ReceiverPair(it.solution.returned, it.submission.returned)
            } ?: error("Couldn't find a receiver pair")
        }
    }

    var created = false
    fun next(): Boolean {
        if (!submission.solution.onlyStatic && !created) {
            steps.addAll(receiverGenerator.generate(runnerID))
            created = true
            return ready
        }

        val solutionMethod = methodIterator.first()
        val submissionMethod = submission.submissionMethods[solutionMethod] ?: error(
            "Answerable couldn't find a submission method that should exist"
        )

        val generator = methodGenerators[solutionMethod]
            ?: error("Couldn't find a parameter generator that should exist")
        val parameters = generator.generate()

        val solutionResult = submission.solution.captureOutput {
            unwrapMethodInvocationException {
                solutionMethod.invoke(receivers.solution, *parameters.solution)
            }
        }
        val submissionResult = submission.solution.captureOutput {
            unwrapMethodInvocationException {
                submissionMethod.invoke(receivers.submission, *parameters.submission)
            }
        }

        Step(runnerID, solutionResult, submissionResult, Step.Type.METHOD).also { step ->
            submission.solution.compare(step)
            steps.add(step)
            if (step.succeeded) {
                generator.next()
            } else {
                generator.prev()
            }
        }
        return ready
    }
}

@Suppress("unused")
fun List<PairRunner.Step>.succeeded() = all { it.succeeded }
fun List<PairRunner.Step>.failed() = any { it.failed }

fun solution(klass: Class<*>) = Solution(klass)

fun Executable.isStatic() = Modifier.isStatic(modifiers)
fun Executable.isPrivate() = Modifier.isPrivate(modifiers)
fun Executable.fullName() = "$name(${parameters.joinToString(", ") { it.type.name }})"

fun Class<*>.findMethod(method: Method) = this.declaredMethods.find {
    it?.parameterTypes?.contentEquals(method.parameterTypes) ?: false
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

typealias CaptureOutput = (run: () -> Any?) -> Result

private val outputLock = ReentrantLock()
fun defaultCaptureOutput(run: () -> Any?): Result = outputLock.withLock {
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
    return Result(result.first, result.second, diverted.first.toString(), diverted.second.toString())
}

fun unwrapMethodInvocationException(run: () -> Any?): Any? = try {
    run()
} catch (e: InvocationTargetException) {
    throw e.cause ?: error("InvocationTargetException should have a cause")
}
