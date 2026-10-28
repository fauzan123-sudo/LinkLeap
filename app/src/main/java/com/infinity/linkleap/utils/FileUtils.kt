package com.infinity.linkleap.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

fun String.toSafeFileName(): String =
    replace(Regex("[\\\\/:*?\"<>|]"), "_").ifBlank { "file" }

object FileUtils {

    fun getDisplayName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) c.getString(idx)?.let { return it.toSafeFileName() }
                }
            }
        return "file_${System.currentTimeMillis()}"
    }

    /** Salin URI ke cache dengan nama asli. Tiap pemanggilan memakai folder sendiri agar nama tidak bentrok. */
    fun uriToFile(context: Context, uri: Uri): File {
        val dir = File(context.cacheDir, "send_${System.nanoTime()}").apply { mkdirs() }
        val file = File(dir, getDisplayName(context, uri))
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output, 64 * 1024) }
        } ?: throw java.io.IOException("Tidak bisa membuka file")
        return file
    }
}