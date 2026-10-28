package com.infinity.linkleap.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object DownloadStore {
    private const val SUB_DIR = "LinkLeap"

    fun openOutput(context: Context, fileName: String): OutputStream {
        val safe = fileName.toSafeFileName()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val ext = safe.substringAfterLast('.', "").lowercase()
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                ?: if (ext == "apk") "application/vnd.android.package-archive"
                else "application/octet-stream"

            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, safe)
                put(MediaStore.Downloads.MIME_TYPE, mime)
                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/$SUB_DIR"
                )
            }
            val uri = context.contentResolver
                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Gagal membuat file di Downloads")
            return context.contentResolver.openOutputStream(uri)
                ?: throw IOException("Gagal membuka file tujuan")
        }

        // Android 7–9: butuh izin WRITE_EXTERNAL_STORAGE (runtime)
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            SUB_DIR
        ).apply { mkdirs() }
        return FileOutputStream(File(dir, safe))
    }
}