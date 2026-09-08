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
import com.aura.sagejournal.domain.Milestone
import com.aura.sagejournal.domain.Mood
import com.aura.sagejournal.domain.SeedData
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

/**
 * Milestone definitions are static, but whether they are earned is not. These
 * were shipping with two hardcoded as achieved regardless of the journal.
 * "Quiet Mind" stays at zero because breathing sessions are not tracked yet —
 * better an honest empty bar than an invented one.
 */
private fun milestonesFor(stats: AuraStats): List<Milestone> = listOf(
    Milestone(
        "First Light", "Wrote your first entry", "🌅",
        achieved = stats.entryCount >= 1,
    ),
    Milestone(
        "Seven Days", "A full week unbroken", "🔥",
        achieved = stats.streakDays >= 7,
        progress = stats.streakDays.coerceAtMost(7), target = 7,
    ),
    Milestone(
        "Quiet Mind", "10 breathing sessions", "🧘",
        achieved = false, progress = 0, target = 10,
    ),
    Milestone(
        "Deep Archive", "50 entries recorded", "📚",
        achieved = stats.entryCount >= 50,
        progress = stats.entryCount.coerceAtMost(50), target = 50,
    ),
)

/**
 * A summary the data actually supports. The previous copy asserted specific
 * facts — which days had morning entries — about days that did not exist.
 */
private fun weeklyInsight(week: List<com.aura.sagejournal.domain.TrendPoint>): String {
    val logged = week.filter { it.mood != null }
    val written = week.sumOf { it.entriesCount }
    if (logged.isEmpty()) {
        return "Nothing logged in the last seven days yet. Check in for a few " +
            "days and patterns will show up here."
    }
    val best = logged.maxByOrNull { it.clarityScore }
    val head = "${logged.size} of 7 days logged, $written " +
        (if (written == 1) "entry" else "entries") + " written."
    return if (logged.size < 3) {
        "$head A few more days and this will be worth reading."
    } else {
        "$head Your clearest day was ${best?.name ?: "-"}, checking in as " +
            "${best?.mood?.label?.lowercase() ?: "-"}."
    }
}

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
    val week by entryStore.weekTrend.collectAsStateWithLifecycle(emptyList())

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
                                week = week,
                                milestones = milestonesFor(stats),
                                aiInsight = weeklyInsight(week),
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
