package com.aura.sagejournal.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.aura.sagejournal.ui.shader.ShaderPalette
import kotlin.math.cos
import kotlin.math.sin

private fun ShaderPalette.asColors(): List<Color> = listOf(
    Color(color1.first, color1.second, color1.third),
    Color(color2.first, color2.second, color2.third),
    Color(color3.first, color3.second, color3.third),
)

/**
 * The pre-API-33 background. AGSL needs Android 13, and the Capacitor build
 * this replaces supports Android 7, so devices below 33 get three large
 * radial gradients drifting on the same clock instead of per-pixel noise.
 *
 * It is not the same picture — there is no turbulence — but it is the same
 * palette, the same slow motion, and it reads as the same product rather than
 * as a flat colour where the shader should be.
 */
fun DrawScope.drawLiquidFallback(palette: ShaderPalette, time: Float) {
    val colors = palette.asColors()
    val b = palette.brightness

    // Base wash, dimmed the way the shader dims its output.
    drawRect(colors[1] * b)

    // Tighter, unequal blobs. A first pass gave all three the same radius and
    // alpha, and the near-white third colour bleached the result into a flat
    // teal wash with no indigo left in it.
    data class Blob(val color: Color, val alpha: Float, val radius: Float, val drift: Float)
    val blobs = listOf(
        Blob(colors[0], 0.62f, 0.62f, 0.36f),   // indigo, the one that went missing
        Blob(colors[1], 0.50f, 0.78f, 0.28f),   // teal wash
        Blob(colors[2], 0.14f, 0.34f, 0.44f),   // highlight, kept faint
    )

    blobs.forEachIndexed { i, blob ->
        val phase = time * 0.13f + i * 2.2f
        val radius = size.minDimension * blob.radius
        val centre = Offset(
            x = size.width * (0.5f + blob.drift * cos(phase + i)),
            y = size.height * (0.42f + blob.drift * 0.9f * sin(phase * 0.77f + i)),
        )
        val tinted = blob.color * b
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(tinted.copy(alpha = blob.alpha), tinted.copy(alpha = 0f)),
                center = centre,
                radius = radius,
            ),
            radius = radius,
            center = centre,
        )
    }
}

/** Scales a colour's channels, mirroring the shader's brightness multiply. */
private operator fun Color.times(factor: Float): Color =
    Color(red * factor, green * factor, blue * factor, alpha)
