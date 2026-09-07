package com.aura.sagejournal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.sagejournal.domain.TrendPoint
import com.aura.sagejournal.ui.theme.AuraColors

/**
 * The composed mood/points chart, drawn directly on a Canvas.
 *
 * Deliberately not a charting library. The web original is a Recharts
 * ComposedChart with two Y axes, a monotone-interpolated area, rounded-top
 * gradient bars and a dashed overlay line, all in a bespoke glass palette with
 * tap-to-select. Bending a library's theming to that costs more than drawing
 * seven points, and hit-testing stays under our control.
 */
@Composable
fun TrendChart(
    points: List<TrendPoint>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current

    Canvas(
        modifier
            .fillMaxWidth()
            .height(210.dp)
            .pointerInput(points.size) {
                detectTapGestures { tap ->
                    if (points.isEmpty()) return@detectTapGestures
                    val leftPad = with(density) { 30.dp.toPx() }
                    val rightPad = with(density) { 34.dp.toPx() }
                    val plotW = size.width - leftPad - rightPad
                    val step = plotW / points.size
                    val i = (((tap.x - leftPad) / step).toInt()).coerceIn(0, points.lastIndex)
                    onSelect(i)
                }
            }
    ) {
        if (points.isEmpty()) return@Canvas

        val leftPad = 30.dp.toPx()
        val rightPad = 34.dp.toPx()
        val bottomPad = 20.dp.toPx()
        val topPad = 8.dp.toPx()
        val plotW = size.width - leftPad - rightPad
        val plotH = size.height - bottomPad - topPad
        val step = plotW / points.size
        fun centerX(i: Int) = leftPad + step * i + step / 2
        fun clarityY(v: Int) = topPad + plotH * (1f - v / 100f)

        // Horizontal guides at 0/25/50/75/100.
        for (g in 0..4) {
            val y = topPad + plotH * (g / 4f)
            drawLine(
                Color.White.copy(alpha = 0.06f),
                Offset(leftPad, y), Offset(size.width - rightPad, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        // Clarity area on the left axis, monotone cubic like Recharts' "monotone".
        val xs = points.indices.map { centerX(it) }
        val ys = points.map { clarityY(it.clarityScore) }
        val curve = monotoneCubicPath(xs, ys)

        val fill = Path().apply {
            addPath(curve)
            lineTo(xs.last(), topPad + plotH)
            lineTo(xs.first(), topPad + plotH)
            close()
        }
        drawPath(
            fill,
            Brush.verticalGradient(
                listOf(AuraColors.Primary.copy(alpha = 0.38f), AuraColors.Primary.copy(alpha = 0.02f))
            ),
        )
        // Points bars sit above the area fill and below its stroke: drawn
        // underneath, the teal fill mutes them into grey.
        val barW = 16.dp.toPx()
        val barBrush = Brush.verticalGradient(
            listOf(AuraColors.Warm.copy(alpha = 0.85f), AuraColors.Warm.copy(alpha = 0.10f))
        )
        points.forEachIndexed { i, p ->
            val h = plotH * (p.pointsEarned.coerceIn(0, 120) / 120f)
            val x = centerX(i) - barW / 2
            val y = topPad + plotH - h
            val r = 6.dp.toPx()
            val path = Path().apply {
                addRoundRect(
                    RoundRect(
                        Rect(x, y, x + barW, topPad + plotH),
                        topLeft = CornerRadius(r, r),
                        topRight = CornerRadius(r, r),
                        bottomRight = CornerRadius.Zero,
                        bottomLeft = CornerRadius.Zero,
                    )
                )
            }
            drawPath(path, barBrush)
        }

        drawPath(curve, AuraColors.Primary, style = Stroke(width = 3.dp.toPx()))

        // Dashed indigo trendline guide over the same series.
        drawPath(
            curve,
            AuraColors.Indigo,
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(4.dp.toPx(), 4.dp.toPx())
                ),
            ),
        )

        // Active dot for the selected day.
        selectedIndex?.let { i ->
            if (i in points.indices) {
                drawCircle(AuraColors.Background, 6.dp.toPx(), Offset(xs[i], ys[i]))
                drawCircle(
                    AuraColors.PrimaryBright, 6.dp.toPx(), Offset(xs[i], ys[i]),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }

        // Axis labels.
        val axisStyle = TextStyle(fontSize = 9.sp, color = AuraColors.TextTertiary)
        points.forEachIndexed { i, p ->
            drawLabel(measurer, p.name, centerX(i), size.height - bottomPad + 4.dp.toPx(), axisStyle, center = true)
        }
        listOf(0, 50, 100).forEach { v ->
            drawLabel(
                measurer, "$v", 2.dp.toPx(), clarityY(v) - 6.dp.toPx(),
                axisStyle.copy(color = AuraColors.Primary),
            )
        }
        listOf(0, 60, 120).forEach { v ->
            drawLabel(
                measurer, "${v}p", size.width - rightPad + 4.dp.toPx(),
                topPad + plotH * (1f - v / 120f) - 6.dp.toPx(),
                axisStyle.copy(color = AuraColors.Warm),
            )
        }
    }
}

private fun DrawScope.drawLabel(
    measurer: TextMeasurer,
    text: String,
    x: Float,
    y: Float,
    style: TextStyle,
    center: Boolean = false,
) {
    val laid = measurer.measure(text, style)
    val dx = if (center) x - laid.size.width / 2f else x
    drawText(laid, topLeft = Offset(dx, y))
}

/**
 * Fritsch–Carlson monotone cubic interpolation. Plain Catmull-Rom overshoots
 * on a sharp dip — the Wednesday low here — which would draw clarity scores
 * the data never contained.
 */
private fun monotoneCubicPath(xs: List<Float>, ys: List<Float>): Path {
    val path = Path()
    if (xs.isEmpty()) return path
    path.moveTo(xs[0], ys[0])
    val n = xs.size
    if (n == 1) return path

    val h = FloatArray(n - 1) { xs[it + 1] - xs[it] }
    val d = FloatArray(n - 1) { (ys[it + 1] - ys[it]) / h[it] }
    val m = FloatArray(n)
    m[0] = d[0]
    m[n - 1] = d[n - 2]
    for (i in 1 until n - 1) m[i] = (d[i - 1] + d[i]) / 2f

    // Clamp tangents so each span stays monotone.
    for (i in 0 until n - 1) {
        if (d[i] == 0f) {
            m[i] = 0f; m[i + 1] = 0f
        } else {
            val a = m[i] / d[i]
            val b = m[i + 1] / d[i]
            val s = a * a + b * b
            if (s > 9f) {
                val t = 3f / kotlin.math.sqrt(s)
                m[i] = t * a * d[i]
                m[i + 1] = t * b * d[i]
            }
        }
    }

    for (i in 0 until n - 1) {
        path.cubicTo(
            xs[i] + h[i] / 3f, ys[i] + m[i] * h[i] / 3f,
            xs[i + 1] - h[i] / 3f, ys[i + 1] - m[i + 1] * h[i] / 3f,
            xs[i + 1], ys[i + 1],
        )
    }
    return path
}
