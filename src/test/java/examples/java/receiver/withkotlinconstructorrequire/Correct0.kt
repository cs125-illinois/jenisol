package examples.java.receiver.withkotlinconstructorrequire

class Correct0(val value: Int) {
    init {
        require(value > 0)
    }
}