package com.homephone.companion.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.homephone.companion.data.api.HomephoneApiClient
import com.homephone.companion.data.settings.BackendConfig
import com.homephone.companion.ui.nav.Screen
import com.homephone.companion.ui.screens.CallHistoryScreen
import com.homephone.companion.ui.screens.DeviceDetailScreen
import com.homephone.companion.ui.screens.DevicesScreen
import com.homephone.companion.ui.screens.SettingsScreen

/**
 * Root of the shared Compose UI, used identically by iosApp and androidApp.
 * Very small hand-rolled nav stack (no navigation library wired up yet) —
 * enough to demonstrate the screens talking to the real API client.
 */
@Composable
fun HomephoneCompanionApp() {
    val backendConfig = remember { BackendConfig() }
    val apiClient = remember { HomephoneApiClient(backendConfig) }

    var screen: Screen by remember {
        mutableStateOf(if (backendConfig.isConfigured) Screen.Devices else Screen.Settings)
    }

    MaterialTheme {
        Scaffold { padding ->
            val modifier = androidx.compose.ui.Modifier.padding(padding)
            when (val current = screen) {
                is Screen.Settings -> SettingsScreen(
                    modifier = modifier,
                    backendConfig = backendConfig,
                    onDone = { screen = Screen.Devices },
                )
                is Screen.Devices -> DevicesScreen(
                    modifier = modifier,
                    apiClient = apiClient,
                    onOpenDevice = { deviceId -> screen = Screen.DeviceDetail(deviceId) },
                    onOpenSettings = { screen = Screen.Settings },
                )
                is Screen.DeviceDetail -> DeviceDetailScreen(
                    modifier = modifier,
                    deviceId = current.deviceId,
                    apiClient = apiClient,
                    onBack = { screen = Screen.Devices },
                    onOpenCallHistory = { screen = Screen.CallHistory(current.deviceId) },
                )
                is Screen.CallHistory -> CallHistoryScreen(
                    modifier = modifier,
                    deviceId = current.deviceId,
                    apiClient = apiClient,
                    onBack = { screen = Screen.DeviceDetail(current.deviceId) },
                )
            }
        }
    }
}
