package edu.illinois.cs.cs125.jenisol.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

class TestSubmissionDesign : StringSpec(
    {
        examples.java.submissiondesign.noconverter.BadSolution0::class.java.also {
            "${it.testDesignName()}" { it.testDesign() }
        }
        examples.java.submissiondesign.noconverter.BadSolution1::class.java.also {
            "${it.testDesignName()}" { it.testDesign() }
        }
        examples.java.submissiondesign.noconverter.BadSolution2::class.java.also {
            "${it.testDesignName()}" { it.testDesign() }
        }
    }
)

fun Class<*>.testDesignName(): String = this.name
fun Class<*>.testDesign() {
    shouldThrow<SolutionDesignError> {
        solution(this)
    }
}