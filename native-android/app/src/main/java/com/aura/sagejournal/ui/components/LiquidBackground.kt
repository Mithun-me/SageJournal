package com.aura.sagejournal.ui.components

import android.graphics.RuntimeShader
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import com.aura.sagejournal.ui.shader.LIQUID_AGSL
import com.aura.sagejournal.ui.shader.ShaderPalette
import com.aura.sagejournal.ui.theme.AuraColors

/**
 * The pre-blurred backdrop, published so any [GlassSurface] in the tree can
 * sample it without threading a layer through every call site. Null means no
 * backdrop is available (below API 33, or in a preview), and glass falls back
 * to a flat tint.
 */
val LocalBackdrop = compositionLocalOf<GraphicsLayer?> { null }

/**
 * Cap on how often the backdrop is re-rasterised. The liquid moves slowly
 * enough that matching a 90Hz display would triple the shader work for no
 * visible gain, and it decouples backdrop cost from display refresh rate.
 */
private const val BACKDROP_FPS = 30f

@Composable
private fun rememberShaderClock(animated: Boolean, speed: () -> Float): Float {
    var seconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(animated) {
        if (!animated) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    // Accumulate scaled time: changing speed eases into the new
                    // rate instead of jumping, which rescaling wall-clock would.
                    seconds += ((now - last) / 1_000_000_000f) * speed()
                }
                last = now
            }
        }
    }
    return seconds
}

/**
 * Draws the animated liquid shader and provides a blurred copy of it as the
 * backdrop for glass surfaces. The shader is rasterised once per change of its
 * inputs, not once per frame — during a scroll over a still background that is
 * zero shader work.
 */
@Composable
fun LiquidBackground(
    palette: ShaderPalette = ShaderPalette.LiquidGlass,
    animated: Boolean = true,
    speed: Float = 1f,
    intensity: Float = 1f,
    content: @Composable BoxScope.() -> Unit,
) {
    // Read through a lambda so the frame loop sees changes without restarting.
    val currentSpeed = rememberUpdatedState(speed)
    val time = rememberShaderClock(animated) { currentSpeed.value }
    val shader = remember { RuntimeShader(LIQUID_AGSL) }
    val brush = remember(shader) { ShaderBrush(shader) }
    val sharp = rememberGraphicsLayer()
    val blurred = rememberGraphicsLayer()
    val blurPx = with(LocalDensity.current) { 28.dp.toPx() }
    val lastKey = remember { arrayOfNulls<Any>(1) }

    Box(
        Modifier
            .fillMaxSize()
            .background(AuraColors.Background)
            .drawWithCache {
                onDrawBehind {
                    // Quantise to the backdrop's own cadence, so a 90Hz display
                    // does not re-rasterise the shader 90 times a second.
                    val frameTime = floor(time * BACKDROP_FPS) / BACKDROP_FPS
                    val key = listOf(frameTime, palette, intensity, size.width, size.height)
                    if (lastKey[0] != key) {
                        lastKey[0] = key
                        shader.setFloatUniform("uResolution", size.width, size.height)
                        shader.setFloatUniform("uTime", frameTime)
                        shader.setFloatUniform("uPointer", size.width / 2f, size.height / 2f)
                        shader.setFloatUniform("uIntensity", intensity)
                        shader.setFloatUniform(
                            "uColor1", palette.color1.first, palette.color1.second, palette.color1.third
                        )
                        shader.setFloatUniform(
                            "uColor2", palette.color2.first, palette.color2.second, palette.color2.third
                        )
                        shader.setFloatUniform(
                            "uColor3", palette.color3.first, palette.color3.second, palette.color3.third
                        )
                        shader.setFloatUniform("uBrightness", palette.brightness)

                        val px = IntSize(size.width.toInt(), size.height.toInt())
                        sharp.record(this, layoutDirection, px) { drawRect(brush) }
                        blurred.renderEffect = BlurEffect(blurPx, blurPx, TileMode.Clamp)
                        blurred.record(this, layoutDirection, px) { drawLayer(sharp) }
                    }
                    drawLayer(sharp)
                }
            }
    ) {
        val scope = this
        CompositionLocalProvider(LocalBackdrop provides blurred) { scope.content() }
    }
}
