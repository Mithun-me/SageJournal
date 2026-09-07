package com.aura.sagejournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraDims
import com.aura.sagejournal.ui.theme.AuraShapes
import com.aura.sagejournal.ui.theme.AuraType

/** Destinations. Write is an action in the same bar, not a destination. */
enum class AuraTab(val label: String, val icon: ImageVector) {
    Today("Today", Icons.AutoMirrored.Filled.MenuBook),
    Archive("Archive", Icons.Filled.CollectionsBookmark),
    Breathe("Breathe", Icons.Filled.Spa),
    Insights("Insights", Icons.Filled.AutoGraph),
}

/**
 * A single 56dp row. The old header carried a theme toggle and a sign-in
 * button; both moved to You, reached from the avatar, so this is chrome again
 * rather than a control panel.
 */
@Composable
fun AuraTopBar(
    streakDays: Int,
    avatarEmoji: String,
    onOpenYou: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            // Scaffold hands insets to content, not to a custom top bar slot,
            // so the 56dp row has to clear the status bar itself.
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(AuraDims.headerHeight)
            .padding(horizontal = AuraDims.screenH),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.AutoAwesome, null, Modifier.size(24.dp), AuraColors.Primary)
        Text(
            "  Aura",
            color = Color.White,
            fontSize = AuraType.brand,
            fontWeight = AuraType.brandWeight,
        )

        Box(Modifier.weight(1f))

        Row(
            Modifier
                .clip(AuraShapes.Pill)
                .background(Color.White.copy(alpha = 0.07f))
                .border(1.dp, AuraColors.CardStrokeRaised, AuraShapes.Pill)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Filled.LocalFireDepartment, null, Modifier.size(13.dp), AuraColors.Warm)
            Text(
                "$streakDays",
                color = AuraColors.WarmPale,
                fontSize = AuraType.meta,
                fontWeight = AuraType.cardTitleWeight,
            )
        }

        Box(
            Modifier
                .padding(start = 10.dp)
                .size(34.dp)
                .clip(AuraShapes.Pill)
                .background(AuraColors.Primary.copy(alpha = 0.16f))
                .border(1.dp, AuraColors.Primary.copy(alpha = 0.4f), AuraShapes.Pill)
                .clickable(onClick = onOpenYou),
            contentAlignment = Alignment.Center,
        ) {
            Text(avatarEmoji, fontSize = AuraType.bodySmall)
        }
    }
}

/**
 * Material 3's NavigationBar rather than a hand-rolled Row, so item sizing,
 * ripple, and accessibility semantics come from the platform. Write sits in
 * the middle as an action — the old floating FAB overlapped a tab, then a
 * link, then a card, and the redesign absorbs it here.
 */
@Composable
fun AuraNavBar(
    selected: AuraTab,
    onSelect: (AuraTab) -> Unit,
    onWrite: () -> Unit,
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = AuraColors.Primary,
        selectedTextColor = AuraColors.Primary,
        unselectedIconColor = AuraColors.TextMuted,
        unselectedTextColor = AuraColors.TextMuted,
        // The redesign shows no pill behind the active icon.
        indicatorColor = Color.Transparent,
    )

    Box {
        HorizontalDivider(color = AuraColors.Hairline)
        NavigationBar(
            containerColor = AuraColors.Chrome.copy(alpha = 0.96f),
            tonalElevation = 0.dp,
        ) {
            NavItem(AuraTab.Today, selected, onSelect, itemColors)
            NavItem(AuraTab.Archive, selected, onSelect, itemColors)

            NavigationBarItem(
                selected = false,
                onClick = onWrite,
                icon = {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(AuraShapes.Pill)
                            .background(AuraColors.Primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Edit, "Write", Modifier.size(20.dp), AuraColors.OnPrimary)
                    }
                },
                colors = itemColors,
            )

            NavItem(AuraTab.Breathe, selected, onSelect, itemColors)
            NavItem(AuraTab.Insights, selected, onSelect, itemColors)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NavItem(
    tab: AuraTab,
    selected: AuraTab,
    onSelect: (AuraTab) -> Unit,
    colors: androidx.compose.material3.NavigationBarItemColors,
) {
    NavigationBarItem(
        selected = tab == selected,
        onClick = { onSelect(tab) },
        icon = { Icon(tab.icon, null, Modifier.size(24.dp)) },
        label = {
            Text(tab.label, fontSize = AuraType.overline, fontWeight = AuraType.overlineWeight)
        },
        colors = colors,
    )
}
