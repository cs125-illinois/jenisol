package com.example

import edu.illinois.cs.cs125.jenisol.core.One
import edu.illinois.cs.cs125.jenisol.core.TestResult

@Suppress("USELESS_CAST")
fun verify(results: TestResult<IntArray, One<Int>>) {
    val parameters = results.submission.parameters as One<Int>
    if (results.submission.returned?.last() != parameters.first) {
        results.differs.add(TestResult.Differs.RETURN)
    }
}