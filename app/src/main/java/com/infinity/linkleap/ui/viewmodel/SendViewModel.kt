package com.infinity.linkleap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infinity.linkleap.data.repository.NetworkRepository
import com.infinity.linkleap.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SendViewModel @Inject constructor(
    private val repository: NetworkRepository
) : ViewModel() {

    private val _sendState = MutableStateFlow<Resource<String>>(Resource.Loading())

    private val _selectedFiles = MutableStateFlow<List<String>>(emptyList())
    val selectedFiles = _selectedFiles.asStateFlow()

    fun setSelectedFiles(files: List<String>) {
        Timber.d("🔵 ViewModel ${this.hashCode()} - setSelectedFiles called")
        Timber.d("   Saving ${files.size} files")
        _selectedFiles.value = files
        Timber.d("   ✅ State updated: ${_selectedFiles.value.size} files")
    }

    private val _connection = MutableStateFlow<Pair<String, Int>?>(null)
    val connection: StateFlow<Pair<String, Int>?> = _connection


    fun setConnection(ip: String, port: Int) {
        _connection.value = ip to port
    }

    fun sendFile(ip: String, port: Int, file: File) {
        viewModelScope.launch {
            repository.sendFile(ip, port, file).collect { result ->
                _sendState.value = result
            }
        }
    }

    fun sendAllFiles(): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        val connection = _connection.value
        val files = _selectedFiles.value

        Timber.d("🔵 sendAllFiles() called")
        Timber.d("  Connection: $connection")
        Timber.d("  Files: ${files.size}")

        if (connection == null) {
            Timber.e("❌ Connection is null!")
            throw Exception("Target belum diset")
        }

        if (files.isEmpty()) {
            Timber.e("❌ Files is empty!")
            throw Exception("Tidak ada file yang dipilih")
        }

        val (ip, port) = connection

        for ((index, filePath) in files.withIndex()) {
            Timber.d("📤 Sending file ${index + 1}/${files.size}: $filePath")
            val file = File(filePath)
            emitAll(repository.sendFile(ip, port, file))
        }

        emit(Resource.Success("Semua file berhasil dikirim"))
    }.flowOn(Dispatchers.IO)

}



