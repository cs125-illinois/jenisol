package edu.illinois.cs.cs125.jenisol.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class KotlinTest {
    @Suppress("UNUSED_PARAMETER")
    fun method(first: Int, second: String): Int {
        return first
    }
}

class TestHelpers : StringSpec({
    "should print methods properly" {
        val parameters = Two(1, "two")
        val method = Test::class.java.declaredMethods.first()
        method.formatBoundMethodCall(
            parameters,
            Test::class.java
        ) shouldBe """method(int first = 1, String second = "two")"""
        method.formatBoundMethodCall(
            parameters,
            KotlinTest::class.java
        ) shouldBe """method(first: Int = 1, second: String = "two")"""
    }
})