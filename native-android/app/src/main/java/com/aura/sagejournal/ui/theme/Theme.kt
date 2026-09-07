package com.aura.sagejournal.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tokens read off the "Aura Mobile Redesign" frames rather than the old web
 * build. Brand teal is unchanged; the grounds got darker and the text ramp
 * lighter, and body copy moves up from 11-13px to 14-17px.
 */
object AuraColors {
    // Accents
    val Primary = Color(0xFF4FDBC8)         // brand teal, unchanged
    val PrimaryBright = Color(0xFF71F8E4)   // live/active text
    val PrimarySoft = Color(0xFF8FE4D6)     // timestamps, quiet accent text
    val PrimaryPale = Color(0xFF9EE9DC)     // the date above the greeting
    val OnPrimary = Color(0xFF06131A)       // text on a teal fill
    val Warm = Color(0xFFFFB783)            // joy, streak numerals
    val WarmPale = Color(0xFFFFDCC5)        // streak pill digit
    val Indigo = Color(0xFF8083FF)          // reflective, points
    val CoolBlue = Color(0xFF93C5FD)        // low
    val SwitchOn = Color(0xFF04B4A2)

    // Grounds
    val Background = Color(0xFF06070C)      // was #101221 — noticeably darker
    val BackgroundAlt = Color(0xFF07080F)   // sheet screens
    val Chrome = Color(0xFF090B14)           // tab bar at 96%
    val Card = Color(0xFF121626)             // entry cards / panels
    val CardRaised = Color(0xFF131728)       // the grounding card
    val Chip = Color(0xFF181C2E)             // unselected mood chip

    // Text ramp
    val TextPrimary = Color(0xFFECECFA)
    val TextBody = Color(0xFFDEDEEC)         // long-form entry body
    val TextSecondary = Color(0xFFC9C9DB)
    val TextTertiary = Color(0xFFA6A6BC)
    val TextMuted = Color(0xFF8788A0)
    val TextPlaceholder = Color(0xFF71727F)

    // Hairlines — the redesign leans on these instead of card edges.
    val Hairline = Color.White.copy(alpha = 0.08f)
    val HairlineFaint = Color.White.copy(alpha = 0.06f)
    val HairlineStrong = Color.White.copy(alpha = 0.16f)
    val CardStroke = Color.White.copy(alpha = 0.07f)
    val CardStrokeRaised = Color.White.copy(alpha = 0.10f)
}

object AuraShapes {
    val Chip = RoundedCornerShape(20.dp)      // mood chips, entry cards
    val Card = RoundedCornerShape(20.dp)
    val CardRaised = RoundedCornerShape(26.dp) // grounding card
    val Cta = RoundedCornerShape(15.dp)        // "Write from this"
    val Thumb = RoundedCornerShape(16.dp)
    val Sheet = RoundedCornerShape(28.dp)      // dictation panel
    val Pill = RoundedCornerShape(50)
}

object AuraDims {
    val headerHeight = 56.dp     // was a two-row control panel
    val screenH = 20.dp
    val moodChip = 82.dp
    val tabBarHeight = 84.dp
    val listRow = 59.dp
    val primaryButton = 56.dp
}

/**
 * The redesign's type scale. Sizes are literal from the frames, which is the
 * point: body copy was 11-13px and is now 14-17px.
 */
object AuraType {
    val display = 32.sp; val displayWeight = FontWeight.ExtraBold   // greeting, "October"
    val title = 28.sp; val titleWeight = FontWeight.ExtraBold       // entry title, stat numerals
    val quote = 21.sp; val quoteWeight = FontWeight.SemiBold
    val sectionHead = 17.sp; val sectionWeight = FontWeight.Bold    // "How is it landing?"
    val readingBody = 17.sp                                          // entry prose
    val brand = 17.sp; val brandWeight = FontWeight.ExtraBold
    val cardTitle = 16.sp; val cardTitleWeight = FontWeight.Bold
    val body = 16.sp                                                 // onboarding + list labels
    val bodySmall = 15.sp                                            // reflection, subtitles
    val meta = 14.sp                                                 // snippets, source
    val label = 13.sp                                                // timestamps, mood labels
    val overline = 12.sp; val overlineWeight = FontWeight.Bold       // "TODAY'S GROUNDING"
}
