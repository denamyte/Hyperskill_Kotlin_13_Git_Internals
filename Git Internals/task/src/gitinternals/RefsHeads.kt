package gitinternals

import java.io.File

open class RefsHeads(root: String) {
    protected val path: File = File(root)
    protected val branchFiles: List<File> = path.resolve("refs/heads")
        .walk()
        .filter { it.isFile }
        .sortedBy { it.name }
        .toList()
}