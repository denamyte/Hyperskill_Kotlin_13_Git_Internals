package gitinternals

import kotlin.IllegalArgumentException

class CommitTree(private val root: String, commitHash: String) {

    private val paths = mutableListOf<String>()

    init {
        val commit = GitObject.factory(root, commitHash) as? GitObject.Commit
            ?: throw IllegalArgumentException("Not a commit hash!")
        resolveTree(commit.treeHash, "")
    }

    private fun resolveTree(hash: String, prefix: String) {
        val obj = GitObject.factory(root, hash) ?: throw IllegalArgumentException("no object by the hash!")
        when (obj.type) {
            "TREE" -> for (i in (obj as GitObject.Tree).items) {
                resolveTree(
                    i.hash,
                    if (prefix.isEmpty()) i.name else "$prefix/${i.name}"
                )
            }
            else -> paths.add(prefix)
        }
    }

    override fun toString() = paths.joinToString("\n")
}