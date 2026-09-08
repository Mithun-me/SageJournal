package com.aura.sagejournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.sagejournal.ui.theme.AuraFonts
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraDims
import com.aura.sagejournal.ui.theme.AuraShapes
import com.aura.sagejournal.ui.theme.AuraType

/**
 * 1b "Aura Bloom" You: profile, auth and settings on one sheet. No cards —
 * hierarchy is type and hairlines, so the rows are Material 3 ListItems with
 * dividers rather than a stack of bordered boxes.
 */
@Composable
fun YouScreen(
    avatarEmoji: String,
    name: String,
    syncState: String,
    streakDays: Int,
    entryCount: Int,
    points: Int,
    themeName: String,
    motionName: String,
    dailyReminder: Boolean,
    onDailyReminder: (Boolean) -> Unit,
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenMotion: () -> Unit,
    onExport: () -> Unit,
    onResetData: () -> Unit,
    hudOn: Boolean,
    onToggleHud: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Column(
            Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(82.dp)
                    .clip(AuraShapes.Pill)
                    .background(AuraColors.Primary.copy(alpha = 0.14f))
                    .border(1.dp, AuraColors.Primary.copy(alpha = 0.45f), AuraShapes.Pill),
                contentAlignment = Alignment.Center,
            ) { Text(avatarEmoji, fontSize = 34.sp) }

            Text(
                name,
                color = Color.White,
                fontSize = AuraType.title,
                fontFamily = AuraFonts.Display,
                fontWeight = AuraType.titleWeight,
            )
            Text(syncState, color = AuraColors.TextTertiary, fontSize = AuraType.bodySmall)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.09f))
        Row(
            Modifier.fillMaxWidth().padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Stat("$streakDays", "day streak", AuraColors.Warm)
            Stat("$entryCount", "entries", AuraColors.Primary)
            Stat("%,d".format(points), "points", AuraColors.Indigo)
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.09f))

        Column(
            Modifier.padding(vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Keep your reflections safe",
                color = Color.White,
                fontSize = 20.sp,
                fontFamily = AuraFonts.Display,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Sync so nothing is lost when you change phones. Only your " +
                    "nickname and entries are stored.",
                color = AuraColors.TextTertiary,
                fontSize = AuraType.body,
                lineHeight = 24.sp,
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(AuraShapes.Pill)
                    .background(AuraColors.Primary)
                    .clickable(onClick = onCreateAccount),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Create an account",
                    color = AuraColors.OnPrimary,
                    fontSize = AuraType.body,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                "I already have one",
                color = AuraColors.PrimarySoft,
                fontSize = AuraType.bodySmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AuraShapes.Pill)
                    .clickable(onClick = onSignIn)
                    .padding(vertical = 6.dp),
            )
        }

        SettingRow("Theme", themeName, onOpenTheme)
        SettingRow("Background motion", motionName, onOpenMotion)
        SwitchRow("Daily reminder", dailyReminder, onDailyReminder)
        SettingRow("Export my journal", null, onExport)
        SettingRow("Reset journal data", null, onResetData)
        SwitchRow("Frame stats overlay", hudOn, onToggleHud)
        HorizontalDivider(color = AuraColors.Hairline)

        Text(
            "Entries are encrypted at rest. Reflections are generated without " +
                "retaining your text.",
            color = AuraColors.TextMuted,
            fontSize = AuraType.label,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}

@Composable
private fun Stat(value: String, label: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = accent, fontSize = AuraType.title, fontWeight = AuraType.titleWeight)
        Text(label, color = AuraColors.TextTertiary, fontSize = AuraType.label)
    }
}

private val rowColors
    @Composable get() = ListItemDefaults.colors(containerColor = Color.Transparent)

@Composable
private fun SettingRow(label: String, value: String?, onClick: () -> Unit) {
    HorizontalDivider(color = AuraColors.Hairline)
    ListItem(
        headlineContent = {
            Text(
                label,
                color = AuraColors.TextPrimary,
                fontSize = AuraType.body,
                fontWeight = FontWeight.SemiBold,
            )
        },
        trailingContent = value?.let {
            { Text(it, color = AuraColors.TextMuted, fontSize = AuraType.bodySmall) }
        },
        colors = rowColors,
        modifier = Modifier.height(AuraDims.listRow).clickable(onClick = onClick),
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    HorizontalDivider(color = AuraColors.Hairline)
    ListItem(
        headlineContent = {
            Text(
                label,
                color = AuraColors.TextPrimary,
                fontSize = AuraType.body,
                fontWeight = FontWeight.SemiBold,
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AuraColors.SwitchOn,
                    checkedBorderColor = Color.Transparent,
                    uncheckedThumbColor = AuraColors.TextMuted,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.08f),
                    uncheckedBorderColor = AuraColors.Hairline,
                ),
            )
        },
        colors = rowColors,
        modifier = Modifier.height(AuraDims.listRow),
    )
}
