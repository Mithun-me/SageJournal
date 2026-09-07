package com.aura.sagejournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 15+ enforces edge-to-edge anyway; opting in explicitly means
        // the inset padding in the bars is the single source of truth.
        //
        // Both bars are pinned to the dark style: Aura has no light theme, and
        // letting the system choose gave dark status icons on a near-black
        // composer, where the clock was barely legible.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        val hz = display?.refreshRate ?: 60f
        setContent { AuraApp(refreshHz = hz) }
    }
}
