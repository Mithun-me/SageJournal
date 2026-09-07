package com.aura.sagejournal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aura.sagejournal.dev.DevHud
import com.aura.sagejournal.domain.Mood
import com.aura.sagejournal.domain.BloomSeed
import com.aura.sagejournal.domain.SeedData
import com.aura.sagejournal.domain.TrendsSeed
import com.aura.sagejournal.ui.components.AuraNavBar
import com.aura.sagejournal.ui.components.AuraTab
import com.aura.sagejournal.ui.components.AuraTopBar
import com.aura.sagejournal.ui.components.LiquidBackground
import com.aura.sagejournal.ui.screens.ArchiveScreen
import com.aura.sagejournal.ui.screens.InsightsScreen
import com.aura.sagejournal.ui.screens.NotPortedYet
import com.aura.sagejournal.ui.screens.TodayScreen
import com.aura.sagejournal.ui.screens.YouScreen
import com.aura.sagejournal.ui.shader.ShaderPalette
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraDims
import com.aura.sagejournal.ui.theme.AuraMaterialTheme

private val motionNames = listOf("Gentle" to 0.5f, "Balanced" to 1.0f, "Dynamic" to 1.6f)

@Composable
fun AuraApp(refreshHz: Float) {
    var tab by remember { mutableStateOf(AuraTab.Today) }
    var showYou by remember { mutableStateOf(false) }
    var deepSea by remember { mutableStateOf(false) }
    var selectedMood by remember { mutableStateOf<Mood?>(Mood.Calm) }
    var checkIns by remember { mutableIntStateOf(3) }
    var showHud by remember { mutableStateOf(false) }
    var motionIndex by remember { mutableIntStateOf(1) }
    var dailyReminder by remember { mutableStateOf(true) }
    var archiveQuery by remember { mutableStateOf("") }
    var shaderIntensity by remember { mutableFloatStateOf(1f) }
    val entries = remember { SeedData.entries.toMutableStateList() }

    AuraMaterialTheme {
        LiquidBackground(
            palette = if (deepSea) ShaderPalette.DeepSea else ShaderPalette.LiquidGlass,
            speed = motionNames[motionIndex].second,
            intensity = shaderIntensity,
        ) {
            Scaffold(
                // Transparent so the shader stays visible behind the chrome.
                containerColor = Color.Transparent,
                topBar = {
                    AuraTopBar(
                        streakDays = 7,
                        avatarEmoji = "🌿",
                        onOpenYou = { showYou = !showYou },
                    )
                },
                bottomBar = {
                    AuraNavBar(
                        selected = tab,
                        onSelect = { tab = it; showYou = false },
                        onWrite = { /* 1b New Entry · Dictating — not built yet */ },
                    )
                },
            ) { inner ->
                // 1b screens are specified on a flat sheet, not the live
                // shader. Without it the low-alpha heatmap dots blend with the
                // moving background and the mood colours go muddy.
                val onSheet = showYou || tab == AuraTab.Archive
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(inner)
                        .background(
                            if (onSheet) AuraColors.BackgroundAlt.copy(alpha = 0.94f)
                            else Color.Transparent
                        )
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = AuraDims.screenH)
                            .padding(top = 14.dp, bottom = 32.dp),
                    ) {
                        if (showYou) {
                            YouScreen(
                                avatarEmoji = "🌿",
                                name = "Mindful Seeker",
                                syncState = "On this device only",
                                streakDays = 7,
                                entryCount = entries.size,
                                points = 2450,
                                themeName = if (deepSea) "Deep Sea" else "Liquid Glass",
                                motionName = motionNames[motionIndex].first,
                                dailyReminder = dailyReminder,
                                onDailyReminder = { dailyReminder = it },
                                onCreateAccount = {},
                                onSignIn = {},
                                onOpenTheme = { deepSea = !deepSea },
                                onOpenMotion = {
                                    motionIndex = (motionIndex + 1) % motionNames.size
                                },
                                onExport = {},
                                hudOn = showHud,
                                onToggleHud = { showHud = it },
                            )
                        } else when (tab) {
                            AuraTab.Today -> TodayScreen(
                                dateLabel = "Monday, 13 October",
                                greeting = "Good morning",
                                name = "Seeker",
                                affirmation = SeedData.affirmation,
                                entries = entries,
                                selectedMood = selectedMood,
                                checkInsDone = checkIns,
                                checkInTarget = 4,
                                onSelectMood = {
                                    selectedMood = it
                                    if (checkIns < 4) checkIns++
                                },
                                onWriteFromQuote = {},
                                onViewAll = { tab = AuraTab.Archive },
                            )

                            AuraTab.Archive -> ArchiveScreen(
                                month = BloomSeed.month,
                                summary = BloomSeed.summary,
                                days = BloomSeed.weeks,
                                entries = entries,
                                query = archiveQuery,
                                onQuery = { archiveQuery = it },
                            )

                            AuraTab.Breathe -> NotPortedYet(
                                "Breathe", 279,
                                "Not covered by the redesign, so the existing breathing " +
                                    "timer design still stands.",
                            )

                            AuraTab.Insights -> InsightsScreen(
                                totalPoints = 2450,
                                streak = 7,
                                week = TrendsSeed.week,
                                milestones = TrendsSeed.milestones,
                                aiInsight = "Clarity climbs on days you log before noon — " +
                                    "Friday and Sunday both started with a morning entry.",
                            )
                        }
                    }

                    if (showHud) {
                        DevHud(refreshHz, Modifier.align(Alignment.TopEnd).padding(8.dp))
                    }
                }
            }
        }
    }
}
