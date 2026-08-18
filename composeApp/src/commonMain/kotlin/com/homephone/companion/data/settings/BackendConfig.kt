package com.homephone.companion.data.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

/**
 * Milestone 2 has no account system of its own — per the project plan,
 * this app just talks to a single self-hosted backend the user already
 * trusts. The base URL + bearer token are entered once in a settings
 * screen and stored locally on-device via multiplatform-settings
 * (Preferences on Android, NSUserDefaults on iOS).
 */
class BackendConfig(private val settings: Settings = Settings()) {

    var baseUrl: String
        get() = settings["baseUrl", ""]
        set(value) = settings.set("baseUrl", value)

    var apiToken: String
        get() = settings["apiToken", ""]
        set(value) = settings.set("apiToken", value)

    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && apiToken.isNotBlank()
}
