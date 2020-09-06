package edu.illinois.cs.cs125.jenisol.core

import io.kotest.core.spec.style.StringSpec

class TestKotlinExamples : StringSpec(
    {
        examples.kotlin.noreceiver.single.noarguments.correct.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
    }
)
