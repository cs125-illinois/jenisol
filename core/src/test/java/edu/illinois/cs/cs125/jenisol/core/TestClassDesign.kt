package edu.illinois.cs.cs125.jenisol.core

import examples.java.submissiondesign.Correct
import examples.java.submissiondesign.Correct1
import examples.java.submissiondesign.MissingConstructor1
import examples.java.submissiondesign.MissingMethod1
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

@Suppress("RemoveSingleExpressionStringTemplate")
class TestClassDesign : StringSpec({
    solution(Correct::class.java).also { solution ->
        "${solution.solution.testName()}" {
            solution.submission(Correct1::class.java)
            shouldThrow<ClassDesignMissingMethodError> {
                solution.submission(MissingMethod1::class.java)
            }
            shouldThrow<ClassDesignMissingMethodError> {
                solution.submission(MissingConstructor1::class.java)
            }
        }
    }
})
