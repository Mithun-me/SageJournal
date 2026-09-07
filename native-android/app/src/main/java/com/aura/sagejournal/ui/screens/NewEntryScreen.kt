package com.aura.sagejournal.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraShapes
import com.aura.sagejournal.ui.theme.AuraType
import kotlin.math.abs
import kotlin.math.sin

/**
 * 1b "New Entry · Dictating". A full-screen composer with no tab bar: the
 * page is the writing surface, and the dictation panel is the only chrome.
 *
 * The panel's listening state is presented but not wired to a microphone —
 * real capture needs RECORD_AUDIO and a SpeechRecognizer, which is a privacy
 * decision rather than a layout one.
 */
@Composable
fun NewEntryScreen(
    dateLabel: String,
    prompt: String,
    onSave: (title: String, body: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(true) }

    // What the recogniser would be offering before it is committed.
    val pendingPhrase = "and then the room went quiet again"

    Column(
        Modifier
            .fillMaxSize()
            .background(AuraColors.BackgroundAlt)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Close, "Discard",
                Modifier
                    .size(40.dp)
                    .clip(AuraShapes.Pill)
                    .clickable(onClick = onDismiss)
                    .padding(9.dp),
                tint = AuraColors.TextMuted,
            )
            Text(
                dateLabel,
                color = AuraColors.TextMuted,
                fontSize = AuraType.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                "Save",
                color = AuraColors.Primary,
                fontSize = AuraType.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(AuraShapes.Pill)
                    .clickable { onSave(title, body) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                prompt,
                color = AuraColors.PrimarySoft,
                fontSize = AuraType.bodySmall,
                lineHeight = 22.sp,
            )

            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = AuraType.title,
                    fontWeight = AuraType.titleWeight,
                ),
                cursorBrush = SolidColor(AuraColors.Primary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (title.isEmpty()) {
                        Text(
                            "Give it a title",
                            color = AuraColors.TextPlaceholder,
                            fontSize = AuraType.title,
                            fontWeight = AuraType.titleWeight,
                        )
                    }
                    inner()
                },
            )

            BasicTextField(
                value = body,
                onValueChange = { body = it },
                textStyle = TextStyle(
                    color = AuraColors.TextBody,
                    fontSize = AuraType.readingBody,
                    lineHeight = 27.sp,
                ),
                cursorBrush = SolidColor(AuraColors.Primary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (body.isEmpty()) {
                        Text(
                            "Start where you are.",
                            color = AuraColors.TextPlaceholder,
                            fontSize = AuraType.readingBody,
                            lineHeight = 27.sp,
                        )
                    }
                    inner()
                },
            )

            // Uncommitted speech reads in the live accent, so it is visibly
            // different from text that has actually been saved into the entry.
            if (listening) {
                Text(
                    pendingPhrase,
                    color = AuraColors.PrimaryBright,
                    fontSize = AuraType.readingBody,
                    lineHeight = 27.sp,
                )
            }

            Text(
                "${wordCount(body)} words",
                color = AuraColors.TextMuted,
                fontSize = AuraType.label,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            )
        }

        DictationPanel(
            listening = listening,
            onToggle = {
                if (listening) body = (body.trim() + " " + pendingPhrase).trim()
                listening = !listening
            },
        )
    }
}

private fun wordCount(s: String) = s.trim().split(Regex("\\s+")).count { it.isNotEmpty() }

@Composable
private fun DictationPanel(listening: Boolean, onToggle: () -> Unit) {
    Column(
        Modifier
            .padding(horizontal = 20.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .clip(AuraShapes.Sheet)
            .background(Color(0xF7101322))
            .border(1.dp, AuraColors.Primary.copy(alpha = 0.3f), AuraShapes.Sheet)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(10.dp).clip(AuraShapes.Pill)
                    .background(if (listening) AuraColors.Primary else AuraColors.TextMuted)
            )
            Text(
                if (listening) "  Listening" else "  Paused",
                color = if (listening) AuraColors.PrimaryBright else AuraColors.TextMuted,
                fontSize = AuraType.bodySmall,
                fontWeight = FontWeight.Bold,
            )
            Box(Modifier.weight(1f))
            Waveform(active = listening)
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundControl(Icons.Filled.MoreHoriz)
            Box(
                Modifier
                    .height(52.dp)
                    .width(182.dp)
                    .clip(AuraShapes.Pill)
                    .background(AuraColors.Primary)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (listening) "Done dictating" else "Resume",
                    color = AuraColors.OnPrimary,
                    fontSize = AuraType.bodySmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            RoundControl(Icons.Filled.Close)
        }
    }
}

@Composable
private fun RoundControl(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        Modifier
            .size(54.dp)
            .clip(AuraShapes.Pill)
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), AuraShapes.Pill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, Modifier.size(20.dp), AuraColors.TextSecondary)
    }
}

/** Nine bars, phase-shifted off one driver rather than nine animations. */
@Composable
private fun Waveform(active: Boolean) {
    val phase by rememberInfiniteTransition("waveform").animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            tween(1100, easing = LinearEasing), RepeatMode.Restart
        ),
        label = "phase",
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(9) { i ->
            val h = if (active) 8f + 18f * abs(sin(phase + i * 0.7f)) else 4f
            Box(
                Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (active) AuraColors.Primary else AuraColors.TextMuted.copy(alpha = 0.5f)
                    )
            )
        }
    }
}
