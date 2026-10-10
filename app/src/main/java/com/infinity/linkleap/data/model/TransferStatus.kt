package com.infinity.linkleap.data.model

sealed class TransferStatus {
    object Idle : TransferStatus()
    object Connecting : TransferStatus()
    data class Progress(val sent: Long, val total: Long) : TransferStatus()
    object Completed : TransferStatus()
    data class Error(val message: String) : TransferStatus()
}