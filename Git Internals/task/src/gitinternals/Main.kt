package gitinternals

fun main() {
    val root = getInput("Enter .git directory location:")
    val cmd = getInput("Enter command:")
    when (cmd) {
        "cat-file" -> {
            val hash = getInput("Enter git object hash:")
            println(GitObject.factory(root, hash))
        }
        "commit-tree" -> {
            val hash = getInput("Enter commit-hash:")
            println(CommitTree(root, hash))
        }
        "list-branches" -> println(ListBranches(root))
        "log" -> {
            val branchName = getInput("Enter branch name:")
            println(Log(root, branchName))
        }
    }
}

fun getInput(text: String): String {
    println(text)
    return readln()
}
