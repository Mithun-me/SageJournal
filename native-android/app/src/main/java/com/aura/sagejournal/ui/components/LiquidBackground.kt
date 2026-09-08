package com.aura.sagejournal.ui.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.aura.sagejournal.ui.shader.LIQUID_AGSL
import com.aura.sagejournal.ui.shader.ShaderPalette
import com.aura.sagejournal.ui.theme.AuraColors
import kotlin.math.floor

/**
 * The pre-blurred backdrop, published so any [GlassSurface] in the tree can
 * sample it. Null below API 31, where RenderEffect does not exist; glass then
 * falls back to a flat tint.
 */
val LocalBackdrop = compositionLocalOf<GraphicsLayer?> { null }

/** Cap on backdrop re-rasterisation; decouples cost from display refresh. */
private const val BACKDROP_FPS = 30f

/** AGSL is Android 13. */
private fun shaderAvailable() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/** RenderEffect, and therefore backdrop blur, is Android 12. */
private fun blurAvailable() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** Isolates every API-33 symbol behind one guarded class. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class LiquidShaderPainter {
    private val shader = RuntimeShader(LIQUID_AGSL)
    val brush = ShaderBrush(shader)

    fun setUniforms(size: Size, time: Float, palette: ShaderPalette, intensity: Float) {
        shader.setFloatUniform("uResolution", size.width, size.height)
        shader.setFloatUniform("uTime", time)
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
    }
}

@Composable
private fun rememberShaderClock(animated: Boolean, speed: () -> Float): Float {
    var seconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(animated) {
        if (!animated) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    seconds += ((now - last) / 1_000_000_000f) * speed()
                }
                last = now
            }
        }
    }
    return seconds
}

/**
 * Draws the animated liquid background and provides a blurred copy of it as
 * the backdrop for glass surfaces.
 *
 * Three tiers, because the Capacitor build this replaces supports Android 7:
 *  - API 33+  AGSL shader, blurred backdrop
 *  - API 31-32  gradient fallback, blurred backdrop
 *  - API 24-30  gradient fallback, no backdrop (glass uses a flat tint)
 */
@Composable
fun LiquidBackground(
    palette: ShaderPalette = ShaderPalette.LiquidGlass,
    animated: Boolean = true,
    speed: Float = 1f,
    intensity: Float = 1f,
    content: @Composable BoxScope.() -> Unit,
) {
    val currentSpeed = rememberUpdatedState(speed)
    val time = rememberShaderClock(animated) { currentSpeed.value }

    val painter = remember {
        if (shaderAvailable()) LiquidShaderPainter() else null
    }
    val sharp = rememberGraphicsLayer()
    val blurred = rememberGraphicsLayer()
    val backdrop = if (blurAvailable()) blurred else null

    val blurPx = with(LocalDensity.current) { 28.dp.toPx() }
    val lastKey = remember { arrayOfNulls<Any>(1) }

    Box(
        Modifier
            .fillMaxSize()
            .background(AuraColors.Background)
            .drawWithCache {
                onDrawBehind {
                    val frameTime = floor(time * BACKDROP_FPS) / BACKDROP_FPS
                    val key = listOf(frameTime, palette, intensity, size.width, size.height)
                    if (lastKey[0] != key) {
                        lastKey[0] = key
                        val px = IntSize(size.width.toInt(), size.height.toInt())

                        sharp.record(this, layoutDirection, px) {
                            paintLiquid(painter, palette, frameTime, intensity)
                        }
                        if (backdrop != null) {
                            backdrop.renderEffect = BlurEffect(blurPx, blurPx, TileMode.Clamp)
                            backdrop.record(this, layoutDirection, px) { drawLayer(sharp) }
                        }
                    }
                    drawLayer(sharp)
                }
            }
    ) {
        val scope = this
        CompositionLocalProvider(LocalBackdrop provides backdrop) { scope.content() }
    }
}

private fun DrawScope.paintLiquid(
    painter: Any?,
    palette: ShaderPalette,
    time: Float,
    intensity: Float,
) {
    if (painter is LiquidShaderPainter && shaderAvailable()) {
        painter.setUniforms(size, time, palette, intensity)
        drawRect(painter.brush)
    } else {
        drawLiquidFallback(palette, time)
    }
}
