package com.aura.sagejournal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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
        content = content,
    )
}
