package examples.java.noreceiver.kotlinsynthetic

fun addOne(value: Int): Int {
    val transform = ::plus1 // Private method reference creates public synthetic accessor
    return transform(value)
}

private fun plus1(x: Int): Int {
    return x + 1
}
