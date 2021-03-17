package examples.java.receiver.compareto

class Correct0(private val string: String) : Comparable<Correct0> {
    override fun compareTo(other: Correct0): Int {
        return string.length.compareTo(other.string.length)
    }
}
