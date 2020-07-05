package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.ExecutableGenerators
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method

data class Result(val returned: Any?, val threw: Throwable?, val stdout: String, val stderr: String)
data class TestStep(
    val runnerID: Int, val executable: Executable, val type: Type,
    val solution: Result, val submission: Result
) {
    enum class Type { CONSTRUCTOR, INITIALIZER, METHOD }
    enum class Differs { STDOUT, STDERR, RETURN, THREW }

    val differs: MutableSet<Differs> = mutableSetOf()
    val succeeded: Boolean
        get() = differs.isEmpty()
    val failed: Boolean
        get() = !succeeded
}

class TestRunner(
    val runnerID: Int,
    val submission: Submission,
    val executableGenerators: ExecutableGenerators,
    val constructors: Sequence<Constructor<*>>
) {

    data class ReceiverPair(var solution: Any?, var submission: Any?)

    val methodIterator = submission.solution.solutionMethods.cycle()
    val testSteps: MutableList<TestStep> = mutableListOf()

    val ready: Boolean
        get() = testSteps.none { it.failed }

    val receivers: ReceiverPair by lazy {
        if (submission.solution.onlyStatic) {
            ReceiverPair(null, null)
        } else {
            testSteps.find { it.type == TestStep.Type.CONSTRUCTOR && it.succeeded }?.let {
                check(it.solution.returned != null) { "Constructor returned null" }
                check(it.submission.returned != null) { "Constructor returned null" }
                ReceiverPair(it.solution.returned, it.submission.returned)
            } ?: error("Couldn't find a receiver pair")
        }
    }

    fun run(solutionExecutable: Executable, type: TestStep.Type? = null): TestStep {
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
        }
        val submissionResult = submission.solution.captureOutput {
            unwrapMethodInvocationException {
                when (submissionExecutable) {
                    is Method -> submissionExecutable.invoke(receivers.submission, *parameters.submission)
                    is Constructor<*> -> submissionExecutable.newInstance(*parameters.submission)
                    else -> error("encountered unknown executable type: $submissionExecutable")
                }
            }
        }
        val stepType = type ?: when (solutionExecutable) {
            is Method -> TestStep.Type.METHOD
            is Constructor<*> -> TestStep.Type.CONSTRUCTOR
            else -> error("encountered unknown executable type: $solutionExecutable")
        }

        return TestStep(runnerID, solutionExecutable, stepType, solutionResult, submissionResult).also { step ->
            submission.solution.compare(step)
            testSteps.add(step)
            if (step.succeeded) {
                generator.next()
            } else {
                generator.prev()
            }
        }
    }

    var created = false
    fun next(): Boolean {
        if (!submission.solution.onlyStatic && !created) {
            run(constructors.first())
            if (ready && submission.solution.initializer != null) {
                run(submission.solution.initializer, TestStep.Type.INITIALIZER)
            }
            created = true
        } else {
            run(methodIterator.first())
        }
        return ready
    }
}

@Suppress("unused")
fun List<TestStep>.succeeded() = all { it.succeeded }
fun List<TestStep>.failed() = any { it.failed }
