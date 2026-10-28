package com.infinity.linkleap.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.infinity.linkleap.data.model.SendItem
import com.infinity.linkleap.data.repository.NetworkRepository
import com.infinity.linkleap.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SendViewModel @Inject constructor(
    private val repository: NetworkRepository
) : ViewModel() {

    private val _selectedFiles = MutableStateFlow<List<SendItem>>(emptyList())
    val selectedFiles = _selectedFiles.asStateFlow()

    private val _connection = MutableStateFlow<Pair<String, Int>?>(null)
    val connection: StateFlow<Pair<String, Int>?> = _connection.asStateFlow()

    val transferStatus = repository.transferStatus

    fun setSelectedFiles(files: List<SendItem>) {
        Timber.d("VM ${hashCode()} setSelectedFiles: ${files.size} file")
        _selectedFiles.value = files
    }

    fun setConnection(ip: String, port: Int) {
        repository.resetStatus()
        _connection.value = ip to port
    }

    fun sendAllFiles(): Flow<Resource<String>> {
        val (ip, port) = _connection.value
            ?: return flowOf(Resource.Error("Target belum diset"))
        val items = _selectedFiles.value
        if (items.isEmpty()) return flowOf(Resource.Error("Tidak ada file yang dipilih"))

        repository.resetStatus()
        return repository.sendFiles(ip, port, items)
    }
}