package com.infinity.linkleap.utils

import timber.log.Timber
import java.net.Inet4Address
import java.net.NetworkInterface

fun getLocalIpAddress(): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (intf in interfaces) {
            val addrs = intf.inetAddresses
            for (addr in addrs) {
                if (!addr.isLoopbackAddress && addr is Inet4Address) {
                    return addr.hostAddress ?: "0.0.0.0"
                }
            }
        }
    } catch (ex: Exception) {
        Timber.e(ex)
    }
    return "0.0.0.0"
}
