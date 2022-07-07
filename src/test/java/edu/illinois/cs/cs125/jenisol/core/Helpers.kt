package edu.illinois.cs.cs125.jenisol.core

import io.github.classgraph.ClassGraph
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldNotContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
    val badReceivers: List<Class<*>>,
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
    val badReceivers = testingClasses
        .filter { it.simpleName.startsWith("BadReceivers") }
    val badDesign = testingClasses
        .filter { it.simpleName.startsWith("Design") }

    return TestingClasses(testName, primarySolution, otherSolutions, incorrect, badReceivers, badDesign)
}

@Suppress("LongMethod")
fun Solution.fullTest(
    klass: Class<*>,
    seed: Int = Random.nextInt(),
    solutionResults: TestResults? = null
): TestResults {
    val submissionKlass = submission(klass)
    run {
        val first = submissionKlass.test(Settings(seed = seed, testing = true))
        val second = submissionKlass.test(Settings(seed = seed, testing = true))
        first.size shouldBe second.size
        first.forEachIndexed { index, firstResult ->
            val secondResult = second[index]
            submissionKlass.compare(firstResult.parameters, secondResult.parameters) shouldBe true
            firstResult.runnerID shouldBe secondResult.runnerID
        }
    }
    run {
        val first = submissionKlass.test(Settings(seed = seed, shrink = false, testing = true))
        val second = submissionKlass.test(Settings(seed = seed, shrink = false, testing = true))

        if (first.size != second.size) {
            println(second.explain())
        }
        first.size shouldBe second.size
        first.forEachIndexed { index, firstResult ->
            val secondResult = second[index]
            submissionKlass.compare(firstResult.parameters, secondResult.parameters) shouldBe true
            firstResult.runnerID shouldBe secondResult.runnerID
        }
    }
    val first = submissionKlass.test(
        Settings(seed = seed, shrink = false, runAll = true, overrideTotalCount = 1024, testing = true),
        followTrace = solutionResults?.randomTrace
    )
    val second = submissionKlass.test(
        Settings(seed = seed, shrink = false, runAll = true, overrideTotalCount = 1024, testing = true),
        followTrace = solutionResults?.randomTrace
    )
    first.size + first.skippedSteps.size shouldBe 1024
    first.size shouldBe second.size
    first.forEachIndexed { index, firstResult ->
        val secondResult = second[index]
        submissionKlass.compare(firstResult.parameters, secondResult.parameters) shouldBe true
        firstResult.runnerID shouldBe secondResult.runnerID
    }
    if (solutionResults != null) {
        solutionResults.size shouldBe first.size + first.skippedSteps.size
        solutionResults.forEach { solutionResult ->
            val firstResult = first.find { it.stepCount == solutionResult.stepCount }
            val secondResult = second.find { it.stepCount == solutionResult.stepCount }
            if (firstResult == null) {
                secondResult shouldBe null
                first.skippedSteps.find { it == solutionResult.stepCount } shouldNotBe null
                second.skippedSteps.find { it == solutionResult.stepCount } shouldNotBe null
                return@forEach
            }
            check(secondResult != null)
            submissionKlass.compare(firstResult.parameters, secondResult.parameters) shouldBe true
            firstResult.runnerID shouldBe secondResult.runnerID
            submissionKlass.compare(solutionResult.parameters, firstResult.parameters) shouldBe true
            solutionResult.runnerID shouldBe firstResult.runnerID
        }
    }
    return first
}

@Suppress("NestedBlockDepth", "ComplexMethod")
fun Class<*>.test() = this.testingClasses().apply {
    val seed = Random.nextInt()
    solution(primarySolution).apply {
        val solutionResults = submission(primarySolution).let {
            if (!primarySolution.isDesignOnly()) {
                fullTest(primarySolution, seed = seed).also { results ->
                    check(results.succeeded) { "Solution did not pass testing: ${results.explain()}" }
                }
            } else {
                null
            }
        }
        otherSolutions.forEach { correct ->
            submission(correct).also {
                if (!primarySolution.isDesignOnly()) {
                    fullTest(correct, seed = seed, solutionResults = solutionResults).also { results ->
                        check(!results.timeout)
                        check(results.succeeded) {
                            "Class marked as correct did not pass testing: ${results.explain(stacktrace = true)}"
                        }
                    }
                }
            }
        }
        (incorrect + badDesign + badReceivers)
            .apply {
                check(isNotEmpty()) { "No incorrect examples.java.examples for $testName" }
            }.forEach { incorrect ->
                if (incorrect in badDesign) {
                    shouldThrow<SubmissionDesignError> {
                        submission(incorrect)
                    }
                } else {
                    check(!primarySolution.isDesignOnly()) {
                        "Can't test Incorrect* examples when solution is design only"
                    }
                    fullTest(
                        incorrect,
                        seed = seed,
                        solutionResults = solutionResults
                    ).also { results ->
                        results.threw shouldBe null
                        results.timeout shouldBe false
                        results.failed shouldBe true
                        results.filter { it.failed }
                            .map { it.type }
                            .distinct() shouldNotContainAll testingStepsShouldNotContain
                    }
                }
            }
    }
}