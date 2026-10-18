package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GamingPrimaryDark,
    onPrimary = GamingOnPrimaryDark,
    secondary = GamingSecondaryDark,
    onSecondary = GamingOnSecondaryDark,
    tertiary = GamingTertiaryDark,
    background = GamingBackgroundDark,
    surface = GamingSurfaceDark,
    surfaceVariant = GamingSurfaceVariantDark,
    error = GamingErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = GamingPrimaryLight,
    onPrimary = GamingOnPrimaryLight,
    secondary = GamingSecondaryLight,
    onSecondary = GamingOnSecondaryLight,
    tertiary = GamingTertiaryLight,
    background = GamingBackgroundLight,
    surface = GamingSurfaceLight,
    surfaceVariant = GamingSurfaceVariantLight,
    error = GamingErrorLight
)

@Composable
fun GamePrepTheme(
    themePreference: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themePreference) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }

    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
