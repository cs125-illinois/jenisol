package examples.java.receiver.kotlinnonnullableparameter

class Incorrect1(val value: String?) {
    init {
        @Suppress("UNUSED_VARIABLE")
        val unused = value!!.length
    }
}