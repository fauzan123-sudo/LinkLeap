package com.infinity.linkleap.data.model

data class TransferProgress(
    val sent: Long,
    val total: Long
) {
    val percentage: Int
        get() = if (total > 0) ((sent * 100) / total).toInt() else 0

    val sentMB: Float
        get() = sent / 1_000_000f

    val totalMB: Float
        get() = total / 1_000_000f
}