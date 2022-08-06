package edu.illinois.cs.cs125.jenisol.core

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

typealias CaptureOutputControlInput = (stdin: String, run: () -> Any?) -> CapturedResult

data class CapturedResult(
    val returned: Any?,
    val threw: Throwable?,
    val stdout: String,
    val stderr: String,
    val stdin: String,
    val tag: Any? = null
)

private val outputLock = ReentrantLock()
fun defaultCaptureOutputControlInput(stdin: String = "", run: () -> Any?): CapturedResult = outputLock.withLock {
    val original = Triple(System.out, System.err, System.`in`)
    val diverted = Pair(ByteArrayOutputStream(), ByteArrayOutputStream()).also {
        System.setOut(PrintStream(it.first))
        System.setErr(PrintStream(it.second))
    }
    System.setIn(ByteArrayInputStream(stdin.toByteArray()))

    @Suppress("TooGenericExceptionCaught")
    val result: Pair<Any?, Throwable?> = try {
        Pair(run(), null)
    } catch (e: ThreadDeath) {
        throw e
    } catch (e: Throwable) {
        Pair(null, e)
    }

    val (originalStdout, originalStderr, originalStdin) = original
    System.setOut(originalStdout)
    System.setErr(originalStderr)
    System.setIn(originalStdin)

    return CapturedResult(result.first, result.second, diverted.first.toString(), diverted.second.toString(), stdin)
}