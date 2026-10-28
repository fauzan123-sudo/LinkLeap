package com.infinity.linkleap.utils

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.0f KB".format(kb)
    val mb = kb / 1024
    if (mb < 1024) return "%.1f MB".format(mb)
    return "%.2f GB".format(mb / 1024)
}

fun formatEta(seconds: Long): String = when {
    seconds < 0 -> "--"
    seconds < 60 -> "$seconds dtk"
    seconds < 3600 -> "${seconds / 60} mnt ${seconds % 60} dtk"
    else -> "${seconds / 3600} j ${(seconds % 3600) / 60} mnt"
}