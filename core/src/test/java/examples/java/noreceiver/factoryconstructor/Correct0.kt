package examples.java.noreceiver.factoryconstructor

class Correct0 private constructor(val value: Int) {
    companion object {
        fun value() = Correct0(0)
    }
}
