package com.aura.sagejournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.sagejournal.domain.JournalEntry
import com.aura.sagejournal.ui.theme.AuraFonts
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraShapes
import com.aura.sagejournal.ui.theme.AuraType

/**
 * 1a Entry Detail: a reading screen. Flat ground rather than the shader, so
 * long prose has an even background to sit on.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EntryDetailScreen(
    entry: JournalEntry,
    onBack: () -> Unit,
    onToggleFavourite: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF080A12))
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)
            Box(Modifier.weight(1f))
            RoundButton(
                if (entry.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                "Favourite",
                onToggleFavourite,
                tint = if (entry.isFavorite) AuraColors.Warm else AuraColors.TextSecondary,
            )
            Box(Modifier.size(8.dp))
            RoundButton(Icons.Filled.MoreHoriz, "More", {})
        }

        Column(
            Modifier.padding(horizontal = 22.dp).padding(top = 48.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    Modifier
                        .clip(AuraShapes.Pill)
                        .background(AuraColors.Primary.copy(alpha = 0.18f))
                        .border(1.dp, AuraColors.Primary.copy(alpha = 0.35f), AuraShapes.Pill)
                        .padding(horizontal = 13.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(entry.mood.emoji, fontSize = AuraType.meta)
                    Text(
                        entry.mood.label,
                        color = AuraColors.PrimaryBright,
                        fontSize = AuraType.meta,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    "${entry.date} · ${entry.time}",
                    color = AuraColors.TextSecondary,
                    fontSize = AuraType.meta,
                )
            }

            Text(
                entry.title,
                color = Color.White,
                fontSize = 30.sp,
                fontFamily = AuraFonts.Display,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 37.sp,
            )

            entry.location?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Filled.Place, null, Modifier.size(15.dp), AuraColors.TextMuted)
                    Text(it, color = AuraColors.TextMuted, fontSize = AuraType.meta)
                }
            }

            Text(
                entry.content,
                color = AuraColors.TextBody,
                fontSize = AuraType.body,
                lineHeight = 26.sp,
            )

            // Rendered only when a reflection actually exists.
            if (entry.aiReflection != null || entry.aiAffirmation != null) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(AuraColors.Card.copy(alpha = 0.93f))
                        .border(1.dp, AuraColors.CardStroke, RoundedCornerShape(22.dp))
                        .padding(21.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    entry.aiReflection?.let { text ->
                        Label("MINDFUL SYNTHESIS", AuraColors.PrimarySoft)
                        Text(
                            text,
                            color = AuraColors.TextBody,
                            fontSize = AuraType.bodySmall,
                            lineHeight = 23.sp,
                        )
                    }
                    entry.aiAffirmation?.let { text ->
                        HorizontalDivider(color = AuraColors.Hairline)
                        Label("AFFIRMATION", AuraColors.TextPlaceholder)
                        Text(
                            text,
                            color = AuraColors.PrimaryBright,
                            fontSize = AuraType.readingBody,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 25.sp,
                        )
                    }
                }
            } else {
                Text(
                    "Reflections are generated on the server, which the native " +
                        "build does not call yet.",
                    color = AuraColors.TextMuted,
                    fontSize = AuraType.label,
                    lineHeight = 19.sp,
                )
            }

            if (entry.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    entry.tags.forEach { tag ->
                        Text(
                            tag,
                            color = AuraColors.TextSecondary,
                            fontSize = AuraType.label,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(AuraShapes.Pill)
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, AuraColors.Hairline, AuraShapes.Pill)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Label(text: String, color: Color) {
    Text(
        text,
        color = color,
        fontSize = AuraType.overline,
        fontWeight = AuraType.overlineWeight,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun RoundButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = AuraColors.TextSecondary,
) {
    Box(
        Modifier
            .size(44.dp)
            .clip(AuraShapes.Pill)
            .background(Color(0xA306070C))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, label, Modifier.size(20.dp), tint)
    }
}
