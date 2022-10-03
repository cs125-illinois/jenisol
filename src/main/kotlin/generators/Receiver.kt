@file:Suppress("MatchingDeclarationName")

package edu.illinois.cs.cs125.jenisol.core.generators

import edu.illinois.cs.cs125.jenisol.core.Submission
import edu.illinois.cs.cs125.jenisol.core.TestRunner
import kotlin.random.Random

fun List<Value<Any>>.findWithComplexity(complexity: Complexity, random: Random): Value<Any> = let { receivers ->
    check(receivers.isNotEmpty()) { "No receivers available" }
    val closest = receivers.map { it.complexity.level }.distinct().sorted().let { complexities ->
        complexities.find { it == complexity.level }
            ?: complexities.filter { complexity.level >= it }.minOrNull()
            ?: complexities.filter { complexity.level < it }.sorted().reversed().firstOrNull()
            ?: error("Couldn't locate a complexity that should exist")
    }
    receivers.filter { it.complexity.level == closest }.shuffled(random).firstOrNull() as Value<Any>
}

class ReceiverGenerator(
    val random: Random = Random,
    val receivers: MutableList<Value<Any>>,
    val submission: Submission
) : TypeGenerator<Any> {

    override val simple: Set<Value<Any>>
        get() = receivers
            .filter { it.complexity.level == 0 }
            .toSet()

    override val edge: Set<Value<Any?>>
        get() = mutableSetOf(Value(null, null, null, null, null, ZeroComplexity))

    override fun random(complexity: Complexity, runner: TestRunner?): Value<Any> =
        if (random.nextBoolean()) {
            simple.shuffled(random).first()
        } else {
            receivers.findWithComplexity(complexity, random)
        }
}

val UnconfiguredReceiverGenerator = object : TypeGenerator<Any> {
    override val simple: Set<Value<Any>>
        get() = error("Receiver generation unconfigured")
    override val edge: Set<Value<Any?>>
        get() = error("Receiver generation unconfigured")

    override fun random(complexity: Complexity, runner: TestRunner?): Value<Any> {
        error("Receiver generation unconfigured")
    }
}