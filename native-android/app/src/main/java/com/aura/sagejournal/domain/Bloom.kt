package com.aura.sagejournal.domain

/** Where a day sits relative to now, which the redesign renders differently. */
enum class DayState { Past, Today, Future }

/**
 * One cell of the bloom heatmap. Mood drives colour, [words] drives both the
 * dot's diameter and its opacity — the legend reads "Bigger dot = more
 * written", and the frames vary alpha alongside size.
 */
data class BloomDay(
    val label: String,
    val mood: Mood?,
    val words: Int,
    val state: DayState = DayState.Past,
)

object BloomSeed {
    // Five weeks ending today, Monday-first. null mood = no check-in.
    private val script: List<Pair<Mood?, Int>> = listOf(
        // week 1
        null to 0, Mood.Calm to 90, Mood.Joy to 180, Mood.Reflective to 140,
        Mood.Calm to 70, null to 0, Mood.Calm to 55,
        // week 2
        Mood.Calm to 210, Mood.Joy to 95, Mood.Calm to 45, Mood.Reflective to 250,
        Mood.Joy to 290, Mood.Calm to 40, null to 0,
        // week 3
        Mood.Reflective to 60, Mood.Calm to 150, Mood.Calm to 240, Mood.Joy to 120,
        Mood.Low to 65, Mood.Calm to 85, Mood.Calm to 30,
        // week 4
        Mood.Joy to 200, Mood.Calm to 100, Mood.Reflective to 160, Mood.Calm to 190,
        Mood.Joy to 320, Mood.Calm to 50, Mood.Low to 35,
        // week 5 — trails into today and then the future
        Mood.Calm to 110, Mood.Joy to 100, Mood.Calm to 300, null to 0,
        null to 0, null to 0, null to 0,
    )

    val weeks: List<BloomDay> = script.mapIndexed { i, (mood, words) ->
        val state = when {
            i < 31 -> DayState.Past
            i == 31 -> DayState.Today
            else -> DayState.Future
        }
        BloomDay("day ${i + 1}", mood, words, state)
    }

    const val month = "October"
    const val summary = "22 check-ins · mostly calm, brightening at the weekends"
}
