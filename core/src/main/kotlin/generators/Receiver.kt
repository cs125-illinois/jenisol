@file:Suppress("MatchingDeclarationName")

package edu.illinois.cs.cs125.jenisol.core.generators

import edu.illinois.cs.cs125.jenisol.core.TestRunner
import kotlin.random.Random

class ReceiverGenerator(val random: Random = Random, val runners: MutableList<TestRunner>) : TypeGenerator<Any> {
    init {
        check(runners.none { it.receivers == null }) { "Found null receivers" }
        check(runners.none { !it.ready }) { "Found non-ready receivers" }
    }

    class ReceiverValue(value: Value<Any>, val testRunner: TestRunner) :
        Value<Any>(value.solution, value.submission, value.solutionCopy, value.submissionCopy, value.complexity)

    @Suppress("UNCHECKED_CAST")
    override val simple: Set<Value<Any>>
        get() = runners
            .filter { it.receivers!!.complexity.level == 0 }
            .map { ReceiverValue(it.receivers as Value<Any>, it) }
            .toSet()

    override val edge: Set<Value<Any?>>
        get() = mutableSetOf(Value<Any?>(null, null, null, null, ZeroComplexity))

    @Suppress("UNCHECKED_CAST")
    override fun random(complexity: Complexity): Value<Any> {
        runners
            .filter { it.ready }
            .map { it.receivers!! }
            .filter { it.complexity == complexity }
            .shuffled(random)
            .firstOrNull()
            ?.also {
                return it as Value<Any>
            }
        runners
            .filter { it.ready }
            .map { it.receivers!! }
            .filter { it.complexity.level <= complexity.level }
            .shuffled(random)
            .firstOrNull()
            ?.also {
                return it as Value<Any>
            }
        error("Couldn't locate receiver with complexity: ${complexity.level}")
    }
}

val UnconfiguredReceiverGenerator = object : TypeGenerator<Any> {
    override val simple: Set<Value<Any>>
        get() = error("Receiver generation unconfigured")
    override val edge: Set<Value<Any?>>
        get() = error("Receiver generation unconfigured")

    override fun random(complexity: Complexity): Value<Any> {
        error("Receiver generation unconfigured")
    }
}
