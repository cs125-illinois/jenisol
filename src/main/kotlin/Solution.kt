@file:Suppress("TooManyFunctions")

package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.GeneratorFactory
import edu.illinois.cs.cs125.jenisol.core.generators.boxType
import edu.illinois.cs.cs125.jenisol.core.generators.getArrayDimension
import edu.illinois.cs.cs125.jenisol.core.generators.getArrayType
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.pow
import kotlin.random.Random
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

class Solution(val solution: Class<*>) {
    init {
        solution.declaredFields.filter { it.isStatic() && !it.isJenisol() && !it.isPrivate() && !it.isFinal() }
            .also { fields ->
                checkDesign(fields.isEmpty()) {
                    "No support for testing classes with modifiable static fields yet: ${fields.map { it.name }}"
                }
            }
    }

    val allFields = solution.declaredFields.filter {
        !it.isJenisol() && !it.isPrivate()
    }

    val allExecutables =
        (solution.declaredMethods.toSet() + solution.declaredConstructors.toSet())
            .filterNotNull()
            .filter {
                !it.isPrivate() && !it.isJenisol()
            }.toSet().also {
                checkDesign(it.isNotEmpty() || allFields.isNotEmpty()) { "Found no methods or fields to test" }
            }

    init {
        allExecutables.forEach { it.isAccessible = true }
    }

    val bothExecutables = solution.declaredMethods.toSet().filterNotNull().filter {
        it.isBoth()
    }.onEach { checkDesign { Both.validate(it, solution) } }.toSet()

    private fun Executable.receiverParameter() = parameterTypes.any { it == solution }

    private fun Executable.objectParameter() = parameterTypes.any { it::class.java == Any::class.java }

    val receiverGenerators = allExecutables.filter { executable ->
        !executable.receiverParameter()
    }.filter { executable ->
        when (executable) {
            is Constructor<*> -> true
            is Method -> executable.isStatic() && (
                executable.returnType == solution || (
                    executable.returnType.isArray &&
                        executable.returnType.getArrayType() == solution &&
                        executable.returnType.getArrayDimension() == 1
                    )
                )
            else -> designError("Unexpected executable type")
        }
    }.toSet()
    val methodsToTest = (allExecutables - receiverGenerators + bothExecutables).also {
        checkDesign(it.isNotEmpty() || solution.isDesignOnly()) {
            "Found methods that generate receivers but no ways to test them"
        }
    }
    private val needsReceiver = methodsToTest.filter { executable ->
        executable.receiverParameter() || (executable is Method && !executable.isStatic())
    }.toSet()
    private val receiverTransformers = methodsToTest.filterIsInstance<Method>().filter { method ->
        method.returnType.name != "void" && method.returnType != solution
    }.filter { method ->
        !method.isStatic() || method.receiverParameter()
    }.toSet()

    val skipReceiver = needsReceiver.isEmpty() && receiverTransformers.isEmpty() &&
        (
            receiverGenerators.isEmpty() ||
                (receiverGenerators.size == 1 && receiverGenerators.first().parameters.isEmpty())
            )

    init {
        if (needsReceiver.isNotEmpty()) {
            checkDesign(receiverGenerators.isNotEmpty()) { "No way to generate needed receivers" }
        }
        if (!skipReceiver && receiverGenerators.isNotEmpty()) {
            checkDesign(!(receiverTransformers.isEmpty() && bothExecutables.isEmpty())) {
                "No way to verify generated receivers"
            }
        }
    }

    val instanceValidator = solution.declaredMethods.filter {
        it.isInstanceValidator()
    }.also {
        checkDesign(it.size <= 1) { "Solution has multiple methods annotated with @InstanceValidator" }
    }.firstOrNull()?.also {
        checkDesign { InstanceValidator.validate(it) }
    }

    val shouldContinue = solution.declaredMethods.filter {
        it.isShouldContinue()
    }.also {
        checkDesign(it.size <= 1) { "Solution has multiple methods annotated with @ShouldContinue" }
    }.firstOrNull()?.also {
        checkDesign { ShouldContinue.validate(it) }
    }

    val customCompares = solution.declaredMethods.filter {
        it.isCompare()
    }.filterNotNull().let { compareMethods ->
        compareMethods.forEach { Compare.validate(it) }
        checkDesign {
            require(compareMethods.distinctBy { it.returnType }.size == compareMethods.size) {
                "Found two or more @Compare methods examining the same type"
            }
        }
        compareMethods.associateBy { it.parameterTypes.first().boxType() }
    }

    val initializer: Executable? = solution.superclass.declaredMethods.filter {
        it.isInitializer()
    }.also {
        checkDesign(it.size <= 1) { "Solution parent class ${solution.superclass.name} has multiple initializers" }
    }.firstOrNull()?.also {
        checkDesign { Initializer.validate(it) }
    }
    private val initializers = initializer?.let { setOf(it) } ?: setOf()
    val receiversAndInitializers = receiverGenerators + initializers

    val fauxStatic = solution.superclass == Any::class.java &&
        solution.declaredFields.all { it.isJenisol() || it.isStatic() } &&
        solution.declaredMethods.all {
            it.isJenisol() ||
                (it.returnType != solution && !it.receiverParameter() && !it.objectParameter())
        } &&
        solution.declaredConstructors.let { it.size == 1 && it.first().parameterCount == 0 }

    val generatorFactory: GeneratorFactory = GeneratorFactory(allExecutables + initializers, this)

    private val receiverEntropy: Int
    private val methodEntropy: Int

    private val defaultReceiverCount: Int
    private val defaultMethodCount: Int

    // These calculations should be improved to create a better test balance
    init {
        val emptyInitializers =
            (receiverGenerators.size == 1 && receiverGenerators.first().parameters.isEmpty()) &&
                (initializer?.parameters?.isEmpty() ?: true)

        val minReceiverCount = if (fauxStatic) {
            1
        } else {
            receiverGenerators.sumOf {
                generatorFactory.get(Random, Settings.DEFAULTS)[it]!!.fixed.size
            } * 2
        }
        val minMethodCount = (allExecutables - receiverGenerators).sumOf {
            if (it.receiverParameter() || (receiverGenerators.isNotEmpty() && it.objectParameter())) {
                minReceiverCount
            } else {
                generatorFactory.get(Random, Settings.DEFAULTS)[it]!!.fixed.size
            }
        } * 2

        @Suppress("MagicNumber")
        receiverEntropy = when {
            skipReceiver || fauxStatic -> 0
            emptyInitializers -> 1
            else -> 3
        }
        @Suppress("MagicNumber")
        methodEntropy = when {
            methodsToTest.size == 1 && methodsToTest.first().parameters.isEmpty() -> 4
            else -> 7
        }
        defaultReceiverCount = maxOf(minReceiverCount, 2.0.pow(receiverEntropy.toDouble()).toInt())
        defaultMethodCount = maxOf(minMethodCount, 2.0.pow(methodEntropy.toDouble()).toInt())
    }

    @Suppress("unused")
    val defaultTotalTests = defaultReceiverCount * (defaultMethodCount + 1)

    fun setCounts(settings: Settings): Settings {
        val receiverCount = if (settings.receiverCount != -1) {
            settings.receiverCount
        } else {
            defaultReceiverCount
        }
        val testCount = if (settings.methodCount != -1) {
            settings.methodCount
        } else {
            defaultMethodCount
        }
        return settings.copy(
            receiverCount = receiverCount,
            methodCount = testCount
        ).also {
            check(it.receiverCount >= 0) { "Invalid receiver count" }
            check(it.methodCount > 0) { "Invalid test count" }
        }
    }

    val verifiers = solution.declaredMethods.filter { it.isVerify() }.associateBy { verifier ->
        val matchingMethod = methodsToTest.filter { methodToTest ->
            val returnType = when (methodToTest) {
                is Constructor<*> -> solution
                is Method -> methodToTest.genericReturnType
                else -> designError("Unexpected executable type")
            }
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            try {
                Verify.validate(verifier, returnType, methodToTest.genericParameterTypes)
                true
            } catch (e: Exception) {
                false
            }
        }
        checkDesign(matchingMethod.isNotEmpty()) { "@Verify method $verifier matched no solution methods" }
        checkDesign(matchingMethod.size == 1) { "@Verify method $verifier matched multiple solution methods" }
        matchingMethod[0]
    }

    val filters: Map<Executable, Method> = solution.declaredMethods.filter { it.isFilterParameters() }
        .mapNotNull { filter ->
            FilterParameters.validate(filter).let { filterTypes ->
                (methodsToTest + receiverGenerators).filter {
                    it.genericParameterTypes.contentEquals(filterTypes)
                }.also {
                    check(it.size <= 1) { "Filter matched multiple methods: ${it.size}" }
                }.firstOrNull()
            }?.let {
                it to filter
            }
        }.toMap()

    val receiverCompare = object : Comparator {
        override val descendants = true
        override fun compare(
            solution: Any,
            submission: Any,
            solutionClass: Class<*>?,
            submissionClass: Class<*>?
        ): Boolean =
            true
    }

    fun submission(submission: Class<*>) = Submission(this, submission)
}

fun solution(klass: Class<*>) = Solution(klass)

fun Executable.isStatic() = Modifier.isStatic(modifiers)
fun Executable.isPrivate() = Modifier.isPrivate(modifiers)
fun Executable.isPublic() = Modifier.isPublic(modifiers)
fun Executable.isProtected() = Modifier.isProtected(modifiers)
fun Executable.isPackagePrivate() = !isPublic() && !isPrivate() && !isProtected()

fun Class<*>.isPrivate() = Modifier.isPrivate(modifiers)
fun Class<*>.isPublic() = Modifier.isPublic(modifiers)
fun Class<*>.isProtected() = Modifier.isProtected(modifiers)

@Suppress("unused")
fun Class<*>.isFinal() = Modifier.isFinal(modifiers)
fun Class<*>.isPackagePrivate() = !isPublic() && !isPrivate() && !isProtected()

fun Class<*>.prettyPrint(): String = if (isArray) {
    getArrayType().name + "[]".repeat(getArrayDimension())
} else {
    name
}

fun Executable.isKotlinCompanionAccessor(): Boolean {
    check(declaringClass.isKotlin()) { "Should only check Kotlin classes: ${declaringClass.name}" }
    return name.startsWith("access${"$"}get")
}

fun Executable.isDataClassGenerated() = name == "equals" ||
    name == "hashCode" ||
    name == "toString" ||
    name == "copy" ||
    name == "copy${"$"}default" ||
    name.startsWith("component")

@Suppress("SwallowedException")
fun Class<*>.hasKotlinCompanion() = try {
    isKotlin() && kotlin.companionObject != null
} catch (e: UnsupportedOperationException) {
    false
}

@Suppress("SwallowedException")
fun Executable.isKotlinCompanion() = try {
    declaringClass.isKotlin() && declaringClass.kotlin.isCompanion
} catch (e: UnsupportedOperationException) {
    false
}

@Suppress("ComplexMethod", "NestedBlockDepth")
fun String.toKotlinType() = when {
    this == "byte" -> "Byte"
    this == "short" -> "Short"
    this == "int" -> "Int"
    this == "long" -> "Long"
    this == "float" -> "Float"
    this == "double" -> "Double"
    this == "char" -> "Char"
    this == "boolean" -> "Boolean"
    this.endsWith("[]") -> {
        var currentType = this
        var arrayCount = -1
        while (currentType.endsWith("[]")) {
            currentType = currentType.removeSuffix("[]")
            arrayCount++
        }
        var name = when (currentType) {
            "byte" -> "ByteArray"
            "short" -> "ShortArray"
            "int" -> "IntArray"
            "long" -> "LongArray"
            "float" -> "FloatArray"
            "double" -> "DoubleArray"
            "char" -> "CharArray"
            "boolean" -> "BooleanArray"
            else -> "Array<$currentType>"
        }
        repeat(arrayCount) {
            name = "Array<$name>"
        }
        name
    }
    else -> this
}

fun Executable.fullName(isKotlin: Boolean = false): String {
    val visibilityModifier = getVisibilityModifier(isKotlin)?.plus(" ")
    val returnType = when (this) {
        is Constructor<*> -> ""
        is Method -> genericReturnType.typeName
        else -> error("Unknown executable type")
    }.let { type ->
        if (isKotlin) {
            type.toKotlinType()
        } else {
            type
        }
    }.let {
        if (it.isNotBlank()) {
            if (isKotlin) {
                it
            } else {
                "$it "
            }
        } else {
            it
        }
    }
    return if (!isKotlin) {
        "${visibilityModifier ?: ""}${
        if (isStatic()) {
            "static "
        } else {
            ""
        }
        }$returnType$name(${parameters.joinToString(", ") { it.type.prettyPrint() }})"
    } else {
        "${visibilityModifier ?: ""}fun $name(${
        parameters.joinToString(", ") {
            it.type.prettyPrint().toKotlinType()
        }
        }): $returnType"
    }
}

fun Field.fullName(): String {
    val visibilityModifier = getVisibilityModifier()?.plus(" ")
    return "${visibilityModifier ?: ""}$type $name"
}

fun Class<*>.visibilityMatches(klass: Class<*>) = when {
    isPublic() -> klass.isPublic()
    isPrivate() -> klass.isPrivate()
    isProtected() -> klass.isProtected()
    else -> klass.isPackagePrivate()
}

fun Executable.visibilityMatches(executable: Executable, submission: Class<*>) = when {
    isPublic() && submission.isKotlin() && executable.isPackagePrivate() -> true
    isPublic() -> executable.isPublic()
    isPrivate() -> executable.isPrivate()
    isProtected() -> executable.isProtected()
    else -> executable.isPackagePrivate()
}

fun Field.visibilityMatches(solutionField: Field) = when {
    isPublic() -> solutionField.isPublic()
    isPrivate() -> solutionField.isPrivate()
    isProtected() -> solutionField.isProtected()
    else -> solutionField.isPackagePrivate()
}

fun Class<*>.getVisibilityModifier() = when {
    isPublic() -> "public"
    isPrivate() -> "private"
    isProtected() -> "protected"
    else -> null
}

fun Executable.getVisibilityModifier(isKotlin: Boolean = false) = when {
    !isKotlin && isPublic() -> "public"
    isPrivate() -> "private"
    isProtected() -> "protected"
    else -> null
}

fun Field.getVisibilityModifier() = when {
    isPublic() -> "public"
    isPrivate() -> "private"
    isProtected() -> "protected"
    else -> null
}

fun Class<*>.findMethod(method: Method, solution: Class<*>) = this.declaredMethods.find {
    it != null &&
        it.visibilityMatches(method, this) &&
        it.name == method.name &&
        compareParameters(method.genericParameterTypes, solution, it.genericParameterTypes, this) &&
        compareReturn(method.genericReturnType, solution, it.genericReturnType, this) &&
        it.isStatic() == method.isStatic()
} ?: if (hasKotlinCompanion()) {
    this.kotlin.companionObject?.java?.declaredMethods?.find {
        it != null &&
            it.visibilityMatches(method, this) &&
            it.name == method.name &&
            compareParameters(method.genericParameterTypes, solution, it.genericParameterTypes, this) &&
            compareReturn(method.genericReturnType, solution, it.genericReturnType, this)
    }
} else {
    null
}

fun compareReturn(
    solutionReturn: Type,
    solution: Class<*>,
    submissionReturn: Type,
    submission: Class<*>
) = when {
    solutionReturn == submissionReturn -> true
    solutionReturn == solution && submissionReturn == submission -> true
    solutionReturn is Class<*> && submissionReturn is Class<*> && solutionReturn.isArray &&
        solutionReturn.getArrayType() == solution &&
        submissionReturn.isArray &&
        submissionReturn.getArrayType() == submission &&
        solutionReturn.getArrayDimension() == submissionReturn.getArrayDimension() -> true
    solutionReturn is TypeVariable<*> && submissionReturn is TypeVariable<*> ->
        solutionReturn.bounds.contentEquals(submissionReturn.bounds)
    else -> false
}

@Suppress("ComplexMethod")
fun compareParameters(
    solutionParameters: Array<Type>,
    solution: Class<*>,
    submissionParameters: Array<Type>,
    submission: Class<*>
): Boolean {
    if (solutionParameters.size != submissionParameters.size) {
        return false
    }
    return solutionParameters
        .zip(submissionParameters.fixReceivers(submission, solution))
        .all { (solutionType, submissionType) ->
            when {
                solutionType == submissionType -> true
                solutionType !is ParameterizedType && submissionType is ParameterizedType &&
                    submissionType.rawType == solutionType -> {
                    submissionType.actualTypeArguments.all { it is Any }
                }
                submission.isKotlin() && solutionType is ParameterizedType && submissionType is ParameterizedType &&
                    solutionType.rawType == submissionType.rawType -> {
                    var matches = true
                    @Suppress("LoopWithTooManyJumpStatements")
                    for ((index, solutionTypeArgument) in solutionType.actualTypeArguments.withIndex()) {
                        val submissionTypeArgument = submissionType.actualTypeArguments[index]
                        if (solutionTypeArgument.typeName == "java.lang.Object" &&
                            submissionTypeArgument.typeName === "?"
                        ) {
                            continue
                        }
                        if (solutionTypeArgument != submissionTypeArgument) {
                            matches = false
                            break
                        }
                    }
                    matches
                }
                else -> false
            }
        }
}

fun Class<*>.findConstructor(constructor: Constructor<*>, solution: Class<*>) = this.declaredConstructors.find {
    it.isPublic() &&
        it?.parameterTypes?.fixReceivers(this, solution)?.contentEquals(constructor.parameterTypes) ?: false
}

fun Array<Type>.fixReceivers(from: Type, to: Type) = map {
    when (it) {
        from -> to
        else -> it
    }
}.toTypedArray()

fun Array<Class<*>>.fixReceivers(from: Class<*>, to: Class<*>) = map {
    when (it) {
        from -> to
        else -> it
    }
}.toTypedArray()

fun Class<*>.findField(solutionField: Field) = this.declaredFields.find { submissionField ->
    submissionField != null &&
        submissionField.visibilityMatches(solutionField) &&
        submissionField.name == solutionField.name &&
        submissionField.type == solutionField.type &&
        submissionField.isStatic() == solutionField.isStatic()
}

typealias CaptureOutput = (run: () -> Any?) -> CapturedResult

data class CapturedResult(val returned: Any?, val threw: Throwable?, val stdout: String, val stderr: String)

private val outputLock = ReentrantLock()
fun defaultCaptureOutput(run: () -> Any?): CapturedResult = outputLock.withLock {
    val original = Pair(System.out, System.err)
    val diverted = Pair(ByteArrayOutputStream(), ByteArrayOutputStream()).also {
        System.setOut(PrintStream(it.first))
        System.setErr(PrintStream(it.second))
    }

    @Suppress("TooGenericExceptionCaught")
    val result: Pair<Any?, Throwable?> = try {
        Pair(run(), null)
    } catch (e: Throwable) {
        if (e is ThreadDeath) {
            throw e
        }
        Pair(null, e)
    }
    System.setOut(original.first)
    System.setErr(original.second)
    return CapturedResult(result.first, result.second, diverted.first.toString(), diverted.second.toString())
}

class SolutionDesignError(message: String?) : Exception(message)

private fun checkDesign(check: Boolean, message: () -> Any) {
    if (!check) {
        designError(message().toString())
    }
}

private fun <T> checkDesign(method: () -> T): T {
    @Suppress("TooGenericExceptionCaught")
    return try {
        method()
    } catch (e: Exception) {
        designError(e.message)
    }
}

private fun designError(message: String?): Nothing = throw SolutionDesignError(message)

data class Settings(
    val shrink: Boolean? = null,
    val methodCount: Int = -1,
    val receiverCount: Int = -1,
    val receiverRetries: Int = -1,
    val seed: Int = -1,
    val simpleCount: Int = -1,
    val edgeCount: Int = -1,
    val mixedCount: Int = -1,
    val fixedCount: Int = -1,
    val overrideTotalCount: Int = -1,
    val minTestCount: Int = -1,
    val startMultipleCount: Int = -1
) {
    companion object {
        val DEFAULTS = Settings(
            shrink = true,
            receiverRetries = 8,
            simpleCount = Int.MAX_VALUE,
            edgeCount = Int.MAX_VALUE,
            mixedCount = Int.MAX_VALUE,
            fixedCount = Int.MAX_VALUE
        )
    }

    infix fun merge(other: Settings): Settings {
        val nameToProperty = Settings::class.declaredMemberProperties.associateBy { it.name }
        val primaryConstructor = Settings::class.primaryConstructor!!
        val args = primaryConstructor.parameters.associateWith { parameter ->
            val property = nameToProperty[parameter.name]!!
            val value = if (property.get(other) != null && property.get(other) != -1) {
                property.get(other)
            } else {
                property.get(this)
            }
            value
        }
        return primaryConstructor.callBy(args)
    }
}