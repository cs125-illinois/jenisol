package edu.illinois.cs.cs125.jenisol.core

import io.github.classgraph.ClassGraph
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldNotContainAll
import io.kotest.matchers.shouldBe

fun Class<*>.isKotlinAnchor() = simpleName == "Correct" && declaredMethods.isEmpty()

fun Class<*>.testName() = packageName.removePrefix("examples.")

val testingStepsShouldNotContain = setOf(
    TestResult.Type.CONSTRUCTOR,
    TestResult.Type.INITIALIZER
)

@Suppress("NestedBlockDepth", "ComplexMethod")
fun Class<*>.test() {
    val testName = packageName.removePrefix("examples.")
    val packageClasses = ClassGraph().acceptPackages(packageName).scan().allClasses.map { it.loadClass() }

    val testingClasses = if (packageClasses.find { it.isKotlinAnchor() } != null) {
        ClassGraph().acceptPackages(
            packageName.split(".").dropLast(1).joinToString(".")
        ).scan().allClasses.map { it.loadClass() }
    } else {
        packageClasses
    }.filter { !it.isInterface && (it.declaredMethods.isNotEmpty() || it.declaredFields.isNotEmpty()) }

    val primarySolution = testingClasses
        .find { it.simpleName == "Correct" || it.simpleName == "CorrectKt" }
        ?: error("Couldn't find primary solution in package $this")

    solution(primarySolution).apply {
        /*
        submission(primarySolution).also {
            if (!primarySolution.isDesignOnly()) {
                it.test().also { results ->
                    check(results.succeeded) { "Solution did not pass testing: ${results.explain()}" }
                }
            }
        }
         */
        testingClasses
            .filter { it != primarySolution && it.simpleName.startsWith("Correct") }
            .forEach { correct ->
                submission(correct).also { submission ->
                    if (!primarySolution.isDesignOnly()) {
                        submission.test().also { results ->
                            check(results.succeeded) {
                                "Class marked as correct did not pass testing: ${results.explain()}"
                            }
                        }
                    }
                }
            }
        testingClasses
            .filter {
                (
                    it.simpleName.startsWith("Incorrect") ||
                        it.simpleName.startsWith("Design")
                    ) &&
                    !it.simpleName.startsWith("Ignore")
            }
            .apply {
                check(isNotEmpty()) { "No incorrect examples.java.examples for $testName" }
            }.forEach { incorrect ->
                if (incorrect.simpleName.startsWith("Design")) {
                    shouldThrow<SubmissionDesignError> { submission(incorrect) }
                } else {
                    check(!primarySolution.isDesignOnly()) {
                        "Can't test Incorrect* examples when solution is design only"
                    }
                    submission(incorrect).test().also { results ->
                        results.failed shouldBe true
                        results.filter { it.failed }
                            .map { it.type }
                            .distinct() shouldNotContainAll testingStepsShouldNotContain
                    }
                }
            }
    }
}
