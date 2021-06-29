package examples.java.noreceiver.arrayreturn

class Correct0 {
    fun biggerTwo(first: IntArray?, second: IntArray?): IntArray {
        val aSum = first!![0] + first[1]
        val bSum = second!![0] + second[1]
        return if (bSum > aSum) {
            second
        } else {
            first
        }
    }
}