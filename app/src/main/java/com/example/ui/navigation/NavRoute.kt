package com.example.ui.navigation

sealed class NavRoute(val route: String) {
    object Splash : NavRoute("splash")
    object Login : NavRoute("login")
    object Dashboard : NavRoute("dashboard")
    object Devices : NavRoute("devices")
    object DeviceDetails : NavRoute("device_details/{deviceId}") {
        fun createRoute(deviceId: String) = "device_details/$deviceId"
    }
    object Topology : NavRoute("topology")
    object Fingerprints : NavRoute("fingerprints")
    object DiscoveryLogs : NavRoute("discovery_logs")
    object RogueDevices : NavRoute("rogue_devices")
    object Timeline : NavRoute("timeline")
    object Compliance : NavRoute("compliance")
    object Reports : NavRoute("reports")
    object Alerts : NavRoute("alerts")
    object Settings : NavRoute("settings")
    object Profile : NavRoute("profile")
    object About : NavRoute("about")
    object Scanning : NavRoute("scanning")
    object LoginHistory : NavRoute("login_history")
}
