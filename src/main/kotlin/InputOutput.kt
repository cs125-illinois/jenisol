package edu.illinois.cs.cs125.jenisol.core

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.PrintStream
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

typealias CaptureOutput = (run: () -> Any?) -> CapturedResult

data class CapturedResult(
    val returned: Any?,
    val threw: Throwable?,
    val stdout: String,
    val stderr: String,
    val tag: Any? = null
)

private val outputLock = ReentrantLock()
fun defaultCaptureOutput(run: () -> Any?): CapturedResult = outputLock.withLock {
    val original = Pair(System.out, System.err)
    val diverted = Pair(ByteArrayOutputStream(), ByteArrayOutputStream()).also {
        System.setOut(PrintStream(it.first))
        System.setErr(PrintStream(it.second))
    }

    @Suppress("TooGenericExceptionCaught")
    val result: Pair<Any?, Throwable?> = try {
        Pair(run(), null)
    } catch (e: ThreadDeath) {
        throw e
    } catch (e: Throwable) {
        Pair(null, e)
    }
    System.setOut(original.first)
    System.setErr(original.second)
    return CapturedResult(result.first, result.second, diverted.first.toString(), diverted.second.toString())
}

interface InputController {
    fun open(input: String)
    fun close()
}
typealias ControlInput<T> = (run: InputController.() -> T) -> T

class ResettableStringInputStream : InputStream() {
    private var inputStream = ByteArrayInputStream("".toByteArray())

    var input: String = ""
        set(value) {
            inputStream = ByteArrayInputStream(value.toByteArray())
            field = value
        }

    override fun read() = inputStream.read().also {
        println("$input: read() $it")
    }

    override fun close() {
        println("Close")
        super.close()
    }

    override fun available(): Int {
        return super.available().also {
            println("$input: available() $it")
        }
    }
}

private val inputLock = ReentrantLock()
fun <T> defaultControlInput(run: InputController.() -> T): T = inputLock.withLock {
    val original = System.`in`
    val inputStream = ResettableStringInputStream()
    System.setIn(inputStream)

    val inputController = object : InputController {
        private var open = false
        override fun open(input: String) {
            inputStream.input = input
            open = true
        }

        override fun close() {
            check(open) { "System.in was never opened" }
            System.`in`.close()
            open = false
        }
    }
    return try {
        inputController.run()
    } finally {
        System.setIn(original)
    }
}