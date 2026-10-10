package com.infinity.linkleap.ui.viewmodel

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infinity.linkleap.data.repository.NetworkRepository
import com.infinity.linkleap.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.net.ServerSocket
import javax.inject.Inject

@HiltViewModel
class ReceiveViewModel @Inject constructor(
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val _serverPort = MutableStateFlow<Int?>(null)
    val serverPort = _serverPort.asStateFlow()

    fun startServer() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val serverSocket = ServerSocket(0)
                val port = serverSocket.localPort
                _serverPort.value = port

                // Tunggu koneksi masuk
                val client = serverSocket.accept()
                Timber.d("Koneksi diterima di port $port dari ${client.inetAddress}")

                val saveFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "received_file.apk"
                )

                networkRepository.receiveFile(port, saveFile).collect {
                    Timber.d("Receiving: $it")
                }

                serverSocket.close()
            } catch (e: Exception) {
                Timber.e(e, "Gagal memulai server")
            }
        }
    }
}

