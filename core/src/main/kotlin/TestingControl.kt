package edu.illinois.cs.cs125.jenisol.core

import java.lang.RuntimeException

sealed class TestingControlException : RuntimeException()
class SkipTest : TestingControlException()
class BoundComplexity : TestingControlException()
