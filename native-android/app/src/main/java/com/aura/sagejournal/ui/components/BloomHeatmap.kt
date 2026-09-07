package com.aura.sagejournal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.aura.sagejournal.domain.BloomDay
import com.aura.sagejournal.domain.DayState

private const val COLS = 7
private const val ROW_H = 47f      // dp, from the frames
private const val MIN_D = 16f      // dp, a day with the least written
private const val MAX_D = 36f      // dp, the most
private const val EMPTY_D = 14f
private const val FUTURE_D = 10f
private const val TODAY_D = 16f
private const val WORDS_FULL = 300f

/**
 * The bloom heatmap: a week-per-row grid where each day is a dot whose colour
 * is the mood and whose size and opacity are how much was written. Drawn on a
 * Canvas because it is 35 circles — a lazy grid of composables would cost more
 * than it explains.
 */
@Composable
fun BloomHeatmap(
    days: List<BloomDay>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = (days.size + COLS - 1) / COLS

    Canvas(
        modifier
            .fillMaxWidth()
            .height((rows * ROW_H).dp)
            .pointerInput(days.size) {
                detectTapGestures { tap ->
                    val cellW = size.width / COLS
                    val cellH = size.height / rows
                    val c = (tap.x / cellW).toInt().coerceIn(0, COLS - 1)
                    val r = (tap.y / cellH).toInt().coerceIn(0, rows - 1)
                    val i = r * COLS + c
                    if (i in days.indices) onSelect(i)
                }
            }
    ) {
        val cellW = size.width / COLS
        val cellH = size.height / rows

        days.forEachIndexed { i, day ->
            val cx = cellW * (i % COLS) + cellW / 2
            val cy = cellH * (i / COLS) + cellH / 2
            val centre = Offset(cx, cy)

            when {
                day.state == DayState.Future -> drawCircle(
                    Color.White.copy(alpha = 0.06f), (FUTURE_D / 2).dp.toPx(), centre
                )

                day.state == DayState.Today && day.mood == null -> drawCircle(
                    Color.White.copy(alpha = 0.20f), (TODAY_D / 2).dp.toPx(), centre,
                    style = Stroke(width = 1.dp.toPx()),
                )

                day.mood == null -> drawCircle(
                    Color.White.copy(alpha = 0.09f), (EMPTY_D / 2).dp.toPx(), centre
                )

                else -> {
                    val t = (day.words / WORDS_FULL).coerceIn(0f, 1f)
                    val d = MIN_D + (MAX_D - MIN_D) * t
                    val alpha = 0.28f + 0.60f * t
                    drawCircle(day.mood.accent.copy(alpha = alpha), (d / 2).dp.toPx(), centre)
                }
            }

            if (i == selectedIndex) {
                drawCircle(
                    Color.White.copy(alpha = 0.85f), (MAX_D / 2 + 3).dp.toPx(), centre,
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
        }
    }
}
