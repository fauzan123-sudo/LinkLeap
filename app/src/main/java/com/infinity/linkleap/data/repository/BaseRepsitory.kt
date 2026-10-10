package com.infinity.linkleap.data.repository

import com.infinity.linkleap.data.model.TransferProgress
import com.infinity.linkleap.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseRepository {

    protected fun <T> safeNetworkTransfer(
        onProgress: ((TransferProgress) -> Unit)? = null,
        block: suspend (progressCallback: (sent: Long, total: Long) -> Unit) -> T
    ): Flow<Resource<T>> = flow {
        emit(Resource.Loading())

        try {
            val progressCallback: (Long, Long) -> Unit = { sent, total ->
                onProgress?.invoke(TransferProgress(sent, total))  // ✅ Langsung invoke tanpa runBlocking
            }

            val result = block(progressCallback)
            emit(Resource.Success(result))

        } catch (e: ConnectException) {
            Timber.e(e, "NetworkTransferError: ${e.message}")
            emit(Resource.Error("Gagal terhubung ke penerima"))
        } catch (e: UnknownHostException) {
            Timber.e(e, "NetworkTransferError: ${e.message}")
            emit(Resource.Error("Alamat IP tidak valid"))
        } catch (e: SocketTimeoutException) {
            Timber.e(e, "NetworkTransferError: ${e.message}")
            emit(Resource.Error("Waktu koneksi habis"))
        } catch (e: IOException) {
            Timber.e(e, "NetworkTransferError: ${e.message}")
            emit(Resource.Error("Kesalahan jaringan: ${e.message}"))
        } catch (e: Exception) {
            Timber.e(e, "NetworkTransferError: ${e.message}")
            emit(Resource.Error(e.localizedMessage ?: "Kesalahan tidak diketahui"))
        }
    }.flowOn(Dispatchers.IO)
}



