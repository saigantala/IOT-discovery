package com.example.discovery

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.example.model.WifiConnectionInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class LocalNetworkScanner(private val context: Context) {

    data class DiscoveredDevice(
        val ip: String,
        val hostname: String,
        val respondingPort: Int? = null,
        val manufacturer: String = "Unknown Vendor"
    )

    fun getCurrentWifiInfo(): WifiConnectionInfo? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        val dhcp = wifiManager.dhcpInfo

        var ipInt = info?.ipAddress ?: 0
        if (ipInt == 0) {
            val ipStr = getLocalIpAddress()
            if (ipStr != null) ipInt = ipToInt(ipStr)
        }

        if (ipInt == 0) return null

        val ssid = info?.ssid?.trim('\"') ?: "Connected Network"
        val ipStr = intToIp(ipInt)
        val gatewayStr = intToIp(dhcp?.gateway ?: 0)
        
        return WifiConnectionInfo(
            ssid = if (ssid == "<unknown ssid>") "Active Network" else ssid,
            bssid = info?.bssid ?: "Unknown",
            ipAddress = ipStr,
            gateway = gatewayStr,
            signalLevel = if (info != null) WifiManager.calculateSignalLevel(info.rssi, 5) else 3,
            frequency = info?.frequency ?: 0,
            linkSpeed = info?.linkSpeed ?: 0
        )
    }

    fun startScan(): Flow<ScanResult> = flow {
        val wifiInfo = getCurrentWifiInfo()
        if (wifiInfo == null || wifiInfo.ipAddress == "0.0.0.0") {
            emit(ScanResult.Error("Check Wi-Fi connection"))
            return@flow
        }

        val subnet = wifiInfo.ipAddress.substringBeforeLast(".")
        val totalHosts = 254
        val completedCount = AtomicInteger(0)
        
        Log.d("LiveScanner", "Starting deep subnet probe on $subnet.0/24")

        val batchSize = 30
        for (i in 1..totalHosts step batchSize) {
            val end = minOf(i + batchSize - 1, totalHosts)
            val batchResults = withContext(Dispatchers.IO) {
                (i..end).map { lastOctet ->
                    async {
                        val targetIp = "$subnet.$lastOctet"
                        try {
                            if (targetIp == wifiInfo.ipAddress) {
                                return@async DiscoveredDevice(targetIp, "My Admin Mobile Device", null, "Android")
                            }
                            if (targetIp == wifiInfo.gateway) {
                                return@async DiscoveredDevice(targetIp, "Main Network Gateway", 80, "Infrastructure")
                            }

                            val address = InetAddress.getByName(targetIp)
                            
                            // 1. Try connect to common ports (fastest for laptops/IoT)
                            val openPort = findOpenPort(targetIp)
                            
                            // 2. Try ping (ICMP)
                            val reachable = address.isReachable(600)
                            
                            if (reachable || openPort != null) {
                                val hostname = address.hostName
                                val cleanName = if (hostname == targetIp || hostname.isNullOrEmpty()) {
                                    "Discovered Asset"
                                } else hostname
                                
                                DiscoveredDevice(targetIp, cleanName, openPort)
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            null
                        } finally {
                            completedCount.incrementAndGet()
                        }
                    }
                }.awaitAll().filterNotNull()
            }
            
            emit(ScanResult.Progress(
                percentage = completedCount.get().toFloat() / totalHosts,
                newDevices = batchResults,
                scannedCount = completedCount.get()
            ))
        }
        
        emit(ScanResult.Finished)
    }

    private fun findOpenPort(ip: String): Int? {
        for (port in portsToProbe(ip)) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(ip, port), 150)
                socket.close()
                return port
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    private fun portsToProbe(ip: String): List<Int> {
        val lastOctet = ip.substringAfterLast(".").toIntOrNull() ?: 0
        return if (lastOctet == 1) {
            listOf(80, 443, 8080)
        } else {
            listOf(80, 443, 139, 445, 554, 1900, 8080)
        }
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        if (sAddr != null && sAddr.indexOf(':') < 0) return sAddr
                    }
                }
            }
        } catch (ex: Exception) { }
        return null
    }

    private fun ipToInt(ip: String): Int {
        val parts = ip.split(".")
        if (parts.size != 4) return 0
        var result = 0
        for (i in 0..3) {
            result = result or (parts[i].toInt() shl (i * 8))
        }
        return result
    }

    private fun intToIp(ip: Int): String {
        return (ip and 0xFF).toString() + "." +
                (ip shr 8 and 0xFF).toString() + "." +
                (ip shr 16 and 0xFF).toString() + "." +
                (ip shr 24 and 0xFF).toString()
    }

    sealed class ScanResult {
        data class Progress(val percentage: Float, val newDevices: List<DiscoveredDevice>, val scannedCount: Int) : ScanResult()
        object Finished : ScanResult()
        data class Error(val message: String) : ScanResult()
    }
}
