@file:Suppress("MemberVisibilityCanBePrivate")

package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.ExecutableGenerators
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.time.Instant

@Suppress("ArrayInDataClass")
data class Result<T>(
    val parameters: Array<Any?>,
    val returned: T?,
    val threw: Throwable?,
    val stdout: String,
    val stderr: String
) {
    @Suppress("UNCHECKED_CAST")
    constructor(parameters: Array<Any?>, capturedResult: CapturedResult) : this(
        parameters, capturedResult.returned as T?, capturedResult.threw, capturedResult.stdout, capturedResult.stderr
    )
}

@Suppress("ArrayInDataClass")
data class TestResult<T>(
    val runnerID: Int,
    val stepCount: Int,
    val runnerCount: Int,
    val executable: Executable,
    val type: Type,
    val parameters: Array<Any?>,
    val solution: Result<T>,
    val submission: Result<T>,
    val interval: Interval
) {
    enum class Type { CONSTRUCTOR, INITIALIZER, METHOD }
    enum class Differs { STDOUT, STDERR, RETURN, THREW, PARAMETERS, VERIFIER_THREW }

    val differs: MutableSet<Differs> = mutableSetOf()
    val succeeded: Boolean
        get() = differs.isEmpty()
    val failed: Boolean
        get() = !succeeded
    var verifierThrew: Throwable? = null
}

class TestRunner(
    val runnerID: Int,
    val submission: Submission,
    val executableGenerators: ExecutableGenerators,
    val constructors: Sequence<Constructor<*>>
) {

    data class ReceiverPair(var solution: Any?, var submission: Any?)

    val methodIterator = submission.solution.solutionMethods.cycle()
    val testResults: MutableList<TestResult<*>> = mutableListOf()

    val ready: Boolean
        get() = testResults.none { it.failed }

    val receivers: ReceiverPair by lazy {
        if (submission.solution.onlyStatic) {
            ReceiverPair(null, null)
        } else {
            testResults.find { it.type == TestResult.Type.CONSTRUCTOR && it.succeeded }?.let {
                check(it.solution.returned != null) { "Constructor returned null" }
                check(it.submission.returned != null) { "Constructor returned null" }
                ReceiverPair(it.solution.returned, it.submission.returned)
            } ?: error("Couldn't find a receiver pair")
        }
    }

    private var count = 0

    @Suppress("ComplexMethod")
    fun run(solutionExecutable: Executable, stepCount: Int, type: TestResult.Type? = null): TestResult<*> {
        val start = Instant.now()
        val submissionExecutable = submission.submissionExecutables[solutionExecutable]
            ?: error("couldn't find a submission method that should exist")
        check(solutionExecutable::class.java == submissionExecutable::class.java) {
            "solution and submission executable are not the same type"
        }

        val generator = executableGenerators[solutionExecutable]
            ?: error("couldn't find a parameter generator that should exist")
        val parameters = generator.generate()

        val solutionResult = submission.solution.captureOutput {
            unwrapMethodInvocationException {
                when (solutionExecutable) {
                    is Method -> solutionExecutable.invoke(receivers.solution, *parameters.solution)
                    is Constructor<*> -> solutionExecutable.newInstance(*parameters.solution)
                    else -> error("encountered unknown executable type: $solutionExecutable")
                }
            }
        }.let { Result<Any>(parameters.solution, it) }

        val submissionResult = submission.solution.captureOutput {
            unwrapMethodInvocationException {
                when (submissionExecutable) {
                    is Method -> submissionExecutable.invoke(receivers.submission, *parameters.submission)
                    is Constructor<*> -> submissionExecutable.newInstance(*parameters.submission)
                    else -> error("encountered unknown executable type: $submissionExecutable")
                }
            }
        }.let { Result<Any>(parameters.submission, it) }

        val stepType = type ?: when (solutionExecutable) {
            is Method -> TestResult.Type.METHOD
            is Constructor<*> -> TestResult.Type.CONSTRUCTOR
            else -> error("encountered unknown executable type: $solutionExecutable")
        }

        return TestResult(
            runnerID,
            stepCount, count++,
            solutionExecutable, stepType, parameters.reference,
            solutionResult, submissionResult,
            Interval(start)
        ).also { step ->
            submission.solution.verify(step)
            testResults.add(step)
            if (step.succeeded) {
                generator.next()
            } else {
                generator.prev()
            }
        }
    }

    var created = false
    fun next(stepCount: Int): Boolean {
        if (!submission.solution.onlyStatic && !created) {
            run(constructors.first(), stepCount)
            if (ready && submission.solution.initializer != null) {
                run(submission.solution.initializer, stepCount, TestResult.Type.INITIALIZER)
            }
            created = true
        } else {
            run(methodIterator.first(), stepCount)
        }
        return ready
    }
}

@Suppress("unused")
fun List<TestResult<*>>.succeeded() = all { it.succeeded }
fun List<TestResult<*>>.failed() = any { it.failed }

data class Interval(val start: Instant, val end: Instant) {
    constructor(start: Instant) : this(start, Instant.now())
}
