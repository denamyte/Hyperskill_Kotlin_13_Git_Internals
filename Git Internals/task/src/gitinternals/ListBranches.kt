package gitinternals

class ListBranches(root: String) : RefsHeads(root) {
    private var selectedBranch: String? = null

    init {
        path.resolve("HEAD").let { head ->
            if (head.exists()) {
                val text = head.readLines().first()
                if (text.isNotEmpty()) {
                    val index = text.indexOfLast { it == '/' }
                    if (index != -1) {
                        selectedBranch = text.substring(index + 1)
                    }
                }
            }
        }
    }

    override fun toString() = branchFiles
        .map { it.name }
        .joinToString("\n") {
            "${if (it == selectedBranch) '*' else ' '} $it"
        }
}
