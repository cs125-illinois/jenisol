@file:Suppress("MatchingDeclarationName")

package edu.illinois.cs.cs125.jenisol.core.generators

import edu.illinois.cs.cs125.jenisol.core.Solution
import edu.illinois.cs.cs125.jenisol.core.findConstructor
import kotlin.random.Random

class ReceiverGenerator(
    private val solution: Solution,
    private val submission: Class<*>,
    private val random: Random = Random
) : TypeGenerator<Any> {
    lateinit var methodGenerator: Generators

    private val fixedReceivers by lazy {
        solution.solutionConstructors.map { solutionConstructor ->
            val submissionConstructor = submission.findConstructor(solutionConstructor, solution.solution)
                ?: error("Can't find submission constructor that should exist")

            methodGenerator[solutionConstructor]!!.fixed.map {
                val solutionReceiver = solutionConstructor.newInstance(*it.solution)
                val submissionReceiver = submissionConstructor.newInstance(*it.submission)
                val solutionCopy = solutionConstructor.newInstance(*it.solutionCopy)
                val submissionCopy = submissionConstructor.newInstance(*it.submissionCopy)
                Pair(it, TypeGenerator.Value<Any>(solutionReceiver, submissionReceiver, solutionCopy, submissionCopy))
            }
        }.flatten().toSet()
    }

    override val simple: Set<TypeGenerator.Value<Any>>
        get() = fixedReceivers
            .filter { (parameters, _) -> parameters.type == Parameters.Type.SIMPLE }
            .map { (_, value) -> value }
            .toSet()

    @Suppress("UNCHECKED_CAST")
    override val edge: Set<TypeGenerator.Value<Any?>>
        get() = fixedReceivers
            .filter { (parameters, _) -> parameters.type != Parameters.Type.SIMPLE }
            .map { (_, value) -> value }
            .toSet() as Set<TypeGenerator.Value<Any?>>

    private val constructors = sequence { yieldAll(solution.solutionConstructors.shuffled(random)) }
    override fun random(complexity: TypeGenerator.Complexity): TypeGenerator.Value<Any> {
        val solutionConstructor = constructors.first()
        val submissionConstructor = submission.findConstructor(solutionConstructor, solution.solution)
            ?: error("Can't find submission constructor that should exist")
        return methodGenerator[solutionConstructor]!!.random(complexity).let {
            val solutionReceiver = solutionConstructor.newInstance(*it.solution)
            val submissionReceiver = submissionConstructor.newInstance(*it.submission)
            val solutionCopy = solutionConstructor.newInstance(*it.solutionCopy)
            val submissionCopy = submissionConstructor.newInstance(*it.submissionCopy)
            TypeGenerator.Value(solutionReceiver, submissionReceiver, solutionCopy, submissionCopy)
        }
    }
}

val UnconfiguredReceiverGenerator = object : TypeGenerator<Any> {
    override val simple: Set<TypeGenerator.Value<Any>>
        get() = error("Receiver generation unconfigured")
    override val edge: Set<TypeGenerator.Value<Any?>>
        get() = error("Receiver generation unconfigured")

    override fun random(complexity: TypeGenerator.Complexity): TypeGenerator.Value<Any> {
        error("Receiver generation unconfigured")
    }
}
