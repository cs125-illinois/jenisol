package edu.illinois.cs.cs125.jenisol.core

import io.github.classgraph.ClassGraph
import io.kotlintest.matchers.collections.shouldNotContainAll
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

@Suppress("RemoveSingleExpressionStringTemplate")
class TestExamples : StringSpec({
    examples.noreceiver.single.noarguments.Correct::class.java.also {
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
    examples.noreceiver.single.withverifier.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.withprimitiveverifier.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.withparameterverifier.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.intarrayargument.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.generatedparameterarrays.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.receiver.single.noarguments.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.receiver.single.withinitialization.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.noreceiver.single.countarrayequals.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
})

@Suppress("NestedBlockDepth")
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
                    submission(correct.loadClass()).test().also { results ->
                        results.succeeded() shouldBe true
                    }
                }
            allClasses
                .filter { it.simpleName.startsWith("Incorrect") || it.simpleName.startsWith("Design") }
                .apply {
                    check(isNotEmpty()) { "No incorrect examples for ${testName()}" }
                }.forEach { incorrect ->
                    if (incorrect.simpleName.startsWith("Design")) {
                        shouldThrow<ClassDesignError> {
                            submission(incorrect.loadClass())
                        }
                    } else {
                        submission(incorrect.loadClass()).test().also { results ->
                            results.failed() shouldBe true
                            results.filter { it.failed }
                                .map { it.type }
                                .distinct() shouldNotContainAll setOf(
                                TestResult.Type.CONSTRUCTOR, TestResult.Type.INITIALIZER
                            )
                        }
                    }
                }
        }
    }
}
