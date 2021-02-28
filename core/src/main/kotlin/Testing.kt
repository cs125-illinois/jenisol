@file:Suppress("MemberVisibilityCanBePrivate")

package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.Complexity
import edu.illinois.cs.cs125.jenisol.core.generators.Generators
import edu.illinois.cs.cs125.jenisol.core.generators.Parameters
import edu.illinois.cs.cs125.jenisol.core.generators.Value
import edu.illinois.cs.cs125.jenisol.core.generators.ZeroComplexity
import edu.illinois.cs.cs125.jenisol.core.generators.getArrayDimension
import edu.illinois.cs.cs125.jenisol.core.generators.getArrayType
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.time.Instant
import java.util.Arrays

@Suppress("ArrayInDataClass")
data class Result<T, P : ParameterGroup>(
    @JvmField val parameters: P,
    @JvmField val returned: T?,
    @JvmField val threw: Throwable?,
    @JvmField val stdout: String,
    @JvmField val stderr: String,
    @JvmField val modifiedParameters: Boolean
) {
    @Suppress("UNCHECKED_CAST")
    constructor(parameters: Array<Any?>, capturedResult: CapturedResult, modifiedParameters: Boolean) : this(
        parameters.toParameterGroup() as P,
        capturedResult.returned as T?,
        capturedResult.threw,
        capturedResult.stdout,
        capturedResult.stderr,
        modifiedParameters
    )
}

internal fun <P : ParameterGroup> Executable.formatBoundMethodCall(parameterValues: P, klass: Class<*>): String {
    val arrayOfParameters = parameterValues.toArray()
    return if (this is Constructor<*>) {
        if (klass.isKotlin()) {
            klass.simpleName
        } else {
            "new ${klass.simpleName}"
        }
    } else {
        name
    } + "(" +
        parameters
            .mapIndexed { index, parameter ->
                if (klass.isKotlin()) {
                    "${parameter.name}: ${parameter.type.kotlin.simpleName} = ${print(arrayOfParameters[index])}"
                } else {
                    "${parameter.type.simpleName} ${parameter.name} = ${print(arrayOfParameters[index])}"
                }
            }
            .joinToString(", ") +
        ")"
}

@Suppress("ArrayInDataClass")
data class TestResult<T, P : ParameterGroup>(
    @JvmField val runnerID: Int,
    @JvmField val stepCount: Int,
    @JvmField val runnerCount: Int,
    @JvmField val solutionExecutable: Executable,
    @JvmField val submissionExecutable: Executable,
    @JvmField val type: Type,
    @JvmField val parameters: P,
    @JvmField val solution: Result<T, P>,
    @JvmField val submission: Result<T, P>,
    @JvmField val interval: Interval,
    @JvmField val complexity: Int,
    @JvmField val solutionClass: Class<*>,
    @JvmField val submissionClass: Class<*>,
    @JvmField var message: String? = null,
    @JvmField val differs: MutableSet<Differs> = mutableSetOf()
) {
    enum class Type { CONSTRUCTOR, INITIALIZER, METHOD, FACTORY_METHOD, COPY_CONSTRUCTOR }
    enum class Differs { STDOUT, STDERR, RETURN, THREW, PARAMETERS, VERIFIER_THREW, INSTANCE_VALIDATION_THREW }

    val succeeded: Boolean
        get() = differs.isEmpty()
    val failed: Boolean
        get() = !succeeded

    @Suppress("unused")
    var verifierThrew: Throwable? = null

    @Suppress("ComplexMethod", "LongMethod")
    fun explain(): String {

        val methodString = submissionExecutable.formatBoundMethodCall(parameters, submissionClass)

        val resultString = when {
            verifierThrew != null -> verifierThrew!!.message
            differs.contains(Differs.THREW) -> {
                if (solution.threw == null) {
                    """Solution did not throw an exception"""
                } else {
                    """Solution threw: ${solution.threw}"""
                } + "\n" + if (submission.threw == null) {
                    """Submission did not throw an exception"""
                } else {
                    """Submission threw: ${submission.threw}"""
                }
            }
            differs.contains(Differs.STDOUT) -> {
                """
Solution printed:
---
${solution.stdout}---
Submission printed:
---
${submission.stdout}---""".trim()
            }
            differs.contains(Differs.STDERR) -> {
                """
Solution printed to STDERR:
---
${solution.stderr}---
Submission printed to STDERR:
---
${submission.stderr}---""".trim()
            }
            differs.contains(Differs.RETURN) -> {
                """
Solution returned: ${print(solution.returned)}
Submission returned: ${print(submission.returned)}
                """.trim()
            }
            differs.contains(Differs.PARAMETERS) -> {
                if (!solution.modifiedParameters && submission.modifiedParameters) {
                    """
Solution did not modify its parameters
Submission did modify its parameters to ${
                    print(
                        submission.parameters.toArray()
                    )
                    }
                    """.trim()
                } else if (solution.modifiedParameters && !submission.modifiedParameters) {
                    """
Solution modified its parameters to ${print(solution.parameters.toArray())}
Submission did not modify its parameters
                    """.trim()
                } else {
                    """
Solution modified its parameters to ${print(solution.parameters.toArray())}
Submission modified its parameters to ${
                    print(
                        submission.parameters.toArray()
                    )
                    }
                    """.trim()
                }
            }
            else -> error("Unexplained result")
        }
        return "Testing $methodString failed:\n${message?.let { it + "\n" } ?: ""}$resultString"
    }
}

fun print(value: Any?): String = when {
    value === null -> "null"
    value is ByteArray -> Arrays.toString(value)
    value is ShortArray -> Arrays.toString(value)
    value is IntArray -> Arrays.toString(value)
    value is LongArray -> Arrays.toString(value)
    value is FloatArray -> Arrays.toString(value)
    value is DoubleArray -> Arrays.toString(value)
    value is CharArray -> Arrays.toString(value)
    value is BooleanArray -> Arrays.toString(value)
    value is Array<*> -> value.joinToString { print(it) }
    value is String -> "\"$value\""
    else -> value.toString()
}

@Suppress("UNUSED")
class TestResults(
    val results: List<TestResult<Any, ParameterGroup>>,
    val settings: Settings,
    designOnly: Boolean? = null
) : List<TestResult<Any, ParameterGroup>> by results {
    val succeeded = designOnly ?: all { it.succeeded }
    val failed = !succeeded
    fun explain() = if (succeeded) {
        "Passed by completing ${results.size} tests"
    } else {
        filter { it.failed }.sortedBy { it.complexity }.let { result ->
            val leastComplex = result.first().complexity
            result.filter { it.complexity == leastComplex }
        }.minByOrNull { it.stepCount }!!.explain()
    }
}

@Suppress("LongParameterList")
class TestRunner(
    val runnerID: Int,
    val submission: Submission,
    var generators: Generators,
    val receiverGenerators: Sequence<Executable>,
    val captureOutput: CaptureOutput,
    val methodIterator: Sequence<Executable>,
    var receivers: Value<Any?>? = null
) {
    val testResults: MutableList<TestResult<*, *>> = mutableListOf()

    val failed: Boolean
        get() = testResults.any { it.failed }
    val ready: Boolean
        get() = testResults.none { it.failed } && receivers != null

    var lastComplexity: Complexity? = null

    var returnedReceivers: List<Value<Any?>>? = null

    var created: Boolean
    var initialized: Boolean = false

    init {
        if (receivers == null && submission.solution.skipReceiver) {
            receivers = Value(null, null, null, null, ZeroComplexity)
        }
        created = receivers != null
    }

    var count = 0

    fun Executable.pairRun(
        receiver: Any?,
        parameters: Array<Any?>,
        parametersCopy: Array<Any?>? = null
    ): Result<Any, ParameterGroup> = captureOutput {
        unwrap {
            when (this) {
                is Method -> this.invoke(receiver, *parameters)
                is Constructor<*> -> this.newInstance(*parameters)
                else -> error("Unknown executable type")
            }
        }
    }.let {
        Result(parameters, it, parametersCopy?.let { !submission.compare(parameters, parametersCopy) } ?: false)
    }

    @Suppress("ComplexMethod", "LongMethod", "ComplexCondition", "ReturnCount", "NestedBlockDepth")
    fun run(solutionExecutable: Executable, stepCount: Int, type: TestResult.Type? = null) {
        val creating = !created && type != TestResult.Type.INITIALIZER

        val isBoth = solutionExecutable.isAnnotationPresent(Both::class.java)

        val start = Instant.now()
        val submissionExecutable = if (isBoth) {
            solutionExecutable
        } else {
            submission.submissionExecutables[solutionExecutable]
                ?: error("couldn't find a submission method that should exist")
        }
        check(solutionExecutable::class.java == submissionExecutable::class.java) {
            "solution and submission executable are not the same type"
        }

        val (parameters, generator) = if (isBoth) {
            Pair(Parameters.fromReceivers(receivers!!), null)
        } else {
            generators[solutionExecutable]?.let {
                Pair(it.generate(this), it)
            } ?: error("couldn't find a parameter generator that should exist: $solutionExecutable")
        }

        val stepType = type ?: if (!created) {
            when (solutionExecutable) {
                is Constructor<*> -> TestResult.Type.CONSTRUCTOR
                is Method -> TestResult.Type.FACTORY_METHOD
                else -> error("Unexpected executable type")
            }
        } else {
            check(receivers != null) { "No receivers available" }
            when (solutionExecutable) {
                is Constructor<*> -> TestResult.Type.COPY_CONSTRUCTOR
                is Method -> TestResult.Type.METHOD
                else -> error("Unexpected executable type")
            }
        }
        val stepReceivers = receivers ?: Value(null, null, null, null, ZeroComplexity)

        try {
            unwrap {
                submission.solution.filters[solutionExecutable]?.invoke(null, *parameters.solution)
            }
        } catch (e: TestingControlException) {
            if (e is SkipTest) {
                // Skip this test like it never happened
                return
            } else if (e is BoundComplexity) {
                // Bound complexity at this point but don't fail
                generator?.prev()
                return
            }
            error("TestingControl exception mismatch: ${e::class.java})")
        }

        val solutionResult =
            solutionExecutable.pairRun(stepReceivers.solution, parameters.solution, parameters.solutionCopy)

        if (solutionResult.threw != null &&
            TestingControlException::class.java.isAssignableFrom(solutionResult.threw::class.java)
        ) {
            if (solutionResult.threw is SkipTest) {
                // Skip this test like it never happened
                return
            } else if (solutionResult.threw is BoundComplexity) {
                // Bound complexity at this point but don't fail
                generator?.prev()
                return
            }
            error("TestingControl exception mismatch: ${solutionResult.threw::class.java})")
        }
        val submissionResult =
            submissionExecutable.pairRun(stepReceivers.submission, parameters.submission, parameters.submissionCopy)

        val (solutionCopy, submissionCopy) = if (
            creating && solutionResult.returned != null && submissionResult.returned != null
        ) {
            // If we are creating receivers and that succeeded, create a second pair to donate to the receiver
            // generator
            Pair(
                solutionExecutable.pairRun(stepReceivers.solutionCopy, parameters.solutionCopy),
                submissionExecutable.pairRun(stepReceivers.submissionCopy, parameters.submissionCopy)
            )
        } else if (created &&
            solutionResult.returned != null && submissionResult.returned != null &&
            (
                (
                    solutionResult.returned::class.java == submission.solution.solution &&
                        submissionResult.returned::class.java == submission.submission
                    ) || (
                    solutionResult.returned::class.java.isArray && submissionResult.returned::class.java.isArray &&
                        solutionResult.returned::class.java.getArrayType() == submission.solution.solution &&
                        submissionResult.returned::class.java.getArrayType() == submission.submission &&
                        solutionResult.returned::class.java.getArrayDimension()
                        == submissionResult.returned::class.java.getArrayDimension()
                    )
                )
        ) {
            if (solutionResult.returned::class.java.isArray) {
                require(solutionResult.returned::class.java.getArrayDimension() == 1) {
                    "No support for multi-dimensional receiver array donations yet"
                }
            }
            // Or if we ran a method that generated more receivers, also donate them
            Pair(
                solutionExecutable.pairRun(stepReceivers.solutionCopy, parameters.solutionCopy),
                submissionExecutable.pairRun(stepReceivers.submissionCopy, parameters.submissionCopy)
            )
        } else {
            Pair(null, null)
        }

        val step = TestResult(
            runnerID,
            stepCount, count++,
            solutionExecutable, submissionExecutable, stepType, parameters.solutionCopy.toParameterGroup(),
            solutionResult, submissionResult,
            Interval(start),
            parameters.complexity.level,
            submission.solution.solution,
            submission.submission
        )

        if (solutionCopy != null && submission.solution.instanceValidator != null) {
            @Suppress("TooGenericExceptionCaught")
            try {
                unwrap { submission.solution.instanceValidator.invoke(null, submissionResult.returned) }
            } catch (e: Throwable) {
                if (e is ThreadDeath) {
                    throw e
                }
                step.differs.add(TestResult.Differs.INSTANCE_VALIDATION_THREW)
                step.verifierThrew = e
            }
        }

        if (step.succeeded) {
            submission.verify(solutionExecutable, step)
        }
        testResults.add(step)

        if (step.succeeded) {
            generator?.next()
        } else {
            generator?.prev()
        }

        lastComplexity = parameters.complexity

        // If both receiver generators throw identically, then the step didn't fail but
        // this test runner still can't proceed
        if (step.succeeded && creating && step.solution.returned != null) {
            if (!step.solution.returned::class.java.isArray) {
                receivers = Value(
                    step.solution.returned,
                    step.submission.returned,
                    solutionCopy!!.returned,
                    submissionCopy!!.returned,
                    parameters.complexity
                )
            } else {
                val solutions = step.solution.returned as Array<*>
                val submissions = step.submission.returned as Array<*>
                val solutionCopies = solutionCopy!!.returned as Array<*>
                val submissionCopies = submissionCopy!!.returned as Array<*>
                check(
                    solutions.size == submissions.size &&
                        solutions.size == solutionCopies.size &&
                        solutions.size == submissionCopies.size
                ) {
                    "Receiver array generation returned unequal arrays: ${solutions.size} ${submissions.size}"
                }
                if (solutions.isNotEmpty()) {
                    receivers = Value(
                        solutions.first(),
                        submissions.first(),
                        solutionCopies.first(),
                        submissionCopies.first(),
                        parameters.complexity
                    )
                    if (solutions.size > 1) {
                        val receiverList = mutableListOf<Value<Any?>>()
                        for (i in 1 until solutions.size) {
                            receiverList.add(
                                Value(
                                    solutions[i],
                                    submissions[i],
                                    solutionCopies[i],
                                    submissionCopies[i],
                                    parameters.complexity
                                )
                            )
                        }
                        returnedReceivers = receiverList.toList()
                    }
                }
            }
        } else if (step.succeeded && solutionCopy != null && submissionCopy != null) {
            check(returnedReceivers == null) { "Returned receivers not retrieved between steps" }
            if (!step.solution.returned!!::class.java.isArray) {
                returnedReceivers = listOf(
                    Value(
                        step.solution.returned,
                        step.submission.returned,
                        solutionCopy.returned,
                        submissionCopy.returned,
                        parameters.complexity
                    )
                )
            } else {
                val solutions = step.solution.returned as Array<*>
                val submissions = step.submission.returned as Array<*>
                val solutionCopies = solutionCopy.returned as Array<*>
                val submissionCopies = submissionCopy.returned as Array<*>
                check(
                    solutions.size == submissions.size &&
                        solutions.size == solutionCopies.size &&
                        solutions.size == submissionCopies.size
                ) {
                    "Receiver array generation returned unequal arrays"
                }
                val receiverList = mutableListOf<Value<Any?>>()
                for (i in solutions.indices) {
                    receiverList.add(
                        Value(
                            solutions[i],
                            submissions[i],
                            solutionCopies[i],
                            submissionCopies[i],
                            parameters.complexity
                        )
                    )
                }
                returnedReceivers = receiverList.toList()
            }
            returnedReceivers = listOf(
                Value(
                    step.solution.returned,
                    step.submission.returned,
                    solutionCopy.returned,
                    submissionCopy.returned,
                    parameters.complexity
                )
            )
        }
    }

    fun next(stepCount: Int): Boolean {
        if (!created) {
            run(receiverGenerators.first(), stepCount)
            created = true
        } else if (!initialized && submission.solution.initializer != null) {
            run(submission.solution.initializer, stepCount, TestResult.Type.INITIALIZER)
            initialized = true
        } else {
            run(methodIterator.first(), stepCount)
        }
        return ready
    }
}

data class Interval(val start: Instant, val end: Instant) {
    constructor(start: Instant) : this(start, Instant.now())
}
