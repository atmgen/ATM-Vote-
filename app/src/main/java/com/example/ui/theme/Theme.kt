package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Convert Hex string safely to Color, with GoldSecondary fallback
fun String.toColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        GoldSecondary // Default Gold fallback
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    primaryHex: String = "#D4AF37", // Default Gold
    secondaryHex: String = "#121212", // Default Black
    content: @Composable () -> Unit
) {
    val parsedPrimary = primaryHex.toColor()
    val parsedSecondary = secondaryHex.toColor()

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = parsedPrimary,
            secondary = parsedSecondary,
            background = PureBlack,
            surface = DarkCharcoal,
            onPrimary = PureBlack, // High-contrast black on gold primary
            onSecondary = Color.White,
            onBackground = SoftWhite,
            onSurface = SoftWhite,
            tertiary = GoldPrimary
        )
    } else {
        lightColorScheme(
            primary = parsedPrimary,
            secondary = parsedSecondary,
            background = SoftWhite,
            surface = Color.White,
            onPrimary = PureBlack, // High-contrast black on gold primary
            onSecondary = Color.White,
            onBackground = PureBlack,
            onSurface = PureBlack,
            tertiary = GoldPrimary
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

