package com.aura.sagejournal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.aura.sagejournal.ui.components.GlassSurface
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraType

/** Ports not started yet. Named so the remaining work is visible in the app. */
@Composable
fun NotPortedYet(screen: String, webLines: Int, notes: String) {
    GlassSurface(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                screen,
                color = AuraColors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Not ported yet — $webLines lines in the web build.",
                color = AuraColors.Primary,
                fontSize = AuraType.bodySize,
                fontWeight = FontWeight.Bold,
            )
            Text(notes, color = AuraColors.TextSecondary, fontSize = AuraType.bodySize)
        }
    }
}

@Composable
fun SettingsPlaceholder(hudOn: Boolean, onToggleHud: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        NotPortedYet(
            "Settings", 333,
            "Theme picker, shader speed/intensity sliders, data reset. The shader " +
                "controls map to the palette and clock already wired up here.",
        )
        GlassSurface(Modifier.fillMaxWidth()) {
            Column(
                Modifier.clickable(onClick = onToggleHud).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    if (hudOn) "Hide frame stats" else "Show frame stats",
                    color = AuraColors.PrimaryBright,
                    fontSize = AuraType.cardTitleSize,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Measures the real screens rather than the synthetic gate.",
                    color = AuraColors.TextTertiary,
                    fontSize = AuraType.labelSize,
                )
            }
        }
    }
}
