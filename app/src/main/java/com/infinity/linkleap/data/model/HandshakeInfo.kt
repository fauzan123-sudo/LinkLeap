package com.infinity.linkleap.data.model

data class HandshakeInfo(
    val mode: String, // "hotspot" or "p2p" or "direct"
    val ip: String, // target IP for socket
    val port: Int,
    val deviceName: String,
    val ssid: String? = null, // hotspot SSID (optional)
    val extra: String? = null
)