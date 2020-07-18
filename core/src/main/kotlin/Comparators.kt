package edu.illinois.cs.cs125.jenisol.core

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

interface Comparator {
    fun compare(solution: Any, submission: Any): Boolean
    val descendants: Boolean
}
