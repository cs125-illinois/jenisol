@file:Suppress("MatchingDeclarationName")

package edu.illinois.cs.cs125.jenisol.core.generators

import edu.illinois.cs.cs125.jenisol.core.TestRunner
import kotlin.random.Random

@Suppress("UNCHECKED_CAST")
fun List<TestRunner>.findWithComplexity(complexity: Complexity, random: Random): Value<Any> = filter { it.ready }
    .map { it.receivers!! }
    .let { receivers ->
        check(receivers.isNotEmpty()) { "No receivers available" }
        val closest = receivers.map { it.complexity.level }.distinct().sorted().let { complexities ->
            complexities.find { it == complexity.level }
                ?: complexities.filter { complexity.level >= it }.minOrNull()
                ?: complexities.filter { complexity.level < it }.sorted().reversed().firstOrNull()
                ?: error("Couldn't locate a complexity that should exist")
        }
        receivers.filter { it.complexity.level == closest }.shuffled(random).firstOrNull() as Value<Any>
    }

class ReceiverGenerator(val random: Random = Random, val runners: MutableList<TestRunner>) : TypeGenerator<Any> {
    init {
        check(runners.none { it.receivers == null }) { "Found null receivers" }
        check(runners.none { !it.ready }) { "Found non-ready receivers" }
    }

    class ReceiverValue(value: Value<Any>) :
        Value<Any>(value.solution, value.submission, value.solutionCopy, value.submissionCopy, value.complexity)

    @Suppress("UNCHECKED_CAST")
    override val simple: Set<Value<Any>>
        get() = runners
            .filter { it.receivers?.complexity?.level == 0 }
            .map { ReceiverValue(it.receivers as Value<Any>) }
            .toSet()

    override val edge: Set<Value<Any?>>
        get() = mutableSetOf(Value(null, null, null, null, ZeroComplexity))

    override fun random(complexity: Complexity, runner: TestRunner?): Value<Any> =
        if (random.nextBoolean()) {
            simple.shuffled(random).first()
        } else {
            runners.findWithComplexity(complexity, random)
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
