package gitinternals

import java.io.FileInputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.InflaterInputStream

private val Int.b get() = this.toByte()
private val BYTE_CHANGE = mapOf(0x0.b to 0xA.b)  // NULL to \n
private val DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx")

abstract class GitObject(val text: List<String>) {
    abstract val type: String
    abstract val body: String

    companion object {
        fun factory(path: String): GitObject? =
            readFile(path).let { list ->
                when (list[0].takeWhile { it != ' ' }) {
                    "blob" -> Blob(list)
                    "commit" -> Commit(list)
                    else -> null
                }
            }

        private fun readFile(path: String): List<String> =
            InflaterInputStream(FileInputStream(path)).use { iis ->
                iis.readAllBytes()
                    .map { b -> BYTE_CHANGE.getOrDefault(b, b) }
                    .toByteArray()
                    .let { bAr -> String(bAr).split("\n") }
            }
    }

    override fun toString() = "*$type*\n$body"

    class Blob(list: List<String>) : GitObject(list) {
        override val type = "BLOB"
        override val body = this.text.drop(1).joinToString("\n")
    }

    class Commit(list: List<String>) : GitObject(list) {
        override val type = "COMMIT"
        private var tree: String = ""
        private var parents = mutableListOf<String>()
        private var authors = mutableListOf<Author>()
        private var commitMsg = ""
        override val body: String
            get() = buildString {
                append(tree)
                if (parents.isNotEmpty())
                    append("\nparents: ").append(parents.joinToString(" | "))
                append('\n').append(authors[0])
                append('\n').append(authors[1])
                append("\ncommit message:\n")
                append(commitMsg)
            }

        init {
            with(text.drop(1).toMutableList()) {
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
            private val role: String
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
                "$role: $name $email $type timestamp: $dateTime"
        }
    }
}