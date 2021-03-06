package edu.illinois.cs.cs125.jenisol.core

import io.github.classgraph.ClassGraph
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldNotContainAll
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.random.Random

fun Class<*>.isKotlinAnchor() = simpleName == "Correct" && declaredMethods.isEmpty()

fun Class<*>.testName() = packageName.removePrefix("examples.")

val testingStepsShouldNotContain = setOf(
    TestResult.Type.CONSTRUCTOR,
    TestResult.Type.INITIALIZER
)

data class TestingClasses(
    val testName: String,
    val primarySolution: Class<*>,
    val otherSolutions: List<Class<*>>,
    val incorrect: List<Class<*>>,
    val badDesign: List<Class<*>>
)

fun Class<*>.testingClasses(): TestingClasses {
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
    val otherSolutions = testingClasses
        .filter { it != primarySolution && it.simpleName.startsWith("Correct") }

    val incorrect = testingClasses
        .filter { it.simpleName.startsWith("Incorrect") }
    val badDesign = testingClasses
        .filter { it.simpleName.startsWith("Design") }

    return TestingClasses(testName, primarySolution, otherSolutions, incorrect, badDesign)
}

fun Solution.doubleTest(klass: Class<*>, source: String? = null): TestResults {
    val seed = Random.nextInt()
    val first = submission(klass, source).test(Settings(seed = seed))
    val second = submission(klass, source).test(Settings(seed = seed))
    first.size shouldBe second.size
    first.forEachIndexed { index, firstResult ->
        val secondResult = second[index]
        submission(klass).compare(firstResult.parameters, secondResult.parameters) shouldBe true
    }
    return first
}

@Suppress("NestedBlockDepth", "ComplexMethod")
fun Class<*>.test() = this.testingClasses().apply {
    solution(primarySolution).apply {
        submission(primarySolution).also {
            if (!primarySolution.isDesignOnly()) {
                doubleTest(primarySolution).also { results ->
                    check(results.succeeded) { "Solution did not pass testing: ${results.explain()}" }
                }
            }
        }

        otherSolutions.forEach { correct ->
            submission(correct).also {
                if (!primarySolution.isDesignOnly()) {
                    doubleTest(correct).also { results ->
                        check(results.succeeded) {
                            "Class marked as correct did not pass testing: ${results.explain(stacktrace = true)}"
                        }
                    }
                }
            }
        }
        (incorrect + badDesign)
            .apply {
                check(isNotEmpty()) { "No incorrect examples.java.examples for $testName" }
            }.forEach { incorrect ->
                val source = try {
                    File(System.getProperty("user.dir"))
                        .resolve("src/test/java/")
                        .resolve(incorrect.name.replace(".", "/") + ".java").readText()
                } catch (e: Exception) {
                    null
                }
                if (incorrect.simpleName.startsWith("Design")) {
                    shouldThrow<SubmissionDesignError> { submission(incorrect, source) }
                } else {
                    check(!primarySolution.isDesignOnly()) {
                        "Can't test Incorrect* examples when solution is design only"
                    }
                    doubleTest(incorrect, source).also { results ->
                        results.failed shouldBe true
                        results.filter { it.failed }
                            .map { it.type }
                            .distinct() shouldNotContainAll testingStepsShouldNotContain
                    }
                }
            }
    }
}
