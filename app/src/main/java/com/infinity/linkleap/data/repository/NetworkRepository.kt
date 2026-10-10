package com.infinity.linkleap.data.repository

import com.infinity.linkleap.data.model.TransferStatus
import com.infinity.linkleap.data.network.FileReceiver
import com.infinity.linkleap.data.network.FileSender
import com.infinity.linkleap.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

class NetworkRepository @Inject constructor(
    private val sender: FileSender,
    private val receiver: FileReceiver
) : BaseRepository() {

    private val _transferStatus = MutableStateFlow<TransferStatus>(TransferStatus.Idle)
    val transferStatus: StateFlow<TransferStatus> = _transferStatus.asStateFlow()

    fun sendFile(ip: String, port: Int, file: File): Flow<Resource<String>> =
        safeNetworkTransfer(
            onProgress = { progress ->
                _transferStatus.value = TransferStatus.Progress(progress.sent, progress.total)
            }
        ) { progressCallback ->
            _transferStatus.value = TransferStatus.Connecting

            sender.sendFile(ip, port, file) { sent, total ->
                progressCallback(sent, total)
            }

            _transferStatus.value = TransferStatus.Completed
            file.name // return result
        }

    fun sendFiles(ip: String, port: Int, file: File): Flow<Resource<String>> =
        safeNetworkTransfer(
            onProgress = { progress ->
                _transferStatus.value = TransferStatus.Progress(progress.sent, progress.total)
            }
        ) { progressCallback ->
            _transferStatus.value = TransferStatus.Connecting

            sender.sendFile(ip, port, file) { sent, total ->
                progressCallback(sent, total)
            }

            _transferStatus.value = TransferStatus.Completed
            file.name // return result
        }

    fun receiveFile(port: Int, saveFile: File): Flow<Resource<String>> =
        safeNetworkTransfer(
            onProgress = { progress ->
                _transferStatus.value = TransferStatus.Progress(progress.sent, progress.total)
            }
        ) { progressCallback ->
            _transferStatus.value = TransferStatus.Connecting

            receiver.startServer(port, saveFile) { received ->
                progressCallback(received, saveFile.length())
            }

            _transferStatus.value = TransferStatus.Completed
            saveFile.name
        }

    fun resetStatus() {
        _transferStatus.value = TransferStatus.Idle
    }
}

