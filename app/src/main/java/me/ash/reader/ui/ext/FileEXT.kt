package me.ash.reader.ui.ext

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Convert [InputStream] to [String].
 */
fun InputStream.readString(): String =
    BufferedReader(InputStreamReader(this)).useLines { lines ->
        val results = StringBuilder()
        lines.forEach { results.append(it) }
        results.toString()
    }

/**
 * Delete a file or directory.
 */
fun File.del() {
    if (this.isFile) {
        delete()
        return
    }
    this.listFiles()?.forEach { it.del() }
    delete()
}

fun File.mkDir() {
    val dirArray = this.absolutePath.split("/".toRegex())
    var pathTemp = ""
    for (i in 1 until dirArray.size) {
        pathTemp = "$pathTemp/${dirArray[i]}"
        val newF = File("${dirArray[0]}$pathTemp")
        if (!newF.exists()) newF.mkdir()
    }
}

fun ByteArray.isProbableProtobuf(): Boolean =
    if (size < 2) false
    else get(0) == 0x0a.toByte() && get(1) == 0x16.toByte()
