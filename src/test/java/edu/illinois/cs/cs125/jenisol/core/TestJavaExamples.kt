package edu.illinois.cs.cs125.jenisol.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

@ExperimentalTime
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
        examples.java.noreceiver.stringequality.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.publicfield.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.designonly.fieldchecks.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.doublecomparison.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.asserts.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.bothonly.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.customtype.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.custominstancecheck.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.multipleverifiers.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.doublecomparison.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.badstatic.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.arrayfromtypegenerator.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.factoryconstructor.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.privateconstructorexample.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.arrayfactory.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.superclassdesign.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.interfacedesign.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.dividebyzero.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.listsum.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.mapreturn.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.lambdaargument.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.sourcechecker.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.withgenericverifier.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.bothwithanothermethod.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.simplegenerics.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.voidverify.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.singleargument.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.setteronly.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.assertvrequire.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.doubleassert.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.doubleassert.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.equalsthreefields.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.withstatic.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.filterreceivers.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.equalswithtwofieldsandfilter.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.kotlindataclass.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.kotlinnonnullableparameter.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.completethreefields.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.compareto.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.stringsplit.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.countmap.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.kotlinrawtypes.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.customcompare.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.customcontainercompare.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.customarraycompare.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.simpletypemethod.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.edgetypemethod.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.withexternalverifier.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.randomparameternocomplexity.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.notnullannotation.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.onewithoutwrapper.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.actuallystatic.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.twostringequality.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.printvprintln.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.parameterdisambiguation.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.noreceiver.parametermatchstar.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.parametermatchinitializer.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.nonstaticparametergenerator.Correct::class.java.also {
            "${it.testName()}" { it.test() }
        }
        examples.java.receiver.timeouttest.Correct::class.java.also {
            "${it.testName()}" {
                val runnable = object : Runnable {
                    var results: TestResults? = null
                    override fun run() {
                        results = solution(it).submission(it).test()
                    }
                }
                withContext(Dispatchers.Default) {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    Thread(runnable).apply {
                        start()
                        join(2048)
                        interrupt()
                        join(1024)
                    }
                }
                runnable.results shouldNotBe null
            }
        }
        examples.java.noreceiver.intargument.Correct::class.java.also {
            "${it.testName()} repeatability" {
                it.testingClasses().apply {
                    solution(primarySolution).apply {
                        val first = submission(primarySolution).test(Settings(seed = 0))
                        val second = submission(primarySolution).test(Settings(seed = 0))
                        first.size shouldBe second.size
                        first.forEachIndexed { index, firstResult ->
                            val secondResult = second[index]
                            firstResult.parameters.toArray()
                                .contentDeepEquals(secondResult.parameters.toArray()) shouldBe true
                        }
                    }
                }
            }
        }
    }
)