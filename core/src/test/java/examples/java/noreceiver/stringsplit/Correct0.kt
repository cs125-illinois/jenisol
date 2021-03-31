package examples.java.noreceiver.stringsplit

class Correct0 private constructor() {
    companion object {
        fun part(string: String): String {
            return string.split(" ")[1]
        }
    }
}
