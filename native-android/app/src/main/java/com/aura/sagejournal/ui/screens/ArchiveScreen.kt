package com.aura.sagejournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.sagejournal.domain.BloomDay
import com.aura.sagejournal.domain.JournalEntry
import com.aura.sagejournal.domain.Mood
import com.aura.sagejournal.ui.components.BloomHeatmap
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraShapes
import com.aura.sagejournal.ui.theme.AuraType

/**
 * 1b "Bloom Heatmap" Archive. No card chrome: the month reads as a title, the
 * heatmap carries the summary, and entries hang off a hairline rail.
 */
@Composable
fun ArchiveScreen(
    month: String,
    summary: String,
    days: List<BloomDay>,
    entries: List<JournalEntry>,
    query: String,
    onQuery: (String) -> Unit,
    onOpenEntry: (JournalEntry) -> Unit,
) {
    var selected by remember { mutableStateOf<Int?>(null) }
    val selectedDay = selected?.let(days::getOrNull)

    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                month,
                color = Color.White,
                fontSize = AuraType.display,
                fontWeight = AuraType.displayWeight,
            )
            Text(
                selectedDay?.let { d ->
                    if (d.mood == null) "No check-in that day"
                    else "${d.mood.label} · ${d.words} words written"
                } ?: summary,
                color = AuraColors.TextTertiary,
                fontSize = AuraType.bodySmall,
                lineHeight = 21.sp,
            )
        }

        BloomHeatmap(
            days = days,
            selectedIndex = selected,
            onSelect = { selected = if (selected == it) null else it },
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Bigger dot = more written",
                color = AuraColors.TextMuted,
                fontSize = AuraType.label,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LegendDot(Mood.Calm)
                LegendDot(Mood.Joy)
                LegendDot(Mood.Reflective)
            }
        }

        TextField(
            value = query,
            onValueChange = onQuery,
            placeholder = {
                Text(
                    "Search your journal",
                    color = AuraColors.TextPlaceholder,
                    fontSize = AuraType.body,
                )
            },
            leadingIcon = {
                Icon(Icons.Filled.Search, null, Modifier.size(19.dp), AuraColors.TextMuted)
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            textStyle = TextStyle(fontSize = AuraType.body, color = AuraColors.TextPrimary),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AuraColors.Primary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Column {
            entries.forEachIndexed { i, entry ->
                TimelineEntry(entry, isLast = i == entries.lastIndex) { onOpenEntry(entry) }
            }
        }
    }
}

@Composable
private fun LegendDot(mood: Mood) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(9.dp).clip(AuraShapes.Pill).background(mood.accent.copy(alpha = 0.8f)))
        Text(mood.shortLabel, color = AuraColors.TextMuted, fontSize = AuraType.overline)
    }
}

/** Entries hang off a hairline rail rather than sitting in cards. */
@Composable
private fun TimelineEntry(entry: JournalEntry, isLast: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(
            Modifier.width(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .padding(top = 5.dp)
                    .size(11.dp)
                    .clip(AuraShapes.Pill)
                    .background(entry.mood.accent)
            )
            if (!isLast) {
                Box(
                    Modifier
                        .padding(top = 4.dp)
                        .width(1.dp)
                        .height(96.dp)
                        .background(AuraColors.Hairline)
                )
            }
        }

        Column(
            Modifier.padding(start = 12.dp, bottom = if (isLast) 0.dp else 22.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                "${entry.date} · ${entry.location ?: entry.time}",
                color = AuraColors.PrimarySoft,
                fontSize = AuraType.label,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                entry.title,
                color = Color.White,
                fontSize = AuraType.cardTitle,
                fontWeight = AuraType.cardTitleWeight,
            )
            Text(
                entry.content,
                color = AuraColors.TextTertiary,
                fontSize = AuraType.meta,
                lineHeight = 21.sp,
            )
        }
    }
}
