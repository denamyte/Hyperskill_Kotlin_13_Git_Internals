package gitinternals

fun main() {
    println("Enter .git directory location:")
    val root = readln()
    println("Enter command:")
    when (readln()) {
        "list-branches" -> println(ListBranches(root))
        "cat-file" -> {
            println("Enter git object hash:")
            val hash = readln()
            val path = makeHashPath(root, hash)
            println(GitObject.factory(path))
        }
    }

}

fun makeHashPath(dir: String, hash: String) =
    with(hash) { "$dir/objects/${take(2)}/${substring(2)}" }
