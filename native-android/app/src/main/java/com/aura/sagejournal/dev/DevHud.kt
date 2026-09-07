package com.aura.sagejournal.dev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.aura.sagejournal.ui.theme.AuraColors

/** Frame pacing for the real screens, toggled from Settings. */
@Composable
fun DevHud(refreshHz: Float, modifier: Modifier = Modifier) {
    val stats = rememberFrameStats(refreshHz)
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.72f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Mono("${"%.1f".format(stats.fps)} fps @ ${"%.0f".format(refreshHz)}Hz")
        Mono("med ${"%.2f".format(stats.medianMs)}  p99 ${"%.2f".format(stats.p99Ms)} ms")
        Mono("jank ${"%.1f".format(stats.jankPercent)} %")
    }
}

@Composable
private fun Mono(text: String) {
    Text(text, color = AuraColors.PrimaryBright, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
}
