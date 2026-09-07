package com.aura.sagejournal.dev

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

/**
 * Drives the shader clock and measures frame pacing at the same time, so the
 * numbers on screen describe the frames that actually rendered the scene.
 */
class FrameStats {
    var timeSeconds by mutableFloatStateOf(0f); internal set
    var fps by mutableFloatStateOf(0f); internal set
    var medianMs by mutableFloatStateOf(0f); internal set
    var p99Ms by mutableFloatStateOf(0f); internal set
    var worstMs by mutableFloatStateOf(0f); internal set
    var jankPercent by mutableFloatStateOf(0f); internal set
}

private const val WINDOW = 120

@Composable
fun rememberFrameStats(refreshHz: Float): FrameStats {
    val stats = remember { FrameStats() }
    LaunchedEffect(refreshHz) {
        // A frame is "janky" once it overruns the display's own budget.
        val budgetMs = 1000f / refreshHz
        val deltas = ArrayDeque<Float>()
        var last = 0L
        var origin = 0L

        while (true) {
            withFrameNanos { now ->
                if (origin == 0L) origin = now
                if (last != 0L) {
                    deltas.addLast((now - last) / 1_000_000f)
                    while (deltas.size > WINDOW) deltas.removeFirst()

                    if (deltas.size >= 10) {
                        val sorted = deltas.sorted()
                        val mean = deltas.sum() / deltas.size
                        stats.fps = if (mean > 0f) 1000f / mean else 0f
                        stats.medianMs = sorted[sorted.size / 2]
                        stats.p99Ms = sorted[(sorted.size * 99) / 100]
                        stats.worstMs = sorted.last()
                        stats.jankPercent =
                            100f * deltas.count { it > budgetMs * 1.5f } / deltas.size
                    }
                }
                last = now
                stats.timeSeconds = (now - origin) / 1_000_000_000f
            }
        }
    }
    return stats
}
