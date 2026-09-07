package com.aura.sagejournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 15+ enforces edge-to-edge anyway; opting in explicitly means
        // the inset padding in the bars is the single source of truth.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val hz = display?.refreshRate ?: 60f
        setContent { AuraApp(refreshHz = hz) }
    }
}
