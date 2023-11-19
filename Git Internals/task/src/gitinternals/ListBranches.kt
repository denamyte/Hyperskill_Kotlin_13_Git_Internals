package gitinternals

import java.io.File

@Suppress("JoinDeclarationAndAssignment")
class ListBranches(root: String) {

    private val path: File = File(root)
    private val branchFiles: List<File>
    private var branch: String? = null

    init {
        branchFiles = this.path.resolve("refs/heads")
            .walk()
            .filter { it.isFile }
            .sortedBy { it.name }
            .toList()
        path.resolve("HEAD").let { head ->
            if (head.exists()) {
                val text = head.readLines().first()
                if (text.isNotEmpty()) {
                    val index = text.indexOfLast { it == '/' }
                    if (index != -1) {
                        branch = text.substring(index + 1)
                    }
                }
            }
        }
    }

    override fun toString() = branchFiles
        .map { it.name }
        .joinToString("\n") {
            "${if (it == branch) '*' else ' '} $it"
        }
}
