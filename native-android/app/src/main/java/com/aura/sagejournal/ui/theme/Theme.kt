package com.aura.sagejournal.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tokens extracted from the web build, where 53 distinct hex values were
 * inlined across the components with no shared layer. Frequency in the web
 * source is noted, because it establishes which value is the real token and
 * which were one-off drifts.
 */
object AuraColors {
    // Accents
    val Primary = Color(0xFF4FDBC8)        // 189 uses — the brand teal
    val PrimaryBright = Color(0xFF71F8E4)  // 28 — hover/emphasis
    val OnPrimary = Color(0xFF003731)      // 18 — text on a teal fill
    val Warm = Color(0xFFFFB783)           // 35 — Joy, favourites
    val Indigo = Color(0xFF8083FF)         // 15 — Reflective
    val CoolBlue = Color(0xFF93C5FD)       // Low
    val GradientStart = Color(0xFF4F46E5)
    val GradientEnd = Color(0xFF4FDBC8)

    // Grounds, darkest to lightest
    val Background = Color(0xFF101221)     // body background
    val Chrome = Color(0xFF0A0C1A)         // 27 — top/bottom bars
    val Surface = Color(0xFF0E1022)        // 23 — cards
    val SurfaceElevated = Color(0xFF14182E)

    // Text ramp
    val TextPrimary = Color(0xFFE1E1F6)    // 22
    val TextSecondary = Color(0xFFC7C4D7)  // 48
    val TextTertiary = Color(0xFF908FA0)   // 86

    // Hairlines: the web used white at 8% and 15%.
    val Hairline = Color.White.copy(alpha = 0.08f)
    val HairlineStrong = Color.White.copy(alpha = 0.15f)

    /**
     * Glass fill. The web build used /80, which at 80% opacity leaves the blur
     * behind it nearly invisible; slightly more translucent here so the
     * backdrop actually reads, which is free now that blur measured free.
     */
    val Glass = Surface.copy(alpha = 0.72f)
}

object AuraShapes {
    val Card = RoundedCornerShape(16.dp)      // rounded-2xl
    val CardLarge = RoundedCornerShape(24.dp) // rounded-3xl
    val Pill = RoundedCornerShape(50)
}

object AuraSpacing {
    val screenH = 16.dp
    val section = 20.dp
    val card = 18.dp
    val tight = 8.dp
}

/**
 * The web build pairs Manrope (headings) with Be Vietnam Pro (body). Neither
 * is bundled here yet, so these fall back to the system family while keeping
 * the size and weight ramp; bundling the real faces is outstanding.
 */
object AuraType {
    val screenTitleSize = 20.sp
    val screenTitleWeight = FontWeight.Bold

    val cardTitleSize = 16.sp
    val cardTitleWeight = FontWeight.Bold

    val bodySize = 13.sp
    val labelSize = 12.sp
    val microSize = 10.sp
}
