package com.homephone.companion.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.homephone.companion.ui.HomephoneCompanionApp

/**
 * Android is a stub target for now — Milestone 2 is iOS-first.
 * This just hosts the same shared Compose UI used on iOS; no
 * Android-specific logic lives here.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomephoneCompanionApp()
        }
    }
}
