package com.infinity.linkleap.utils

import timber.log.Timber
import java.net.Inet4Address
import java.net.NetworkInterface

fun getLocalIpAddress(): String {
    val candidates = mutableListOf<Pair<String, String>>() // nama interface -> IP
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
        for (intf in interfaces) {
            if (!intf.isUp || intf.isLoopback) continue
            for (addr in intf.inetAddresses) {
                if (addr is Inet4Address && !addr.isLoopbackAddress) {
                    addr.hostAddress?.let { candidates += intf.name to it }
                }
            }
        }
    } catch (ex: Exception) {
        Timber.e(ex)
    }

    val preferred = listOf("wlan", "ap", "swlan", "p2p", "eth")
    return candidates.firstOrNull { (name, _) -> preferred.any { name.startsWith(it) } }?.second
        ?: candidates.firstOrNull { (name, _) -> !name.startsWith("rmnet") }?.second
        ?: "0.0.0.0"
}