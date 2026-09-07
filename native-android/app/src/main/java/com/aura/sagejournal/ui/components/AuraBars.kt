package com.aura.sagejournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraShapes
import com.aura.sagejournal.ui.theme.AuraType

enum class AuraTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.AutoMirrored.Filled.MenuBook),
    Archive("Archive", Icons.Filled.CollectionsBookmark),
    Sanctuary("Sanctuary", Icons.Filled.Spa),
    Insights("Insights", Icons.Filled.AutoGraph),
    Settings("Settings", Icons.Filled.Settings),
}

@Composable
fun AuraTopBar(
    streakDays: Int,
    deepSea: Boolean,
    onToggleTheme: () -> Unit,
    onOpenProfile: () -> Unit,
    onSignIn: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(AuraColors.Chrome.copy(alpha = 0.85f))
            // The simulated iOS status bar from the web build is gone; the OS
            // draws its own and we just inset past it.
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.clip(AuraShapes.Pill).clickable(onClick = onOpenProfile).padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(AuraShapes.Pill)
                    .background(
                        Brush.linearGradient(
                            listOf(AuraColors.GradientStart, AuraColors.GradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.AutoAwesome, null, Modifier.size(15.dp), Color.White)
            }
            Text(
                "Aura",
                color = Color.White,
                fontSize = AuraType.screenTitleSize,
                fontWeight = AuraType.screenTitleWeight,
            )
        }

        Box(Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                Modifier
                    .clip(AuraShapes.Pill)
                    .border(1.dp, AuraColors.Hairline, AuraShapes.Pill)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Icons.Filled.LocalFireDepartment, null,
                    Modifier.size(14.dp), AuraColors.Warm,
                )
                Text(
                    "${streakDays}d",
                    color = AuraColors.TextPrimary,
                    fontSize = AuraType.labelSize,
                    fontWeight = AuraType.cardTitleWeight,
                )
            }

            Box(
                Modifier
                    .size(30.dp)
                    .clip(AuraShapes.Pill)
                    .border(1.dp, AuraColors.Hairline, AuraShapes.Pill)
                    .clickable(onClick = onToggleTheme),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (deepSea) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    contentDescription = "Toggle theme",
                    modifier = Modifier.size(15.dp),
                    tint = AuraColors.Primary,
                )
            }

            Row(
                Modifier
                    .clip(AuraShapes.Pill)
                    .border(1.dp, AuraColors.Primary.copy(alpha = 0.5f), AuraShapes.Pill)
                    .clickable(onClick = onSignIn)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.Login, null, Modifier.size(14.dp), AuraColors.Primary)
                Text(
                    "Sign In",
                    color = AuraColors.Primary,
                    fontSize = AuraType.labelSize,
                    fontWeight = AuraType.cardTitleWeight,
                )
            }
        }
    }
}

@Composable
fun AuraBottomBar(selected: AuraTab, onSelect: (AuraTab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(AuraColors.Chrome.copy(alpha = 0.92f))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(top = 8.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuraTab.entries.forEach { tab ->
            val active = tab == selected
            Column(
                Modifier
                    .clip(AuraShapes.Card)
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    tab.icon, null,
                    Modifier.size(22.dp),
                    if (active) AuraColors.Primary else AuraColors.TextTertiary,
                )
                Text(
                    tab.label,
                    color = if (active) AuraColors.Primary else AuraColors.TextTertiary,
                    fontSize = AuraType.microSize,
                    fontWeight = AuraType.cardTitleWeight,
                )
            }
        }
    }
}

/** Sits clear above the tab bar, matching the web fix rather than the bug. */
@Composable
fun NewEntryFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(52.dp)
            .clip(AuraShapes.Pill)
            .background(
                Brush.linearGradient(listOf(AuraColors.GradientStart, AuraColors.GradientEnd))
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), AuraShapes.Pill)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Add, "New entry", Modifier.size(26.dp), Color.White)
    }
}
