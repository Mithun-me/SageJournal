package com.aura.sagejournal.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.sagejournal.ui.components.playChime
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraShapes
import com.aura.sagejournal.ui.theme.AuraType
import kotlinx.coroutines.delay

/** Phase lengths in seconds; hold2 of 0 means the phase is skipped. */
private data class Technique(
    val key: String,
    val name: String,
    val pattern: String,
    val blurb: String,
    val inhale: Int,
    val hold1: Int,
    val exhale: Int,
    val hold2: Int,
)

private val techniques = listOf(
    Technique("box", "Box", "4-4-4-4",
        "Stabilises focus and balances the nervous system.", 4, 4, 4, 4),
    Technique("calm", "Deep calm", "5-2-6",
        "Lengthens the exhale to settle the nervous system.", 5, 2, 6, 1),
    Technique("relax", "Deep rest", "4-7-8",
        "Eases physical tension and quietens mental chatter.", 4, 7, 8, 0),
)

private enum class Phase(val label: String, val chime: Double) {
    Inhale("Breathe in", 432.0),
    Hold("Hold", 528.0),
    Exhale("Breathe out", 396.0),
    Rest("Rest", 432.0),
}

/**
 * Not covered by the redesign, so this ports the web Sanctuary behaviour into
 * the new design language: the ring is the whole interface, and it stays on
 * the shader because a breathing screen is the one place the moving background
 * is the point.
 */
@Composable
fun BreatheScreen(
    chimesOn: Boolean,
    onToggleChimes: (Boolean) -> Unit,
    onSessionComplete: () -> Unit,
    sessionsLogged: Int,
) {
    var techniqueIndex by remember { mutableIntStateOf(0) }
    val technique = techniques[techniqueIndex]

    var running by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf(Phase.Inhale) }
    var secondsLeft by remember { mutableIntStateOf(technique.inhale) }
    var cycles by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    fun lengthOf(p: Phase) = when (p) {
        Phase.Inhale -> technique.inhale
        Phase.Hold -> technique.hold1
        Phase.Exhale -> technique.exhale
        Phase.Rest -> technique.hold2
    }

    fun reset() {
        running = false
        phase = Phase.Inhale
        secondsLeft = technique.inhale
        cycles = 0
    }

    LaunchedEffect(running, techniqueIndex) {
        if (!running) return@LaunchedEffect
        while (true) {
            delay(1000)
            if (secondsLeft > 1) {
                secondsLeft--
            } else {
                // Advance, skipping any phase this technique gives zero seconds.
                var next = Phase.entries[(phase.ordinal + 1) % Phase.entries.size]
                while (lengthOf(next) == 0) {
                    next = Phase.entries[(next.ordinal + 1) % Phase.entries.size]
                }
                if (next == Phase.Inhale) {
                    cycles++
                    if (cycles == 1) onSessionComplete()
                }
                phase = next
                secondsLeft = lengthOf(next)
                if (chimesOn) scope.playChime(next.chime)
            }
        }
    }

    // The ring is the breath: it grows on the inhale and holds where the
    // pattern holds, so the animation duration is the phase length.
    val target = when (phase) {
        Phase.Inhale -> 1f
        Phase.Hold -> 1f
        Phase.Exhale -> 0.62f
        Phase.Rest -> 0.62f
    }
    val scale by animateFloatAsState(
        targetValue = if (running) target else 0.8f,
        animationSpec = tween(durationMillis = lengthOf(phase).coerceAtLeast(1) * 1000),
        label = "breath",
    )

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Breathe",
                    color = Color.White,
                    fontSize = AuraType.display,
                    fontWeight = AuraType.displayWeight,
                )
                Text(
                    if (sessionsLogged == 0) "No sessions yet"
                    else "$sessionsLogged " + (if (sessionsLogged == 1) "session" else "sessions") +
                        " logged",
                    color = AuraColors.TextTertiary,
                    fontSize = AuraType.bodySmall,
                )
            }
            Box(
                Modifier
                    .size(40.dp)
                    .clip(AuraShapes.Pill)
                    .border(1.dp, AuraColors.Hairline, AuraShapes.Pill)
                    .clickable { onToggleChimes(!chimesOn) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (chimesOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    "Chimes",
                    Modifier.size(18.dp),
                    if (chimesOn) AuraColors.Primary else AuraColors.TextMuted,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            techniques.forEachIndexed { i, t ->
                val active = i == techniqueIndex
                Column(
                    Modifier
                        .weight(1f)
                        .clip(AuraShapes.Chip)
                        .background(
                            if (active) AuraColors.Primary.copy(alpha = 0.14f)
                            else AuraColors.Chip.copy(alpha = 0.72f)
                        )
                        .border(
                            if (active) 1.5.dp else 1.dp,
                            if (active) AuraColors.Primary else Color.White.copy(alpha = 0.09f),
                            AuraShapes.Chip,
                        )
                        .clickable { techniqueIndex = i; reset() }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        t.name,
                        color = if (active) Color.White else AuraColors.TextSecondary,
                        fontSize = AuraType.label,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(t.pattern, color = AuraColors.TextMuted, fontSize = AuraType.overline)
                }
            }
        }

        Box(
            Modifier.fillMaxWidth().height(300.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(260.dp)
                    .scale(scale)
                    .clip(AuraShapes.Pill)
                    .background(AuraColors.Primary.copy(alpha = 0.13f))
                    .border(1.5.dp, AuraColors.Primary.copy(alpha = 0.45f), AuraShapes.Pill),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (running) phase.label else "Ready",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (running) "$secondsLeft" else technique.pattern,
                    color = AuraColors.PrimaryBright,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    if (cycles == 0) "" else "$cycles " +
                        (if (cycles == 1) "cycle" else "cycles"),
                    color = AuraColors.TextTertiary,
                    fontSize = AuraType.label,
                )
            }
        }

        Text(
            technique.blurb,
            color = AuraColors.TextSecondary,
            fontSize = AuraType.bodySmall,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(AuraShapes.Pill)
                    .background(AuraColors.Primary)
                    .clickable {
                        if (!running && chimesOn) scope.playChime(Phase.Inhale.chime)
                        running = !running
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        null, Modifier.size(20.dp), AuraColors.OnPrimary,
                    )
                    Text(
                        if (running) "Pause" else "Begin",
                        color = AuraColors.OnPrimary,
                        fontSize = AuraType.body,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Box(
                Modifier
                    .size(56.dp)
                    .clip(AuraShapes.Pill)
                    .border(1.dp, AuraColors.Hairline, AuraShapes.Pill)
                    .clickable { reset() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Refresh, "Reset", Modifier.size(20.dp), AuraColors.TextSecondary)
            }
        }
    }
}
