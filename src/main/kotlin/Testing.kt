@file:Suppress("MemberVisibilityCanBePrivate")

package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.Complexity
import edu.illinois.cs.cs125.jenisol.core.generators.Generators
import edu.illinois.cs.cs125.jenisol.core.generators.ParameterValues
import edu.illinois.cs.cs125.jenisol.core.generators.Parameters
import edu.illinois.cs.cs125.jenisol.core.generators.Value
import edu.illinois.cs.cs125.jenisol.core.generators.ZeroComplexity
import edu.illinois.cs.cs125.jenisol.core.generators.boxType
import edu.illinois.cs.cs125.jenisol.core.generators.getArrayDimension
import edu.illinois.cs.cs125.jenisol.core.generators.getArrayType
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.time.Instant
import kotlin.reflect.full.companionObjectInstance

data class Result<T, P : ParameterGroup>(
    @JvmField val parameters: P,
    @JvmField val returned: T?,
    @JvmField val threw: Throwable?,
    @JvmField val stdout: String,
    @JvmField val stderr: String,
    @JvmField val tag: Any?,
    @JvmField val modifiedParameters: Boolean
) {
    @Suppress("UNCHECKED_CAST")
    constructor(parameters: Array<Any?>, capturedResult: CapturedResult, modifiedParameters: Boolean) : this(
        parameters.toParameterGroup() as P,
        capturedResult.returned as T?,
        capturedResult.threw,
        capturedResult.stdout,
        capturedResult.stderr,
        capturedResult.tag,
        modifiedParameters
    )

    override fun toString(): String {
        return "Result(parameters=$parameters, " +
            "returned=${returned?.safePrint()}, " +
            "threw=${threw?.safePrint()}, " +
            "stdout='$stdout', " +
            "stderr='$stderr', " +
            "tag='$tag', " +
            "modifiedParameters=$modifiedParameters)"
    }
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

@Suppress("unused")
data class TestResult<T, P : ParameterGroup>(
    @JvmField val runnerID: Int,
    @JvmField val stepCount: Int,
    @JvmField val runnerCount: Int,
    @JvmField val solutionExecutable: Executable,
    @JvmField val submissionExecutable: Executable,
    @JvmField val type: Type,
    @JvmField val allParameters: Parameters,
    @JvmField val solution: Result<T, P>,
    @JvmField val submission: Result<T, P>,
    @JvmField val interval: Interval,
    @JvmField val complexity: Int,
    @JvmField val solutionClass: Class<*>,
    @JvmField val submissionClass: Class<*>,
    @JvmField val solutionReceiver: Any?,
    @JvmField val submissionReceiver: Any?,
    @JvmField var message: String? = null,
    @JvmField val differs: MutableSet<Differs> = mutableSetOf(),
    @JvmField val submissionIsKotlin: Boolean = submissionClass.isKotlin(),
    @JvmField val existingReceiverMismatch: Boolean = false
) {
    @Suppress("UNCHECKED_CAST")
    @JvmField
    val parameters: P = allParameters.solutionCopy.toParameterGroup() as P

    enum class Type { CONSTRUCTOR, INITIALIZER, METHOD, STATIC_METHOD, FACTORY_METHOD, COPY_CONSTRUCTOR }
    enum class Differs { STDOUT, STDERR, RETURN, THREW, PARAMETERS, VERIFIER_THREW, INSTANCE_VALIDATION_THREW }

    val succeeded: Boolean
        get() = differs.isEmpty()
    val failed: Boolean
        get() = !succeeded

    var verifierThrew: Throwable? = null

    fun methodCall() =
        submissionExecutable.formatBoundMethodCall(allParameters.solution.toParameterGroup(), submissionClass)

    @Suppress("ComplexMethod", "LongMethod", "NestedBlockDepth")
    fun explain(stacktrace: Boolean = false): String {
        val methodString = methodCall()

        val resultString = when {
            verifierThrew != null -> "Verifier threw an exception: ${verifierThrew!!.message}"
            differs.contains(Differs.THREW) -> {
                if (solution.threw == null) {
                    """Solution did not throw an exception"""
                } else {
                    """Solution threw: ${solution.threw}"""
                } + "\n" + if (submission.threw == null) {
                    """Submission did not throw an exception"""
                } else {
                    """Submission threw: ${submission.threw}""" + if (stacktrace) {
                        "\n" + submission.threw.stackTraceToString().lines().let { lines ->
                            val trimIndex = lines.indexOfFirst { it.trim().startsWith("at java.base") }.let {
                                if (it == -1) {
                                    lines.size
                                } else {
                                    it
                                }
                            }
                            lines.take(trimIndex)
                        }.joinToString("\n")
                    } else {
                        ""
                    }
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
        return "Testing $methodString failed:\n$resultString${message?.let { "\nAdditional Explanation: $it" } ?: ""}"
    }

    override fun toString(): String {
        return "TestResult(runnerID=$runnerID, " +
            "stepCount=$stepCount, " +
            "runnerCount=$runnerCount, " +
            "solutionExecutable=$solutionExecutable, " +
            "submissionExecutable=$submissionExecutable, " +
            "type=$type, " +
            "parameters=$parameters, " +
            "solution=$solution, " +
            "submission=${submission.safePrint()}, " +
            "interval=$interval, " +
            "complexity=$complexity, " +
            "solutionClass=${solutionClass.name}, " +
            "submissionClass=${submissionClass.name}, " +
            "message=$message, " +
            "differs=$differs, " +
            "succeeded=$succeeded, " +
            "failed=$failed, " +
            "verifierThrew=$verifierThrew)"
    }
}

fun print(value: Any?): String = when {
    value === null -> "null"
    value is ByteArray -> value.contentToString()
    value is ShortArray -> value.contentToString()
    value is IntArray -> value.contentToString()
    value is LongArray -> value.contentToString()
    value is FloatArray -> value.contentToString()
    value is DoubleArray -> value.contentToString()
    value is CharArray -> value.contentToString()
    value is BooleanArray -> value.contentToString()
    value is Array<*> -> value.safeContentDeepToString()
    value is String -> "\"$value\""
    else -> value.safePrint()
}

@Suppress("UNUSED", "LongParameterList")
class TestResults(
    val results: List<TestResult<Any, ParameterGroup>>,
    val settings: Settings,
    val completed: Boolean,
    val threw: Throwable? = null,
    val timeout: Boolean,
    val finishedReceivers: Boolean,
    val untestedReceivers: Int,
    designOnly: Boolean? = null,
    val skippedSteps: List<Int>,
    val randomTrace: List<Int>? = null
) : List<TestResult<Any, ParameterGroup>> by results {
    val succeeded = designOnly ?: finishedReceivers && all { it.succeeded } && completed
    val failed = !succeeded
    fun explain(stacktrace: Boolean = false) = if (succeeded) {
        "Passed by completing ${results.size} tests"
    } else if (!finishedReceivers) {
        "Didn't complete generating receivers"
    } else if (!completed && !failed) {
        "Did not complete testing: $timeout"
    } else {
        filter { it.failed }.sortedBy { it.complexity }.let { result ->
            val leastComplex = result.first().complexity
            result.filter { it.complexity == leastComplex }
        }.minByOrNull { it.stepCount }!!.explain(stacktrace)
    }

    @Suppress("MagicNumber")
    fun printTrace() {
        forEach { result ->
            result.apply {
                println(
                    "${runnerID.toString().padStart(4, ' ')}: $solutionReceiver ${
                    solutionExecutable.formatBoundMethodCall(
                        allParameters.solution.toParameterGroup(),
                        solutionClass
                    )
                    } -> ${solution.returned}" +
                        "\n${" ".repeat(4)}: $submissionReceiver ${
                        submissionExecutable.formatBoundMethodCall(
                            allParameters.submission.toParameterGroup(),
                            submissionClass
                        )
                        } -> ${submission.returned}"
                )
            }
        }
    }
}

@Suppress("LongParameterList")
class TestRunner(
    val runnerID: Int,
    val submission: Submission,
    var generators: Generators,
    val receiverGenerators: Sequence<Executable>,
    val captureOutput: CaptureOutput,
    val methodPicker: Submission.ExecutablePicker,
    val settings: Settings,
    val runners: List<TestRunner>,
    var receivers: Value<Any?>?
) {
    val testResults: MutableList<TestResult<*, *>> = mutableListOf()
    val skippedTests: MutableList<Int> = mutableListOf()

    var staticOnly = submission.solution.skipReceiver

    val failed: Boolean
        get() = testResults.any { it.failed }
    val ready: Boolean
        get() = methodPicker.more() && if (staticOnly) {
            true
        } else {
            (settings.runAll!! && receivers?.solution != null) || (testResults.none { it.failed } && receivers != null)
        }
    var ranLastTest = false
    var skippedLastTest = false

    var lastComplexity: Complexity? = null

    var returnedReceivers: List<Value<Any?>>? = null

    var created: Boolean
    var initialized: Boolean = false
    var tested: Boolean = false

    init {
        if (receivers == null && staticOnly) {
            receivers = if (!submission.submission.hasKotlinCompanion()) {
                Value(null, null, null, null, null, ZeroComplexity)
            } else {
                Value(
                    null,
                    submission.submission.kotlin.companionObjectInstance,
                    null,
                    submission.submission.kotlin.companionObjectInstance,
                    null,
                    ZeroComplexity
                )
            }
        }
        created = receivers != null
    }

    var count = 0

    fun Executable.checkParameters(parameters: Array<Any?>) {
        val mismatchedTypes = parameterTypes.zip(parameters).filter { (klass, parameter) ->
            if (parameter == null) {
                klass.isPrimitive
            } else {
                !klass.boxType().isAssignableFrom(parameter::class.java)
            }
        }

        check(mismatchedTypes.isEmpty()) {
            mismatchedTypes.first().let { (klass, parameter) -> "Can't assign $klass from $parameter" }
        }
    }

    fun Executable.pairRun(
        receiver: Any?,
        parameters: Array<Any?>,
        parametersCopy: Array<Any?>? = null
    ): Result<Any, ParameterGroup> {
        checkParameters(parameters)
        if (parametersCopy != null) {
            checkParameters(parametersCopy)
        }

        return captureOutput {
            @Suppress("SpreadOperator")
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
    }

    @Suppress("ReturnCount")
    fun ParameterValues<Result<Any, ParameterGroup>>.returnedReceivers(): Boolean {
        if (solution.returned == null) {
            return false
        }
        val solutionClass = this@TestRunner.submission.solution.solution
        val solutionReturnedClass = solution.returned::class.java
        if (!(
            solutionReturnedClass == solutionClass ||
                (solutionReturnedClass.isArray && solutionReturnedClass.getArrayType() == solutionClass)
            )
        ) {
            return false
        }
        if (settings.runAll!!) {
            return true
        }
        if (submission.returned == null) {
            return false
        }
        val submissionClass = this@TestRunner.submission.submission
        val submissionReturnedClass = submission.returned::class.java
        if (submissionClass == submissionReturnedClass && !solutionReturnedClass.isArray) {
            return true
        }
        if (!(submissionReturnedClass.isArray && submissionReturnedClass.getArrayType() == submissionClass)) {
            return false
        }
        check(submissionReturnedClass.isArray)
        check(solutionReturnedClass.isArray)
        require(solution.returned::class.java.getArrayDimension() == 1) {
            "No support for multi-dimensional receiver array donations"
        }
        if (submission.returned::class.java.getArrayDimension() != 1) {
            return false
        }
        val solutionSize = (solution.returned as Array<*>).size
        val submissionSize = (submission.returned as Array<*>).size
        return solutionSize == submissionSize
    }

    @Suppress("ReturnCount")
    fun extractReceivers(
        results: ParameterValues<Result<Any, ParameterGroup>>,
        parameters: Parameters,
        settings: Settings
    ): MutableList<Value<Any?>> {
        if (!results.returnedReceivers()) {
            return mutableListOf()
        }

        check(results.solution.returned!!::class.java == results.solutionCopy.returned!!::class.java) {
            "${parameters.solutionCopy.map { it }} ${parameters.solution.map { it }} " +
                "${results.solution.returned::class.java} ${results.solutionCopy.returned::class.java}"
        }

        return if (!results.solution.returned::class.java.isArray) {
            listOf(
                Value(
                    results.solution.returned,
                    results.submission.returned,
                    results.solutionCopy.returned,
                    results.submissionCopy.returned,
                    results.unmodifiedCopy.returned,
                    parameters.complexity
                )
            )
        } else {
            val solutions = results.solution.returned as Array<*>
            val submissions = results.submission.returned as Array<*>
            val solutionCopies = results.solutionCopy.returned as Array<*>
            val submissionCopies = results.submissionCopy.returned as Array<*>
            val unmodifiedCopies = results.unmodifiedCopy.returned as Array<*>

            if (solutions.size != submissions.size && !settings.runAll!!) {
                return mutableListOf()
            }
            solutions.indices.map { i ->
                Value(
                    solutions[i],
                    submissions.getOrNull(i),
                    solutionCopies[i],
                    submissionCopies.getOrNull(i),
                    unmodifiedCopies[i],
                    parameters.complexity
                )
            }.toList()
        }.toMutableList()
    }

    @Suppress("ComplexMethod", "LongMethod", "ComplexCondition", "ReturnCount", "NestedBlockDepth")
    fun run(solutionExecutable: Executable, stepCount: Int, type: TestResult.Type? = null) {
        ranLastTest = false
        skippedLastTest = false

        val creating = !created && type != TestResult.Type.INITIALIZER
        // Only proceed past failures if forced
        check(!failed || (settings.runAll!! || staticOnly))

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

        check(
            parameters.solution.filterNotNull().none {
                it::class.java == submission.submission::class.java
            }
        )
        check(
            parameters.submission.filterNotNull().none {
                it::class.java == submission.solution.solution::class.java
            }
        )
        check(
            parameters.submissionCopy.filterNotNull().none {
                it::class.java == submission.solution.solution::class.java
            }
        )
        check(
            parameters.solutionCopy.filterNotNull().none {
                it::class.java == submission.submission::class.java
            }
        )
        check(
            parameters.solution.zip(parameters.submission).filter { (solutionParameter, submissionParameter) ->
                solutionParameter != null &&
                    submissionParameter != null &&
                    solutionParameter::class.java == submission.solution.solution::class.java &&
                    submissionParameter::class.java == submission.submission::class.java
            }.none { (solutionParameter, submissionParameter) ->
                solutionParameter!!::class.java != submissionParameter!!::class.java
            }
        )
        check(
            parameters.solutionCopy.zip(parameters.submissionCopy).filter { (solutionParameter, submissionParameter) ->
                solutionParameter != null &&
                    submissionParameter != null &&
                    solutionParameter::class.java == submission.solution.solution::class.java &&
                    submissionParameter::class.java == submission.submission::class.java
            }.none { (solutionParameter, submissionParameter) ->
                solutionParameter!!::class.java != submissionParameter!!::class.java
            }
        )

        val stepType = type ?: if (!created) {
            when (solutionExecutable) {
                is Constructor<*> -> TestResult.Type.CONSTRUCTOR
                is Method -> TestResult.Type.FACTORY_METHOD
                else -> error("Unexpected executable type")
            }
        } else {
            check(receivers != null) { "No receivers available" }
            when (solutionExecutable) {
                is Constructor<*> -> {
                    check(!staticOnly) { "Static-only testing should not generate receivers" }
                    TestResult.Type.COPY_CONSTRUCTOR
                }

                is Method -> {
                    when (staticOnly || submission.solution.fauxStatic) {
                        true -> TestResult.Type.STATIC_METHOD
                        false -> TestResult.Type.METHOD
                    }
                }

                else -> error("Unexpected executable type")
            }
        }

        val stepReceivers = when {
            solutionExecutable.isStatic() && submissionExecutable.isKotlinCompanion() -> {
                Value(
                    receivers?.solution,
                    submission.submission.kotlin.companionObjectInstance,
                    receivers?.solutionCopy,
                    submission.submission.kotlin.companionObjectInstance,
                    receivers?.unmodifiedCopy,
                    receivers?.complexity ?: ZeroComplexity
                )
            }

            receivers != null -> receivers
            else -> Value(null, null, null, null, null, ZeroComplexity)
        } ?: error("Didn't set receivers")

        @Suppress("SpreadOperator")
        try {
            unwrap {
                submission.solution.filters[solutionExecutable]?.invoke(null, *parameters.solution)
            }
        } catch (e: SkipTest) {
            return
        } catch (e: BoundComplexity) {
            generator?.prev()
            return
        } catch (e: TestingControlException) {
            error("TestingControl exception mismatch: ${e::class.java})")
        }

        // Have to run these together to keep things in sync
        val solutionResult =
            solutionExecutable.pairRun(stepReceivers.solution, parameters.solution, parameters.solutionCopy)
        val solutionCopy =
            solutionExecutable.pairRun(stepReceivers.solutionCopy, parameters.solutionCopy)
        val unmodifiedCopy =
            solutionExecutable.pairRun(stepReceivers.unmodifiedCopy, parameters.unmodifiedCopy)

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

        if (settings.runAll!! && !staticOnly && created && receivers!!.submission == null) {
            skippedLastTest = true
            skippedTests += stepCount
            return
        }

        val submissionResult =
            submissionExecutable.pairRun(stepReceivers.submission, parameters.submission, parameters.submissionCopy)
        val submissionCopy =
            submissionExecutable.pairRun(stepReceivers.submissionCopy, parameters.submissionCopy)

        val createdReceivers = extractReceivers(
            ParameterValues(solutionResult, submissionResult, solutionCopy, submissionCopy, unmodifiedCopy),
            parameters,
            settings
        )
        val existingReceiverMismatch = createdReceivers.map {
            Pair(it, submission.findReceiver(runners, it.solution!!))
        }.filter { (_, runner) ->
            runner != null
        }.any { (result, runner) ->
            runner!!.receivers!!.submission !== result.submission
        }

        ranLastTest = true
        val step = TestResult(
            runnerID,
            stepCount, count++,
            solutionExecutable, submissionExecutable, stepType, parameters,
            solutionResult, submissionResult,
            Interval(start),
            parameters.complexity.level,
            submission.solution.solution,
            submission.submission,
            stepReceivers.solution,
            stepReceivers.submission,
            existingReceiverMismatch = existingReceiverMismatch
        )
        if (creating && submissionResult.returned != null && submission.solution.instanceValidator != null) {
            @Suppress("TooGenericExceptionCaught")
            try {
                unwrap { submission.solution.instanceValidator.invoke(null, submissionResult.returned) }
            } catch (e: ThreadDeath) {
                throw e
            } catch (e: Throwable) {
                step.differs.add(TestResult.Differs.INSTANCE_VALIDATION_THREW)
                step.verifierThrew = e
            }
        }

        if (step.succeeded) {
            submission.verify(solutionExecutable, step)
        }
        testResults.add(step)

        if (step.succeeded || settings.runAll) {
            generator?.next()
        } else {
            generator?.prev()
        }

        lastComplexity = parameters.complexity

        if ((step.succeeded || settings.runAll) && creating && createdReceivers.isNotEmpty()) {
            receivers = createdReceivers.removeAt(0)
        }
        if ((step.succeeded || settings.runAll)) {
            returnedReceivers = createdReceivers
        }
    }

    fun next(stepCount: Int): TestRunner {
        if (!created) {
            run(receiverGenerators.first(), stepCount)
            created = true
        } else if (!initialized && submission.solution.initializer != null) {
            run(submission.solution.initializer, stepCount, TestResult.Type.INITIALIZER)
            initialized = true
        } else {
            initialized = true
            run(methodPicker.next(), stepCount)
        }
        tested = true
        return this
    }
}

data class Interval(val start: Instant, val end: Instant) {
    constructor(start: Instant) : this(start, Instant.now())
}

sealed class TestingControlException : RuntimeException()
class SkipTest : TestingControlException()
class BoundComplexity : TestingControlException()