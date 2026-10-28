package com.infinity.linkleap.data.repository

import com.infinity.linkleap.data.model.SendItem
import com.infinity.linkleap.data.model.TransferStatus
import com.infinity.linkleap.data.network.FileReceiver
import com.infinity.linkleap.data.network.FileSender
import com.infinity.linkleap.utils.Resource
import com.infinity.linkleap.utils.TransferNotifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import java.io.OutputStream
import java.net.ServerSocket
import javax.inject.Inject

class NetworkRepository @Inject constructor(
    private val sender: FileSender,
    private val receiver: FileReceiver,
    private val notifier: TransferNotifier
) : BaseRepository() {

    private val _transferStatus = MutableStateFlow<TransferStatus>(TransferStatus.Idle)
    val transferStatus: StateFlow<TransferStatus> = _transferStatus.asStateFlow()

    // Pelacak kecepatan
    private var lastTimeNs = 0L
    private var lastBytes = 0L
    private var speedBps = 0.0

    private fun resetTracking() {
        lastTimeNs = 0L
        lastBytes = 0L
        speedBps = 0.0
    }

    private fun updateProgress(isSending: Boolean, sent: Long, total: Long) {
        val now = System.nanoTime()
        if (lastTimeNs == 0L) lastTimeNs = now

        val dt = (now - lastTimeNs) / 1_000_000_000.0
        if (dt >= 0.5) {
            val instant = (sent - lastBytes) / dt
            speedBps = if (speedBps == 0.0) instant else speedBps * 0.7 + instant * 0.3
            lastTimeNs = now
            lastBytes = sent
        }

        val speed = speedBps.toLong()
        val eta = if (speed > 0) (total - sent) / speed else -1L

        _transferStatus.value = TransferStatus.Progress(sent, total, speed, eta)
        notifier.showProgress(isSending, sent, total, speed, eta)
    }

    fun sendFiles(ip: String, port: Int, items: List<SendItem>): Flow<Resource<String>> =
        safeNetworkTransfer(onProgress = { updateProgress(true, it.sent, it.total) }) { progress ->
            resetTracking()
            _transferStatus.value = TransferStatus.Connecting
            sender.sendFiles(ip, port, items, progress)
            _transferStatus.value = TransferStatus.Completed
            "${items.size} file terkirim"
        }.track(isSending = true) { it }

    fun receiveFiles(
        serverSocket: ServerSocket,
        openOutput: (String) -> OutputStream
    ): Flow<Resource<List<String>>> =
        safeNetworkTransfer(onProgress = { updateProgress(false, it.sent, it.total) }) { progress ->
            resetTracking()
            _transferStatus.value = TransferStatus.Connecting // menunggu pengirim
            val names = receiver.receive(serverSocket, openOutput, progress)
            _transferStatus.value = TransferStatus.Completed
            names
        }.track(isSending = false) { "${it.size} file tersimpan di Download/LinkLeap" }

    fun resetStatus() {
        _transferStatus.value = TransferStatus.Idle
    }

    /** Catat hasil ke status + notifikasi, dan bersihkan jika dibatalkan. */
    private fun <T> Flow<Resource<T>>.track(
        isSending: Boolean,
        successText: (T) -> String
    ): Flow<Resource<T>> = onEach { result ->
        when (result) {
            is Resource.Success -> result.data?.let { notifier.showDone(isSending, successText(it)) }
            is Resource.Error -> {
                val msg = result.message ?: "Terjadi kesalahan"
                _transferStatus.value = TransferStatus.Error(msg)
                notifier.showFailed(isSending, msg)
            }
            else -> Unit
        }
    }.onCompletion { cause ->
        if (cause != null) { // dibatalkan (pengguna keluar layar) atau error tak terduga
            notifier.cancelProgress()
            _transferStatus.value = TransferStatus.Idle
        }
    }
}