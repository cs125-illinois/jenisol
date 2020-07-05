package edu.illinois.cs.cs125.jenisol.core

import io.github.classgraph.ClassGraph
import io.kotlintest.matchers.collections.shouldNotContainAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

@Suppress("RemoveSingleExpressionStringTemplate")
class TestExamples : StringSpec({
    examples.noreceiver.single.noarguments.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.receiver.single.noarguments.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.receiver.single.withinitialization.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.intargument.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.twointarguments.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.prints.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.submissionprints.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.edgetype.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.simpletype.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.randomtype.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.returnsarray.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.onefixedparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.twofixedparameters.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.onerandomparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.onegeneratedparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
})

fun Class<*>.test() {
    solution(this).apply {
        try {
            submission(solution).test()
        } catch (e: Exception) {
            e.printStackTrace()
            error("Solution did not pass testing")
        }
        ClassGraph().acceptPackages(packageName).scan().apply {
            allClasses
                .filter { it.simpleName != "Correct" && it.simpleName.startsWith("Correct") }
                .forEach { correct ->
                    submission(correct.loadClass()).test()
                }
            allClasses
                .filter { it.simpleName.startsWith("Incorrect") }
                .apply {
                    check(isNotEmpty()) { "No incorrect examples for ${testName()}" }
                }.forEach { incorrect ->
                    submission(incorrect.loadClass()).test().also { results ->
                        results.failed() shouldBe true
                        results.filter { it.failed }
                            .map { it.type }
                            .distinct() shouldNotContainAll setOf(
                            PairRunner.Step.Type.CONSTRUCTOR, PairRunner.Step.Type.INITIALIZER
                        )
                    }
                }
        }
    }
}
