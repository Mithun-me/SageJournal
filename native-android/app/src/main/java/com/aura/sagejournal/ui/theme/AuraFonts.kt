package com.aura.sagejournal.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.aura.sagejournal.R

/**
 * Plus Jakarta Sans for display, Inter for everything read at length.
 *
 * Both are the variable fonts from google/fonts — the static instances were
 * removed upstream, so each family is one file carrying its weight axis.
 * FontVariation applies from API 26; on 24-25 Android falls back to synthetic
 * bolding, which loses some of the weight contrast but keeps the hierarchy.
 */
@OptIn(ExperimentalTextApi::class)
private fun weight(w: Int) = FontVariation.Settings(FontVariation.weight(w))

@OptIn(ExperimentalTextApi::class)
object AuraFonts {
    /** Headings, numerals, anything set large. */
    val Display = FontFamily(
        Font(R.font.plus_jakarta_sans, FontWeight.Normal, variationSettings = weight(400)),
        Font(R.font.plus_jakarta_sans, FontWeight.Medium, variationSettings = weight(500)),
        Font(R.font.plus_jakarta_sans, FontWeight.SemiBold, variationSettings = weight(600)),
        Font(R.font.plus_jakarta_sans, FontWeight.Bold, variationSettings = weight(700)),
        Font(R.font.plus_jakarta_sans, FontWeight.ExtraBold, variationSettings = weight(800)),
    )

    /** Body copy, labels, and the entry prose — the text that gets read. */
    val Body = FontFamily(
        Font(R.font.inter, FontWeight.Normal, variationSettings = weight(400)),
        Font(R.font.inter, FontWeight.Medium, variationSettings = weight(500)),
        Font(R.font.inter, FontWeight.SemiBold, variationSettings = weight(600)),
        Font(R.font.inter, FontWeight.Bold, variationSettings = weight(700)),
    )
}
