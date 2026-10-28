package com.infinity.linkleap.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infinity.linkleap.data.repository.NetworkRepository
import com.infinity.linkleap.utils.DownloadStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.net.ServerSocket
import javax.inject.Inject

@HiltViewModel
class ReceiveViewModel @Inject constructor(
    private val repository: NetworkRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _serverPort = MutableStateFlow<Int?>(null)
    val serverPort = _serverPort.asStateFlow()

    val transferStatus = repository.transferStatus

    private var serverSocket: ServerSocket? = null
    private var job: Job? = null

    fun startServer() {
        if (job?.isActive == true) return

        job = viewModelScope.launch(Dispatchers.IO) {
            val server = try {
                ServerSocket(0)
            } catch (e: IOException) {
                Timber.e(e, "Gagal membuka server")
                return@launch
            }
            serverSocket = server
            _serverPort.value = server.localPort
            repository.resetStatus()

            repository.receiveFiles(server) { name ->
                DownloadStore.openOutput(context, name)
            }.collect { Timber.d("Receive: ${it.javaClass.simpleName} ${it.message ?: ""}") }
        }
    }

    override fun onCleared() {
        runCatching { serverSocket?.close() } // membatalkan accept() yang sedang menunggu
        super.onCleared()
    }
}