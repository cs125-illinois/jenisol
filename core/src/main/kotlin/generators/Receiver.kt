// ktlint-disable filename
@file:Suppress("MatchingDeclarationName", "MemberVisibilityCanBePrivate")

package edu.illinois.cs.cs125.jenisol.core.generators

import edu.illinois.cs.cs125.jenisol.core.PairRunner
import edu.illinois.cs.cs125.jenisol.core.Submission
import edu.illinois.cs.cs125.jenisol.core.unwrapMethodInvocationException
import kotlin.random.Random

class ReceiverGenerator(
    val submission: Submission,
    val methodGenerators: MethodGenerators,
    random: Random = Random
) {
    @Suppress("ArrayInDataClass")

    val constructors = sequence {
        while (true) {
            yieldAll(submission.solution.solutionConstructors.toList().shuffled(random))
        }
    }

    fun generate(runnerID: Int): List<PairRunner.Step> = create(runnerID).let { createResult ->
        if (!submission.solution.hasInitializer || createResult.failed) {
            listOf(createResult)
        } else {
            listOf(createResult, initialize(runnerID, createResult))
        }
    }

    private fun create(runnerID: Int): PairRunner.Step {
        check(!submission.solution.onlyStatic) { "Won't generate receivers when all test methods are static" }

        val solutionConstructor = constructors.first()
        val generator = methodGenerators[solutionConstructor]
            ?: error("Couldn't find a parameter generator that should exist")
        val parameters = generator.generate()
        val solutionResult = submission.solution.captureOutput {
            unwrapMethodInvocationException {
                solutionConstructor.newInstance(*parameters.solution)
            }
        }
        val submissionConstructor = submission.submissionConstructors[solutionConstructor] ?: error(
            "Answerable couldn't find a submission constructor that should exist"
        )
        val submissionResult = submission.solution.captureOutput {
            unwrapMethodInvocationException {
                submissionConstructor.newInstance(*parameters.submission)
            }
        }
        return PairRunner.Step(
            runnerID, solutionResult, submissionResult, PairRunner.Step.Type.CONSTRUCTOR
        ).also { step ->
            submission.solution.compare(step)
            if (step.succeeded) {
                generator.next()
            } else {
                generator.prev()
            }
        }
    }

    private fun initialize(runnerID: Int, created: PairRunner.Step): PairRunner.Step {
        val solutionReceiver = created.solution.returned
        val submissionReceiver = created.submission.returned
        val initializer = submission.solution.initializer.first()

        val generator = methodGenerators[initializer]
            ?: error("Couldn't find a parameter generator that should exist")
        val parameters = generator.generate()
        val solutionResult = submission.solution.captureOutput {
            unwrapMethodInvocationException {
                initializer.invoke(solutionReceiver, *parameters.solution)
            }
        }
        val submissionResult = submission.solution.captureOutput {
            unwrapMethodInvocationException {
                initializer.invoke(submissionReceiver, *parameters.submission)
            }
        }
        return PairRunner.Step(
            runnerID, solutionResult, submissionResult, PairRunner.Step.Type.INITIALIZER
        ).also { step ->
            submission.solution.compare(step)
            if (step.succeeded) {
                generator.next()
            } else {
                generator.prev()
            }
        }
    }
}
