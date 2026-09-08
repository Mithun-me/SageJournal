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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.sagejournal.data.AuraSettings
import com.aura.sagejournal.data.AuraStats
import com.aura.sagejournal.data.EntryStore
import com.aura.sagejournal.data.SettingsStore
import com.aura.sagejournal.dev.DevHud
import com.aura.sagejournal.domain.Mood
import com.aura.sagejournal.domain.SeedData
import com.aura.sagejournal.domain.TrendsSeed
import com.aura.sagejournal.ui.components.AuraNavBar
import com.aura.sagejournal.ui.components.AuraTab
import com.aura.sagejournal.ui.components.AuraTopBar
import com.aura.sagejournal.ui.components.LiquidBackground
import com.aura.sagejournal.ui.screens.ArchiveScreen
import com.aura.sagejournal.ui.screens.InsightsScreen
import com.aura.sagejournal.ui.screens.NewEntryScreen
import com.aura.sagejournal.ui.screens.NotPortedYet
import com.aura.sagejournal.ui.screens.TodayScreen
import com.aura.sagejournal.ui.screens.YouScreen
import com.aura.sagejournal.ui.shader.ShaderPalette
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraDims
import com.aura.sagejournal.ui.theme.AuraMaterialTheme
import kotlinx.coroutines.launch

/** The heading tracks the real calendar; it was pinned to the seed's "October". */
private fun currentMonthName(): String =
    java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault())
        .format(java.util.Date())

private val motionLevels = listOf("Gentle" to 0.5f, "Balanced" to 1.0f, "Dynamic" to 1.6f)

@Composable
fun AuraApp(refreshHz: Float) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val entryStore = remember { EntryStore(context) }
    val settingsStore = remember { SettingsStore(context) }

    LaunchedEffect(Unit) { entryStore.seedIfEmpty() }

    val entries by entryStore.entries.collectAsStateWithLifecycle(emptyList())
    val settings by settingsStore.settings.collectAsStateWithLifecycle(AuraSettings())
    val bloomDays by entryStore.bloom.collectAsStateWithLifecycle(emptyList())
    val today by entryStore.today.collectAsStateWithLifecycle(null)
    val stats by entryStore.stats.collectAsStateWithLifecycle(AuraStats())

    var tab by remember { mutableStateOf(AuraTab.Today) }
    var showYou by remember { mutableStateOf(false) }
    var writing by remember { mutableStateOf(false) }
    var archiveQuery by remember { mutableStateOf("") }

    val todayMood = today?.mood?.let { m -> runCatching { Mood.valueOf(m) }.getOrNull() }

    AuraMaterialTheme {
        if (writing) {
            NewEntryScreen(
                dateLabel = "Monday, 8:04 AM",
                prompt = "What is a small detail you noticed today that brought " +
                    "an unexpected sense of calm?",
                onSave = { title, body ->
                    scope.launch {
                        entryStore.save(title, body, todayMood ?: Mood.Calm)
                        writing = false
                    }
                },
                onDismiss = { writing = false },
            )
            return@AuraMaterialTheme
        }

        LiquidBackground(
            palette = if (settings.deepSea) ShaderPalette.DeepSea else ShaderPalette.LiquidGlass,
            speed = motionLevels[settings.motionIndex.coerceIn(0, 2)].second,
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    AuraTopBar(
                        streakDays = stats.streakDays,
                        avatarEmoji = "🌿",
                        onOpenYou = { showYou = !showYou },
                    )
                },
                bottomBar = {
                    AuraNavBar(
                        selected = tab,
                        onSelect = { tab = it; showYou = false },
                        onWrite = { writing = true },
                    )
                },
            ) { inner ->
                // 1b screens are specified on a flat sheet, not the live shader.
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
                                streakDays = stats.streakDays,
                                entryCount = stats.entryCount,
                                points = stats.points,
                                themeName = if (settings.deepSea) "Deep Sea" else "Liquid Glass",
                                motionName = motionLevels[
                                    settings.motionIndex.coerceIn(0, 2)
                                ].first,
                                dailyReminder = settings.dailyReminder,
                                onDailyReminder = {
                                    scope.launch { settingsStore.setDailyReminder(it) }
                                },
                                onCreateAccount = {},
                                onSignIn = {},
                                onOpenTheme = {
                                    scope.launch { settingsStore.setDeepSea(!settings.deepSea) }
                                },
                                onOpenMotion = {
                                    scope.launch {
                                        settingsStore.setMotion((settings.motionIndex + 1) % 3)
                                    }
                                },
                                onExport = {},
                                onResetData = { scope.launch { entryStore.reset() } },
                                hudOn = settings.showHud,
                                onToggleHud = { scope.launch { settingsStore.setShowHud(it) } },
                            )
                        } else when (tab) {
                            AuraTab.Today -> TodayScreen(
                                dateLabel = "Monday, 13 October",
                                greeting = "Good morning",
                                name = "Seeker",
                                affirmation = SeedData.affirmation,
                                entries = entries,
                                selectedMood = todayMood,
                                checkInsDone = today?.checkIns ?: 0,
                                checkInTarget = 4,
                                onSelectMood = { scope.launch { entryStore.checkIn(it) } },
                                onWriteFromQuote = { writing = true },
                                onViewAll = { tab = AuraTab.Archive },
                            )

                            AuraTab.Archive -> ArchiveScreen(
                                month = currentMonthName(),
                                summary = bloomDays.count { it.mood != null }.toString() +
                                    " check-ins · " + entries.size + " entries written",
                                days = bloomDays,
                                entries = entries.filter {
                                    archiveQuery.isBlank() ||
                                        it.title.contains(archiveQuery, true) ||
                                        it.content.contains(archiveQuery, true)
                                },
                                query = archiveQuery,
                                onQuery = { archiveQuery = it },
                            )

                            AuraTab.Breathe -> NotPortedYet(
                                "Breathe", 279,
                                "Not covered by the redesign, so the existing breathing " +
                                    "timer design still stands.",
                            )

                            AuraTab.Insights -> InsightsScreen(
                                totalPoints = stats.points,
                                streak = stats.streakDays,
                                week = TrendsSeed.week,
                                milestones = TrendsSeed.milestones,
                                aiInsight = "Clarity climbs on days you log before noon — " +
                                    "Friday and Sunday both started with a morning entry.",
                            )
                        }
                    }

                    if (settings.showHud) {
                        DevHud(refreshHz, Modifier.align(Alignment.TopEnd).padding(8.dp))
                    }
                }
            }
        }
    }
}
