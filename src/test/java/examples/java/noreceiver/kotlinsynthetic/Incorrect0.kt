package examples.java.noreceiver.kotlinsynthetic

@Suppress("UtilityClassWithPublicConstructor")
class Incorrect0 {
    companion object {
        @JvmStatic // Can't have two loose addOne Kotlin functions in the same package
        fun addOne(value: Int): Int {
            val transform = ::plus2
            return transform(value)
        }

        private fun plus2(x: Int): Int {
            return x + 2
        }
    }
}