fun f(n: Int): Int =
    when (n) {
        0 -> 4
        -1 -> 1
        else -> f(n - 1) / 2 + 2 * f(n - 2)
    }

fun main() = print(f(readln().toInt()))