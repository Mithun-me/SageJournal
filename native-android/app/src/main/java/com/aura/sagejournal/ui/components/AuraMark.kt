package com.aura.sagejournal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.aura.sagejournal.ui.theme.AuraColors

/**
 * The Aura mark: one ring, one still point — the same two primitives as the
 * launcher icon, so the thing on the home screen and the thing in the header
 * are recognisably one object.
 *
 * Proportions are taken from the icon set's optical variant (aura-small.svg:
 * ring r140, stroke 34, dot r62 on 512) rather than the master. The master's
 * 20/512 stroke is drawn for a 48dp-and-up launcher tile; scaled to header
 * size it lands under a pixel and the ring disappears. Ratios are relative to
 * the mark's own outer diameter, not the icon canvas, so the mark fills the box
 * it is given instead of carrying the launcher tile's padding into a layout.
 *
 * Drawn rather than bundled as a raster so it takes theme colours and stays
 * crisp at any size.
 */
@Composable
fun AuraMark(
    modifier: Modifier = Modifier,
    ring: Color = AuraColors.Primary,
    stillPoint: Color = AuraColors.PrimaryBright,
) {
    Canvas(Modifier.size(22.dp).then(modifier)) {
        val d = size.minDimension
        val stroke = d * 0.1083f
        drawCircle(
            color = ring,
            radius = d / 2f - stroke / 2f,
            style = Stroke(width = stroke),
        )
        drawCircle(color = stillPoint, radius = d * 0.1974f)
    }
}
