package com.aura.sagejournal.lab

import android.graphics.RuntimeShader
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val hz = display?.refreshRate ?: 60f
        setContent { GateScreen(refreshHz = hz) }
    }
}

@Composable
fun GateScreen(refreshHz: Float) {
    var theme by remember { mutableStateOf(AuraTheme.LiquidGlass) }
    var blurEnabled by remember { mutableStateOf(true) }
    var shaderAnimated by remember { mutableStateOf(true) }
    var panelCount by remember { mutableIntStateOf(4) }

    val stats = rememberFrameStats(refreshHz)
    val shader = remember { RuntimeShader(LIQUID_AGSL) }
    val brush = remember(shader) { ShaderBrush(shader) }

    // One recording of the shader, drawn twice: sharp as the page background,
    // blurred as the backdrop every glass panel samples. Recording once rather
    // than re-evaluating the shader per panel is the whole point — the naive
    // version costs O(panels) shader passes per frame.
    val sharpLayer = rememberGraphicsLayer()
    val blurredLayer = rememberGraphicsLayer()

    val blurPx = with(LocalDensity.current) { 24.dp.toPx() }
    val time = if (shaderAnimated) stats.timeSeconds else 0f

    // Holds the inputs of the last recording. Re-recording a full-screen
    // shader on every frame is pure waste when none of its inputs moved.
    val lastKey = remember { arrayOfNulls<Any>(1) }

    Box(
        Modifier
            .fillMaxSize()
            .drawWithCache {
                onDrawBehind {
                    val key = listOf(time, theme, blurEnabled, size.width, size.height)
                    if (lastKey[0] != key) {
                        lastKey[0] = key
                        recordBackdrop(
                            shader, brush, time, theme, blurEnabled, blurPx,
                            sharpLayer, blurredLayer
                        )
                    }
                    drawLayer(sharpLayer)
                }
            }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Hud(stats, refreshHz, blurEnabled, shaderAnimated, panelCount)

            Controls(
                blurEnabled = blurEnabled,
                onBlurChange = { blurEnabled = it },
                shaderAnimated = shaderAnimated,
                onAnimatedChange = { shaderAnimated = it },
                panelCount = panelCount,
                onPanelCountChange = { panelCount = it.coerceIn(0, 12) },
                theme = theme,
                onThemeChange = { theme = it },
            )

            repeat(panelCount) { i ->
                GlassPanel(
                    backdrop = blurredLayer,
                    title = "Daily Affirmation ${i + 1}",
                    body = "Smile, breathe and go slowly. Scroll this list — the " +
                        "backdrop moves under each panel, which is the worst case " +
                        "for blur-over-shader.",
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

/** Records the shader once (sharp) and once more through a blur. */
private fun DrawScope.recordBackdrop(
    shader: RuntimeShader,
    brush: ShaderBrush,
    time: Float,
    theme: AuraTheme,
    blurEnabled: Boolean,
    blurPx: Float,
    sharpLayer: GraphicsLayer,
    blurredLayer: GraphicsLayer,
) {
    shader.setFloatUniform("uResolution", size.width, size.height)
    shader.setFloatUniform("uTime", time)
    shader.setFloatUniform("uPointer", size.width / 2f, size.height / 2f)
    shader.setFloatUniform("uIntensity", 1f)
    shader.setFloatUniform("uColor1", theme.color1.first, theme.color1.second, theme.color1.third)
    shader.setFloatUniform("uColor2", theme.color2.first, theme.color2.second, theme.color2.third)
    shader.setFloatUniform("uColor3", theme.color3.first, theme.color3.second, theme.color3.third)
    shader.setFloatUniform("uBrightness", theme.brightness)

    val px = IntSize(size.width.toInt(), size.height.toInt())
    sharpLayer.record(this, layoutDirection, px) { drawRect(brush) }
    blurredLayer.renderEffect =
        if (blurEnabled) BlurEffect(blurPx, blurPx, TileMode.Clamp) else null
    blurredLayer.record(this, layoutDirection, px) { drawLayer(sharpLayer) }
}

/**
 * A glass panel that blurs what is behind it. Compose has no backdrop-blur
 * modifier — Modifier.blur blurs the element itself — so the pre-blurred
 * full-screen layer is translated into the panel's local space and clipped.
 */
@Composable
private fun GlassPanel(backdrop: GraphicsLayer, title: String, body: String) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    val shape = RoundedCornerShape(24.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .onGloballyPositioned { origin = it.positionInRoot() }
            .clip(shape)
            .drawBehind {
                translate(-origin.x, -origin.y) { drawLayer(backdrop) }
                // Matches the web build's bg-[#0e1022]/80 over the blur.
                drawRect(Color(0xCC0E1022))
            }
            .border(1.dp, Color.White.copy(alpha = 0.10f), shape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, color = Color(0xFF71F8E4), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(body, color = Color(0xFFC7C4D7), fontSize = 13.sp)
    }
}

@Composable
private fun Hud(
    stats: FrameStats,
    refreshHz: Float,
    blurEnabled: Boolean,
    shaderAnimated: Boolean,
    panelCount: Int,
) {
    val verdict = when {
        stats.jankPercent > 10f -> "FAIL" to Color(0xFFFF8A8A)
        stats.jankPercent > 2f -> "MARGINAL" to Color(0xFFFFD08A)
        else -> "PASS" to Color(0xFF71F8E4)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xF00A0C1A))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "blur-over-shader gate  ",
                color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold,
            )
            Text(verdict.first, color = verdict.second, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Mono("display        ${"%.0f".format(refreshHz)} Hz  (budget ${"%.1f".format(1000f / refreshHz)} ms)")
        Mono("fps            ${"%.1f".format(stats.fps)}")
        Mono("frame median   ${"%.2f".format(stats.medianMs)} ms")
        Mono("frame p99      ${"%.2f".format(stats.p99Ms)} ms")
        Mono("frame worst    ${"%.2f".format(stats.worstMs)} ms")
        Mono("jank           ${"%.1f".format(stats.jankPercent)} %")
        Mono("blur=$blurEnabled  anim=$shaderAnimated  panels=$panelCount")
    }
}

@Composable
private fun Mono(text: String) {
    Text(text, color = Color(0xFFC7C4D7), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
}

@Composable
private fun Controls(
    blurEnabled: Boolean,
    onBlurChange: (Boolean) -> Unit,
    shaderAnimated: Boolean,
    onAnimatedChange: (Boolean) -> Unit,
    panelCount: Int,
    onPanelCountChange: (Int) -> Unit,
    theme: AuraTheme,
    onThemeChange: (AuraTheme) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Chip(if (blurEnabled) "blur ON" else "blur OFF") { onBlurChange(!blurEnabled) }
        Chip(if (shaderAnimated) "anim ON" else "anim OFF") { onAnimatedChange(!shaderAnimated) }
        Chip("panels -") { onPanelCountChange(panelCount - 2) }
        Chip("panels +") { onPanelCountChange(panelCount + 2) }
        Chip("theme") {
            onThemeChange(
                if (theme == AuraTheme.LiquidGlass) AuraTheme.DeepSea else AuraTheme.LiquidGlass
            )
        }
    }
}

@Composable
private fun Chip(label: String, onClick: () -> Unit) {
    Text(
        label,
        color = Color(0xFF003731),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF4FDBC8))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
    )
}
