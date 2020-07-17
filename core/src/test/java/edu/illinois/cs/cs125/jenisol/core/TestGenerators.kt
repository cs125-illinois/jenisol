package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.Complexity
import edu.illinois.cs.cs125.jenisol.core.generators.Defaults
import edu.illinois.cs.cs125.jenisol.core.generators.TypeGenerator
import edu.illinois.cs.cs125.jenisol.core.generators.TypeParameterGenerator
import edu.illinois.cs.cs125.jenisol.core.generators.compareBoxed
import edu.illinois.cs.cs125.jenisol.core.generators.getArrayType
import edu.illinois.cs.cs125.jenisol.core.generators.product
import examples.generatortesting.TestGenerators
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeLessThanOrEqual
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.lang.reflect.Method
import kotlin.math.pow

class TestGenerators : StringSpec({
    "it should generate bytes properly" {
        methodNamed("testByte").also { method ->
            method.invoke(null, 0.toByte())
            method.testGenerator()
        }
        methodNamed("testBoxedByte").also { method ->
            method.invoke(null, null)
            method.invoke(null, 0.toByte())
            method.testGenerator()
        }
    }
    "it should generate shorts properly" {
        methodNamed("testShort").also { method ->
            method.invoke(null, 0.toShort())
            method.testGenerator()
        }
        methodNamed("testBoxedShort").also { method ->
            method.invoke(null, null)
            method.invoke(null, 0.toShort())
            method.testGenerator()
        }
    }
    "it should generate ints properly" {
        methodNamed("testInt").also { method ->
            method.invoke(null, 0)
            method.testGenerator()
        }
        methodNamed("testBoxedInt").also { method ->
            method.invoke(null, null)
            method.invoke(null, 0)
            method.testGenerator()
        }
    }
    "it should generate longs properly" {
        methodNamed("testLong").also { method ->
            method.invoke(null, 0.toLong())
            method.testGenerator()
        }
        methodNamed("testBoxedLong").also { method ->
            method.invoke(null, null)
            method.invoke(null, 0.toLong())
            method.testGenerator()
        }
    }
    "it should generate floats properly" {
        methodNamed("testFloat").also { method ->
            method.invoke(null, 0.0f)
            method.testGenerator()
        }
        methodNamed("testBoxedFloat").also { method ->
            method.invoke(null, null)
            method.invoke(null, 0.0f)
            method.testGenerator()
        }
    }
    "it should generate doubles properly" {
        methodNamed("testDouble").also { method ->
            method.invoke(null, 0.0)
            method.testGenerator()
        }
        methodNamed("testBoxedDouble").also { method ->
            method.invoke(null, null)
            method.invoke(null, 0.0)
            method.testGenerator()
        }
    }
    "it should generate booleans properly" {
        methodNamed("testBoolean").also { method ->
            method.invoke(null, true)
            method.testGenerator()
        }
        methodNamed("testBoxedBoolean").also { method ->
            method.invoke(null, null)
            method.invoke(null, true)
            method.testGenerator()
        }
    }
    "it should generate Strings properly" {
        methodNamed("testString").also { method ->
            method.invoke(null, null)
            method.invoke(null, "test")
            method.testGenerator()
        }
    }
    "it should generate arrays properly" {
        methodNamed("testIntArray").also { method ->
            method.invoke(null, null)
            method.invoke(null, intArrayOf())
            method.invoke(null, intArrayOf(1, 2, 4))
            method.testGenerator()
        }
        methodNamed("testLongArray").also { method ->
            method.invoke(null, null)
            method.invoke(null, longArrayOf())
            method.invoke(null, longArrayOf(1, 2, 4))
            method.testGenerator()
        }
        methodNamed("testStringArray").also { method ->
            method.invoke(null, null)
            method.invoke(null, arrayOf<String>())
            method.invoke(null, arrayOf("test", "test me"))
            method.testGenerator()
        }
        methodNamed("testIntArrayArray").also { method ->
            method.invoke(null, null)
            method.invoke(null, arrayOf(intArrayOf()))
            method.invoke(null, arrayOf(intArrayOf(1, 2, 3), intArrayOf(4, 5, 6)))
            method.testGenerator()
        }
        methodNamed("testStringArrayArray").also { method ->
            method.invoke(null, null)
            method.invoke(null, arrayOf(arrayOf("")))
            method.invoke(null, arrayOf(arrayOf("test", "me"), arrayOf("again")))
            method.testGenerator()
        }
    }
    "it should generate nested arrays properly" {
        Defaults.create(Array<Array<IntArray>>::class.java).also { generator ->
            (0..128).map {
                @Suppress("UNCHECKED_CAST")
                generator.random(Complexity(Complexity.MIN))
                .let { it.solutionCopy as Array<Array<IntArray>> }.totalSize().also {
                        it shouldBeGreaterThan 0
                        it shouldBeLessThanOrEqual 8
                    }
            }

            (0..128).map {
                @Suppress("UNCHECKED_CAST")
                generator.random(Complexity(Complexity.MAX))
                .let { it.solutionCopy as Array<Array<IntArray>> }.totalSize().also {
                        it shouldBeGreaterThan 0
                        it shouldBeLessThanOrEqual 512
                    }
            }
        }
    }
    "it should generate parameters properly" {
        methodNamed("testInt").testParameterGenerator(3, 2)
        methodNamed("testTwoInts").testParameterGenerator(3, 2, 2)
        methodNamed("testIntArray").testParameterGenerator(2, 1)
        methodNamed("testTwoIntArrays").testParameterGenerator(2, 1, 2)
        methodNamed("testIntAndBoolean").testParameterGenerator(3 * 2, 0, 1, 4)
    }
    "it should determine array enclosed types correctly" {
        IntArray::class.java.getArrayType() shouldBe Int::class.java
        Array<IntArray>::class.java.getArrayType() shouldBe Int::class.java
        Array<Array<IntArray>>::class.java.getArrayType() shouldBe Int::class.java
        Array<Array<Array<String>>>::class.java.getArrayType() shouldBe String::class.java
    }
    "cartesian product should work" {
        listOf(listOf(1, 2), setOf(3, 4)).product().also {
            it shouldHaveSize 4
            it shouldContainExactlyInAnyOrder setOf(listOf(1, 3), listOf(1, 4), listOf(2, 3), listOf(2, 4))
        }
    }
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    "boxed compare should work" {
        Int::class.java.compareBoxed(Integer::class.java) shouldBe true
        IntArray::class.java.compareBoxed(Array<Integer>::class.java) shouldBe true
        Array<IntArray>::class.java.compareBoxed(Array<Array<Integer>>::class.java) shouldBe true
    }
    "generated parameters should compare properly" {
        One(arrayOf(intArrayOf(1, 2, 3), intArrayOf(4, 5))).also {
            it shouldBe One(arrayOf(intArrayOf(1, 2, 3), intArrayOf(4, 5)))
        }
        Two(
            arrayOf(intArrayOf(1, 2, 3), intArrayOf(4, 5)),
            arrayOf(booleanArrayOf(true, false), booleanArrayOf(false, true))
        ).also {
            it shouldBe Two(
                arrayOf(intArrayOf(1, 2, 3), intArrayOf(4, 5)),
                arrayOf(booleanArrayOf(true, false), booleanArrayOf(false, true))
            )
        }
    }
})

private fun methodNamed(name: String) = TestGenerators::class.java.declaredMethods
    .find { it.name == name } ?: error("Couldn't find method $name")

private fun Method.testGenerator(
    typeGenerator: TypeGenerator<*> = Defaults.create(this.parameterTypes.first())
) {
    typeGenerator.simple.forEach { invoke(null, it.solutionCopy) }
    typeGenerator.edge.forEach { invoke(null, it.solutionCopy) }
    (1..8).forEach { complexity ->
        repeat(4) { invoke(null, typeGenerator.random(Complexity(complexity)).solutionCopy) }
    }
}

private fun Int.pow(exponent: Int) = toDouble().pow(exponent.toDouble()).toInt()

private fun Method.testParameterGenerator(
    simpleSize: Int,
    edgeSize: Int,
    dimensionality: Int = 1,
    mixedSize: Int = (simpleSize + edgeSize).pow(dimensionality) - simpleSize.pow(dimensionality) - edgeSize.pow(
        dimensionality
    )
) {
    val parameterGenerator =
        TypeParameterGenerator(parameters)
    parameterGenerator.simple.also { simple ->
        simple shouldHaveSize simpleSize.pow(dimensionality)
        simple.forEach { invoke(null, *it.solutionCopy) }
    }
    parameterGenerator.edge.also { edge ->
        edge shouldHaveSize edgeSize.pow(dimensionality)
        edge.forEach { invoke(null, *it.solutionCopy) }
    }
    parameterGenerator.mixed.also { mixed ->
        mixed shouldHaveSize mixedSize
        mixed.forEach { invoke(null, *it.solutionCopy) }
    }
    Complexity.ALL.forEach { complexity ->
        invoke(null, *parameterGenerator.random(complexity).solutionCopy)
    }
}

fun Array<Array<IntArray>>.totalSize() = size.let {
    var total = it
    total += getOrElse(0) { arrayOf() }.size
    total += getOrElse(0) { arrayOf() }.getOrElse(0) { intArrayOf() }.size
    total
}

fun Array<Array<IntArray>>.elementCount(): Int {
    var count = 0
    println(size)
    for (i in 0 until size) {
        println("$i:${get(i).size}")
        for (j in 0 until get(i).size) {
            println("$i:$j:${get(i)[j].size}")
            count += get(i)[j].size
        }
    }
    return count
}
