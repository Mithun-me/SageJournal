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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.sagejournal.domain.DailyAffirmation
import com.aura.sagejournal.domain.JournalEntry
import com.aura.sagejournal.domain.Mood
import com.aura.sagejournal.ui.components.GlassSurface
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraShapes
import com.aura.sagejournal.ui.theme.AuraType

@Composable
fun HomeScreen(
    affirmation: DailyAffirmation,
    entries: List<JournalEntry>,
    selectedMood: Mood?,
    onSelectMood: (Mood) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onViewAll: () -> Unit,
    onJournalQuote: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        AffirmationCard(affirmation, onJournalQuote)
        MoodSection(selectedMood, onSelectMood)
        DailyGoalCard()
        RecentEntries(entries, onToggleFavorite, onViewAll)
    }
}

@Composable
private fun AffirmationCard(affirmation: DailyAffirmation, onJournalQuote: () -> Unit) {
    GlassSurface(Modifier.fillMaxWidth(), AuraShapes.CardLarge) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(AuraShapes.Card)
                        .background(AuraColors.Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.AutoAwesome, null, Modifier.size(17.dp), AuraColors.Primary)
                }
                Text(
                    "DAILY AFFIRMATION",
                    color = AuraColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )
                Text(
                    affirmation.theme,
                    color = AuraColors.Primary,
                    fontSize = AuraType.labelSize,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(AuraShapes.Pill)
                        .background(AuraColors.Primary.copy(alpha = 0.15f))
                        .border(1.dp, AuraColors.Primary.copy(alpha = 0.3f), AuraShapes.Pill)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Icons.AutoMirrored.Filled.VolumeUp, Icons.Filled.ContentCopy,
                    Icons.Filled.Bookmark, Icons.Filled.Refresh,
                ).forEach { GhostIconButton(it) }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(AuraColors.Hairline))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    Icons.Filled.FormatQuote, null,
                    Modifier.size(26.dp), AuraColors.Primary.copy(alpha = 0.6f),
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "“${affirmation.quote}”",
                        color = AuraColors.TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 26.sp,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "— ${affirmation.author}",
                            color = AuraColors.Primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        affirmation.source?.let {
                            Text(
                                "· $it",
                                color = AuraColors.TextTertiary,
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                            )
                        }
                    }
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(AuraShapes.Card)
                    .background(AuraColors.Background.copy(alpha = 0.55f))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        Modifier.size(7.dp).clip(AuraShapes.Pill).background(AuraColors.Primary)
                    )
                    Text(
                        "Mindful Grounding",
                        color = AuraColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    affirmation.reflection,
                    color = AuraColors.TextSecondary,
                    fontSize = AuraType.bodySize,
                    lineHeight = 19.sp,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.Public, null, Modifier.size(15.dp), AuraColors.Primary)
                Text(
                    "Search Grounded",
                    color = AuraColors.TextTertiary,
                    fontSize = AuraType.labelSize,
                )
            }

            Text(
                "Journal this quote",
                color = AuraColors.Primary,
                fontSize = AuraType.bodySize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(AuraShapes.Pill)
                    .background(AuraColors.Primary.copy(alpha = 0.12f))
                    .border(1.dp, AuraColors.Primary.copy(alpha = 0.35f), AuraShapes.Pill)
                    .clickable(onClick = onJournalQuote)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun GhostIconButton(icon: ImageVector) {
    Box(
        Modifier
            .size(34.dp)
            .clip(AuraShapes.Card)
            .border(1.dp, AuraColors.Hairline, AuraShapes.Card),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, Modifier.size(16.dp), AuraColors.TextSecondary)
    }
}

@Composable
private fun MoodSection(selected: Mood?, onSelect: (Mood) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "How are you feeling today?",
            color = AuraColors.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // The web build surfaces only these four on Home.
            listOf(Mood.Calm, Mood.Joy, Mood.Reflective, Mood.Low).forEach { mood ->
                val active = mood == selected
                Column(
                    Modifier
                        .weight(1f)
                        .clip(AuraShapes.Card)
                        .background(
                            if (active) AuraColors.Primary.copy(alpha = 0.10f)
                            else AuraColors.Surface.copy(alpha = 0.7f)
                        )
                        .border(
                            1.dp,
                            if (active) AuraColors.Primary else AuraColors.Hairline,
                            AuraShapes.Card,
                        )
                        .clickable { onSelect(mood) }
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(mood.emoji, fontSize = 26.sp)
                    Text(
                        mood.label,
                        color = if (active) AuraColors.TextPrimary else AuraColors.TextSecondary,
                        fontSize = AuraType.bodySize,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyGoalCard() {
    var checkins by remember { mutableIntStateOf(3) }
    val target = 4
    val progress = checkins.toFloat() / target

    GlassSurface(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(Modifier.size(56.dp)) {
                    val stroke = 6.dp.toPx()
                    val inset = stroke / 2
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    drawArc(
                        color = Color.White.copy(alpha = 0.10f),
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = AuraColors.Primary,
                        startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                }
                Text(
                    "${(progress * 100).toInt()}%",
                    color = AuraColors.Primary,
                    fontSize = AuraType.labelSize,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "Daily Mindfulness Goal",
                    color = AuraColors.TextPrimary,
                    fontSize = AuraType.cardTitleSize,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "$checkins of $target mindful check-ins completed",
                    color = AuraColors.TextTertiary,
                    fontSize = AuraType.labelSize,
                )
            }

            if (checkins < target) {
                Text(
                    "+ Log",
                    color = AuraColors.PrimaryBright,
                    fontSize = AuraType.labelSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(AuraShapes.Pill)
                        .background(AuraColors.Primary.copy(alpha = 0.15f))
                        .border(1.dp, AuraColors.Primary.copy(alpha = 0.3f), AuraShapes.Pill)
                        .clickable { checkins = (checkins + 1).coerceAtMost(target) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                )
            } else {
                Text(
                    "Completed ✨",
                    color = AuraColors.PrimaryBright,
                    fontSize = AuraType.labelSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(AuraShapes.Pill)
                        .background(Color(0xFF005149))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun RecentEntries(
    entries: List<JournalEntry>,
    onToggleFavorite: (String) -> Unit,
    onViewAll: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Recent Entries",
                color = AuraColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "View All (${entries.size})",
                color = AuraColors.Primary,
                fontSize = AuraType.labelSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(AuraShapes.Pill)
                    .clickable(onClick = onViewAll)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        entries.take(3).forEach { entry ->
            GlassSurface(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(entry.mood.accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(entry.mood.emoji, fontSize = 20.sp)
                    }

                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            entry.title,
                            color = AuraColors.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        Text(
                            entry.content,
                            color = AuraColors.TextSecondary,
                            fontSize = AuraType.labelSize,
                            lineHeight = 17.sp,
                            maxLines = 2,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                "${entry.date} · ${entry.time}",
                                color = AuraColors.TextTertiary,
                                fontSize = AuraType.microSize,
                            )
                            if (entry.isAudio) {
                                Icon(
                                    Icons.Filled.Mic, null,
                                    Modifier.size(11.dp), AuraColors.Primary,
                                )
                                Text(
                                    entry.audioDuration ?: "",
                                    color = AuraColors.Primary,
                                    fontSize = AuraType.microSize,
                                )
                            }
                        }
                    }

                    Icon(
                        if (entry.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favourite",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(AuraShapes.Pill)
                            .clickable { onToggleFavorite(entry.id) }
                            .padding(6.dp),
                        tint = if (entry.isFavorite) AuraColors.Warm else AuraColors.TextTertiary,
                    )
                }
            }
        }
    }
}
