package edu.illinois.cs.cs125.jenisol.core

import examples.java.noreceiver.single.generatedparameterarrays.Correct
import io.github.classgraph.ClassGraph
import io.kotlintest.matchers.collections.shouldNotContainAll
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

@Suppress("RemoveSingleExpressionStringTemplate")
class TestExamples : StringSpec({
    examples.java.noreceiver.single.noarguments.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.intargument.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.twointarguments.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.prints.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.submissionprints.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.edgetype.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.simpletype.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.randomtype.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.returnsarray.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.onefixedparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.twofixedparameters.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.onerandomparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.onegeneratedparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.withverifier.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.withprimitiveverifier.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.withparameterverifier.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.intarrayargument.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.single.noarguments.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.single.withconstructor.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.single.withinitialization.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.countarrayequals.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.single.receiverparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.single.constructorthrows.Correct::class.java.also {
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
                .filter { !it.isInterface && it.simpleName != "Correct" && it.simpleName.startsWith("Correct") }
                .forEach { correct ->
                    submission(correct.loadClass()).test().also { results ->
                        results.succeeded() shouldBe true
                    }
                }
            allClasses
                .filter { it.simpleName.startsWith("Incorrect") || it.simpleName.startsWith("Design") }
                .apply {
                    check(isNotEmpty()) { "No incorrect examples.java.examples for ${testName()}" }
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
