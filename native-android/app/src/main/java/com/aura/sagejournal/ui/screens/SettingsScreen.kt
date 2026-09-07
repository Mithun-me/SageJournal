package com.aura.sagejournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.sagejournal.ui.components.GlassSurface
import com.aura.sagejournal.ui.shader.ShaderPalette
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraShapes
import com.aura.sagejournal.ui.theme.AuraType
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    palette: ShaderPalette,
    onSelectPalette: (ShaderPalette) -> Unit,
    shaderSpeed: Float,
    onShaderSpeed: (Float) -> Unit,
    shaderIntensity: Float,
    onShaderIntensity: (Float) -> Unit,
    entryCount: Int,
    onResetData: () -> Unit,
    hudOn: Boolean,
    onToggleHud: () -> Unit,
) {
    var dailyReminder by remember { mutableStateOf(true) }
    var haptics by remember { mutableStateOf(true) }
    var confirmingReset by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Settings",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
        )

        Section("Aesthetic Themes", Icons.Filled.Palette) {
            ThemeCard(
                name = "Liquid Glass Aura",
                blurb = "Deep oceanic midnight with luminous electric teal and " +
                    "vibrant indigo liquid refraction.",
                swatches = listOf(Color(0xFF101221), Color(0xFF4F46E5), Color(0xFF4FDBC8)),
                selected = palette == ShaderPalette.LiquidGlass,
                accent = AuraColors.Primary,
                onClick = { onSelectPalette(ShaderPalette.LiquidGlass) },
            )
            ThemeCard(
                name = "Deep Sea Hydro",
                blurb = "Serene abyssal aqua gradients inspired by restorative " +
                    "coastal deep water and soft sand.",
                swatches = listOf(Color(0xFF004F56), Color(0xFF85D3DD), Color(0xFFFAF7F2)),
                selected = palette == ShaderPalette.DeepSea,
                accent = Color(0xFF71D7CD),
                onClick = { onSelectPalette(ShaderPalette.DeepSea) },
            )
        }

        Section("Shader Motion", Icons.Filled.Speed) {
            LabelledSlider(
                label = "Shader Flow Speed",
                readout = when {
                    shaderSpeed < 0.7f -> "Gentle Flow"
                    shaderSpeed <= 1.2f -> "Balanced"
                    else -> "Dynamic"
                },
                value = shaderSpeed,
                range = 0.3f..2.0f,
                steps = 16,
                onChange = onShaderSpeed,
            )
            LabelledSlider(
                label = "Wave Distortion Intensity",
                readout = "${(shaderIntensity * 100).roundToInt()}%",
                value = shaderIntensity,
                range = 0.3f..1.8f,
                steps = 14,
                onChange = onShaderIntensity,
            )
            Text(
                "Both drive the live background. It re-rasterises at most 30 times " +
                    "a second whatever the display refresh, so speed changes how it " +
                    "moves without changing what it costs.",
                color = AuraColors.TextTertiary,
                fontSize = AuraType.microSize,
                lineHeight = 15.sp,
            )
        }

        Section("Preferences", Icons.Filled.Notifications) {
            ToggleRow(
                Icons.Filled.Notifications, "Daily reminder",
                "A nudge to write, once a day.", dailyReminder,
            ) { dailyReminder = it }
            ToggleRow(
                Icons.Filled.Vibration, "Haptics",
                "Tactile feedback on taps and saves.", haptics,
            ) { haptics = it }
        }

        Section("Account", Icons.Filled.CloudQueue) {
            Text(
                "Not wired up in the native build yet. Firebase auth and Firestore " +
                    "sync still live only in the Capacitor app.",
                color = AuraColors.TextSecondary,
                fontSize = AuraType.bodySize,
                lineHeight = 19.sp,
            )
        }

        Section("Data", Icons.Filled.Storage) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "$entryCount entries stored",
                        color = AuraColors.TextPrimary,
                        fontSize = AuraType.bodySize,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "In memory only — persistence is not ported.",
                        color = AuraColors.TextTertiary,
                        fontSize = AuraType.microSize,
                    )
                }
                Icon(Icons.Filled.Download, null, Modifier.size(18.dp), AuraColors.TextTertiary)
            }

            // Two-step, because the web build wipes entries on a single tap.
            Text(
                if (confirmingReset) "Tap again to confirm reset" else "Reset journal data",
                color = if (confirmingReset) Color(0xFFFF8A8A) else AuraColors.TextSecondary,
                fontSize = AuraType.bodySize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AuraShapes.Card)
                    .background(
                        if (confirmingReset) Color(0xFFFF8A8A).copy(alpha = 0.12f)
                        else Color.White.copy(alpha = 0.05f)
                    )
                    .border(
                        1.dp,
                        if (confirmingReset) Color(0xFFFF8A8A).copy(alpha = 0.45f)
                        else AuraColors.Hairline,
                        AuraShapes.Card,
                    )
                    .clickable {
                        if (confirmingReset) {
                            onResetData(); confirmingReset = false
                        } else confirmingReset = true
                    }
                    .padding(14.dp),
            )
        }

        Section("Developer", Icons.Filled.Bolt) {
            ToggleRow(
                Icons.Filled.Speed, "Frame stats overlay",
                "Measures the real screens, not a synthetic scene.", hudOn,
            ) { onToggleHud() }
        }
    }
}

@Composable
private fun Section(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassSurface(Modifier.fillMaxWidth(), AuraShapes.CardLarge) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(icon, null, Modifier.size(16.dp), AuraColors.Primary)
                Text(
                    title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            content()
        }
    }
}

@Composable
private fun ThemeCard(
    name: String,
    blurb: String,
    swatches: List<Color>,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(AuraShapes.Card)
            .background(
                if (selected) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.04f)
            )
            .border(1.dp, if (selected) accent else AuraColors.Hairline, AuraShapes.Card)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                name,
                color = Color.White,
                fontSize = AuraType.bodySize,
                fontWeight = FontWeight.Bold,
            )
            if (selected) Icon(Icons.Filled.Check, null, Modifier.size(16.dp), accent)
        }
        Text(
            blurb,
            color = AuraColors.TextSecondary,
            fontSize = AuraType.labelSize,
            lineHeight = 17.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            swatches.forEach { c ->
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(AuraShapes.Pill)
                        .background(c)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), AuraShapes.Pill)
                )
            }
        }
    }
}

@Composable
private fun LabelledSlider(
    label: String,
    readout: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onChange: (Float) -> Unit,
) {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, color = AuraColors.TextSecondary, fontSize = AuraType.labelSize)
            Text(
                readout,
                color = AuraColors.Primary,
                fontSize = AuraType.labelSize,
                fontWeight = FontWeight.Bold,
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = AuraColors.Primary,
                activeTrackColor = AuraColors.Primary,
                inactiveTrackColor = Color.White.copy(alpha = 0.12f),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, Modifier.size(17.dp), AuraColors.TextTertiary)
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = AuraColors.TextPrimary,
                fontSize = AuraType.bodySize,
                fontWeight = FontWeight.Bold,
            )
            Text(subtitle, color = AuraColors.TextTertiary, fontSize = AuraType.microSize)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AuraColors.OnPrimary,
                checkedTrackColor = AuraColors.Primary,
                uncheckedThumbColor = AuraColors.TextTertiary,
                uncheckedTrackColor = Color.White.copy(alpha = 0.08f),
                uncheckedBorderColor = AuraColors.Hairline,
            ),
        )
    }
}
