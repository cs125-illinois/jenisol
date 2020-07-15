package edu.illinois.cs.cs125.jenisol.core

import io.kotlintest.specs.StringSpec

@Suppress("RemoveSingleExpressionStringTemplate")
class TestJavaExamples : StringSpec({
    examples.java.noreceiver.single.noarguments.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.intargument.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.twointarguments.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.prints.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.submissionprints.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.edgetype.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.simpletype.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.randomtype.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.returnsarray.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.onefixedparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.twofixedparameters.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.onerandomparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.onegeneratedparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.withverifier.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.withprimitiveverifier.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.withparameterverifier.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.intarrayargument.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.generatedparameterarrays.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.single.noarguments.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.single.withconstructor.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.single.withinitialization.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.single.countarrayequals.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.single.receiverparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.single.constructorthrows.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
})

