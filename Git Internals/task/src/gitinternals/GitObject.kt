package gitinternals

import java.io.FileInputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.InflaterInputStream

private const val byteNull: Byte = 0
private const val byteSpace = ' '.code.toByte()
private val BYTE_CHANGE = mapOf(byteNull to '\n'.code.toByte())
private val DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx")

abstract class GitObject(val bytes: ByteArray, val hash: String) {
    abstract val type: String
    abstract val body: String

    override fun toString() = "*$type*\n$body"

    companion object {
        fun factory(root: String, hash: String): GitObject? {
            val path = makeHashPath(root, hash)
            val bytes = readFile(path)
            val type = String(bytes.takeWhile { it != byteSpace }.toByteArray())
            return when (type) {
                "blob" -> Blob(bytes, hash)
                "commit" -> Commit(bytes, hash)
                "tree" -> Tree(bytes, hash)
                else -> null
            }
        }

        private fun makeHashPath(root: String, hash: String) =
            with(hash) { "$root/objects/${take(2)}/${drop(2)}" }

        private fun readFile(path: String): ByteArray =
            InflaterInputStream(FileInputStream(path))
                .use { iis -> iis.readAllBytes()!! }

        private fun byteArrayToLines(bytes: ByteArray): List<String> =
            bytes.map { b -> BYTE_CHANGE.getOrDefault(b, b) }
                .toByteArray()
                .let { bAr -> String(bAr).split("\n") }

        private fun bytesToStr(bytes: List<Byte>): String =
            bytes.map { Char(it.toUShort()) }.joinToString("")

        fun getCommit(root: String, hash: String): Commit =
            factory(root, hash).run {
                this as? Commit
                    ?: throw IllegalArgumentException("A commit file required!")
            }
    }

    class Blob(bytes: ByteArray, hash: String) : GitObject(bytes, hash) {
        override val type = "BLOB"
        override val body = byteArrayToLines(bytes).drop(1).joinToString("\n")
    }

    class Commit(bytes: ByteArray, hash: String) : GitObject(bytes, hash) {
        override val type = "COMMIT"
        private var tree: String = ""
        var parents = mutableListOf<String>()
            private set
        var authors = mutableListOf<Author>()
            private set
        var commitMsg: String? = null
            private set
        override val body get() = buildString {
            append(tree)
            if (parents.isNotEmpty())
                append("\nparents: ").append(parents.joinToString(" | "))
            authors.forEach { append('\n').append("${it.role}: $it") }
            append("\ncommit message:\n")
            append(commitMsg)
        }

        init {
            with(byteArrayToLines(bytes).drop(1).toMutableList()) {
                // Parse commit message:
                val emptyIndex = indexOf("")
                if (emptyIndex > -1) {
                    commitMsg = drop(emptyIndex + 1).joinToString("\n")
                    while (lastIndex >= emptyIndex) removeAt(lastIndex)
                }
                // Parse the rest:
                forEach { line ->
                    when {
                        line.startsWith("tree") -> tree = line.replaceBefore(" ", "tree:")
                        line.startsWith("parent") -> parents.add(line.replace("parent ", ""))
                        line.startsWith("author") || line.startsWith("committer") -> authors.add(Author(line))
                    }
                }
            }
        }

        class Author(text: String) {
            val role: String
            private val name: String
            private val email: String
            private val type: String get() = if (role == "author") "original" else "commit"
            private val dateTime: String

            init {
                val parts = text.split(" ")
                role = parts[0]
                name = parts[1]
                email = parts[2].trim { it in "<>" }
                val zone = ZoneOffset.of(parts[4])
                val zonedDT = Instant.ofEpochSecond(parts[3].toLong()).atZone(zone)
                dateTime = DT_FORMATTER.format(zonedDT)
            }

            override fun toString(): String =
                "$name $email $type timestamp: $dateTime"
        }
    }

    class Tree(bytes: ByteArray, hash: String) : GitObject(bytes, hash) {
        override val type = "TREE"
        override val body get() = bytes.indexOf(byteNull)
            .let { nullIndex ->  // get rid of the header
                var treeBytes = bytes.drop(nullIndex + 1)
                val lines = mutableListOf<String>()
                while (treeBytes.isNotEmpty()) {
                    var i = treeBytes.indexOf(byteSpace)
                    val permissions = bytesToStr(treeBytes.take(i))
                    treeBytes = treeBytes.drop(i + 1)

                    i = treeBytes.indexOf(byteNull)
                    val name = bytesToStr(treeBytes.take(i))
                    treeBytes = treeBytes.drop(i + 1)

                    val hash = treeBytes.take(20).joinToString("") { "%02x".format(it) }
                    treeBytes = treeBytes.drop(20)

                    lines.add("$permissions $hash $name")
                }
                lines.joinToString("\n")
            }
    }
}