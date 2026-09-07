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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.sagejournal.domain.DailyAffirmation
import com.aura.sagejournal.domain.JournalEntry
import com.aura.sagejournal.domain.Mood
import com.aura.sagejournal.ui.components.GlassSurface
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraDims
import com.aura.sagejournal.ui.theme.AuraShapes
import com.aura.sagejournal.ui.theme.AuraType

/**
 * 1a "Still Water". Three surfaces only: mood chips, one grounding card, entry
 * cards. The greeting deliberately sits on the shader with no card behind it —
 * the first screen where the background is actually visible.
 */
@Composable
fun TodayScreen(
    dateLabel: String,
    greeting: String,
    name: String,
    affirmation: DailyAffirmation,
    entries: List<JournalEntry>,
    selectedMood: Mood?,
    checkInsDone: Int,
    checkInTarget: Int,
    onSelectMood: (Mood) -> Unit,
    onWriteFromQuote: () -> Unit,
    onViewAll: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                dateLabel,
                color = AuraColors.PrimaryPale,
                fontSize = AuraType.meta,
                fontWeight = FontWeight.Medium,
            )
            Text(
                "$greeting,\n$name.",
                color = Color.White,
                fontSize = AuraType.display,
                fontWeight = AuraType.displayWeight,
                lineHeight = 38.sp,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "How is it landing?",
                    color = AuraColors.TextPrimary,
                    fontSize = AuraType.sectionHead,
                    fontWeight = AuraType.sectionWeight,
                )
                Text(
                    "$checkInsDone of $checkInTarget today",
                    color = AuraColors.PrimarySoft,
                    fontSize = AuraType.label,
                    fontWeight = FontWeight.Medium,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                listOf(Mood.Calm, Mood.Joy, Mood.Reflective, Mood.Low).forEach { mood ->
                    MoodChip(mood, mood == selectedMood, Modifier.weight(1f)) { onSelectMood(mood) }
                }
            }
        }

        GroundingCard(affirmation, onWriteFromQuote)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Lately",
                    color = AuraColors.TextPrimary,
                    fontSize = AuraType.sectionHead,
                    fontWeight = AuraType.sectionWeight,
                )
                Text(
                    "All ${entries.size}",
                    color = AuraColors.Primary,
                    fontSize = AuraType.meta,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(AuraShapes.Pill)
                        .clickable(onClick = onViewAll)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                )
            }
            entries.take(2).forEach { EntryCard(it) }
        }
    }
}

@Composable
private fun MoodChip(mood: Mood, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .height(AuraDims.moodChip)
            .clip(AuraShapes.Chip)
            .background(
                if (selected) AuraColors.Primary.copy(alpha = 0.14f)
                else AuraColors.Chip.copy(alpha = 0.72f)
            )
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) AuraColors.Primary else Color.White.copy(alpha = 0.09f),
                AuraShapes.Chip,
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(mood.emoji, fontSize = 28.sp)
        Text(
            mood.shortLabel,
            color = if (selected) Color.White else AuraColors.TextSecondary,
            fontSize = AuraType.label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun GroundingCard(affirmation: DailyAffirmation, onWrite: () -> Unit) {
    GlassSurface(
        Modifier.fillMaxWidth(),
        shape = AuraShapes.CardRaised,
        tint = AuraColors.CardRaised.copy(alpha = 0.90f),
        stroke = AuraColors.CardStrokeRaised,
    ) {
        Column(
            Modifier.padding(horizontal = 23.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "TODAY'S GROUNDING",
                color = AuraColors.PrimarySoft,
                fontSize = AuraType.overline,
                fontWeight = AuraType.overlineWeight,
                letterSpacing = 1.sp,
            )
            Text(
                affirmation.quote,
                color = Color.White,
                fontSize = AuraType.quote,
                fontWeight = AuraType.quoteWeight,
                lineHeight = 28.sp,
            )
            Text(
                listOfNotNull(affirmation.author, affirmation.source).joinToString(" · "),
                color = AuraColors.TextTertiary,
                fontSize = AuraType.meta,
            )
            Text(
                affirmation.reflection,
                color = AuraColors.TextSecondary,
                fontSize = AuraType.bodySmall,
                lineHeight = 22.sp,
            )
            // The web build stacked eight icon buttons here; the redesign
            // collapses them to this one.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(AuraShapes.Cta)
                    .background(AuraColors.Primary.copy(alpha = 0.16f))
                    .border(1.dp, AuraColors.Primary.copy(alpha = 0.4f), AuraShapes.Cta)
                    .clickable(onClick = onWrite),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Write from this",
                    color = AuraColors.PrimaryBright,
                    fontSize = AuraType.bodySmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun EntryCard(entry: JournalEntry) {
    GlassSurface(Modifier.fillMaxWidth(), shape = AuraShapes.Card) {
        Row(
            Modifier.padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(AuraShapes.Thumb)
                    .background(entry.mood.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(entry.mood.emoji, fontSize = 26.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${entry.time} · ${entry.date.lowercase()}",
                    color = AuraColors.PrimarySoft,
                    fontSize = AuraType.label,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    entry.title,
                    color = Color.White,
                    fontSize = AuraType.cardTitle,
                    fontWeight = AuraType.cardTitleWeight,
                    maxLines = 1,
                )
                Text(
                    entry.content,
                    color = AuraColors.TextTertiary,
                    fontSize = AuraType.meta,
                    lineHeight = 20.sp,
                    maxLines = 2,
                )
            }
        }
    }
}
