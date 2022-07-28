@file:Suppress("MemberVisibilityCanBePrivate")

package edu.illinois.cs.cs125.jenisol.core.generators

import com.rits.cloning.Cloner
import edu.illinois.cs.cs125.jenisol.core.EdgeType
import edu.illinois.cs.cs125.jenisol.core.FixedParameters
import edu.illinois.cs.cs125.jenisol.core.One
import edu.illinois.cs.cs125.jenisol.core.ParameterGroup
import edu.illinois.cs.cs125.jenisol.core.RandomParameters
import edu.illinois.cs.cs125.jenisol.core.RandomType
import edu.illinois.cs.cs125.jenisol.core.Settings
import edu.illinois.cs.cs125.jenisol.core.SimpleType
import edu.illinois.cs.cs125.jenisol.core.Solution
import edu.illinois.cs.cs125.jenisol.core.TestRunner
import edu.illinois.cs.cs125.jenisol.core.deepCopy
import edu.illinois.cs.cs125.jenisol.core.fixedParametersMatchAll
import edu.illinois.cs.cs125.jenisol.core.getFixedFieldParametersName
import edu.illinois.cs.cs125.jenisol.core.getRandomParametersMethodName
import edu.illinois.cs.cs125.jenisol.core.isEdgeType
import edu.illinois.cs.cs125.jenisol.core.isFixedParameters
import edu.illinois.cs.cs125.jenisol.core.isInitializer
import edu.illinois.cs.cs125.jenisol.core.isNotNull
import edu.illinois.cs.cs125.jenisol.core.isRandomParameters
import edu.illinois.cs.cs125.jenisol.core.isRandomType
import edu.illinois.cs.cs125.jenisol.core.isSimpleType
import edu.illinois.cs.cs125.jenisol.core.isStatic
import edu.illinois.cs.cs125.jenisol.core.randomParametersMatchAll
import edu.illinois.cs.cs125.jenisol.core.unwrap
import java.lang.ClassCastException
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.Type
import kotlin.random.Random

data class Parameters(
    val solution: Array<Any?>,
    val submission: Array<Any?>,
    val solutionCopy: Array<Any?>,
    val submissionCopy: Array<Any?>,
    val unmodifiedCopy: Array<Any?>,
    val type: Type,
    val complexity: Complexity = ZeroComplexity
) {
    enum class Type { EMPTY, SIMPLE, EDGE, MIXED, RANDOM, FIXED_FIELD, RANDOM_METHOD, RECEIVER }

    @Suppress("ExceptionRaisedInUnexpectedLocation")
    override fun equals(other: Any?) = when {
        this === other -> true
        other is Parameters ->
            @Suppress("TooGenericExceptionCaught")
            try {
                solutionCopy.contentDeepEquals(other.solutionCopy)
            } catch (e: ThreadDeath) {
                throw e
            } catch (e: Throwable) {
                false
            }

        else -> false
    }

    override fun hashCode() = solutionCopy.contentHashCode()

    companion object {
        fun fromReceivers(value: Value<Any?>) = Parameters(
            arrayOf(value.solution),
            arrayOf(value.submission),
            arrayOf(value.solutionCopy),
            arrayOf(value.submissionCopy),
            arrayOf(value.unmodifiedCopy),
            Type.RECEIVER,
            value.complexity
        )
    }
}

typealias ParametersGeneratorGenerator = (random: Random, cloner: Cloner) -> TypeParameterGenerator

class GeneratorFactory(private val executables: Set<Executable>, val solution: Solution) {
    val solutionClass = solution.solution

    private val methodParameterGenerators =
        executables.associateWith { MethodParametersGeneratorGenerator(it, solution.solution) }

    init {
        methodParameterGenerators.values
            .mapNotNull { it.fixedParameters }
            .groupingBy { it }
            .eachCount()
            .filter { it.value > 1 }.let { list ->
                if (list.isNotEmpty()) {
                    list.keys.first().let { fixed ->
                        methodParameterGenerators.values.filter { it.fixedParameters == fixed }
                    }
                } else {
                    null
                }
            }?.also { matched ->
                val field = matched.first().fixedParametersField!!
                val methods = matched.map { it.target }
                if (!field.fixedParametersMatchAll()) {
                    error(
                        """Found @FixedParameter annotations that matched multiple methods
                    |$field matched ${matched.size} methods: $methods
                    |If you want to match multiple methods, use @FixedParameters("*")
                    |If you want to target one method, use @FixedParameters(methodName)
                        """.trimMargin()
                    )
                }
            }

        methodParameterGenerators.values
            .mapNotNull { it.randomParameters }
            .groupingBy { it }
            .eachCount()
            .filter { it.value > 1 }.let { list ->
                if (list.isNotEmpty()) {
                    list.keys.first().let { randomMethod ->
                        methodParameterGenerators.values.filter { it.randomParameters == randomMethod }
                    }
                } else {
                    null
                }
            }?.also { matched ->
                val method = matched.first().randomParameters!!
                val methods = matched.map { it.target }
                if (!method.randomParametersMatchAll()) {
                    error(
                        """Found @RandomParameters annotations that matched multiple methods
                    |$method matched ${matched.size} methods: $methods
                    |If you want to match multiple methods, use @RandomParameters("*")
                    |If you want to target one method, use @RandomParameters(methodName)
                        """.trimMargin()
                    )
                }
            }

        solution.solution.declaredFields.filter { it.isFixedParameters() }.forEach { field ->
            val used = methodParameterGenerators.values.filter { it.fixedParametersField == field }
            check(used.isNotEmpty()) {
                """Found unused @FixedParameters field: $field"""
            }
        }

        solution.solution.declaredMethods.filter { it.isRandomParameters() }.forEach { method ->
            val used = methodParameterGenerators.values.filter { it.randomParameters == method }
            check(used.isNotEmpty()) {
                """Found unused @RandomParameters method: $method"""
            }
        }

        if (solution.fauxStatic) {
            check(methodParameterGenerators.values.all { it.randomParameters?.isStatic() ?: true }) {
                """Found non-static @RandomParameters methods for a faux-static problem with no state
                    |These should be converted to static methods
                """.trimMargin()
            }
        } else {
            methodParameterGenerators.entries
                .filter { it.value.randomParameters != null }
                .forEach { (executable, generator) ->
                    if (executable is Method && executable.isInitializer()) {
                        check(generator.randomParameters?.isStatic() == true) {
                            "@RandomParameter methods for @Initializers must be static"
                        }
                    }
                    if (executable is Constructor<*>) {
                        check(generator.randomParameters?.isStatic() == true) {
                            "@RandomParameter methods for constructors must be static"
                        }
                    }
                    if (executable is Method && executable.isStatic()) {
                        check(generator.randomParameters?.isStatic() == true) {
                            "@RandomParameter methods for static methods must be static"
                        }
                    }
                }
        }
    }

    private val typesNeeded = methodParameterGenerators
        .filter { (_, generator) -> generator.needsParameterGenerator }
        .keys
    val typeGenerators: Map<Type, TypeGeneratorGenerator>

    init {
        val neededTypes = executables
            .filter { it in typesNeeded }
            .map { it.parameterTypes }
            .toTypedArray().flatten().distinct().toSet()
        val arrayNeededTypes = neededTypes.filter { it.isArray }.map { it.getArrayType() }

        val simpleArray: MutableMap<Class<*>, Set<Any>> = mutableMapOf()
        val edgeArray: MutableMap<Class<*>, Set<Any?>> = mutableMapOf()
        solutionClass.declaredFields
            .filter { it.isSimpleType() || it.isEdgeType() }
            .forEach { field ->
                val simpleName = SimpleType::class.java.simpleName
                val edgeName = EdgeType::class.java.simpleName
                check(!(field.isSimpleType() && field.isEdgeType())) {
                    "Cannot use both @$simpleName and @$edgeName annotations on same field"
                }
                if (field.isSimpleType()) {
                    SimpleType.validate(field).also { klass ->
                        check(klass !in simpleArray) { "Duplicate @$simpleName annotation for type ${klass.name}" }
                        check(klass in neededTypes || klass in arrayNeededTypes) {
                            "@$simpleName annotation for type ${klass.name} that is not used by the solution"
                        }
                        @Suppress("UNCHECKED_CAST")
                        simpleArray[klass] = field.get(null).boxArray().toSet().also { simpleCases ->
                            check(simpleCases.none { it == null }) { "@$simpleName values should not include null" }
                        } as Set<Any>
                    }
                } else if (field.isEdgeType()) {
                    EdgeType.validate(field).also { klass ->
                        check(klass !in edgeArray) { "Duplicate @$edgeName annotation for type ${klass.name}" }
                        check(klass in neededTypes || klass in arrayNeededTypes) {
                            "@$edgeName annotation for type ${klass.name} that is not used by the solution"
                        }
                        edgeArray[klass] = field.get(null).boxArray().toSet()
                    }
                }
            }

        val simpleMethod: MutableMap<Class<*>, Method> = mutableMapOf()
        val edgeMethod: MutableMap<Class<*>, Method> = mutableMapOf()
        val rand: MutableMap<Class<*>, Method> = mutableMapOf()
        solutionClass.declaredMethods
            .filter { it.isSimpleType() || it.isEdgeType() || it.isRandomType() }
            .forEach { method ->
                when {
                    method.isSimpleType() -> {
                        val name = SimpleType::class.java.simpleName
                        SimpleType.validate(method).also { klass ->
                            check(klass !in simpleArray) {
                                "@$name method for type ${klass.name} that already defines a @$name array"
                            }
                            check(klass !in simpleMethod) { "Duplicate @$name method for type ${klass.name}" }
                            check(klass in neededTypes || klass in arrayNeededTypes) {
                                "@$name annotation for type ${klass.name} that is not used by the solution"
                            }
                            simpleMethod[klass] = method
                        }
                    }

                    method.isEdgeType() -> {
                        val name = EdgeType::class.java.simpleName
                        EdgeType.validate(method).also { klass ->
                            check(klass !in edgeArray) {
                                "@$name method for type ${klass.name} that already defines a @$name array"
                            }
                            check(klass !in edgeMethod) { "Duplicate @$name method for type ${klass.name}" }
                            check(klass in neededTypes || klass in arrayNeededTypes) {
                                "@$name annotation for type ${klass.name} that is not used by the solution"
                            }
                            edgeMethod[klass] = method
                        }
                    }

                    method.isRandomType() -> {
                        val name = RandomType::class.java.simpleName
                        RandomType.validate(method).also { klass ->
                            check(klass !in rand) { "Duplicate @$name method for type ${klass.name}" }
                            check(klass in neededTypes || klass in arrayNeededTypes) {
                                "@$name annotation for type ${klass.name} that is not used by the solution"
                            }
                            rand[klass] = method
                        }
                    }
                }
            }

        neededTypes
            .filter { it !in simpleArray || it !in simpleMethod }
            .forEach { klass ->
                klass.declaredFields.filter { it.isSimpleType() }.let {
                    check(it.size <= 1) { "Found duplicate @SimpleType fields for type ${klass.name}" }
                    it.firstOrNull()
                }?.also { field ->
                    SimpleType.validate(field).also { klass ->
                        @Suppress("UNCHECKED_CAST")
                        simpleArray[klass] = field.get(null).boxArray().toSet().also { simpleCases ->
                            check(simpleCases.none { it == null }) { "@SimpleType arrays should not include null" }
                        } as Set<Any>
                    }
                }
                klass.declaredMethods.filter { it.isSimpleType() }.let {
                    check(it.size <= 1) { "Found duplicate @SimpleType methods for type ${klass.name}" }
                    it.firstOrNull()
                }?.also { method ->
                    check(klass !in simpleArray) { "Duplicate @SimpleType method for type ${klass.name}" }
                    SimpleType.validate(method).also { klass -> simpleMethod[klass] = method }
                }
            }
        neededTypes
            .filter { it !in edgeArray || it !in edgeMethod }
            .forEach { klass ->
                klass.declaredFields.filter { it.isEdgeType() }.let {
                    check(it.size <= 1) { "Found duplicate @EdgeType fields for type ${klass.name}" }
                    it.firstOrNull()
                }?.also { field ->
                    EdgeType.validate(field).also { klass ->
                        edgeArray[klass] = field.get(null).boxArray().toSet()
                    }
                }
                klass.declaredMethods.filter { it.isEdgeType() }.let {
                    check(it.size <= 1) { "Found duplicate @EdgeType methods for type ${klass.name}" }
                    it.firstOrNull()
                }?.also { method ->
                    check(klass !in edgeArray) { "Duplicate @EdgeType method for type ${klass.name}" }
                    EdgeType.validate(method).also { klass ->
                        edgeMethod[klass] = method
                    }
                }
            }
        neededTypes
            .filter { it !in rand }
            .forEach { klass ->
                klass.declaredMethods.find { it.isRandomType() }?.also { method ->
                    RandomType.validate(method).also { klass ->
                        rand[klass] = method
                    }
                }
            }

        val generatorMappings: MutableList<Pair<Type, (Random, Cloner) -> TypeGenerator<Any>>> =
            (simpleArray.keys + simpleMethod.keys + edgeArray.keys + edgeMethod.keys + rand.keys).toSet().map { klass ->
                val needsDefault = (klass !in simpleArray && klass !in simpleMethod) ||
                    (klass !in edgeArray && klass !in edgeMethod) ||
                    klass !in rand
                val defaultGenerator = if (needsDefault) {
                    Defaults[klass]
                } else {
                    null
                }
                klass to { random: Random, cloner: Cloner ->
                    OverrideTypeGenerator(
                        klass,
                        simpleArray[klass],
                        simpleMethod[klass],
                        edgeArray[klass],
                        edgeMethod[klass],
                        rand[klass],
                        random,
                        cloner,
                        defaultGenerator
                    )
                }
            }.toMutableList()

        if (solution.skipReceiver) {
            check(solutionClass !in neededTypes) {
                "Incorrectly calculated whether we needed a receiver: " +
                    "${solution.skipReceiver} v. ${solutionClass !in neededTypes}"
            }
        }

        if (solutionClass in neededTypes) {
            check(
                solutionClass !in simpleArray.keys &&
                    solutionClass !in edgeArray.keys &&
                    solutionClass !in rand.keys
            ) {
                "Type generation annotations not supported for receiver types"
            }
            // Add this so the next check doesn't fail.
            // The receiver generator cannot be set up until the submission class is available
            generatorMappings.add(solutionClass to { _: Random, _: Cloner -> UnconfiguredReceiverGenerator })
        }

        val currentGenerators = generatorMappings.toMap().toMutableMap()
        // Fill in any array types that we need and have type overrides for
        executables
            .filter { it in typesNeeded }
            .flatMap { method -> method.parameters.map { it.type }.toList() }
            .filter { it !in currentGenerators && it.isArray && it.getArrayType() in currentGenerators }
            .filterNotNull().forEach {
                var currentArray = it.getArrayType().arrayType()
                while (true) {
                    val previousArray = currentArray.componentType
                    currentGenerators[currentArray] = { random, cloner ->
                        ArrayGenerator(
                            random,
                            cloner,
                            previousArray,
                            currentGenerators[previousArray]!!.invoke(random, cloner)
                        )
                    }
                    if (currentArray == it) {
                        break
                    }
                    currentArray = currentArray.arrayType()
                }
            }

        solutionClass.typeParameters.forEach {
            check(it.bounds.size == 1 && it.bounds.first() == Object::class.java) {
                "No support for generic type bounds yet"
            }
            currentGenerators[it] = { random, cloner ->
                ObjectGenerator(random, cloner)
            }
        }
        typeGenerators = currentGenerators.toMap()
    }

    // Check to make sure we can generate all needed parameters
    init {
        executables.filter { it in typesNeeded }.forEach { executable ->
            TypeParameterGenerator(executable.parameters, typeGenerators, cloner = Cloner.shared())
        }
    }

    fun get(
        random: Random = Random,
        cloner: Cloner,
        settings: Settings,
        typeGeneratorOverrides: Map<Type, TypeGeneratorGenerator>? = null,
        forExecutables: Set<Executable> = executables
    ): Generators {
        val typeGeneratorsWithOverrides = typeGenerators.toMutableMap().also {
            it.putAll(typeGeneratorOverrides ?: mapOf())
        }
        return forExecutables.associate { executable ->
            if (executable.parameters.isEmpty()) {
                executable to EmptyParameterMethodGenerator()
            } else {
                val parameterGenerator = { random: Random, cloner: Cloner ->
                    TypeParameterGenerator(executable.parameters, typeGeneratorsWithOverrides, random, cloner)
                }
                executable to (
                    methodParameterGenerators[executable]?.generate(
                        parameterGenerator,
                        settings,
                        random,
                        cloner
                    ) ?: error("Didn't find a method parameter generator that should exist")
                    )
            }
        }
            .let {
                Generators(it)
            }
    }
}

class Generators(private val map: Map<Executable, ExecutableGenerator>) : Map<Executable, ExecutableGenerator> by map

interface ExecutableGenerator {
    val fixed: List<Parameters>
    fun random(complexity: Complexity, runner: TestRunner): Parameters
    fun generate(runner: TestRunner): Parameters
    fun next()
    fun prev()
}

class MethodParametersGeneratorGenerator(val target: Executable, val solution: Class<*>) {
    val fixedParameters: Collection<ParameterGroup>?
    var fixedParametersField: Field? = null
    val randomParameters: Method?
    val notNullParameters = target.parameters.map { it.isNotNull() }

    init {
        val parameterTypes = target.genericParameterTypes.map { type -> type as Type }.toTypedArray()
        fixedParameters = solution.declaredFields
            .filter { field -> field.isFixedParameters() }
            .filter { field ->
                FixedParameters.validate(field, solution).compareBoxed(parameterTypes)
            }.filter { field ->
                field.getFixedFieldParametersName().let {
                    if (it.isNotBlank()) {
                        it == "*" || target.name == it
                    } else {
                        true
                    }
                }
            }.also {
                check(it.size <= 1) {
                    "Multiple @${FixedParameters.name} annotations match method ${target.name}"
                }
            }.firstOrNull()?.let { field ->
                fixedParametersField = field
                val values = field.get(null)
                check(values is Collection<*>) { "@${FixedParameters.name} field does not contain a collection" }
                check(values.isNotEmpty()) { "@${FixedParameters.name} field contains as empty collection" }
                @Suppress("SwallowedException")
                val actualValues = try {
                    values.filterNotNull().forEach { it as ParameterGroup }
                    @Suppress("UNCHECKED_CAST")
                    values as Collection<ParameterGroup>
                } catch (e: ClassCastException) {
                    values.map { One(it) }
                }
                actualValues.forEach { group ->
                    check(group.toList().none { it != null && it::class.java == solution }) {
                        """
        |@${FixedParameters.name} field should not contain receiver objects, since this will not work as you expect.
        |Target the constructor with @${FixedParameters.name} if you need to create receivers."""
                            .trimMargin().trim()
                    }
                    val cloner = Cloner.shared()
                    val solutionParameters = group.deepCopy(cloner)
                    val submissionParameters = group.deepCopy(cloner)
                    check(solutionParameters !== submissionParameters) {
                        "@${FixedParameters.name} field produces referentially equal copies"
                    }
                    check(solutionParameters == submissionParameters) {
                        "@${FixedParameters.name} field does not produce equal copies"
                    }
                }
                actualValues
            }
        randomParameters = solution.declaredMethods
            .filter { method -> method.isRandomParameters() }
            .filter { method -> RandomParameters.validate(method, solution).compareBoxed(parameterTypes) }
            .filter { method ->
                method.getRandomParametersMethodName().let {
                    if (it.isNotBlank()) {
                        it == "*" || target.name == it
                    } else {
                        true
                    }
                }
            }
            .also {
                check(it.size <= 1) {
                    "Multiple @${RandomParameters.name} annotations match method ${target.name}"
                }
            }.firstOrNull()
    }

    val needsParameterGenerator = fixedParameters == null || randomParameters == null
    fun generate(
        parametersGenerator: ParametersGeneratorGenerator?,
        settings: Settings,
        random: Random = Random,
        cloner: Cloner
    ) = ConfiguredParametersGenerator(
        parametersGenerator,
        settings,
        random,
        cloner,
        fixedParameters,
        randomParameters,
        notNullParameters
    )
}

@Suppress("LongParameterList")
class ConfiguredParametersGenerator(
    parametersGenerator: ParametersGeneratorGenerator?,
    private val settings: Settings,
    private val random: Random = Random,
    private val cloner: Cloner,
    overrideFixed: Collection<ParameterGroup>?,
    private val overrideRandom: Method?,
    private val notNullParameters: List<Boolean>
) : ExecutableGenerator {
    private val generator = if (overrideFixed != null && overrideRandom != null) {
        null
    } else {
        check(parametersGenerator != null) { "Parameter generator required but not provided" }
        parametersGenerator(random, cloner)
    }

    private fun Collection<ParameterGroup>.toFixedParameters(): List<Parameters> = map {
        Parameters(
            it.deepCopy(cloner).toArray(),
            it.deepCopy(cloner).toArray(),
            it.deepCopy(cloner).toArray(),
            it.deepCopy(cloner).toArray(),
            it.deepCopy(cloner).toArray(),
            Parameters.Type.FIXED_FIELD
        )
    }

    private fun List<Parameters>.trim(count: Int) = if (this.size <= count) {
        this
    } else {
        this.shuffled(random).take(count)
    }

    override val fixed: List<Parameters> by lazy {
        if (overrideFixed != null) {
            overrideFixed.toFixedParameters().also { parameters ->
                check(parameters.none { it.filterNotNullParameters() }) {
                    "@FixedParameters list contains null values for parameters marked as @NotNull"
                }
            }
        } else {
            check(generator != null) { "Automatic parameter generator was unexpectedly null" }
            generator.let {
                it.simple.trim(settings.simpleCount) +
                    it.edge.trim(settings.edgeCount) +
                    it.mixed.trim(settings.mixedCount)
            }.filter { !it.filterNotNullParameters() }.trim(settings.fixedCount)
        }
    }

    private fun Parameters.filterNotNullParameters() = solution.filterIndexed { index, any ->
        notNullParameters[index] && any == null
    }.isNotEmpty()

    private val randomGroup = RandomGroup(random.nextLong())
    private var index = 0
    private var bound: Complexity? = null
    private val complexity = Complexity()
    private var randomStarted = false

    val canShrink: Boolean = if (overrideRandom == null) {
        true
    } else {
        overrideRandom.parameters.size == 2
    }

    @Suppress("TooGenericExceptionCaught")
    private fun getRandom(random: java.util.Random, runner: TestRunner) = try {
        unwrap {
            when (overrideRandom!!.parameters.size) {
                1 -> overrideRandom.invoke(runner.receivers?.solution, random)
                2 -> overrideRandom.invoke(runner.receivers?.solution, complexity.level, random)
                else -> error("Bad argument count for @RandomParameters")
            }.let {
                if (it is ParameterGroup) {
                    it
                } else {
                    One(it)
                }
            }
        } as ParameterGroup
    } catch (e: Exception) {
        error("@RandomParameters method threw an exception: $e\n${e.stackTraceToString()}")
    }

    private val randomFastCopy = overrideRandom?.getAnnotation(RandomParameters::class.java)?.fastCopy ?: false

    @Suppress("LongMethod")
    override fun random(complexity: Complexity, runner: TestRunner): Parameters = if (overrideRandom != null) {
        randomGroup.start()

        val solutionParameters = getRandom(randomGroup.random, runner)
        check(solutionParameters.size == notNullParameters.size)
        check(
            solutionParameters.toList()
                .zip(notNullParameters)
                .none { (parameter, notNull) ->
                    parameter == null && notNull
                }
        ) {
            "@RandomParameter method parameter generator returned null for parameter annotated as @NotNull"
        }
        @Suppress("TooGenericExceptionCaught")
        val submissionParameters = try {
            cloneOrCopy(solutionParameters, cloner, randomFastCopy) { getRandom(randomGroup.random, runner) }
        } catch (e: Throwable) {
            if (!randomFastCopy) {
                throw e
            }
            error("Cloning parameters failed. Try disabling fast copy by setting fastCopy = false on @RandomParameters")
        }

        val solutionCopyParameters =
            cloneOrCopy(solutionParameters, cloner, randomFastCopy) { getRandom(randomGroup.random, runner) }
        val submissionCopyParameters =
            cloneOrCopy(solutionParameters, cloner, randomFastCopy) { getRandom(randomGroup.random, runner) }
        val unmodifiedParameters =
            cloneOrCopy(solutionParameters, cloner, randomFastCopy) { getRandom(randomGroup.random, runner) }
        randomGroup.stop()

        setOf(
            solutionParameters,
            submissionParameters,
            solutionCopyParameters,
            submissionCopyParameters,
            unmodifiedParameters
        ).size.also { distinct ->
            check(distinct == 1) {
                "@${RandomParameters.name} did not generate equal parameters ($distinct)"
            }
        }
        Parameters(
            solutionParameters.toArray(),
            submissionParameters.toArray(),
            solutionCopyParameters.toArray(),
            submissionCopyParameters.toArray(),
            unmodifiedParameters.toArray(),
            Parameters.Type.RANDOM_METHOD,
            complexity
        )
    } else {
        check(generator != null) { "Automatic parameter generator was unexpectedly null" }
        generator.random(complexity, runner).also {
            check(it.solution.size == notNullParameters.size)
            check(
                it.solution.toList()
                    .zip(notNullParameters)
                    .none { (parameter, notNull) ->
                        parameter == null && notNull
                    }
            ) {
                "Built-in method parameter generator returned null for parameter annotated as @NotNull"
            }
        }
    }

    override fun generate(runner: TestRunner): Parameters {
        return if (index in fixed.indices) {
            fixed[index]
        } else {
            random(bound ?: complexity, runner).also { randomStarted = true }
        }.also {
            index++
        }
    }

    override fun next() {
        if (randomStarted && canShrink) {
            complexity.next()
        }
    }

    override fun prev() {
        if (randomStarted && canShrink) {
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
    override val fixed = listOf(
        Parameters(arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf(), Parameters.Type.EMPTY)
    )

    override fun random(complexity: Complexity, runner: TestRunner) = fixed.first()

    override fun prev() {}
    override fun next() {}
    override fun generate(runner: TestRunner): Parameters = fixed.first()
}

class TypeParameterGenerator(
    parameters: Array<out Parameter>,
    generators: Map<Type, TypeGeneratorGenerator> = mapOf(),
    private val random: Random = Random,
    private val cloner: Cloner
) {
    private val parameterGenerators = parameters.map {
        val type = it.parameterizedType
        val generator = if (type in generators) {
            generators[type]
        } else {
            Defaults[type]
        }
        generator?.invoke(random, cloner) ?: error(
            "Couldn't find generator for parameter ${it.name} with type ${it.parameterizedType.typeName}"
        )
    }

    private fun List<Set<Value<*>>>.combine(type: Parameters.Type) = product().shuffled(random).map { list ->
        list.map {
            check(it is Value<*>) { "Didn't find the right type in our parameter list" }
            ParameterValues(it.solution, it.submission, it.solutionCopy, it.submissionCopy, it.unmodifiedCopy)
        }.unzip().let { (solution, submission, solutionCopy, submissionCopy, unmodifiedCopy) ->
            Parameters(
                solution.toTypedArray(),
                submission.toTypedArray(),
                solutionCopy.toTypedArray(),
                submissionCopy.toTypedArray(),
                unmodifiedCopy.toTypedArray(),
                type
            )
        }
    }

    val simple by lazy {
        parameterGenerators.map { it.simple }.combine(Parameters.Type.SIMPLE)
    }
    val edge by lazy {
        parameterGenerators.map { it.edge }.combine(Parameters.Type.EDGE)
    }
    val mixed by lazy {
        parameterGenerators.map { it.simple + it.edge }.combine(Parameters.Type.MIXED).filter {
            it !in simple && it !in edge
        }
    }

    fun random(complexity: Complexity, runner: TestRunner?): Parameters {
        return parameterGenerators.map { it.random(complexity, runner) }.map {
            ParameterValues(it.solution, it.submission, it.solutionCopy, it.submissionCopy, it.unmodifiedCopy)
        }.unzip().let { (solution, submission, solutionCopy, submissionCopy, unmodifiedCopy) ->
            Parameters(
                solution.toTypedArray(),
                submission.toTypedArray(),
                solutionCopy.toTypedArray(),
                submissionCopy.toTypedArray(),
                unmodifiedCopy.toTypedArray(),
                Parameters.Type.RANDOM,
                complexity
            )
        }
    }
}

fun List<*>.product() = fold(listOf(listOf<Any?>())) { acc, set ->
    require(set is Collection<*>) { "Error computing product" }
    acc.flatMap { list -> set.map { element -> list + element } }
}.toSet()

data class ParameterValues<T>(
    val solution: T,
    val submission: T,
    val solutionCopy: T,
    val submissionCopy: T,
    val unmodifiedCopy: T
)

@Suppress("MagicNumber")
fun <E> List<ParameterValues<E>>.unzip(): List<List<E>> {
    @Suppress("RemoveExplicitTypeArguments")
    return fold(listOf(ArrayList<E>(), ArrayList<E>(), ArrayList<E>(), ArrayList<E>(), ArrayList<E>())) { r, i ->
        r[0].add(i.solution)
        r[1].add(i.submission)
        r[2].add(i.solutionCopy)
        r[3].add(i.submissionCopy)
        r[4].add(i.unmodifiedCopy)
        r
    }
}