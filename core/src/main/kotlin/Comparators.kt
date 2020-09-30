package edu.illinois.cs.cs125.jenisol.core

import edu.illinois.cs.cs125.jenisol.core.generators.boxArray
import edu.illinois.cs.cs125.jenisol.core.generators.isAnyArray
import kotlin.math.abs

interface Comparator {
    fun compare(solution: Any, submission: Any): Boolean
    val descendants: Boolean
}

class Comparators(
    private val comparators: MutableMap<Class<*>, Comparator>
) : MutableMap<Class<*>, Comparator> by comparators {
    init {
        comparators[Any::class.java] = object : Comparator {
            override val descendants = false
            override fun compare(solution: Any, submission: Any) = true
        }
        comparators[Throwable::class.java] = object : Comparator {
            override val descendants = true
            override fun compare(solution: Any, submission: Any) = solution::class.java == submission::class.java
        }
        comparators[Double::class.java] = object : Comparator {
            override val descendants = false
            override fun compare(solution: Any, submission: Any): Boolean = when {
                solution is Double && submission is Double -> compareDoubles(solution, submission)
                else -> false
            }
        }
        comparators[java.lang.Double::class.java] = object : Comparator {
            override val descendants = false
            override fun compare(solution: Any, submission: Any): Boolean = when {
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

@Suppress("ComplexMethod", "MapGetWithNotNullAssertionOperator")
fun Any.deepEquals(
    submission: Any?,
    comparators: Comparators
): Boolean = when {
    this === submission -> true
    submission == null -> false
    this::class.java in comparators -> comparators[this::class.java].compare(this, submission)
    this is ParameterGroup && submission is ParameterGroup ->
        this.toArray().deepEquals(submission.toArray(), comparators)
    this.isAnyArray() != submission.isAnyArray() -> false
    this.isAnyArray() && submission.isAnyArray() -> {
        val solutionBoxed = this.boxArray()
        val submissionBoxed = submission.boxArray()
        (solutionBoxed.size == submissionBoxed.size) && solutionBoxed.zip(submissionBoxed)
            .all { (solution, submission) ->
                when {
                    solution === submission -> true
                    solution == null || submission == null -> false
                    else -> solution.deepEquals(submission, comparators)
                }
            }
    }
    else -> this == submission
}

const val DEFAULT_DOUBLE_THRESHOLD = 0.000001
fun compareDoubles(first: Double, second: Double) = when {
    first == second -> true
    first.isNaN() && second.isNaN() -> true
    (abs(first - second) / abs(first).coerceAtMost(abs(second))) < DEFAULT_DOUBLE_THRESHOLD -> true
    else -> false
}
