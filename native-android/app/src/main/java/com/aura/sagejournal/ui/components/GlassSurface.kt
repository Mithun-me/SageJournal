package com.aura.sagejournal.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraShapes

/**
 * The frosted card the whole design rests on.
 *
 * Compose has no backdrop-blur modifier — Modifier.blur blurs the element, not
 * what sits behind it — so the full-screen pre-blurred layer from
 * [LiquidBackground] is translated into this surface's local space and clipped
 * to its shape.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = AuraShapes.Card,
    content: @Composable ColumnScope.() -> Unit,
) {
    val backdrop = LocalBackdrop.current
    var origin by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier
            .onGloballyPositioned { origin = it.positionInRoot() }
            .clip(shape)
            .drawBehind {
                backdrop?.let { translate(-origin.x, -origin.y) { drawLayer(it) } }
                drawRect(AuraColors.Glass)
            }
            .border(1.dp, AuraColors.Hairline, shape),
        content = content,
    )
}
