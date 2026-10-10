package com.infinity.linkleap.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class FileSender {
    suspend fun sendFile(ip: String, port: Int, file: File, onProgress: (Long, Long) -> Unit) = withContext(Dispatchers.IO) {
        val socket = Socket(ip, port)
        val outputStream: OutputStream = socket.getOutputStream()
        val fileSize = file.length()
        val buffer = ByteArray(4096)
        var totalSent = 0L
        FileInputStream(file).use { fis ->
            var bytes = fis.read(buffer)
            while (bytes != -1) {
                outputStream.write(buffer, 0, bytes)
                totalSent += bytes
                onProgress(totalSent, fileSize)
                bytes = fis.read(buffer)
            }
        }
        outputStream.flush()
        socket.close()
    }
}