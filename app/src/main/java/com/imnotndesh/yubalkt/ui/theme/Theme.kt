package com.imnotndesh.yubalkt.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Teal = Color(0xFF2FD8B8)
val TealDark = Color(0xFF0EA5A0)
val TealBadge = Color(0xFF123430)
val Danger = Color(0xFFE5484D)
val DangerLight = Color(0xFFDC2626)
val DarkBg = Color(0xFF0A0A0B)
val DarkSurface = Color(0xFF16181A)
val DarkElevated = Color(0xFF1D2022)
val DarkTextPrimary = Color(0xFFF5F7F7)
val DarkTextSecondary = Color(0xFF9AA3A3)
val LightBg = Color(0xFFF5F7F7)
val LightSurface = Color(0xFFFFFFFF)
val LightElevated = Color(0xFFF0F2F2)
val LightTextPrimary = Color(0xFF0A0A0B)
val LightTextSecondary = Color(0xFF6B7280)
val GreenComplete = Color(0xFF16A34A)

private val DarkColorScheme = darkColorScheme(
    primary = Teal,
    onPrimary = DarkBg,
    primaryContainer = TealBadge,
    onPrimaryContainer = Teal,
    secondary = Teal.copy(alpha = 0.7f),
    onSecondary = DarkBg,
    secondaryContainer = TealBadge.copy(alpha = 0.7f),
    onSecondaryContainer = Teal.copy(alpha = 0.7f),
    tertiary = Color(0xFF9AA3A3),
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerHighest = DarkElevated,
    surfaceContainerHigh = DarkElevated,
    surfaceContainer = DarkSurface,
    surfaceContainerLow = DarkSurface,
    surfaceContainerLowest = DarkBg,
    error = Danger,
    onError = DarkTextPrimary,
    errorContainer = Danger.copy(alpha = 0.12f),
    onErrorContainer = Danger,
    outline = DarkTextSecondary.copy(alpha = 0.3f),
    outlineVariant = DarkTextSecondary.copy(alpha = 0.12f),
    inverseSurface = DarkTextPrimary,
    inverseOnSurface = DarkBg,
    inversePrimary = TealDark,
    scrim = DarkBg,
)

private val LightColorScheme = lightColorScheme(
    primary = TealDark,
    onPrimary = LightBg,
    primaryContainer = Teal.copy(alpha = 0.15f),
    onPrimaryContainer = TealDark,
    secondary = TealDark.copy(alpha = 0.7f),
    onSecondary = LightBg,
    secondaryContainer = Teal.copy(alpha = 0.1f),
    onSecondaryContainer = TealDark.copy(alpha = 0.7f),
    tertiary = LightTextSecondary,
    onTertiary = LightBg,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerHighest = LightElevated,
    surfaceContainerHigh = LightElevated,
    surfaceContainer = LightSurface,
    surfaceContainerLow = LightBg,
    surfaceContainerLowest = LightSurface,
    error = DangerLight,
    onError = LightBg,
    errorContainer = DangerLight.copy(alpha = 0.1f),
    onErrorContainer = DangerLight,
    outline = LightTextSecondary.copy(alpha = 0.3f),
    outlineVariant = LightTextSecondary.copy(alpha = 0.12f),
    inverseSurface = LightTextPrimary,
    inverseOnSurface = LightBg,
    inversePrimary = Teal,
    scrim = LightBg,
)

@Composable
fun YubalktTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme, content = content)
}