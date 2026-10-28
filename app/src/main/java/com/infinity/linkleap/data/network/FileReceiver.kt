package com.infinity.linkleap.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.File
import java.io.OutputStream
import java.net.ServerSocket

class FileReceiver {

    suspend fun receive(
        serverSocket: ServerSocket,
        openOutput: (fileName: String) -> OutputStream,
        onProgress: (received: Long, total: Long) -> Unit
    ): List<String> = withContext(Dispatchers.IO) {
        val names = mutableListOf<String>()

        serverSocket.use { server ->
            server.accept().use { client ->
                val input = DataInputStream(BufferedInputStream(client.getInputStream(), BUFFER_SIZE))

                val count = input.readInt()
                val totalBytes = input.readLong()
                var received = 0L
                val buffer = ByteArray(BUFFER_SIZE)

                repeat(count) {
                    // File(...).name mencegah path traversal ("../../x")
                    val name = File(input.readUTF()).name
                    var remaining = input.readLong()

                    openOutput(name).use { out ->
                        while (remaining > 0) {
                            ensureActive()
                            val toRead = minOf(buffer.size.toLong(), remaining).toInt()
                            val n = input.read(buffer, 0, toRead)
                            if (n == -1) throw EOFException("Koneksi terputus saat menerima $name")
                            out.write(buffer, 0, n)
                            remaining -= n
                            received += n
                            onProgress(received, totalBytes)
                        }
                    }
                    names += name
                }
            }
        }
        names
    }

    private companion object {
        const val BUFFER_SIZE = 64 * 1024
    }
}