package examples.java.receiver.withstatic

class Correct0(private val message: String?) {
    init {
        require(message != null)
    }
    companion object {
        fun doubleMessage(correct0: Correct0) = correct0.message + correct0.message
    }
}
