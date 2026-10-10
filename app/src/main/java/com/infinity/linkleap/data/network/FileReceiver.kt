package com.infinity.linkleap.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.ServerSocket

class FileReceiver {
    suspend fun startServer(port: Int, outputFile: File, onProgress: (Long) -> Unit) =
        withContext(Dispatchers.IO) {
            val serverSocket = ServerSocket(port)
            val client = serverSocket.accept()
            val inputStream = client.getInputStream()
            val buffer = ByteArray(4096)
            var totalReceived = 0L

            FileOutputStream(outputFile).use { fos ->
                var bytes = inputStream.read(buffer)
                while (bytes != -1) {
                    fos.write(buffer, 0, bytes)
                    totalReceived += bytes
                    onProgress(totalReceived)
                    bytes = inputStream.read(buffer)
                }
            }

            client.close()
            serverSocket.close()
        }
}
