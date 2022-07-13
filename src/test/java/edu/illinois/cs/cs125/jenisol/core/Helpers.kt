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
    isCorrect: Boolean,
    solutionResults: TestResults? = null
): Pair<TestResults, TestResults> {
    val baseSettings = Settings(
        seed = seed,
        testing = true,
        minTestCount = 64.coerceAtMost(maxCount),
        maxTestCount = 1024.coerceAtMost(maxCount)
    )

    @Suppress("RethrowCaughtException")
    fun TestResults.checkResults() = try {
        filter { !receiverAsParameter || it.type != TestResult.Type.CONSTRUCTOR }
            .map { it.runnerID }.zipWithNext().all { (first, second) -> first <= second } shouldBe true

        if (isCorrect) {
            succeeded shouldBe true
        } else {
            succeeded shouldBe false
            threw shouldBe null
            timeout shouldBe false
            failed shouldBe true
            results.filter { it.failed }
                .map { it.type }
                .distinct() shouldNotContainAll testingStepsShouldNotContain
        }
        this
    } catch (e: Throwable) {
        throw e
    }

    val submissionKlass = submission(klass)
    val original = submissionKlass.test(baseSettings).checkResults()
    run {
        val second = submissionKlass.test(baseSettings).checkResults()
        original.size shouldBe second.size
        original.forEachIndexed { index, firstResult ->
            val secondResult = second[index]
            submissionKlass.compare(firstResult.parameters, secondResult.parameters) shouldBe true
            firstResult.runnerID shouldBe secondResult.runnerID
        }
    }
    run {
        val noShrinkSettings = baseSettings.copy(shrink = false)
        val first = submissionKlass.test(noShrinkSettings).checkResults()
        val second = submissionKlass.test(noShrinkSettings).checkResults()

        first.size shouldBe second.size
        first.forEachIndexed { index, firstResult ->
            val secondResult = second[index]
            submissionKlass.compare(firstResult.parameters, secondResult.parameters) shouldBe true
            firstResult.runnerID shouldBe secondResult.runnerID
        }
    }
    val testAllCounts = solutionResults?.size ?: 256.coerceAtLeast(original.size).coerceAtMost(maxCount)
    val testAllSettings =
        baseSettings.copy(
            shrink = false,
            runAll = !isCorrect,
            totalTestCount = testAllCounts,
            minTestCount = -1,
            maxTestCount = -1
        )

    val first = submissionKlass.test(testAllSettings, followTrace = solutionResults?.randomTrace).checkResults()
    val second = submissionKlass.test(testAllSettings, followTrace = solutionResults?.randomTrace).checkResults()
    first.size + first.skippedSteps.size shouldBe testAllCounts
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
    return Pair(original, first)
}

@Suppress("NestedBlockDepth", "ComplexMethod")
fun Class<*>.test() = this.testingClasses().apply {
    val seed = Random.nextInt()
    solution(primarySolution).apply {
        val (_, solutionResults) = submission(primarySolution).let {
            if (!primarySolution.isDesignOnly()) {
                fullTest(primarySolution, seed = seed, isCorrect = true).also { (results) ->
                    check(results.succeeded) { "Solution did not pass testing: ${results.explain()}" }
                }
            } else {
                Pair(null, null)
            }
        }
        otherSolutions.forEach { correct ->
            submission(correct).also {
                if (!primarySolution.isDesignOnly()) {
                    fullTest(
                        correct,
                        seed = seed,
                        isCorrect = true,
                        solutionResults = solutionResults
                    ).first.also { results ->
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
                        isCorrect = false,
                        solutionResults = solutionResults
                    ).first.also { results ->
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