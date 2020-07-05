@file:Suppress("TooManyFunctions", "MemberVisibilityCanBePrivate")

package edu.illinois.cs.cs125.jenisol.core

import com.rits.cloning.Cloner
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.Random

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SimpleType {
    companion object {
        fun validate(field: Field): Class<*> = validateTypeField(field, SimpleType::class.java.simpleName)
    }
}

fun Field.isSimpleType() = isAnnotationPresent(SimpleType::class.java)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class EdgeType {
    companion object {
        fun validate(field: Field): Class<*> = validateTypeField(field, EdgeType::class.java.simpleName)
    }
}

fun Field.isEdgeType() = isAnnotationPresent(EdgeType::class.java)

private val typeFieldAnnotations = setOf(SimpleType::class.java, EdgeType::class.java)
private fun validateTypeField(field: Field, name: String): Class<*> {
    check(field.isStatic()) { "@$name fields must be static" }
    check(field.isFinal()) { "@$name fields must be final" }
    check(field.type.isArray) { "@$name fields must annotate arrays" }
    field.isAccessible = true
    return field.type.componentType
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RandomType {
    companion object {
        val name: String = RandomType::class.java.simpleName
        fun validateAsType(method: Method): Class<*> {
            check(method.isStatic()) { "@$name methods must be static" }
            check(
                method.parameterTypes.size == 2 &&
                    method.parameterTypes[0] == Int::class.java &&
                    method.parameterTypes[1] == Random::class.java
            ) {
                "@$name methods must accept parameters (int complexity, java.util.Random random)"
            }
            method.isAccessible = true
            return method.returnType
        }
    }
}

fun Method.isRandomType() = isAnnotationPresent(RandomType::class.java)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class FixedParameters {
    companion object {
        val name: String = FixedParameters::class.java.simpleName
        fun validate(field: Field): Array<Type> {
            check(field.isStatic()) { "@$name fields must be static" }
            check(field.genericType is ParameterizedType) {
                "@$name parameter fields must annotate parameterized collections"
            }
            (field.genericType as ParameterizedType).also { collectionType ->
                check(collectionType.actualTypeArguments.size == 1) {
                    "@$name parameter fields must annotate parameterized collections"
                }
                collectionType.actualTypeArguments.first().also { itemType ->
                    check(itemType is ParameterizedType && itemType.rawType in parameterGroupTypes) {
                        "@$name parameter fields must annotate collections of types " +
                            parameterGroupTypes.joinToString(", ") { it.simpleName }
                    }
                    field.isAccessible = true
                    return itemType.actualTypeArguments
                }
            }
        }
    }
}

fun Field.isFixedParameters() = isAnnotationPresent(FixedParameters::class.java)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RandomParameters {
    companion object {
        val name: String = RandomParameters::class.java.simpleName
        fun validate(method: Method): Array<Type> {
            check(method.isStatic()) { "@$name methods must be static" }
            check(
                method.parameterTypes.size == 2 &&
                    method.parameterTypes[0] == Int::class.java &&
                    method.parameterTypes[1] == Random::class.java
            ) {
                "@$name methods must accept parameters (int complexity, java.util.Random random)"
            }
            check(method.returnType in parameterGroupTypes) {
                "@$name parameter methods must return one of types " +
                    parameterGroupTypes.joinToString(", ") { it.simpleName }
            }
            return (method.genericReturnType as ParameterizedType).actualTypeArguments
        }
    }
}

fun Method.isRandomParameters() = isAnnotationPresent(RandomParameters::class.java)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Initializer {
    companion object {
        val name: String = Initializer::class.java.simpleName
        fun validate(method: Method) {
            check(!method.isStatic()) { "@$name methods must not be static" }
            check(method.returnType.name == "void") {
                "@$name method return values will not be used and should be void"
            }
            method.isAccessible = true
        }
    }
}

fun Method.isInitializer() = isAnnotationPresent(Initializer::class.java)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Verify {
    companion object {
        val name: String = Verify::class.java.simpleName
        fun validate(method: Method, returnType: Type) {
            check(method.isStatic()) { "@$name methods must be static" }
            check(method.returnType.name == "void") {
                "@$name method return values will not be used and should be void"
            }
            check(
                method.parameterTypes.size == 1 &&
                    method.parameterTypes[0] == TestResult::class.java &&
                    method.genericParameterTypes[0] is ParameterizedType &&
                    (method.genericParameterTypes[0] as ParameterizedType).actualTypeArguments.size == 1 &&
                    (method.genericParameterTypes[0] as ParameterizedType).actualTypeArguments[0] == returnType
            ) {
                "@$name methods must accept parameters " +
                    "(${TestResult::class.java.simpleName}<${returnType.typeName}> results)"
            }
            method.isAccessible = true
        }
    }
}

fun Method.isVerify() = isAnnotationPresent(Verify::class.java)

fun Field.isStatic() = Modifier.isStatic(modifiers)
fun Field.isFinal() = Modifier.isFinal(modifiers)

fun Any.asArray(): Array<*> {
    return when (this) {
        is ByteArray -> this.toTypedArray()
        is ShortArray -> this.toTypedArray()
        is IntArray -> this.toTypedArray()
        is LongArray -> this.toTypedArray()
        is FloatArray -> this.toTypedArray()
        is DoubleArray -> this.toTypedArray()
        is CharArray -> this.toTypedArray()
        is BooleanArray -> this.toTypedArray()
        else -> this as Array<*>
    }
}

fun Executable.isAnswerable() = setOf(
    RandomType::class.java, RandomParameters::class.java, Initializer::class.java, Verify::class.java
).any {
    isAnnotationPresent(it)
}

fun Field.isAnswerable() = (typeFieldAnnotations + FixedParameters::class.java).any {
    isAnnotationPresent(it)
}

private val parameterGroupTypes = setOf(One::class.java, Two::class.java, Three::class.java, Four::class.java)

interface ParameterGroup {
    fun toArray(): Array<Any?>
}

fun ParameterGroup.deepCopy(): ParameterGroup = Cloner().deepClone(this)

data class One<I>(val first: I) : ParameterGroup {
    override fun toArray() = arrayOf<Any?>(first)
}

data class Two<I, J>(val first: I, val second: J) : ParameterGroup {
    override fun toArray() = arrayOf(first, second)
}

data class Three<I, J, K>(val first: I, val second: J, val third: K) : ParameterGroup {
    override fun toArray() = arrayOf(first, second, third)
}

data class Four<I, J, K, L>(val first: I, val second: J, val third: K, val fourth: L) : ParameterGroup {
    override fun toArray() = arrayOf(first, second, third, fourth)
}
