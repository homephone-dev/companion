package com.homephone.companion.ui.nav

sealed interface Screen {
    data object Settings : Screen
    data object Devices : Screen
    data class DeviceDetail(val deviceId: String) : Screen
    data class CallHistory(val deviceId: String) : Screen
}
