package gitinternals

class Log(root: String, branchName: String): RefsHeads(root) {
    private val commits = mutableListOf<GitObject.Commit>()
    private val mergedCommits = mutableSetOf<String>()

    init {
        val branch = branchFiles.find { it.name == branchName }
            ?: throw IllegalArgumentException()
        var hash: String? = branch.readLines()[0]
        while (hash != null) {
            val commit = GitObject.getCommit(root, hash)
            commits.add(commit)
            val par = commit.parents
            if (par.size == 2)
                GitObject.getCommit(root, par[1]).run {
                    commits.add(this)
                    mergedCommits.add(par[1])
                }
            hash = if (par.isNotEmpty()) par[0] else null
        }
    }

    override fun toString(): String {
        return commits.joinToString("\n") {
            with(it) {
                val mergedFlag = if (hash in mergedCommits) " (merged)" else ""
                buildString {
                    append("Commit: $hash$mergedFlag").append('\n')
                    append("${authors.last()}").append('\n')
                    append("$commitMsg")
                }
            }
        }
    }
}