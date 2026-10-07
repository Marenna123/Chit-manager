package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

class ThemeController(
    val currentMode: ThemeMode,
    val isDark: Boolean,
    val onModeChanged: (ThemeMode) -> Unit
) {
    fun toggleLightDark() {
        if (isDark) onModeChanged(ThemeMode.LIGHT) else onModeChanged(ThemeMode.DARK)
    }

    fun setLight() = onModeChanged(ThemeMode.LIGHT)
    fun setDark() = onModeChanged(ThemeMode.DARK)
    fun setSystem() = onModeChanged(ThemeMode.SYSTEM)
}

val LocalThemeController = staticCompositionLocalOf<ThemeController> {
    ThemeController(ThemeMode.SYSTEM, false) {}
}

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCFCE7),
    onPrimaryContainer = NavyDark,
    secondary = TealAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = AmberAccent,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = TextDark,
    surface = SurfaceCard,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextMuted,
    outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF86EFAC),
    onPrimary = Color(0xFF00381B),
    primaryContainer = Color(0xFF144D29),
    onPrimaryContainer = Color(0xFFDCFCE7),
    secondary = Color(0xFFFDE68A),
    onSecondary = Color(0xFF451A03),
    tertiary = Color(0xFFFDE68A),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val themeController = remember(themeMode, isDark, onThemeModeChange) {
        ThemeController(
            currentMode = themeMode,
            isDark = isDark,
            onModeChanged = onThemeModeChange
        )
    }

    CompositionLocalProvider(LocalThemeController provides themeController) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Overload for backward compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(
        themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
        onThemeModeChange = {},
        dynamicColor = dynamicColor,
        content = content
    )
}
