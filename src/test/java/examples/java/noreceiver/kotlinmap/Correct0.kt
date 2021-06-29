package examples.java.noreceiver.kotlinmap

fun sumValues(map: Map<String, Int>): Int {
    var sum = 0
    for (value in map.values) {
        sum += value
    }
    return sum
}