package edu.illinois.cs.cs125.jenisol.core

fun Class<*>.testName() = packageName.removePrefix("examples.")
