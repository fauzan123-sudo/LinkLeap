package com.infinity.linkleap.data.model

sealed class TransferStatus {
    object Idle : TransferStatus()
    object Connecting : TransferStatus()
    data class Progress(
        val sent: Long,
        val total: Long,
        val speedBps: Long = 0L,
        val etaSeconds: Long = -1L
    ) : TransferStatus()
    object Completed : TransferStatus()
    data class Error(val message: String) : TransferStatus()
}