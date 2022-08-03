package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.boxArray
import edu.illinois.cs.cs125.jenisol.core.generators.isAnyArray
import edu.illinois.cs.cs125.jenisol.core.generators.isLambdaMethod
import kotlin.math.abs

interface Comparator {
    fun compare(solution: Any, submission: Any, solutionClass: Class<*>?, submissionClass: Class<*>?): Boolean
    val descendants: Boolean
}

class Comparators(
    private val comparators: MutableMap<Class<*>, Comparator> = mutableMapOf()
) : MutableMap<Class<*>, Comparator> by comparators {
    init {
        comparators[Any::class.java] = object : Comparator {
            override val descendants = false
            override fun compare(solution: Any, submission: Any, solutionClass: Class<*>?, submissionClass: Class<*>?) =
                true
        }
        comparators[Throwable::class.java] = object : Comparator {
            override val descendants = true

            @Suppress("LongMethod")
            override fun compare(solution: Any, submission: Any, solutionClass: Class<*>?, submissionClass: Class<*>?) =
                @Suppress("MagicNumber")
                when {
                    solution::class.java == submission::class.java -> true
                    solutionClass != null && submissionClass != null &&
                        solution is AssertionError &&
                        submission is IllegalArgumentException &&
                        !solutionClass.isKotlin() &&
                        submissionClass.isKotlin() -> true

                    solutionClass != null && submissionClass != null &&
                        (solution is AssertionError || solution is IllegalArgumentException) &&
                        submission is NullPointerException &&
                        submission.message?.startsWith("Parameter specified as non-null is null") == true &&
                        !solutionClass.isKotlin() &&
                        submissionClass.isKotlin() -> true

                    solutionClass != null && submissionClass != null &&
                        solution is NullPointerException &&
                        solution.message?.startsWith("Parameter specified as non-null is null") == true &&
                        (submission is AssertionError || submission is IllegalArgumentException) &&
                        solutionClass.isKotlin() &&
                        !submissionClass.isKotlin() -> true

                    solutionClass != null && submissionClass != null &&
                        solution is IllegalArgumentException &&
                        submission is AssertionError &&
                        solutionClass.isKotlin() &&
                        !submissionClass.isKotlin() -> true

                    solutionClass != null && submissionClass != null &&
                        solution is ArrayIndexOutOfBoundsException &&
                        submission is IndexOutOfBoundsException &&
                        solution.message == submission.message -> true

                    solutionClass != null && submissionClass != null &&
                        solution is IndexOutOfBoundsException &&
                        submission is ArrayIndexOutOfBoundsException &&
                        solution.message == submission.message -> true

                    solutionClass != null && submissionClass != null &&
                        solution is ArrayIndexOutOfBoundsException &&
                        solution.message != null &&
                        submission is IndexOutOfBoundsException &&
                        submission.message != null -> {
                        val solutionMatch =
                            """Index (\d+) out of bounds for length (\d+)""".toRegex().matchEntire(solution.message!!)
                        val submissionMatch =
                            """Index: (\d+), Size: (\d+)""".toRegex().matchEntire(submission.message!!)
                        when {
                            solutionMatch == null -> false
                            submissionMatch == null -> false
                            solutionMatch.groupValues.size != 3 -> false
                            submissionMatch.groupValues.size != 3 -> false
                            else -> {
                                solutionMatch.groupValues[1].toInt() == submissionMatch.groupValues[1].toInt() &&
                                    solutionMatch.groupValues[2].toInt() == submissionMatch.groupValues[2].toInt()
                            }
                        }
                    }

                    solutionClass != null && submissionClass != null &&
                        solution is java.lang.IndexOutOfBoundsException &&
                        solution.message != null &&
                        submission is ArrayIndexOutOfBoundsException &&
                        submission.message != null &&
                        solutionClass.isKotlin() &&
                        !submissionClass.isKotlin() -> {
                        val submissionMatch =
                            """Index (\d+) out of bounds for length (\d+)""".toRegex().matchEntire(solution.message!!)
                        val solutionMatch =
                            """Index: (\d+), Size: (\d+)""".toRegex().matchEntire(submission.message!!)
                        when {
                            solutionMatch == null -> false
                            submissionMatch == null -> false
                            solutionMatch.groupValues.size != 3 -> false
                            submissionMatch.groupValues.size != 3 -> false
                            else -> {
                                solutionMatch.groupValues[1].toInt() == submissionMatch.groupValues[1].toInt() &&
                                    solutionMatch.groupValues[2].toInt() == submissionMatch.groupValues[2].toInt()
                            }
                        }
                    }

                    else -> solution::class.java == submission::class.java
                }
        }
        comparators[Double::class.java] = object : Comparator {
            override val descendants = false
            override fun compare(
                solution: Any,
                submission: Any,
                solutionClass: Class<*>?,
                submissionClass: Class<*>?
            ): Boolean =
                when {
                    solution is Double && submission is Double -> compareDoubles(solution, submission)
                    else -> false
                }
        }
        comparators[java.lang.Double::class.java] = object : Comparator {
            override val descendants = false
            override fun compare(
                solution: Any,
                submission: Any,
                solutionClass: Class<*>?,
                submissionClass: Class<*>?
            ): Boolean =
                when {
                    solution is Double && submission is Double -> compareDoubles(solution, submission)
                    else -> false
                }
        }
    }

    @Suppress("ReturnCount")
    private fun searchUp(klass: Class<*>): Class<*>? {
        if (comparators.containsKey(klass)) {
            return klass
        }
        var current: Class<*>? = klass
        while (current != null) {
            if (comparators[current]?.descendants == true) {
                return current
            }
            current = current.superclass
        }
        return null
    }

    override fun containsKey(key: Class<*>) = searchUp(key) != null
    override fun get(key: Class<*>) = comparators[searchUp(key)] ?: error("No comparator for $key")
}

fun Any.lambdaGuessEquals(submission: Any): Boolean {
    if (!(this.isLambdaMethod() && submission.isLambdaMethod())) {
        return false
    }
    val myLambdaName = this::class.java.name.split("/").also {
        it.dropLast(1)
    }.joinToString("/")
    val submissionLambdaName = submission::class.java.name.split("/").also {
        it.dropLast(1)
    }.joinToString("/")
    return myLambdaName == submissionLambdaName
}

@Suppress("ComplexMethod", "MapGetWithNotNullAssertionOperator")
fun Any.deepEquals(
    submission: Any?,
    comparators: Comparators,
    solutionClass: Class<*>?,
    submissionClass: Class<*>?
): Boolean = when {
    this === submission -> true
    submission == null -> false
    this::class.java in comparators -> comparators[this::class.java].compare(
        this,
        submission,
        solutionClass,
        submissionClass
    )

    this is ParameterGroup && submission is ParameterGroup ->
        this.toArray().deepEquals(submission.toArray(), comparators, solutionClass, submissionClass)

    this.isAnyArray() != submission.isAnyArray() -> false
    this.isAnyArray() && submission.isAnyArray() -> {
        val solutionBoxed = this.boxArray()
        val submissionBoxed = submission.boxArray()
        (solutionBoxed.size == submissionBoxed.size) && solutionBoxed.zip(submissionBoxed)
            .all { (solution, submission) ->
                when {
                    solution === submission -> true
                    solution == null || submission == null -> false
                    else -> solution.deepEquals(submission, comparators, solutionClass, submissionClass)
                }
            }
    }

    else -> this.lambdaGuessEquals(submission) || this == submission
}

const val DEFAULT_DOUBLE_THRESHOLD = 0.000001
fun compareDoubles(first: Double, second: Double) = when {
    first == second -> true
    first.isNaN() && second.isNaN() -> true
    (abs(first - second) / abs(first).coerceAtMost(abs(second))) < DEFAULT_DOUBLE_THRESHOLD -> true
    else -> false
}