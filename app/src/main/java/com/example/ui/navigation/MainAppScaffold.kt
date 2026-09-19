package com.example.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.AppNavigationDrawerContent
import com.example.ui.components.AppTopBar
import com.example.ui.screens.about.AboutScreen
import com.example.ui.screens.alerts.AlertsScreen
import com.example.ui.screens.compliance.ComplianceScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.details.DeviceDetailsScreen
import com.example.ui.screens.devices.DevicesScreen
import com.example.ui.screens.fingerprints.FingerprintsScreen
import com.example.ui.screens.history.LoginHistoryScreen
import com.example.ui.screens.login.LoginScreen
import com.example.ui.screens.logs.DiscoveryLogsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.rogue.RogueDevicesScreen
import com.example.ui.screens.scanning.ScanningScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.timeline.TimelineScreen
import com.example.ui.screens.topology.NetworkTopologyScreen
import kotlinx.coroutines.launch

@Composable
fun MainAppScaffold(
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoute.Splash.route

    // Hide Top/Bottom bars on Splash & Login
    val isAuthOrSplash = currentRoute == NavRoute.Splash.route || currentRoute == NavRoute.Login.route

    val screenTitles = mapOf(
        NavRoute.Dashboard.route to "Dashboard",
        NavRoute.Devices.route to "Discovered Assets",
        NavRoute.Topology.route to "Network Topology",
        NavRoute.Fingerprints.route to "IoT Fingerprints",
        NavRoute.DiscoveryLogs.route to "History Dashboard",
        NavRoute.RogueDevices.route to "Rogue Devices Alert",
        NavRoute.Timeline.route to "Activity Timeline",
        NavRoute.Compliance.route to "Security Compliance",
        NavRoute.Reports.route to "Enterprise Reports",
        NavRoute.Alerts.route to "Security Alerts",
        NavRoute.Settings.route to "Settings",
        NavRoute.Profile.route to "Admin Profile",
        NavRoute.About.route to "About System"
    )

    val currentTitle = when {
        currentRoute.startsWith("device_details") -> "Device Details"
        else -> screenTitles[currentRoute] ?: "IoT Discovery"
    }

    val isTopLevelRoute = listOf(
        NavRoute.Dashboard.route,
        NavRoute.Devices.route,
        NavRoute.Topology.route,
        NavRoute.Alerts.route,
        NavRoute.Settings.route
    ).contains(currentRoute)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isAuthOrSplash,
        drawerContent = {
            AppNavigationDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    coroutineScope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo(NavRoute.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    coroutineScope.launch { drawerState.close() }
                    navController.navigate(NavRoute.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (!isAuthOrSplash) {
                    AppTopBar(
                        title = currentTitle,
                        onMenuClick = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        onBackClick = if (!isTopLevelRoute) {
                            { navController.popBackStack() }
                        } else null
                    )
                }
            },
            bottomBar = {
                if (!isAuthOrSplash && isTopLevelRoute) {
                    AppBottomNavigation(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(NavRoute.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavRoute.Splash.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(NavRoute.Splash.route) {
                    SplashScreen(
                        onSplashFinished = {
                            navController.navigate(NavRoute.Login.route) {
                                popUpTo(NavRoute.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(NavRoute.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(NavRoute.Dashboard.route) {
                                popUpTo(NavRoute.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(NavRoute.Dashboard.route) {
                    DashboardScreen(
                        onNavigate = { route -> navController.navigate(route) },
                        onSelectDevice = { deviceId ->
                            navController.navigate(NavRoute.DeviceDetails.createRoute(deviceId))
                        },
                        onStartScan = { navController.navigate(NavRoute.Scanning.route) }
                    )
                }

                composable(NavRoute.Scanning.route) {
                    ScanningScreen(
                        onScanFinished = {
                            navController.navigate(NavRoute.Devices.route) {
                                popUpTo(NavRoute.Scanning.route) { inclusive = true }
                            }
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(NavRoute.Devices.route) {
                    DevicesScreen(
                        onSelectDevice = { deviceId ->
                            navController.navigate(NavRoute.DeviceDetails.createRoute(deviceId))
                        },
                        onStartScan = { /* mock scan triggered */ }
                    )
                }

                composable(
                    route = NavRoute.DeviceDetails.route,
                    arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val deviceId = backStackEntry.arguments?.getString("deviceId") ?: "dev-01"
                    DeviceDetailsScreen(
                        deviceId = deviceId,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(NavRoute.Topology.route) {
                    NetworkTopologyScreen(
                        onSelectDevice = { deviceId ->
                            navController.navigate(NavRoute.DeviceDetails.createRoute(deviceId))
                        }
                    )
                }

                composable(NavRoute.Fingerprints.route) {
                    FingerprintsScreen()
                }

                composable(NavRoute.DiscoveryLogs.route) {
                    DiscoveryLogsScreen()
                }

                composable(NavRoute.RogueDevices.route) {
                    RogueDevicesScreen(
                        onSelectDevice = { deviceId ->
                            navController.navigate(NavRoute.DeviceDetails.createRoute(deviceId))
                        }
                    )
                }

                composable(NavRoute.Timeline.route) {
                    TimelineScreen()
                }

                composable(NavRoute.Compliance.route) {
                    ComplianceScreen()
                }

                composable(NavRoute.Reports.route) {
                    ReportsScreen()
                }

                composable(NavRoute.Alerts.route) {
                    AlertsScreen(
                        onSelectDevice = { deviceId ->
                            navController.navigate(NavRoute.DeviceDetails.createRoute(deviceId))
                        }
                    )
                }

                composable(NavRoute.Settings.route) {
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = onToggleDarkTheme,
                        onNavigateToAbout = { navController.navigate(NavRoute.About.route) }
                    )
                }

                composable(NavRoute.Profile.route) {
                    ProfileScreen(
                        onLogout = {
                            navController.navigate(NavRoute.Login.route) {
                                popUpTo(0)
                            }
                        }
                    )
                }

                composable(NavRoute.About.route) {
                    AboutScreen()
                }

                composable(NavRoute.LoginHistory.route) {
                    LoginHistoryScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
