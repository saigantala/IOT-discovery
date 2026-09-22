package com.example.data

import android.content.Context
import android.util.Log
import com.example.discovery.LocalNetworkScanner
import com.example.model.*
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class DataRepository(private val context: Context) {

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

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    /**
     * Source of Truth Strategy:
     * UI -> Repository -> REST API -> Room (Offline Cache) -> UI
     */
    suspend fun refreshDevicesFromApi(): Result<List<Device>> = withContext(Dispatchers.IO) {
        try {
            Log.d("DataRepo", "🔄 Fetching live devices from REST API...")
            val response = RetrofitClient.getApiService(context).getDevices()
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                val entities = dtos.map { it.toRoomEntity() }
                Log.d("DataRepo", "✅ Received ${entities.size} devices from REST API. Updating Room offline cache.")
                deviceDao.insertDevices(entities)
                _lastError.value = null
                Result.success(entities)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown API Error"
                val msg = "API Error ${response.code()}: $errorBody"
                Log.e("DataRepo", "❌ $msg")
                _lastError.value = msg
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            val msg = "Network Exception: ${e.message}"
            Log.e("DataRepo", "❌ $msg", e)
            _lastError.value = msg
            Result.failure(e)
        }
    }

    suspend fun runNetworkDiscovery() {
        Log.d("DataRepo", "🔍 Starting Active Subnet Discovery Process")
        _isScanning.value = true
        _scanProgress.value = 0f
        _currentScanDevices.value = emptyList()
        _scannedIpsCount.value = 0
        _lastError.value = null
        
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
                                macAddress = discovered.ip.replace(".", "_"),
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
                                riskReason = "Discovered during active subnet sweep",
                                discoveryMethod = "Active Discovery"
                            )
                        }
                        
                        // 1. Cache locally in Room
                        deviceDao.insertDevices(devicesToSave)

                        // 2. Sync immediately with REST API (Requirement 5)
                        try {
                            val syncRequest = SyncDevicesRequest(
                                devices = devicesToSave.map {
                                    DeviceSyncDto(
                                        deviceId = it.id,
                                        name = it.name,
                                        type = it.type.name,
                                        ipAddress = it.ipAddress,
                                        status = it.status.name
                                    )
                                }
                            )
                            withContext(Dispatchers.IO) {
                                Log.d("DataRepo", "📡 Syncing ${devicesToSave.size} discovered devices to REST API...")
                                val response = RetrofitClient.getApiService(context).syncDevices(syncRequest)
                                if (response.isSuccessful) {
                                    Log.d("DataRepo", "✅ Device sync confirmed by backend! Response Code: ${response.code()}")
                                } else {
                                    val errBody = response.errorBody()?.string() ?: "Empty error body"
                                    val errMsg = "Device sync rejected by server (Code ${response.code()}): $errBody"
                                    Log.e("DataRepo", "❌ $errMsg")
                                    _lastError.value = errMsg
                                }
                            }
                        } catch (e: Exception) {
                            val errMsg = "Failed to reach REST API during device sync: ${e.message}"
                            Log.e("DataRepo", "❌ $errMsg", e)
                            _lastError.value = errMsg
                        }
                    }
                }
                is LocalNetworkScanner.ScanResult.Finished -> {
                    _scanProgress.value = 1f
                    // Trigger a refresh from backend after discovery finishes
                    refreshDevicesFromApi()
                }
                is LocalNetworkScanner.ScanResult.Error -> {
                    val errMsg = "Scan failure: ${result.message}"
                    Log.e("DataRepo", "❌ $errMsg")
                    _lastError.value = errMsg
                }
            }
        }
        _isScanning.value = false
    }

    suspend fun quarantineDevice(deviceId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("DataRepo", "🔒 Requesting quarantine for device: $deviceId")
            val response = RetrofitClient.getApiService(context).quarantineDevice(deviceId)
            if (response.isSuccessful) {
                Log.d("DataRepo", "✅ Device $deviceId successfully quarantined on backend.")
                refreshDevicesFromApi()
                Result.success(true)
            } else {
                val err = "Quarantine failed (${response.code()}): ${response.errorBody()?.string()}"
                Log.e("DataRepo", "❌ $err")
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Log.e("DataRepo", "❌ Quarantine exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchAlertsFromApi(): Result<List<AlertItem>> = withContext(Dispatchers.IO) {
        try {
            Log.d("DataRepo", "🔄 Fetching live alerts from REST API...")
            val response = RetrofitClient.getApiService(context).getAlerts()
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                val items = dtos.map { it.toAlertItem() }
                Log.d("DataRepo", "✅ Received ${items.size} alerts from REST API.")
                Result.success(items)
            } else {
                val err = "Alerts API Error (${response.code()}): ${response.errorBody()?.string()}"
                Log.e("DataRepo", "❌ $err")
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Log.e("DataRepo", "❌ Alerts API Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchDashboardSummary(): Result<DashboardSummary> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.getApiService(context).getDashboardSummary()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Dashboard summary error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapHostnameToType(hostname: String, ip: String, port: Int?): DeviceType {
        val h = hostname.lowercase()
        val currentWifi = getCurrentWifiInfo()
        val lastOctet = ip.substringAfterLast(".").toIntOrNull() ?: 0
        
        if (h.contains("gateway") || h.contains("router") || lastOctet == 1 || (currentWifi != null && ip == currentWifi.gateway)) return DeviceType.ROUTER
        if (h.contains("phone") || h.contains("android") || h.contains("iphone") || h.contains("ipad") || h.contains("samsung") || h.contains("pixel") || h.contains("mobile") || h.contains("galaxy")) return DeviceType.MOBILE
        if (h.contains("desktop") || h.contains("laptop") || h.contains("pc") || h.contains("macbook") || h.contains("windows") || h.contains("dell") || h.contains("hp") || h.contains("lenovo") || h.contains("workstation")) return DeviceType.LAPTOP
        if (h.contains("camera") || h.contains("cctv") || h.contains("cam") || h.contains("door") || h.contains("video")) return DeviceType.CAMERA
        if (h.contains("tv") || h.contains("cast") || h.contains("roku") || h.contains("bravia") || h.contains("vizio") || h.contains("sony")) return DeviceType.SMART_TV
        if (h.contains("print") || h.contains("hp-") || h.contains("epson") || h.contains("canon") || h.contains("lexmark")) return DeviceType.PRINTER
        if (h.contains("hub") || h.contains("bridge") || h.contains("zigbee") || h.contains("smart-hub")) return DeviceType.GATEWAY
        if (h.contains("nas") || h.contains("server") || h.contains("synology") || h.contains("qnap") || h.contains("storage")) return DeviceType.SERVER

        if (port != null) {
            return when (port) {
                554, 8000 -> DeviceType.CAMERA
                631, 9100 -> DeviceType.PRINTER
                445, 139 -> DeviceType.LAPTOP
                1900 -> DeviceType.SMART_TV
                5000, 5001 -> DeviceType.SERVER
                else -> DeviceType.UNKNOWN
            }
        }

        if (lastOctet == 254) return DeviceType.ROUTER
        return DeviceType.UNKNOWN
    }

    /**
     * Emits Room DB cached list.
     */
    fun getDevicesStream(): Flow<List<Device>> = deviceDao.getAllDevices().map { list ->
        val currentWifi = getCurrentWifiInfo()
        val currentIp = currentWifi?.ipAddress ?: "0.0.0.0"
        
        if (currentIp == "0.0.0.0" || currentIp == "127.0.0.1") return@map list
        
        val prefix = currentIp.substringBeforeLast(".")
        val currentSubnetDevices = list.filter { it.ipAddress.startsWith("$prefix.") }
        
        currentSubnetDevices.ifEmpty { list }.sortedBy { device ->
            device.ipAddress.split(".").lastOrNull()?.toIntOrNull() ?: 0
        }
    }

    fun getComplianceStream(): Flow<ComplianceReport> = getDevicesStream().map { devices ->
        val insecurePorts = devices.flatMap { it.openPorts }.count { it in listOf(21, 23, 445) }
        val rogueCount = devices.count { it.status == DeviceStatus.ROGUE }
        val score = (100 - (rogueCount * 12) - (insecurePorts * 6)).coerceIn(0, 100)
        
        val recommendations = mutableListOf<String>()
        if (rogueCount > 0) recommendations.add("Isolate $rogueCount unidentified hardware nodes from the subnet.")
        if (insecurePorts > 0) recommendations.add("Legacy ports detected. Block legacy ports 21/23 at Gateway.")
        if (score < 90) recommendations.add("Audit all assets with low fingerprint confidence.")
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

    fun getAlertsStream(): Flow<List<AlertItem>> = flow {
        // First try live API
        val apiResult = fetchAlertsFromApi()
        if (apiResult.isSuccess && apiResult.getOrNull() != null) {
            emit(apiResult.getOrNull()!!)
        } else {
            // Fallback to room device alerts
            getDevicesStream().collect { devices ->
                val derived = devices.filter { it.status == DeviceStatus.ROGUE }.map { device ->
                    AlertItem("alert_${device.id}", "Unverified Asset Detected", "${device.name} identified on live subnet.", RiskLevel.HIGH, device.lastSeen, device.name, device.ipAddress)
                }
                emit(derived)
            }
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
        if (devices.isEmpty()) return@map emptyList()
        val gateway = devices.find { it.type == DeviceType.ROUTER } ?: devices.first()
        devices.map { NetworkNode(it.id, it.name, it.type, it.ipAddress, it.status, if (it.id == gateway.id) null else gateway.id) }
    }

    fun getLoginHistory(): List<LoginHistory> = emptyList()

    fun getRealTimeStatus(): Flow<String> = flow {
        while (true) {
            emit(if (_isScanning.value) "Active Subnet Sweep Running..." else "Shield Online - Subnet Protected")
            delay(2000)
        }
    }

    fun getCurrentWifiInfo(): WifiConnectionInfo? = scanner.getCurrentWifiInfo()
}
