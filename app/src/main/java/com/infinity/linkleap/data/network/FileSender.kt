package com.infinity.linkleap.data.network

import com.infinity.linkleap.data.model.SendItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.net.Socket

class FileSender {

    suspend fun sendFiles(
        ip: String,
        port: Int,
        items: List<SendItem>,
        onProgress: (sent: Long, total: Long) -> Unit
    ) = withContext(Dispatchers.IO) {
        val totalBytes = items.sumOf { File(it.path).length() }
        var totalSent = 0L

        Socket().use { socket ->
            socket.connect(InetSocketAddress(ip, port), 10_000)
            val out = DataOutputStream(BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE))

            out.writeInt(items.size)
            out.writeLong(totalBytes)

            val buffer = ByteArray(BUFFER_SIZE)
            for (item in items) {
                val file = File(item.path)
                out.writeUTF(item.name)
                out.writeLong(file.length())

                FileInputStream(file).use { input ->
                    var n = input.read(buffer)
                    while (n != -1) {
                        ensureActive()
                        out.write(buffer, 0, n)
                        totalSent += n
                        onProgress(totalSent, totalBytes)
                        n = input.read(buffer)
                    }
                }
            }
            out.flush()
        }
    }

    private companion object {
        const val BUFFER_SIZE = 64 * 1024
    }
}