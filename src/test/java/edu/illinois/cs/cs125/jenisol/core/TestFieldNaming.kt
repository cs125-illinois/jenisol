package edu.illinois.cs.cs125.jenisol.core

import examples.fieldnamingtesting.TestFieldNaming
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestFieldNaming : StringSpec({
    "it should name fields properly" {
        fieldNamed("test0").fullName() shouldBe "int test0"
    }
    "it should name arrays properly" {
        fieldNamed("test1").fullName() shouldBe "int[] test1"
    }
})

private fun fieldNamed(name: String) = TestFieldNaming::class.java.declaredFields
    .find { it.name == name } ?: error("Couldn't find field $name")