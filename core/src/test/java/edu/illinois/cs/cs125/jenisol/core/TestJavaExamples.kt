package edu.illinois.cs.cs125.jenisol.core

import examples.java.receiver.withconstructor.Correct
import io.kotlintest.specs.StringSpec

@Suppress("RemoveSingleExpressionStringTemplate")
class TestJavaExamples : StringSpec({
    examples.java.noreceiver.noarguments.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.intargument.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.twointarguments.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.prints.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.submissionprints.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.edgetype.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.simpletype.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.randomtype.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.returnsarray.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.onefixedparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.twofixedparameters.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.onerandomparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.onegeneratedparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.withverifier.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.withprimitiveverifier.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.withparameterverifier.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.intarrayargument.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.generatedparameterarrays.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.noarguments.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.withinitialization.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.countarrayequals.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.noreceiver.factoryconstructor.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.receiverparameter.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
    examples.java.receiver.constructorthrows.Correct::class.java.also {
        "${it.testName()}" { it.test() }
    }
})
