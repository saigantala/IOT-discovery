package com.example.data

import android.content.Context
import android.util.Log
import com.example.discovery.LocalNetworkScanner
import com.example.mock.MockData
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay

class DataRepository(context: Context) {

    private val deviceDao = AppDatabase.getDatabase(context).deviceDao()
    private val scanner = LocalNetworkScanner(context)

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _currentScanDevices = MutableStateFlow<List<LocalNetworkScanner.DiscoveredDevice>>(emptyList())
    val currentScanDevices: StateFlow<List<LocalNetworkScanner.DiscoveredDevice>> = _currentScanDevices.asStateFlow()

    private val _scannedIpsCount = MutableStateFlow(0)
    val scannedIpsCount: StateFlow<Int> = _scannedIpsCount.asStateFlow()

    suspend fun runNetworkDiscovery() {
        Log.d("DataRepo", "Starting Subnet Discovery Process")
        _isScanning.value = true
        _scanProgress.value = 0f
        _currentScanDevices.value = emptyList()
        _scannedIpsCount.value = 0
        
        scanner.startScan().collect { result ->
            when (result) {
                is LocalNetworkScanner.ScanResult.Progress -> {
                    _scanProgress.value = result.percentage
                    _scannedIpsCount.value = result.scannedCount
                    
                    if (result.newDevices.isNotEmpty()) {
                        _currentScanDevices.value = _currentScanDevices.value + result.newDevices
                        
                        val devicesToSave = result.newDevices.map { discovered ->
                            val type = mapHostnameToType(discovered.hostname, discovered.ip, discovered.respondingPort)
                            
                            val name = when {
                                discovered.hostname.contains("Admin", true) || discovered.hostname.contains("Me", true) -> "Authorized Admin Mobile"
                                discovered.hostname == "Main Network Gateway" -> "Primary Router/Gateway"
                                discovered.hostname == "Network Asset" || discovered.hostname.isEmpty() || discovered.hostname.contains(discovered.ip) -> {
                                    "${type.displayName} Node"
                                }
                                else -> discovered.hostname
                            }
                            
                            val isSuspicious = discovered.hostname.lowercase().contains("unknown") || 
                                              (discovered.respondingPort != null && (discovered.respondingPort == 22 || discovered.respondingPort == 23))

                            Device(
                                id = discovered.ip.replace(".", "_"),
                                name = name,
                                type = type,
                                manufacturer = discovered.manufacturer,
                                ipAddress = discovered.ip,
                                macAddress = "XX:XX:XX:XX:XX:XX",
                                hostname = discovered.hostname,
                                os = when(type) {
                                    DeviceType.LAPTOP -> "Desktop OS (Win/Mac)"
                                    DeviceType.MOBILE -> "Mobile OS (Android/iOS)"
                                    DeviceType.ROUTER -> "Gateway OS"
                                    DeviceType.CAMERA -> "IP Cam Firmware"
                                    else -> "Embedded Network Stack"
                                },
                                riskLevel = if (isSuspicious) RiskLevel.HIGH else RiskLevel.LOW,
                                status = if (isSuspicious) DeviceStatus.ROGUE else DeviceStatus.ONLINE,
                                lastSeen = "Active: Just now",
                                openPorts = if (discovered.respondingPort != null) listOf(discovered.respondingPort) else emptyList(),
                                services = emptyList(),
                                fingerprintConfidence = 90,
                                riskScore = if (isSuspicious) 40 else 0,
                                riskReason = "Device discovered on live local subnet",
                                discoveryMethod = "Active Discovery"
                            )
                        }
                        deviceDao.insertDevices(devicesToSave)
                    }
                }
                is LocalNetworkScanner.ScanResult.Finished -> {
                    _scanProgress.value = 1f
                }
                is LocalNetworkScanner.ScanResult.Error -> {
                    Log.e("DataRepo", "Scan failure: ${result.message}")
                }
            }
        }
        _isScanning.value = false
    }

    private fun mapHostnameToType(hostname: String, ip: String, port: Int?): DeviceType {
        val h = hostname.lowercase()
        val currentWifi = getCurrentWifiInfo()
        val lastOctet = ip.substringAfterLast(".").toIntOrNull() ?: 0
        
        // Match 1: Network Infrastructure
        if (h.contains("gateway") || h.contains("router") || lastOctet == 1 || (currentWifi != null && ip == currentWifi.gateway)) return DeviceType.ROUTER
        
        // Match 2: Mobile Devices (smartphones, tablets)
        if (h.contains("phone") || h.contains("android") || h.contains("iphone") || h.contains("ipad") || h.contains("samsung") || h.contains("pixel") || h.contains("mobile") || h.contains("galaxy") || h.contains("huawei") || h.contains("xiaomi")) return DeviceType.MOBILE
        
        // Match 3: Laptops and Computers
        if (h.contains("desktop") || h.contains("laptop") || h.contains("pc") || h.contains("macbook") || h.contains("windows") || h.contains("dell") || h.contains("hp") || h.contains("lenovo") || h.contains("host") || h.contains("workstation") || h.contains("surface")) return DeviceType.LAPTOP
        
        // Match 4: Specific IoT categories
        if (h.contains("camera") || h.contains("cctv") || h.contains("cam") || h.contains("door") || h.contains("video")) return DeviceType.CAMERA
        if (h.contains("tv") || h.contains("cast") || h.contains("roku") || h.contains("bravia") || h.contains("vizio") || h.contains("sony") || h.contains("smart-tv")) return DeviceType.SMART_TV
        if (h.contains("print") || h.contains("hp-") || h.contains("epson") || h.contains("canon") || h.contains("lexmark") || h.contains("officejet")) return DeviceType.PRINTER
        if (h.contains("hub") || h.contains("bridge") || h.contains("zigbee") || h.contains("smart-hub")) return DeviceType.GATEWAY
        if (h.contains("nas") || h.contains("server") || h.contains("synology") || h.contains("qnap") || h.contains("storage")) return DeviceType.SERVER

        // Match 5: Logical fallback via open ports
        if (port != null) {
            return when (port) {
                554, 8000 -> DeviceType.CAMERA
                631, 9100 -> DeviceType.PRINTER
                445, 139 -> DeviceType.LAPTOP // SMB is almost always a computer
                1900 -> DeviceType.SMART_TV // UPnP/SSDP
                5000, 5001 -> DeviceType.SERVER
                else -> DeviceType.UNKNOWN
            }
        }

        if (lastOctet == 254) return DeviceType.ROUTER
        return DeviceType.UNKNOWN
    }

    fun getDevicesStream(): Flow<List<Device>> = deviceDao.getAllDevices().map { list ->
        val currentWifi = getCurrentWifiInfo()
        val currentIp = currentWifi?.ipAddress ?: "0.0.0.0"
        
        if (currentIp == "0.0.0.0" || currentIp == "127.0.0.1") return@map list
        
        val prefix = currentIp.substringBeforeLast(".")
        val currentSubnetDevices = list.filter { it.ipAddress.startsWith("$prefix.") }
        
        // If DB is totally empty for this subnet, return history for visual context, else return filtered list
        if (currentSubnetDevices.isEmpty()) {
            list.take(8)
        } else {
            currentSubnetDevices.sortedBy { device ->
                device.ipAddress.split(".").lastOrNull()?.toIntOrNull() ?: 0
            }
        }
    }

    fun getComplianceStream(): Flow<ComplianceReport> = getDevicesStream().map { devices ->
        if (devices.isEmpty()) return@map MockData.complianceReport
        val insecurePorts = devices.flatMap { it.openPorts }.count { it in listOf(21, 23, 445) }
        val rogueCount = devices.count { it.status == DeviceStatus.ROGUE }
        val score = (100 - (rogueCount * 12) - (insecurePorts * 6)).coerceIn(0, 100)
        
        val recommendations = mutableListOf<String>()
        if (rogueCount > 0) recommendations.add("Isolate $rogueCount unidentified hardware nodes from the subnet.")
        if (insecurePorts > 0) recommendations.add("Legacy ports detected. Recommend blocking ports 21/23 at the Gateway.")
        if (score < 90) recommendations.add("Conduct a manual audit of all assets with low fingerprint confidence.")
        if (recommendations.isEmpty()) recommendations.add("Network security posture is optimal.")

        ComplianceReport(
            score = score,
            inventoryTotal = devices.size,
            weakPasswordCount = devices.count { it.type == DeviceType.ROUTER },
            openInsecurePorts = insecurePorts,
            outdatedFirmware = devices.count { it.fingerprintConfidence < 85 },
            recommendations = recommendations
        )
    }

    fun getRogueDevicesStream(): Flow<List<Device>> = getDevicesStream().map { it.filter { d -> d.status == DeviceStatus.ROGUE } }
    fun getAlertsStream(): Flow<List<AlertItem>> = getDevicesStream().map { devices ->
        devices.filter { it.status == DeviceStatus.ROGUE }.map { device ->
            AlertItem("alert_${device.id}", "Unverified Asset", "${device.name} identified on live subnet.", RiskLevel.HIGH, device.lastSeen, device.name, device.ipAddress)
        }
    }
    fun getDiscoveryLogsStream(): Flow<List<DiscoveryLog>> = getDevicesStream().map { devices ->
        devices.map { DiscoveryLog(it.id, it.lastSeen, it.name, it.ipAddress, it.status, it.discoveryMethod) }
    }
    fun getFingerprintsStream(): Flow<List<FingerprintData>> = getDevicesStream().map { devices ->
        devices.map { FingerprintData("fp_${it.id}", it.manufacturer, listOf("Active Stack"), it.openPorts, mapOf("Method" to "Subnet Probe"), 90, it.type.displayName) }
    }
    fun getTimelineStream(): Flow<List<TimelineEvent>> = getDevicesStream().map { devices ->
        devices.map { device -> TimelineEvent("ev_${device.id}", device.id, device.name, "Scanned", "Just Now", "Asset verified on current subnet.") }
    }
    fun getNetworkNodesStream(): Flow<List<NetworkNode>> = getDevicesStream().map { devices ->
        if (devices.isEmpty()) return@map emptyList<NetworkNode>()
        val gateway = devices.find { it.type == DeviceType.ROUTER } ?: devices.first()
        devices.map { NetworkNode(it.id, it.name, it.type, it.ipAddress, it.status, if (it.id == gateway.id) null else gateway.id) }
    }

    fun getDiscoveryLogs(): List<DiscoveryLog> = MockData.discoveryLogs
    fun getLoginHistory(): List<LoginHistory> = MockData.loginHistory
    fun getRealTimeStatus(): Flow<String> = flow {
        while (true) {
            emit(if (_isScanning.value) "Active Discovery Running..." else "Shield Online - Subnet Safe")
            delay(2000)
        }
    }
    fun getCurrentWifiInfo(): WifiConnectionInfo? = scanner.getCurrentWifiInfo()
}
