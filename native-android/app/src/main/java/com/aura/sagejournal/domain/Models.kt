package com.aura.sagejournal.domain

import androidx.compose.ui.graphics.Color
import com.aura.sagejournal.ui.theme.AuraColors

/** Ported from src/types.ts. */
enum class Mood(
    val label: String,
    val shortLabel: String,
    val emoji: String,
    val accent: Color,
) {
    Calm("Calm", "Calm", "😌", AuraColors.Primary),
    Joy("Joy", "Joy", "✨", AuraColors.Warm),
    Reflective("Reflective", "Reflect", "🌿", AuraColors.Indigo),
    Low("Low", "Low", "🌧️", AuraColors.CoolBlue),
    Grounded("Grounded", "Ground", "🪨", AuraColors.Primary),
    Energetic("Energetic", "Charged", "⚡", AuraColors.Warm),
}

data class JournalEntry(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val time: String,
    val timestamp: Long,
    val mood: Mood,
    val isFavorite: Boolean = false,
    val isAudio: Boolean = false,
    val audioDuration: String? = null,
    val location: String? = null,
    val tags: List<String> = emptyList(),
    // Populated by the Gemini reflection call, which the native build does not
    // make yet. Null means "not generated", and the detail screen omits the
    // section rather than inventing one.
    val aiReflection: String? = null,
    val aiAffirmation: String? = null,
)

@kotlinx.serialization.Serializable
data class DailyAffirmation(
    val quote: String,
    val author: String,
    val source: String? = null,
    val reflection: String,
    val theme: String,
)

/** Mirrors INITIAL_ENTRIES / the fallback affirmation in the web build. */
object SeedData {
    val affirmation = DailyAffirmation(
        quote = "Smile, breathe and go slowly.",
        author = "Thích Nhất Hạnh",
        source = "Peace Is Every Step",
        reflection = "When you slow down your pace, you create space to witness " +
            "the stillness already present within you.",
        theme = "Presence",
    )

    val entries = listOf(
        JournalEntry(
            id = "entry-1", title = "Morning clarity by the window",
            content = "The light came in soft today and I noticed my breath before " +
                "I noticed my phone. A small win worth keeping.",
            date = "Today", time = "08:12", timestamp = 5, mood = Mood.Calm,
            isFavorite = true, location = "Home",
        ),
        JournalEntry(
            id = "entry-2", title = "A walk that reset the afternoon",
            content = "Twenty minutes outside undid two hours of tension. " +
                "Movement is the cheapest therapy I have.",
            date = "Today", time = "15:40", timestamp = 4, mood = Mood.Grounded,
        ),
        JournalEntry(
            id = "entry-3", title = "Voice note: unfinished thought",
            content = "Recorded on the way back, still turning it over.",
            date = "Yesterday", time = "19:05", timestamp = 3, mood = Mood.Reflective,
            isAudio = true, audioDuration = "1:24",
        ),
    )
}
