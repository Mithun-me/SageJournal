package com.aura.sagejournal.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.FontFamily

/**
 * Maps Aura's tokens onto the Material 3 colour scheme, so the framework
 * components (NavigationBar, ListItem, Switch, TextField) inherit the design
 * instead of each call site restating it.
 */
@Composable
fun AuraMaterialTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AuraColors.Primary,
            onPrimary = AuraColors.OnPrimary,
            secondary = AuraColors.Indigo,
            background = AuraColors.Background,
            onBackground = AuraColors.TextPrimary,
            surface = AuraColors.Card,
            onSurface = AuraColors.TextPrimary,
            onSurfaceVariant = AuraColors.TextTertiary,
            outline = AuraColors.TextMuted,
            error = AuraColors.Warm,
        ),
        // Framework components read typography roles, so Inter has to be set
        // there as well as on the raw Text default below.
        typography = interTypography(),
    ) {
        // Anything that does not name a family gets Inter; headings opt in to
        // AuraFonts.Display explicitly.
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = AuraFonts.Body),
            content = content,
        )
    }
}

private fun interTypography(f: FontFamily = AuraFonts.Body): Typography {
    val t = Typography()
    return t.copy(
        displayLarge = t.displayLarge.copy(fontFamily = f),
        displayMedium = t.displayMedium.copy(fontFamily = f),
        displaySmall = t.displaySmall.copy(fontFamily = f),
        headlineLarge = t.headlineLarge.copy(fontFamily = f),
        headlineMedium = t.headlineMedium.copy(fontFamily = f),
        headlineSmall = t.headlineSmall.copy(fontFamily = f),
        titleLarge = t.titleLarge.copy(fontFamily = f),
        titleMedium = t.titleMedium.copy(fontFamily = f),
        titleSmall = t.titleSmall.copy(fontFamily = f),
        bodyLarge = t.bodyLarge.copy(fontFamily = f),
        bodyMedium = t.bodyMedium.copy(fontFamily = f),
        bodySmall = t.bodySmall.copy(fontFamily = f),
        labelLarge = t.labelLarge.copy(fontFamily = f),
        labelMedium = t.labelMedium.copy(fontFamily = f),
        labelSmall = t.labelSmall.copy(fontFamily = f),
    )
}
