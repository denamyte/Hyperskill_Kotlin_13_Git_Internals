package gitinternals

import java.io.FileInputStream
import java.util.zip.InflaterInputStream

private val Int.b get() = this.toByte()
private val BYTE_CHANGE = mapOf(0x0.b to 0xA.b)  // NULL to \n

fun main() {
    println("Enter git object location:")
    val path = readln()

    FileInputStream(path).use {
        InflaterInputStream(it).use { iis ->
            iis.readAllBytes()
                .map { b -> BYTE_CHANGE.getOrDefault(b, b) }
                .toByteArray()
                .let { bAr -> String(bAr) }
                .let(::print)
        }
    }
}
