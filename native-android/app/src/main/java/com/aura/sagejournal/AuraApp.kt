package com.aura.sagejournal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aura.sagejournal.dev.DevHud
import com.aura.sagejournal.domain.Mood
import com.aura.sagejournal.domain.SeedData
import com.aura.sagejournal.ui.components.AuraBottomBar
import com.aura.sagejournal.ui.components.AuraTab
import com.aura.sagejournal.ui.components.AuraTopBar
import com.aura.sagejournal.ui.components.LiquidBackground
import com.aura.sagejournal.ui.components.NewEntryFab
import com.aura.sagejournal.ui.screens.HomeScreen
import com.aura.sagejournal.ui.screens.NotPortedYet
import com.aura.sagejournal.ui.screens.SettingsPlaceholder
import com.aura.sagejournal.ui.shader.ShaderPalette

@Composable
fun AuraApp(refreshHz: Float) {
    var tab by remember { mutableStateOf(AuraTab.Home) }
    var deepSea by remember { mutableStateOf(false) }
    var selectedMood by remember { mutableStateOf<Mood?>(Mood.Calm) }
    var showHud by remember { mutableStateOf(false) }
    val entries = remember { SeedData.entries.toMutableStateList() }

    LiquidBackground(
        palette = if (deepSea) ShaderPalette.DeepSea else ShaderPalette.LiquidGlass,
    ) {
        Column(Modifier.fillMaxSize()) {
            AuraTopBar(
                streakDays = 7,
                deepSea = deepSea,
                onToggleTheme = { deepSea = !deepSea },
                onOpenProfile = {},
                onSignIn = {},
            )

            // The FAB lives inside the scroll region, so it clears the tab bar
            // by construction rather than by a hand-tuned offset.
            Box(Modifier.weight(1f)) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    when (tab) {
                        AuraTab.Home -> HomeScreen(
                            affirmation = SeedData.affirmation,
                            entries = entries,
                            selectedMood = selectedMood,
                            onSelectMood = { selectedMood = it },
                            onToggleFavorite = { id ->
                                val i = entries.indexOfFirst { it.id == id }
                                if (i >= 0) {
                                    entries[i] = entries[i].copy(
                                        isFavorite = !entries[i].isFavorite
                                    )
                                }
                            },
                            onViewAll = { tab = AuraTab.Archive },
                            onJournalQuote = {},
                        )

                        AuraTab.Archive -> NotPortedYet(
                            "Archive", 469,
                            "Masonry grid, mood/audio/favourite filters, search. The " +
                                "grid spans map onto a staggered lazy grid.",
                        )

                        AuraTab.Sanctuary -> NotPortedYet(
                            "Sanctuary", 279,
                            "Breathing timer with three techniques. The Web Audio " +
                                "chimes should ship as audio assets, not runtime synthesis.",
                        )

                        AuraTab.Insights -> NotPortedYet(
                            "Insights", 625,
                            "The largest screen. Recharts needs replacing with Vico, " +
                                "and the custom glass tooltip rebuilt.",
                        )

                        AuraTab.Settings -> SettingsPlaceholder(showHud) { showHud = !showHud }
                    }
                }

                NewEntryFab(
                    onClick = {},
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                )

                if (showHud) {
                    DevHud(
                        refreshHz,
                        Modifier.align(Alignment.TopEnd).padding(8.dp),
                    )
                }
            }

            AuraBottomBar(tab) { tab = it }
        }
    }
}
