package examples.java.noreceiver.assertvrequire

fun positive(value: Int): Int {
    require(value > 0)
    return value
}
