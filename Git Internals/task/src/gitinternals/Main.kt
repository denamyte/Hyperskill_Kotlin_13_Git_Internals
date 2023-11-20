package gitinternals

fun main() {
    val root = getInput("Enter .git directory location:")
    val cmd = getInput("Enter command:")
    when (cmd) {
        "list-branches" -> println(ListBranches(root))
        "log" -> {
            val branchName = getInput("Enter branch name:")
            println(Log(root, branchName))
        }
        "cat-file" -> {
            val hash = getInput("Enter git object hash:")
            println(GitObject.factory(root, hash))
        }
    }
}

fun getInput(text: String): String {
    println(text)
    return readln()
}
