package gitinternals

import java.io.FileInputStream
import java.util.zip.InflaterInputStream

private val Int.b get() = this.toByte()

fun main() {
    println("Enter .git directory location:")
    val dir = readln()
    println("Enter git object hash:")
    val hash = readln()

    val path = makePath(dir, hash)
    val header = readHeader(path)
    println(formatHeader(header))
}

fun makePath(dir: String, hash: String) =
    with(hash) { "$dir/objects/${take(2)}/${substring(2)}" }


fun readHeader(path: String) =
    InflaterInputStream(FileInputStream(path)).use { iis ->
        iis.readAllBytes().takeWhile { it != 0.b }
            .toByteArray()
            .let { bAr -> String(bAr) }
    }

fun formatHeader(header: String): String {
    val split = header.split(" ")
    return "type:${split[0]} length:${split[1]}"
}
