@file:Suppress("MemberVisibilityCanBePrivate")

package edu.illinois.cs.cs125.jenisol.core.generators

import edu.illinois.cs.cs125.jenisol.core.EdgeType
import edu.illinois.cs.cs125.jenisol.core.FixedParameters
import edu.illinois.cs.cs125.jenisol.core.ParameterGroup
import edu.illinois.cs.cs125.jenisol.core.RandomPair
import edu.illinois.cs.cs125.jenisol.core.RandomParameters
import edu.illinois.cs.cs125.jenisol.core.RandomType
import edu.illinois.cs.cs125.jenisol.core.SimpleType
import edu.illinois.cs.cs125.jenisol.core.Solution
import edu.illinois.cs.cs125.jenisol.core.asArray
import edu.illinois.cs.cs125.jenisol.core.deepCopy
import edu.illinois.cs.cs125.jenisol.core.isEdgeType
import edu.illinois.cs.cs125.jenisol.core.isFixedParameters
import edu.illinois.cs.cs125.jenisol.core.isRandomParameters
import edu.illinois.cs.cs125.jenisol.core.isRandomType
import edu.illinois.cs.cs125.jenisol.core.isSimpleType
import java.lang.ClassCastException
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.Type
import kotlin.random.Random

class ParameterGeneratorFactory(private val executables: Set<Executable>, solution: Class<*>) {

    private val methodParameterGenerators = executables.map { it to MethodParametersGeneratorGenerator(it) }.toMap()

    private val typesNeeded = methodParameterGenerators
        .filter { (_, generator) -> generator.needsParameterGenerator }
        .keys
    private val typeGenerators: Map<Type, TypeGeneratorGenerator>

    init {
        val neededTypes = executables
            .filter { it in typesNeeded }
            .map { it.parameterTypes }
            .toTypedArray().flatten().distinct().toSet()
        val simple: MutableMap<Class<*>, Set<Any>> = mutableMapOf()
        val edge: MutableMap<Class<*>, Set<Any?>> = mutableMapOf()
        solution.declaredFields
            .filter { it.isSimpleType() || it.isEdgeType() }
            .forEach { field ->
                val simpleName = SimpleType::class.java.simpleName
                val edgeName = EdgeType::class.java.simpleName
                check(!(field.isSimpleType() && field.isEdgeType())) {
                    "Cannot use both @$simpleName and @$edgeName annotations on same field"
                }
                if (field.isSimpleType()) {
                    SimpleType.validate(field).also { klass ->
                        check(klass !in simple) { "Duplicate @$simpleName annotation for type ${klass.name}" }
                        check(klass in neededTypes) {
                            "@$simpleName annotation for type ${klass.name} that is not used by the solution"
                        }
                        @Suppress("UNCHECKED_CAST")
                        simple[klass] = field.get(null).asArray().toSet().also { simpleCases ->
                            check(simpleCases.none { it == null }) { "@$simpleName values should not include null" }
                        } as Set<Any>
                    }
                }
                if (field.isEdgeType()) {
                    EdgeType.validate(field).also { klass ->
                        check(klass !in edge) { "Duplicate @$edgeName annotation for type ${klass.name}" }
                        check(klass in neededTypes) {
                            "@$edgeName annotation for type ${klass.name} that is not used by the solution"
                        }
                        edge[klass] = field.get(null).asArray().toSet()
                    }
                }
            }
        val rand: MutableMap<Class<*>, Method> = mutableMapOf()
        solution.declaredMethods
            .filter { it.isRandomType() }
            .forEach { method ->
                val randName = Random::class.java.simpleName
                RandomType.validateAsType(method).also { klass ->
                    check(klass !in rand) { "Duplicate @$randName method for type ${klass.name}" }
                    check(klass in neededTypes) {
                        "@$randName annotation for type ${klass.name} that is not used by the solution"
                    }
                    rand[klass] = method
                }
            }
        typeGenerators = (simple.keys + edge.keys + rand.keys).toSet().map { klass ->
            klass to { random: Random ->
                OverrideTypeGenerator(
                    klass,
                    simple[klass],
                    edge[klass],
                    rand[klass],
                    random
                )
            }
        }.toMap()
    }

    private val parameterGenerators: Map<Executable, ParametersGeneratorGenerator> = executables
        .filter { it in typesNeeded }
        .map { executable ->
            // Generate one unnecessarily to make sure that we can
            TypeParameterGenerator(executable.parameters, typeGenerators)
            Pair<Executable, ParametersGeneratorGenerator>(
                executable,
                { random ->
                    TypeParameterGenerator(
                        executable.parameters,
                        typeGenerators,
                        random
                    )
                }
            )
        }
        .toMap()

    fun get(random: Random = Random, settings: Solution.Settings) = executables
        .map { executable ->
            if (executable.parameters.isEmpty()) {
                executable to EmptyParameterMethodGenerator()
            } else {
                executable to (
                    methodParameterGenerators[executable]?.generate(
                        parameterGenerators[executable],
                        settings,
                        random
                    ) ?: error("Didn't find a method parameter generator that should exist")
                    )
            }
        }
        .toMap()
        .let {
            ExecutableGenerators(it)
        }
}

class ExecutableGenerators(private val map: Map<Executable, ExecutableGenerator>) :
    Map<Executable, ExecutableGenerator> by map

interface ExecutableGenerator {
    fun generate(): ParametersGenerator.Value
    fun next()
    fun prev()
}

class MethodParametersGeneratorGenerator(target: Executable) {
    val fixedParameters: Collection<ParameterGroup>?
    val randomParameters: Method?

    init {
        val parameterTypes = target.parameterTypes.map { type -> type as Type }.toTypedArray()
        fixedParameters = target.declaringClass.declaredFields
            .filter { field -> field.isFixedParameters() }
            .filter { field ->
                FixedParameters.validate(field).compareBoxed(parameterTypes)
            }.also {
                check(it.size <= 1) {
                    "Multiple @${FixedParameters.name} annotations match method ${target.name}"
                }
            }.firstOrNull()?.let { field ->
                val values = field.get(null)
                check(values is Collection<*>) { "@${FixedParameters.name} field does not contain a collection" }
                check(values.isNotEmpty()) { "@${FixedParameters.name} field contains as empty collection" }
                try {
                    @Suppress("UNCHECKED_CAST")
                    values as Collection<ParameterGroup>
                } catch (e: ClassCastException) {
                    error("@${FixedParameters.name} field does not contain a collection of parameter groups")
                }
                values.forEach {
                    val solutionParameters = it.deepCopy()
                    val submissionParameters = it.deepCopy()
                    check(solutionParameters !== submissionParameters) {
                        "@${FixedParameters.name} field produces referentially equal copies"
                    }
                    check(solutionParameters == submissionParameters) {
                        "@${FixedParameters.name} field does not produce equal copies"
                    }
                }
                values
            }
        randomParameters = target.declaringClass.declaredMethods
            .filter { method -> method.isRandomParameters() }
            .filter { method -> RandomParameters.validate(method).compareBoxed(parameterTypes) }
            .also {
                check(it.size <= 1) {
                    "Multiple @${RandomParameters.name} annotations match method ${target.name}"
                }
            }.firstOrNull()
    }

    val needsParameterGenerator = fixedParameters == null || randomParameters == null
    fun generate(
        parametersGenerator: ParametersGeneratorGenerator?,
        settings: Solution.Settings,
        random: Random = Random
    ) = ConfiguredParametersGenerator(parametersGenerator, settings, random, fixedParameters, randomParameters)
}

class ConfiguredParametersGenerator(
    parametersGenerator: ParametersGeneratorGenerator?,
    private val settings: Solution.Settings,
    private val random: Random = Random,
    overrideFixed: Collection<ParameterGroup>? = null,
    private val overrideRandom: Method? = null
) : ExecutableGenerator {
    private val generator = if (overrideFixed != null && overrideRandom != null) {
        null
    } else {
        check(parametersGenerator != null) { "Parameter generator required but not provided" }
        parametersGenerator(random)
    }

    private fun Collection<ParameterGroup>.toFixedParameters(): List<ParametersGenerator.Value> = map {
        ParametersGenerator.Value(
            it.deepCopy().toArray(),
            it.deepCopy().toArray(),
            ParametersGenerator.Type.FIXED_FIELD
        )
    }

    private fun List<ParametersGenerator.Value>.trim(count: Int) = if (this.size <= count) {
        this
    } else {
        this.shuffled(random).take(count)
    }

    private val fixed: List<ParametersGenerator.Value>

    init {
        fixed = if (overrideFixed != null) {
            overrideFixed.toFixedParameters()
        } else {
            check(generator != null) { "Automatic parameter generator was unexpectedly null" }
            generator.let {
                it.simple.trim(settings.simpleCount) +
                    it.edge.trim(settings.edgeCount) +
                    it.mixed.trim(settings.mixedCount)
            }.trim(settings.fixedCount)
        }
    }

    private val randomPair = RandomPair(random.nextLong())
    private var index = 0
    private var bound: TypeGenerator.Complexity? = null
    private val complexity = TypeGenerator.Complexity()
    private var randomStarted = false

    override fun generate(): ParametersGenerator.Value {
        return if (index in fixed.indices) {
            fixed[index]
        } else {
            val currentComplexity = bound ?: complexity
            if (overrideRandom != null) {
                check(randomPair.synced) { "Random pair was out of sync before parameter generation" }
                val solutionParameters =
                    overrideRandom.invoke(null, currentComplexity.level, randomPair.solution) as ParameterGroup
                val submissionParameters =
                    overrideRandom.invoke(null, currentComplexity.level, randomPair.submission) as ParameterGroup
                check(randomPair.synced) { "Random pair was out of sync after parameter generation" }
                ParametersGenerator.Value(
                    solutionParameters.toArray(),
                    submissionParameters.toArray(),
                    ParametersGenerator.Type.RANDOM_METHOD,
                    currentComplexity
                )
            } else {
                check(generator != null) { "Automatic parameter generator was unexpectedly null" }
                generator.random(currentComplexity)
            }.also {
                randomStarted = true
            }
        }.also {
            index++
        }
    }

    override fun next() {
        if (randomStarted) {
            complexity.next()
        }
    }

    override fun prev() {
        if (randomStarted) {
            if (bound == null) {
                bound = complexity
            } else {
                bound!!.prev()
            }
        }
    }
}

@Suppress("EmptyFunctionBlock")
class EmptyParameterMethodGenerator : ExecutableGenerator {
    private val empty = ParametersGenerator.Value(arrayOf(), arrayOf(), ParametersGenerator.Type.EMPTY)

    override fun prev() {}
    override fun next() {}
    override fun generate(): ParametersGenerator.Value = empty
}

typealias ParametersGeneratorGenerator = (random: Random) -> ParametersGenerator

interface ParametersGenerator {
    enum class Type { EMPTY, SIMPLE, EDGE, MIXED, RANDOM, FIXED_FIELD, RANDOM_METHOD }

    val simple: List<Value>
    val edge: List<Value>
    val mixed: List<Value>
    fun random(complexity: TypeGenerator.Complexity): Value

    data class Value(
        val solution: Array<Any?>,
        val submission: Array<Any?>,
        val type: Type,
        val complexity: TypeGenerator.Complexity = TypeGenerator.Complexity(0)
    ) {
        val either = solution
        override fun equals(other: Any?): Boolean {
            return when {
                this === other -> true
                other is Value -> either.contentDeepEquals(other.either)
                else -> false
            }
        }

        override fun hashCode(): Int {
            return either.contentHashCode()
        }
    }
}

class TypeParameterGenerator(
    parameters: Array<out Parameter>,
    generators: Map<Type, TypeGeneratorGenerator> = mapOf(),
    private val random: Random = Random
) : ParametersGenerator {
    private val parameterGenerators = parameters.map {
        val type = it.parameterizedType
        if (type in generators) {
            generators[type]
        } else {
            require(type is Class<*>) { "No default generators are registered for non-class types" }
            Defaults[type]
        }?.invoke(random) ?: error(
            "Couldn't find generator for parameter ${it.name} with type ${it.parameterizedType.typeName}"
        )
    }

    private fun List<Set<TypeGenerator.Value<*>>>.combine(type: ParametersGenerator.Type) = product().map { list ->
        list.map {
            check(it is TypeGenerator.Value<*>) { "Didn't find the right type in our parameter list" }
            Pair(it.solution, it.submission)
        }.unzip().let { (solution, submission) ->
            ParametersGenerator.Value(
                solution.toTypedArray(),
                submission.toTypedArray(),
                type
            )
        }
    }

    override val simple by lazy {
        parameterGenerators.map { it.simple }.combine(ParametersGenerator.Type.SIMPLE)
    }
    override val edge by lazy {
        parameterGenerators.map { it.edge }.combine(ParametersGenerator.Type.EDGE)
    }
    override val mixed by lazy {
        parameterGenerators.map { it.simple + it.edge }.combine(ParametersGenerator.Type.MIXED).filter {
            it !in simple && it !in edge
        }
    }

    override fun random(complexity: TypeGenerator.Complexity): ParametersGenerator.Value {
        return parameterGenerators.map { it.random(complexity) }.map {
            Pair(it.solution, it.submission)
        }.unzip().let { (solution, submission) ->
            ParametersGenerator.Value(
                solution.toTypedArray(),
                submission.toTypedArray(),
                ParametersGenerator.Type.RANDOM,
                complexity
            )
        }
    }
}

fun List<*>.product() = fold(listOf(listOf<Any?>())) { acc, set ->
    require(set is Collection<*>) { "Error computing product" }
    acc.flatMap { list -> set.map { element -> list + element } }
}.toSet()
