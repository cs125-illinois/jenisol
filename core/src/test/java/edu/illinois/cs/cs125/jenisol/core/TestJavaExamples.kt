package edu.illinois.cs.cs125.jenisol.core

import io.kotest.core.spec.style.StringSpec

class TestJavaExamples : StringSpec(
    {
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
        examples.java.noreceiver.randomtypetwoparameters.Correct::class.java.also {
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
        examples.java.receiver.withinitialization.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.countarrayequals1d.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.countarrayequals2d.Correct::class.java.also {
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
        examples.java.receiver.equals.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.equalswithtwofields.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.missinginheritance.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.packageprivatemethod.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.strictoutput.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.skiptest.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.filtertest.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.boundcomplexity.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
    }
)
