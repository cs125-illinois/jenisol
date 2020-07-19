package edu.illinois.cs.cs125.jenisol.core

import io.kotlintest.specs.StringSpec

class TestKotlinExamples : StringSpec({
    examples.kotlin.noreceiver.single.noarguments.correct.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
})
