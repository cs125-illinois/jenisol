package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.ObjectGenerator
import edu.illinois.cs.cs125.jenisol.core.generators.ReceiverGenerator
import edu.illinois.cs.cs125.jenisol.core.generators.TypeGeneratorGenerator
import edu.illinois.cs.cs125.jenisol.core.generators.getArrayDimension
import java.lang.RuntimeException
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.random.Random

class Submission(val solution: Solution, val submission: Class<*>, private val source: String? = null) {
    init {
        if (!solution.solution.visibilityMatches(submission)) {
            throw SubmissionDesignClassError(
                submission,
                "is not ${solution.solution.getVisibilityModifier() ?: "package private"}"
            )
        }
        if (solution.solution.superclass != null && solution.solution.superclass != submission.superclass) {
            throw SubmissionDesignClassError(
                submission,
                "does not extend ${solution.solution.superclass.name}"
            )
        }
        val solutionInterfaces = solution.solution.interfaces.toSet()
        val submissionInterfaces = submission.interfaces.toSet()
        val missingInterfaces = solutionInterfaces.minus(submissionInterfaces)
        if (missingInterfaces.isNotEmpty()) {
            throw SubmissionDesignClassError(
                submission,
                "does not implement ${missingInterfaces.joinToString(separator = ", ") { it.name }}"
            )
        }
        val extraInterfaces = submissionInterfaces.minus(solutionInterfaces)
        if (extraInterfaces.isNotEmpty()) {
            throw SubmissionDesignClassError(
                submission,
                "does implements extra interfaces ${extraInterfaces.joinToString(separator = ", ") { it.name }}"
            )
        }
        solution.solution.typeParameters.forEachIndexed { i, type ->
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            try {
                if (!submission.typeParameters[i].bounds.contentEquals(type.bounds)) {
                    throw SubmissionTypeParameterError(submission)
                }
            } catch (e: Exception) {
                throw SubmissionTypeParameterError(submission)
            }
        }
        if (submission.typeParameters.size > solution.solution.typeParameters.size) {
            throw SubmissionTypeParameterError(submission)
        }
    }

    init {
        solution.bothExecutables.forEach {
            if (!it.parameterTypes[0].isAssignableFrom(submission)) {
                throw SubmissionDesignInheritanceError(
                    submission,
                    it.parameterTypes[0]
                )
            }
        }
    }

    private val submissionFields =
        solution.allFields.filter { it.name != "${"$"}assertionsDisabled" }.map { solutionField ->
            submission.findField(solutionField) ?: throw SubmissionDesignMissingFieldError(
                submission,
                solutionField
            )
        }.toSet()

    val submissionExecutables = solution.allExecutables
        .filter {
            !submission.isKotlin() || (!solution.skipReceiver || it !in solution.receiverGenerators)
        }
        .map { solutionExecutable ->
            when (solutionExecutable) {
                is Constructor<*> -> submission.findConstructor(solutionExecutable, solution.solution)
                is Method -> submission.findMethod(solutionExecutable, solution.solution)
                else -> error("Encountered unexpected executable type: $solutionExecutable")
            }?.let { executable ->
                executable.isAccessible = true
                solutionExecutable to executable
            } ?: throw SubmissionDesignMissingMethodError(
                submission,
                solutionExecutable
            )
        }.toMap().toMutableMap().also {
            if (solution.initializer != null) {
                it[solution.initializer] = solution.initializer
            }
        }.toMap()

    init {
        if (submission != solution.solution) {
            (submission.declaredMethods.toSet() + submission.declaredConstructors.toSet()).filter {
                !it.isPrivate()
            }.forEach { executable ->
                if (executable !in submissionExecutables.values) {
                    if (submission.isKotlin()) {
                        if (executable is Method && executable.name.startsWith("get")) {
                            val setterName = executable.name.replace("get", "set")
                            if (submissionExecutables.values.map { it.name }.contains(setterName)) {
                                return@forEach
                            }
                        }
                        if (solution.skipReceiver && executable is Constructor<*>) {
                            return@forEach
                        }
                        if (executable.isKotlinCompanionAccessor()) {
                            return@forEach
                        }
                        if (executable is Constructor<*> &&
                            executable.parameterTypes.last()?.name == "kotlin.jvm.internal.DefaultConstructorMarker"
                        ) {
                            return@forEach
                        }
                        if (submission.kotlin.isData && executable.isDataClassGenerated()) {
                            return@forEach
                        }
                    }
                    throw SubmissionDesignExtraMethodError(
                        submission,
                        executable
                    )
                }
            }
            submission.declaredFields.toSet().filter {
                it.name != "${"$"}assertionsDisabled" &&
                    !(submission.isKotlin() && it.name == "Companion")
            }.forEach {
                if (!it.isPrivate() && it !in submissionFields) {
                    throw SubmissionDesignExtraFieldError(submission, it)
                }
                if (it.isStatic()) {
                    throw SubmissionStaticFieldError(submission, it)
                }
            }
            if (solution.sourceChecker != null) {
                require(source != null) { "@CheckSource method provided but source not available" }
                unwrap {
                    solution.sourceChecker.invoke(null, source)
                }
            }
        }
    }

    private fun MutableList<TestRunner>.readyCount() = filter { it.ready }.count()

    private val comparators = Comparators(
        mutableMapOf(solution.solution to solution.receiverCompare, submission to solution.receiverCompare)
    )

    fun compare(solution: Any?, submission: Any?, solutionClass: Class<*>? = null, submissionClass: Class<*>? = null) =
        when (solution) {
            null -> submission == null
            else -> solution.deepEquals(submission, comparators, solutionClass, submissionClass)
        }

    fun verify(executable: Executable, result: TestResult<*, *>) {
        solution.verifiers[executable]?.also { customVerifier ->
            @Suppress("TooGenericExceptionCaught")
            try {
                unwrap { customVerifier.invoke(null, result) }
            } catch (e: Throwable) {
                if (e is ThreadDeath) {
                    throw e
                }
                result.differs.add(TestResult.Differs.VERIFIER_THREW)
                result.verifierThrew = e
            }
        } ?: defaultVerify(result)
    }

    @Suppress("ComplexMethod")
    private fun defaultVerify(result: TestResult<*, *>) {
        val solution = result.solution
        val submission = result.submission

        val strictOutput = result.solutionExecutable.annotations.find { it is Configure }?.let {
            (it as Configure).strictOutput
        } ?: false

        if (!compare(solution.threw, submission.threw, result.solutionClass, result.submissionClass)) {
            result.differs.add(TestResult.Differs.THREW)
        }
        if ((strictOutput || solution.stdout.isNotBlank()) && solution.stdout != submission.stdout) {
            result.differs.add(TestResult.Differs.STDOUT)
        }
        if ((strictOutput || solution.stderr.isNotBlank()) && solution.stderr != submission.stderr) {
            result.differs.add(TestResult.Differs.STDERR)
        }

        if (result.type == TestResult.Type.METHOD && !compare(
                solution.returned,
                submission.returned,
                result.solutionClass,
                result.submissionClass
            )
        ) {
            result.differs.add(TestResult.Differs.RETURN)
        }
        if (result.type == TestResult.Type.FACTORY_METHOD &&
            solution.returned != null &&
            solution.returned::class.java.isArray
        ) {
            @Suppress("ComplexCondition")
            if (submission.returned == null ||
                !submission.returned::class.java.isArray ||
                solution.returned::class.java.getArrayDimension()
                != submission.returned::class.java.getArrayDimension() ||
                (solution.returned as Array<*>).size != (submission.returned as Array<*>).size
            ) {
                result.differs.add(TestResult.Differs.RETURN)
            }
        }
        if (!compare(solution.parameters, submission.parameters, result.solutionClass, result.submissionClass)) {
            result.differs.add(TestResult.Differs.PARAMETERS)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun List<TestRunner>.toResults(settings: Settings) =
        TestResults(
            map { it.testResults as List<TestResult<Any, ParameterGroup>> }.flatten().sortedBy { it.stepCount },
            settings
        )

    private fun List<TestRunner>.failed() = filter { it.failed }.also { runners ->
        check(runners.all { it.lastComplexity != null }) { "Runner failed without recording complexity" }
    }.minByOrNull { it.lastComplexity!!.level }

    @Suppress("LongMethod", "ComplexMethod", "ReturnCount")
    fun test(
        passedSettings: Settings = Settings(),
        captureOutput: CaptureOutput = ::defaultCaptureOutput
    ): TestResults {
        if (solution.solution.isDesignOnly()) {
            throw DesignOnlyTestingError(solution.solution)
        }
        val settings = solution.setCounts(Settings.DEFAULTS merge passedSettings)

        val random = if (settings.seed == -1) {
            Random
        } else {
            Random(settings.seed.toLong())
        }

        val methodIterator = sequence {
            yield(solution.methodsToTest.shuffled(random).first())
        }

        val runners: MutableList<TestRunner> = mutableListOf()
        var stepCount = 0

        val receiverGenerators = sequence {
            while (true) {
                yieldAll(solution.receiverGenerators.toList().shuffled(random))
            }
        }

        val (receiverGenerator, initialGenerators) = if (!solution.skipReceiver) {
            check(settings.receiverCount > 1) { "Incorrect receiver count" }

            val generators = solution.generatorFactory.get(
                random,
                settings,
                null,
                solution.receiversAndInitializers
            )
            var receiverGoalMet = false
            @Suppress("UnusedPrivateMember")
            for (unused in 0..(settings.receiverCount * settings.receiverRetries)) {
                TestRunner(
                    runners.size,
                    this,
                    generators,
                    receiverGenerators,
                    captureOutput,
                    methodIterator
                ).also { runner ->
                    runner.next(stepCount++)
                    runners.add(runner)
                }
                runners.failed()?.also {
                    if (!settings.shrink!! || it.lastComplexity!!.level == 0) {
                        return runners.toResults(settings)
                    }
                }
                if (runners.readyCount() == settings.receiverCount) {
                    receiverGoalMet = true
                    break
                }
            }
            // If we couldn't generate the requested number of receivers due to constructor failures,
            // just give up and return at this point
            if (!receiverGoalMet) {
                return runners.toResults(settings)
            }
            Pair(
                ReceiverGenerator(
                    random,
                    runners.filter { it.ready }.toMutableList()
                ),
                generators
            )
        } else {
            Pair(null, null)
        }

        @Suppress("UNCHECKED_CAST")
        val generatorOverrides = if (receiverGenerator != null) {
            mutableMapOf(
                (solution.solution as Type) to ({ _: Random -> receiverGenerator } as TypeGeneratorGenerator),
                (Any::class.java as Type) to { r: Random ->
                    ObjectGenerator(
                        r,
                        receiverGenerator
                    )
                }
            )
        } else {
            mapOf()
        }

        val generators = solution.generatorFactory.get(random, settings, generatorOverrides, from = initialGenerators)
        runners.filter { it.ready }.forEach {
            it.generators = generators
        }

        val totalTests = if (settings.overrideTotalCount != -1) {
            settings.overrideTotalCount
        } else {
            settings.receiverCount * settings.methodCount
        }.let {
            if (settings.minTestCount != -1) {
                listOf(settings.minTestCount, it).maxOrNull() ?: error("Bad min")
            } else {
                it
            }
        }
        val startMultipleCount = if (settings.startMultipleCount != -1) {
            settings.startMultipleCount
        } else {
            settings.methodCount
        }

        for (totalCount in 0 until totalTests) {
            if (Thread.interrupted()) {
                break
            }
            val usedRunner = if (runners.readyCount() < settings.receiverCount) {
                TestRunner(
                    runners.size,
                    this,
                    generators,
                    receiverGenerators,
                    captureOutput,
                    methodIterator
                ).also { runner ->
                    runner.next(stepCount++)
                    if (solution.initializer != null && runner.ready) {
                        runner.next(stepCount++)
                    }
                    runners.add(runner)
                    receiverGenerator?.runners?.add(runner)
                }
            } else {
                if (totalCount < startMultipleCount) {
                    runners.first { it.ready }.also {
                        it.next(stepCount++)
                    }
                } else {
                    runners.filter { it.ready }.shuffled(random).first().also {
                        it.next(stepCount++)
                    }
                }
            }
            runners.failed()?.also {
                if (!settings.shrink!! || it.lastComplexity!!.level == 0) {
                    return runners.toResults(settings)
                }
            }

            if (usedRunner.returnedReceivers != null) {
                usedRunner.returnedReceivers!!.forEach { returnedReceiver ->
                    runners.add(
                        TestRunner(
                            runners.size,
                            this,
                            generators,
                            receiverGenerators,
                            captureOutput,
                            methodIterator,
                            returnedReceiver
                        )
                    )
                }
                usedRunner.returnedReceivers = null
            }
        }
        return runners.toResults(settings)
    }
}

sealed class SubmissionDesignError(message: String) : RuntimeException(message)
class SubmissionDesignMissingMethodError(klass: Class<*>, executable: Executable) : SubmissionDesignError(
    "Submission class ${klass.name} didn't provide ${
        if (executable.isStatic()) {
            "static "
        } else {
            ""
        }
    }${
        if (executable is Method) {
            "method"
        } else {
            "constructor"
        }
    } ${executable.fullName()}"
)

class SubmissionDesignExtraMethodError(klass: Class<*>, executable: Executable) : SubmissionDesignError(
    "Submission class ${klass.name} provided extra ${
        if (executable.isStatic()) {
            "static "
        } else {
            ""
        }
    }${
        if (executable is Method) {
            "method"
        } else {
            "constructor"
        }
    } ${executable.fullName()}"
)

class SubmissionDesignInheritanceError(klass: Class<*>, parent: Class<*>) : SubmissionDesignError(
    "Submission class ${klass.name} didn't inherit from ${parent.name}"
)

class SubmissionTypeParameterError(klass: Class<*>) : SubmissionDesignError(
    "Submission class ${klass.name} has missing, unnecessary, or incorrectly-bounded type parameters"
)

class SubmissionDesignMissingFieldError(klass: Class<*>, field: Field) : SubmissionDesignError(
    "Field ${field.fullName()} is not accessible in submission class ${klass.name} but should be"
)

class SubmissionDesignExtraFieldError(klass: Class<*>, field: Field) : SubmissionDesignError(
    "Field ${field.fullName()} is accessible in submission class ${klass.name} but should not be"
)

class SubmissionStaticFieldError(klass: Class<*>, field: Field) : SubmissionDesignError(
    "Field ${field.fullName()} is static in submission class ${klass.name} but no current support for static fields"
)

class SubmissionDesignClassError(klass: Class<*>, message: String) : SubmissionDesignError(
    "Submission class ${klass.name} $message"
)

class DesignOnlyTestingError(klass: Class<*>) : Exception(
    "Solution class ${klass.name} is marked as design only"
)

class SourceCheckError(message: String) : SubmissionDesignError(message)

@Suppress("SwallowedException")
fun unwrap(run: () -> Any?): Any? = try {
    run()
} catch (e: InvocationTargetException) {
    throw e.cause ?: error("InvocationTargetException should have a cause")
}

fun Class<*>.isKotlin() = getAnnotation(Metadata::class.java) != null
