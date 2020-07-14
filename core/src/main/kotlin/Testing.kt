@file:Suppress("MemberVisibilityCanBePrivate")

package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.Generators
import edu.illinois.cs.cs125.jenisol.core.generators.boxArray
import edu.illinois.cs.cs125.jenisol.core.generators.isAnyArray
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.time.Instant

@Suppress("ArrayInDataClass")
data class Result<T, P : ParameterGroup>(
    @JvmField val parameters: P,
    @JvmField val returned: T?,
    @JvmField val threw: Throwable?,
    @JvmField val stdout: String,
    @JvmField val stderr: String
) {
    @Suppress("UNCHECKED_CAST")
    constructor(parameters: Array<Any?>, capturedResult: CapturedResult) : this(
        parameters.toParameterGroup() as P,
        capturedResult.returned as T?,
        capturedResult.threw,
        capturedResult.stdout,
        capturedResult.stderr
    )
}

@Suppress("ArrayInDataClass")
data class TestResult<T, P : ParameterGroup>(
    @JvmField val runnerID: Int,
    @JvmField val stepCount: Int,
    @JvmField val runnerCount: Int,
    @JvmField val executable: Executable,
    @JvmField val type: Type,
    @JvmField val parameters: P,
    @JvmField val solution: Result<T, P>,
    @JvmField val submission: Result<T, P>,
    @JvmField val interval: Interval
) {
    enum class Type { CONSTRUCTOR, INITIALIZER, METHOD }
    enum class Differs { STDOUT, STDERR, RETURN, THREW, PARAMETERS, VERIFIER_THREW }

    @JvmField
    val differs: MutableSet<Differs> = mutableSetOf()
    val succeeded: Boolean
        get() = differs.isEmpty()
    val failed: Boolean
        get() = !succeeded

    @Suppress("unused")
    var verifierThrew: Throwable? = null
}

class TestRunner(
    val runnerID: Int,
    val submission: Submission,
    val generators: Generators,
    val constructors: Sequence<Constructor<*>>
) {

    data class Receivers(var solution: Any?, var submission: Any?, val reference: Any?)

    val methodIterator = submission.solution.solutionMethods.cycle()
    val testResults: MutableList<TestResult<*, *>> = mutableListOf()

    val ready: Boolean
        get() = testResults.none { it.failed } && receivers != null

    var receivers: Receivers? = null

    init {
        if (submission.solution.allStaticMethods) {
            receivers = Receivers(null, null, null)
        }
    }

    private var count = 0

    @Suppress("ComplexMethod")
    fun run(solutionExecutable: Executable, stepCount: Int, type: TestResult.Type? = null): TestResult<*, *> {
        val start = Instant.now()
        val submissionExecutable = submission.submissionExecutables[solutionExecutable]
            ?: error("couldn't find a submission method that should exist")
        check(solutionExecutable::class.java == submissionExecutable::class.java) {
            "solution and submission executable are not the same type"
        }

        val generator = generators[solutionExecutable]
            ?: error("couldn't find a parameter generator that should exist")
        val parameters = generator.generate()

        val stepType = type ?: when (solutionExecutable) {
            is Method -> TestResult.Type.METHOD
            is Constructor<*> -> TestResult.Type.CONSTRUCTOR
            else -> error("encountered unknown executable type: $solutionExecutable")
        }

        if (stepType == TestResult.Type.METHOD) {
            check(receivers != null) { "No receivers available" }
        }
        val solutionResult = submission.solution.captureOutput {
            unwrapMethodInvocationException {
                when (solutionExecutable) {
                    is Method -> solutionExecutable.invoke(receivers!!.solution, *parameters.solution)
                    is Constructor<*> -> solutionExecutable.newInstance(*parameters.solution)
                    else -> error("encountered unknown executable type: $solutionExecutable")
                }
            }
        }.let { Result<Any, ParameterGroup>(parameters.solution, it) }

        // If this is a constructor and it didn't fail on the solution, generate an additional reference object
        // so that we can donate this to the receiver generator later
        val referenceResult = if (stepType == TestResult.Type.CONSTRUCTOR && solutionResult.returned != null) {
            submission.solution.captureOutput {
                unwrapMethodInvocationException {
                    when (solutionExecutable) {
                        is Constructor<*> -> solutionExecutable.newInstance(*parameters.reference)
                        else -> error("encountered unknown executable type: $solutionExecutable")
                    }
                }
            }.let { Result<Any, ParameterGroup>(parameters.reference, it) }
        } else {
            null
        }

        val submissionResult = submission.solution.captureOutput {
            unwrapMethodInvocationException {
                when (submissionExecutable) {
                    is Method -> submissionExecutable.invoke(receivers!!.submission, *parameters.submission)
                    is Constructor<*> -> submissionExecutable.newInstance(*parameters.submission)
                    else -> error("encountered unknown executable type: $submissionExecutable")
                }
            }
        }.let { Result<Any, ParameterGroup>(parameters.submission, it) }

        return TestResult(
            runnerID,
            stepCount, count++,
            solutionExecutable, stepType, parameters.reference.toParameterGroup(),
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
            if (step.succeeded && stepType == TestResult.Type.CONSTRUCTOR) {
                // If both constructors throw identically, then the step didn't fail but
                // this test runner still can't proceed
                receivers = if (step.solution.returned != null) {
                    Receivers(step.solution.returned, step.submission.returned, referenceResult!!.returned)
                } else {
                    null
                }
            }
        }
    }

    var created = false
    fun next(stepCount: Int): Boolean {
        if (!submission.solution.allStaticMethods && !created) {
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
fun List<TestResult<*, *>>.succeeded() = all { it.succeeded }
fun List<TestResult<*, *>>.failed() = any { it.failed }

data class Interval(val start: Instant, val end: Instant) {
    constructor(start: Instant) : this(start, Instant.now())
}

@Suppress("ComplexMethod", "MapGetWithNotNullAssertionOperator")
fun Any.deepEquals(
    submission: Any?,
    comparators: Comparators
): Boolean = when {
    this === submission -> true
    submission == null -> false
    this::class.java in comparators -> comparators[this::class.java].compare(this, submission)
    this is ParameterGroup && submission is ParameterGroup ->
        this.toArray().deepEquals(submission.toArray(), comparators)
    this.isAnyArray() != submission.isAnyArray() -> false
    this.isAnyArray() && submission.isAnyArray() -> {
        val solutionBoxed = this.boxArray()
        val submissionBoxed = submission.boxArray()
        (solutionBoxed.size == submissionBoxed.size) && solutionBoxed.zip(submissionBoxed)
            .all { (solution, submission) ->
                when {
                    solution === submission -> true
                    solution == null || submission === null -> false
                    else -> solution.deepEquals(submission, comparators)
                }
            }
    }
    else -> this == submission
}
