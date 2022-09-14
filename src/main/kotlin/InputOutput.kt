package edu.illinois.cs.cs125.jenisol.core

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

typealias CaptureOutputControlInput = (stdin: List<String>, run: () -> Any?) -> CapturedResult

data class CapturedResult(
    val returned: Any?,
    val threw: Throwable?,
    val stdout: String,
    val stderr: String,
    val stdin: String,
    val interleavedInputOutput: String,
    val truncatedLines: Int,
    val tag: Any? = null
)

private val outputLock = ReentrantLock()

fun defaultCaptureOutputControlInput(stdin: List<String> = listOf(), run: () -> Any?): CapturedResult =
    outputLock.withLock {
        val ioBytes = mutableListOf<Byte>()

        val originalStdin = System.`in`
        val stdinBytes = mutableListOf<Byte>()
        val divertedStdin = object : InputStream() {
            private val inputs = stdin.map { "$it\n".toByteArray() }
            private var index = 0
            private var usedIndex = false
            private var stream = ByteArrayInputStream(inputs.getOrNull(index) ?: "".toByteArray())

            fun bump() {
                if (usedIndex) {
                    index++
                    stream = ByteArrayInputStream(inputs.getOrNull(index) ?: "".toByteArray())
                    usedIndex = false
                }
            }

            override fun read(): Int {
                usedIndex = true
                val b = stream.read()
                if (b != -1) {
                    ioBytes += b.toByte()
                    stdinBytes += b.toByte()
                }
                return b
            }
        }
        System.setIn(divertedStdin)

        val originalStdout = System.out
        val divertedStdout = object : OutputStream() {
            val stream = ByteArrayOutputStream()
            override fun write(b: Int) {
                stream.write(b)
                ioBytes += b.toByte()
                divertedStdin.bump()
            }
        }
        System.setOut(PrintStream(divertedStdout))

        val originalStderr = System.err
        val divertedStderr = object : OutputStream() {
            val stream = ByteArrayOutputStream()
            override fun write(b: Int) {
                stream.write(b)
                ioBytes += b.toByte()
            }
        }
        System.setErr(PrintStream(divertedStderr))

        @Suppress("TooGenericExceptionCaught")
        val result: Pair<Any?, Throwable?> = try {
            Pair(run(), null)
        } catch (e: ThreadDeath) {
            throw e
        } catch (e: Throwable) {
            Pair(null, e)
        }

        System.setOut(originalStdout)
        System.setErr(originalStderr)
        System.setIn(originalStdin)

        return CapturedResult(
            result.first,
            result.second,
            divertedStdout.stream.toString(),
            divertedStderr.stream.toString(),
            stdinBytes.toByteArray().decodeToString(),
            ioBytes.toByteArray().decodeToString(),
            0
        )
    }