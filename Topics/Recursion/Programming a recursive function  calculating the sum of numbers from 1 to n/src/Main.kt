fun sumRecursive(n: Int): Int = if (n == 1) 1 else n + sumRecursive(n - 1)

fun main() = print(sumRecursive(readln().toInt()))