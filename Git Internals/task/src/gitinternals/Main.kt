package gitinternals

fun main() {
    println("Enter .git directory location:")
    val dir = readln()
    println("Enter git object hash:")
    val hash = readln()

    val path = makePath(dir, hash)
    println(GitObject.factory(path))
}

fun makePath(dir: String, hash: String) =
    with(hash) { "$dir/objects/${take(2)}/${substring(2)}" }
