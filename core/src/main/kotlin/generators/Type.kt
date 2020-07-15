@file:Suppress("MagicNumber", "TooManyFunctions")

package edu.illinois.cs.cs125.jenisol.core.generators

import com.rits.cloning.Cloner
import edu.illinois.cs.cs125.jenisol.core.RandomGroup
import edu.illinois.cs.cs125.jenisol.core.RandomType
import java.lang.reflect.Array
import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.math.pow
import kotlin.random.Random

interface TypeGenerator<T> {
    val simple: Set<Value<T>>
    val edge: Set<Value<T?>>
    fun random(complexity: Complexity): Value<T>

    class Complexity(level: Int = MIN) {
        init {
            require(level in 0..MAX) { "Invalid complexity value: $level" }
        }

        @Suppress("MemberVisibilityCanBePrivate")
        var level = level
            set(value) {
                require(value in MIN..MAX) { "Invalid complexity value: $value" }
                field = value
            }

        fun next(): Complexity {
            if (level < MAX) {
                level++
            }
            return this
        }

        fun prev(): Complexity {
            if (level > MIN) {
                level--
            }
            return this
        }

        fun power(base: Int = 2) = base.toDouble().pow(level.toDouble()).toLong()

        companion object {
            const val MIN = 1
            const val MAX = 8
            val ALL = (MIN..MAX).map { Complexity(it) }
        }
    }

    data class Value<T>(val solution: T, val submission: T, val reference: T)
}

@Suppress("UNCHECKED_CAST")
class OverrideTypeGenerator(
    private val klass: Class<*>,
    simple: Set<Any>? = null,
    edge: Set<Any?>? = null,
    private val rand: Method? = null,
    random: Random = Random,
    defaultGenerator: TypeGeneratorGenerator? = null
) : TypeGenerator<Any> {
    private val name: String = klass.name
    private val default = if (simple == null || edge == null || rand == null) {
        check(defaultGenerator != null) { "Override type generator for $name needs default generator" }
        defaultGenerator(random)
    } else {
        null
    }
    private val simpleOverride: Set<TypeGenerator.Value<Any>>? = simple?.values()
    private val edgeOverride: Set<TypeGenerator.Value<Any?>>? = edge?.values()
    private val randomGroup: RandomGroup = RandomGroup(random.nextLong())

    override val simple: Set<TypeGenerator.Value<Any>> =
        simpleOverride ?: default?.simple as Set<TypeGenerator.Value<Any>>
            ?: error("Couldn't find simple generator for $name")

    override val edge: Set<TypeGenerator.Value<Any?>> =
        edgeOverride ?: default?.edge as Set<TypeGenerator.Value<Any?>>
            ?: error("Couldn't find edge generator for $name")

    override fun random(complexity: TypeGenerator.Complexity): TypeGenerator.Value<Any> {
        if (rand == null) {
            check(default != null) { "Couldn't find rand generator for $name" }
            return default.random(complexity) as TypeGenerator.Value<Any>
        }
        check(randomGroup.synced) {
            "grouped random number generator out of sync before call to @${RandomType.name} method for ${klass.name}"
        }
        val solution = rand.invoke(null, complexity.level, randomGroup.solution)
        val submission = rand.invoke(null, complexity.level, randomGroup.submission)
        val reference = rand.invoke(null, complexity.level, randomGroup.reference)
        check(randomGroup.synced) {
            "grouped random number generator out of sync after call to @${RandomType.name} method for ${klass.name}"
        }
        check(setOf(solution, submission, reference).size == 1) {
            "@${RandomType.name} method for ${klass.name} did not return equal values"
        }
        return TypeGenerator.Value(solution, submission, reference)
    }
}

sealed class TypeGenerators<T>(internal val random: Random) : TypeGenerator<T>
typealias TypeGeneratorGenerator = (random: Random) -> TypeGenerator<*>

object Defaults {
    private val map = mutableMapOf<Class<*>, TypeGeneratorGenerator>()

    init {
        map[Byte::class.java] = ByteGenerator.Companion::create
        map[java.lang.Byte::class.java] = BoxedGenerator.create(Byte::class.java)
        map[Short::class.java] = ShortGenerator.Companion::create
        map[java.lang.Short::class.java] = BoxedGenerator.create(Short::class.java)
        map[Int::class.java] = IntGenerator.Companion::create
        map[java.lang.Integer::class.java] = BoxedGenerator.create(Int::class.java)
        map[Long::class.java] = LongGenerator.Companion::create
        map[java.lang.Long::class.java] = BoxedGenerator.create(Long::class.java)
        map[Float::class.java] = FloatGenerator.Companion::create
        map[java.lang.Float::class.java] = BoxedGenerator.create(Float::class.java)
        map[Double::class.java] = DoubleGenerator.Companion::create
        map[java.lang.Double::class.java] = BoxedGenerator.create(Double::class.java)
        map[Boolean::class.java] = BooleanGenerator.Companion::create
        map[java.lang.Boolean::class.java] = BoxedGenerator.create(Boolean::class.java)
        map[String::class.java] = StringGenerator.Companion::create
    }

    operator fun get(klass: Class<*>): TypeGeneratorGenerator {
        map[klass]?.also { return it }
        if (klass.isArray && map.containsKey(klass.getArrayType())) {
            return { random ->
                ArrayGenerator(
                    random,
                    klass.componentType
                )
            }
        }
        error("Cannot find generator for class ${klass.name}")
    }

    fun create(klass: Class<*>, random: Random = Random): TypeGenerator<*> = get(klass)(random)
}

@Suppress("UNCHECKED_CAST")
class ArrayGenerator(random: Random, private val klass: Class<*>) : TypeGenerators<Any>(random) {
    private val componentGenerator = Defaults.create(klass, random)

    override val simple: Set<TypeGenerator.Value<Any>>
        get() {
            val simpleCases = componentGenerator.simple.map { it.reference }
            return setOf(
                Array.newInstance(klass, 0),
                Array.newInstance(klass, simpleCases.size).also { array ->
                    simpleCases.forEachIndexed { index, value ->
                        Array.set(array, index, value)
                    }
                }
            ).values()
        }
    override val edge = setOf<Any?>(null).values()

    override fun random(complexity: TypeGenerator.Complexity): TypeGenerator.Value<Any> {
        return random(complexity, complexity, true)
    }

    fun random(
        complexity: TypeGenerator.Complexity,
        componentComplexity: TypeGenerator.Complexity,
        top: Boolean
    ): TypeGenerator.Value<Any> {
        val (currentComplexity, nextComplexity) = if (klass.isArray) {
            complexity.level.let { level ->
                val currentLevel = if (level == 0) {
                    0
                } else {
                    random.nextInt(level)
                }
                Pair(
                    TypeGenerator.Complexity(currentLevel),
                    TypeGenerator.Complexity(level - currentLevel)
                )
            }
        } else {
            Pair(complexity, null)
        }
        val arraySize = random.nextInt((currentComplexity.power().toInt() * 2) + 1).let {
            if (top && it == 0) {
                1
            } else {
                it
            }
        }
        return (
            Array.newInstance(klass, arraySize).also { array ->
                (0 until arraySize).forEach { index ->
                    val value = if (componentGenerator is ArrayGenerator) {
                        check(nextComplexity != null) { "Invalid complexity split" }
                        componentGenerator.random(nextComplexity, componentComplexity, false)
                    } else {
                        componentGenerator.random(componentComplexity)
                    }.reference
                    Array.set(array, index, value)
                }
            }
            ).value()
    }
}

@Suppress("UNCHECKED_CAST")
class BoxedGenerator(random: Random, klass: Class<*>) : TypeGenerators<Any>(random) {
    private val primitiveGenerator = Defaults.create(klass, random)
    override val simple = primitiveGenerator.simple as Set<TypeGenerator.Value<Any>>
    override val edge = (primitiveGenerator.edge + setOf(TypeGenerator.Value(null, null, null)))
        as Set<TypeGenerator.Value<Any?>>

    override fun random(complexity: TypeGenerator.Complexity) =
        primitiveGenerator.random(complexity) as TypeGenerator.Value<Any>

    companion object {
        fun create(klass: Class<*>) = { random: Random -> BoxedGenerator(random, klass) }
    }
}

private fun randomNumber(max: Number, range: LongRange, random: Random) =
    (random.nextLong(max.toLong()) - (max.toLong() / 2)).also {
        check(it in range) { "Random number generated out of range" }
    }

class ByteGenerator(random: Random) : TypeGenerators<Byte>(random) {

    override val simple = byteArrayOf(-1, 0, 1).toSet().values()

    @Suppress("UNCHECKED_CAST")
    override val edge =
        byteArrayOf(Byte.MIN_VALUE, Byte.MAX_VALUE).toSet().values() as Set<TypeGenerator.Value<Byte?>>

    override fun random(complexity: TypeGenerator.Complexity) = random(complexity, random).value()

    companion object {
        fun random(complexity: TypeGenerator.Complexity, random: Random = Random) =
            randomNumber(complexity.power(), Byte.MIN_VALUE.toLong()..Byte.MAX_VALUE.toLong(), random)
                .toByte()

        fun create(random: Random = Random) = ByteGenerator(random)
    }
}

class ShortGenerator(random: Random) : TypeGenerators<Short>(random) {

    override val simple = shortArrayOf(-1, 0, 1).toSet().values()

    @Suppress("UNCHECKED_CAST")
    override val edge =
        shortArrayOf(Short.MIN_VALUE, Short.MAX_VALUE).toSet().values() as Set<TypeGenerator.Value<Short?>>

    override fun random(complexity: TypeGenerator.Complexity) = random(complexity, random).value()

    companion object {
        fun random(complexity: TypeGenerator.Complexity, random: Random = Random) =
            randomNumber(complexity.power(4), Short.MIN_VALUE.toLong()..Short.MAX_VALUE.toLong(), random)
                .toShort()

        fun create(random: Random = Random) = ShortGenerator(random)
    }
}

class IntGenerator(random: Random) : TypeGenerators<Int>(random) {

    override val simple = (-1..1).toSet().values()
    override val edge = setOf<Int?>(Int.MIN_VALUE, Int.MAX_VALUE).values()
    override fun random(complexity: TypeGenerator.Complexity) = random(complexity, random).value()

    companion object {
        fun random(complexity: TypeGenerator.Complexity, random: Random = Random) =
            randomNumber(complexity.power(8), Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong(), random)
                .toInt()

        fun create(random: Random = Random) = IntGenerator(random)
    }
}

class LongGenerator(random: Random) : TypeGenerators<Long>(random) {

    override val simple = (-1L..1L).toSet().values()
    override val edge = setOf<Long?>(Long.MIN_VALUE, Long.MAX_VALUE).values()
    override fun random(complexity: TypeGenerator.Complexity) = random(complexity, random).value()

    companion object {
        fun random(complexity: TypeGenerator.Complexity, random: Random = Random) =
            randomNumber(complexity.power(16), Long.MIN_VALUE..Long.MAX_VALUE, random)

        fun create(random: Random = Random) = LongGenerator(random)
    }
}

class FloatGenerator(random: Random) : TypeGenerators<Float>(random) {

    override val simple = setOf(-0.1f, 0.0f, 0.1f).values()
    override val edge = setOf<Float?>(Float.MIN_VALUE, Float.MAX_VALUE).values()
    override fun random(complexity: TypeGenerator.Complexity) = random(complexity, random).value()

    companion object {
        fun random(complexity: TypeGenerator.Complexity, random: Random = Random) =
            IntGenerator.random(complexity, random) * random.nextFloat()

        fun create(random: Random = Random) = FloatGenerator(random)
    }
}

class DoubleGenerator(random: Random) : TypeGenerators<Double>(random) {

    override val simple = setOf(-0.1, 0.0, 0.1).values()
    override val edge = setOf<Double?>(Double.MIN_VALUE, Double.MAX_VALUE).values()
    override fun random(complexity: TypeGenerator.Complexity) = random(complexity, random).value()

    companion object {
        fun random(complexity: TypeGenerator.Complexity, random: Random = Random) =
            FloatGenerator.random(complexity, random) * random.nextDouble()

        fun create(random: Random = Random) = DoubleGenerator(random)
    }
}

class BooleanGenerator(random: Random) : TypeGenerators<Boolean>(random) {

    override val simple = setOf(true, false).values()
    override val edge = setOf<Boolean?>().values()
    override fun random(complexity: TypeGenerator.Complexity) = random.nextBoolean().value()

    companion object {
        fun create(random: Random = Random) = BooleanGenerator(random)
    }
}

class StringGenerator(random: Random) : TypeGenerators<String>(random) {

    override val simple = setOf("test", "test string").values()
    override val edge = listOf(null, "").values()
    override fun random(complexity: TypeGenerator.Complexity) = random(complexity, random).value()

    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        val ALPHANUMERIC_CHARS: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9') + ' '
        fun random(complexity: TypeGenerator.Complexity, random: Random = Random): String {
            return (1..complexity.power())
                .map { random.nextInt(ALPHANUMERIC_CHARS.size) }
                .map(ALPHANUMERIC_CHARS::get)
                .joinToString("")
        }

        fun create(random: Random = Random) = StringGenerator(random)
    }
}

fun <T> Collection<T>.values() = Cloner().let { cloner ->
    toSet().also {
        check(size == it.size) { "Collection of values was not distinct" }
    }.map {
        TypeGenerator.Value(cloner.deepClone(it), cloner.deepClone(it), cloner.deepClone(it))
    }.toSet()
}

fun <T> T.value() = Cloner().let { cloner ->
    TypeGenerator.Value(cloner.deepClone(this), cloner.deepClone(this), cloner.deepClone(this))
}

fun <T> Class<T>.getArrayType(start: Boolean = true): Class<*> {
    check(!start || isArray) { "Must be called on an array type" }
    return if (!isArray) {
        this
    } else {
        componentType.getArrayType(false)
    }
}

fun <T> Class<T>.getArrayDimension(start: Boolean = true): Int {
    check(!start || isArray) { "Must be called on an array type" }
    return if (!isArray) {
        0
    } else {
        1 + componentType.getArrayDimension(false)
    }
}

fun kotlin.Array<Type>.compareBoxed(other: kotlin.Array<Type>) = when {
    size != other.size -> false
    else -> zip(other).all { (mine, other) -> (mine as Class<*>).compareBoxed(other as Class<*>) }
}

fun kotlin.Array<Type>.compareBoxed(other: kotlin.Array<Class<*>>) = when {
    size != other.size -> false
    else -> zip(other).all { (mine, other) -> (mine as Class<*>).compareBoxed(other) }
}

fun <T> Class<T>.compareBoxed(other: Class<*>) = when {
    this == other -> true
    wrap() == other.wrap() -> true
    compareBoxedArrays(other) -> true
    else -> false
}

fun <T> Class<T>.compareBoxedArrays(other: Class<*>) = when {
    !isArray -> false
    !other.isArray -> false
    getArrayDimension() != other.getArrayDimension() -> false
    getArrayType().wrap() != other.getArrayType().wrap() -> false
    else -> true
}

fun <T> Class<T>.wrap(): Class<*> = when {
    this == Byte::class.java -> java.lang.Byte::class.java
    this == Short::class.java -> java.lang.Short::class.java
    this == Int::class.java -> java.lang.Integer::class.java
    this == Long::class.java -> java.lang.Long::class.java
    this == Float::class.java -> java.lang.Float::class.java
    this == Double::class.java -> java.lang.Double::class.java
    this == Char::class.java -> java.lang.Character::class.java
    this == Boolean::class.java -> java.lang.Boolean::class.java
    else -> this
}

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "RemoveRedundantQualifierName")
fun Any.box(): Any = when {
    this == Byte::class.java -> this as java.lang.Byte
    this == Short::class.java -> this as java.lang.Short
    this == Int::class.java -> this as java.lang.Integer
    this == Long::class.java -> this as java.lang.Long
    this == Float::class.java -> this as java.lang.Float
    this == Double::class.java -> this as java.lang.Double
    this == Char::class.java -> this as java.lang.Character
    this == Boolean::class.java -> this as java.lang.Boolean
    else -> this
}

fun Any.boxArray(): kotlin.Array<*> = when (this) {
    is ByteArray -> this.map { it.box() }.toTypedArray()
    is ShortArray -> this.map { it.box() }.toTypedArray()
    is IntArray -> this.map { it.box() }.toTypedArray()
    is LongArray -> this.map { it.box() }.toTypedArray()
    is FloatArray -> this.map { it.box() }.toTypedArray()
    is DoubleArray -> this.map { it.box() }.toTypedArray()
    is CharArray -> this.map { it.box() }.toTypedArray()
    is BooleanArray -> this.map { it.box() }.toTypedArray()
    is kotlin.Array<*> -> this
    else -> error("Value is not an array: ${this::class.java}")
}

fun Any.isAnyArray() = when (this) {
    is ByteArray -> true
    is ShortArray -> true
    is IntArray -> true
    is LongArray -> true
    is FloatArray -> true
    is DoubleArray -> true
    is CharArray -> true
    is BooleanArray -> true
    is kotlin.Array<*> -> true
    else -> false
}
