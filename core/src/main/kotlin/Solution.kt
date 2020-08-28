@file:Suppress("TooManyFunctions")

package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.GeneratorFactory
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.pow
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

class Solution(val solution: Class<*>) {
    init {
        solution.declaredFields.filter { it.isStatic() && !it.isJenisol() }.also {
            checkDesign(it.isEmpty()) { "No support for testing classes with static fields yet" }
        }
    }

    val allExecutables =
        (solution.declaredMethods.toSet() + solution.declaredConstructors.toSet())
            .filterNotNull()
            .filter {
                !it.isPrivate() && !it.isJenisol()
            }.toSet().also {
                checkDesign(it.isNotEmpty()) { "Found no methods to test" }
            }

    init {
        allExecutables.forEach { it.isAccessible = true }
    }

    val bothExecutables = solution.declaredMethods.toSet().filterNotNull().filter {
        it.isBoth()
    }.also { methods ->
        methods.forEach { checkDesign { Both.validate(it, solution) } }
    }.toSet()

    private fun Executable.receiverParameter() = parameterTypes.any { it == solution }

    val receiverGenerators = allExecutables.filter { executable ->
        !executable.receiverParameter()
    }.filter { executable ->
        when (executable) {
            is Constructor<*> -> true
            is Method -> executable.isStatic() && executable.returnType == solution
            else -> designError("Unexpected executable type")
        }
    }.toSet()
    val methodsToTest = (allExecutables - receiverGenerators + bothExecutables).also {
        checkDesign(it.isNotEmpty()) { "Found methods that generate receivers but no ways to test them" }
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
            checkDesign(receiverTransformers.isNotEmpty()) { "No way to verify generated receivers" }
        }
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

    val generatorFactory: GeneratorFactory = GeneratorFactory(allExecutables + initializers, this)

    private val receiverEntropy: Int
    private val methodEntropy: Int

    // These calculations should be improved to create a better test balance
    init {
        val emptyInitializers =
            (receiverGenerators.size == 1 && receiverGenerators.first().parameters.isEmpty()) &&
                (initializer?.parameters?.isEmpty() ?: true)

        @Suppress("MagicNumber")
        receiverEntropy = when {
            skipReceiver -> 0
            emptyInitializers -> 2
            else -> 5
        }
        @Suppress("MagicNumber")
        methodEntropy = when {
            methodsToTest.size == 1 && methodsToTest.first().parameters.isEmpty() -> 0
            else -> 5
        }
    }

    private val defaultReceiverCount = 2.0.pow(receiverEntropy.toDouble()).toInt()
    private val defaultMethodCount = 2.0.pow(methodEntropy.toDouble()).toInt()

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
            receiverCount = receiverCount, methodCount = testCount
        ).also {
            check(it.receiverCount >= 0) { "Invalid receiver count" }
            check(it.methodCount > 0) { "Invalid test count" }
        }
    }

    val verifier: Method? = solution.declaredMethods.filter { it.isVerify() }.also {
        checkDesign(it.size <= 1) { "No support yet for multiple verifiers" }
    }.firstOrNull()?.also {
        checkDesign(methodsToTest.size == 1) { "No support yet for multiple verifiers" }
        val methodToTest = methodsToTest.first()
        val returnType = when (methodToTest) {
            is Constructor<*> -> solution
            is Method -> methodToTest.genericReturnType
            else -> designError("Unexpected executable type")
        }
        Verify.validate(it, returnType, methodToTest.parameterTypes)
    }

    val receiverCompare = object : Comparator {
        override val descendants = true
        override fun compare(solution: Any, submission: Any): Boolean = true
    }

    fun submission(submission: Class<*>) = Submission(this, submission)
}

fun Set<Executable>.cycle() = sequence {
    yield(shuffled().first())
}

fun solution(klass: Class<*>) = Solution(klass)

fun Executable.isStatic() = Modifier.isStatic(modifiers)
fun Executable.isPrivate() = Modifier.isPrivate(modifiers)
fun Executable.isPublic() = Modifier.isPublic(modifiers)
fun Executable.isProtected() = Modifier.isProtected(modifiers)
fun Executable.isPackagePrivate() = !isPublic() && !isPrivate() && !isProtected()

fun Executable.fullName(): String {
    val visibilityModifier = getVisibilityModifier()?.plus(" ")
    return "${visibilityModifier ?: ""}$name(${parameters.joinToString(", ") { it.type.name }})"
}

fun Executable.visibilityMatches(executable: Executable) = when {
    isPublic() -> executable.isPublic()
    isPrivate() -> executable.isPrivate()
    isProtected() -> executable.isProtected()
    else -> executable.isPackagePrivate()
}

fun Executable.getVisibilityModifier() = when {
    isPublic() -> "public"
    isPrivate() -> "private"
    isProtected() -> "protected"
    else -> null
}

fun Class<*>.findMethod(method: Method, solution: Class<*>) = this.declaredMethods.find {
    it.visibilityMatches(method) &&
        it != null &&
        it.name == method.name &&
        it.parameterTypes.fixReceivers(this, solution).contentEquals(method.parameterTypes) &&
        (it.returnType == method.returnType || (it.returnType == this && method.returnType == solution)) &&
        it.isStatic() == method.isStatic()
}

fun Class<*>.findConstructor(constructor: Constructor<*>, solution: Class<*>) = this.declaredConstructors.find {
    it.isPublic() &&
        it?.parameterTypes?.fixReceivers(this, solution)?.contentEquals(constructor.parameterTypes) ?: false
}

fun Array<Class<*>>.fixReceivers(from: Class<*>, to: Class<*>) = map {
    when (it) {
        from -> to
        else -> it
    }
}.toTypedArray()

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
    val fixedCount: Int = -1
) {
    companion object {
        val DEFAULTS = Settings(
            shrink = true,
            receiverRetries = 4,
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

