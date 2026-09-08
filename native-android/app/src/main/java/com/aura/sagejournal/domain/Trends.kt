package com.aura.sagejournal.domain

/** Mirrors TrendDataPoint in TrendsScreen.tsx. */
data class TrendPoint(
    val name: String,
    val fullDate: String,
    val mood: Mood?,        // null when there was no check-in
    val clarityScore: Int,   // 0..100, left axis
    val pointsEarned: Int,   // 0..120, right axis
    val entriesCount: Int,
    val notes: String,
)

data class Milestone(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val achieved: Boolean,
    val progress: Int = 0,
    val target: Int = 0,
)

/** MOOD_SCORE_MAP from the web build. */
val moodClarityScore: Map<Mood, Int> = mapOf(
    Mood.Joy to 92,
    Mood.Calm to 80,
    Mood.Reflective to 72,
    Mood.Grounded to 78,
    Mood.Energetic to 85,
    Mood.Low to 40,
)

object TrendsSeed {
    val week = listOf(
        TrendPoint("Mon", "Mon 1 Sep", Mood.Reflective, 72, 40, 1, "Slow start, found footing by evening."),
        TrendPoint("Tue", "Tue 2 Sep", Mood.Calm, 80, 55, 2, "Two entries, both before noon."),
        TrendPoint("Wed", "Wed 3 Sep", Mood.Low, 40, 20, 1, "Rough day. Logged it anyway."),
        TrendPoint("Thu", "Thu 4 Sep", Mood.Grounded, 78, 70, 2, "Walk in the afternoon reset things."),
        TrendPoint("Fri", "Fri 5 Sep", Mood.Joy, 92, 95, 3, "Best day of the week by a distance."),
        TrendPoint("Sat", "Sat 6 Sep", Mood.Calm, 80, 60, 2, "Quiet morning, no rush."),
        TrendPoint("Sun", "Sun 7 Sep", Mood.Energetic, 85, 80, 2, "Planned the week, felt ahead."),
    )

    val milestones = listOf(
        Milestone("First Light", "Wrote your first entry", "🌅", achieved = true),
        Milestone("Seven Days", "A full week unbroken", "🔥", achieved = true),
        Milestone("Quiet Mind", "10 breathing sessions", "🧘", achieved = false, progress = 6, target = 10),
        Milestone("Deep Archive", "50 entries recorded", "📚", achieved = false, progress = 13, target = 50),
    )
}
