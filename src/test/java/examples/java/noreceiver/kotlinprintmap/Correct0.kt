package examples.java.noreceiver.kotlinprintmap

fun printValues(map: Map<String, Int>) {
    for (value in map.values) {
        println(value)
    }
}