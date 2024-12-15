package io.github.devriesl.raptormark.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = DeepBlue,
    onPrimary = White,
    primaryContainer = LightBlue,
    onPrimaryContainer = DarkBlue,
    secondary = Teal,
    onSecondary = White,
    secondaryContainer = LightTeal,
    onSecondaryContainer = DarkTeal,
    tertiary = Purple,
    onTertiary = White,
    tertiaryContainer = Lavender,
    onTertiaryContainer = DarkPurple,
    background = White,
    onBackground = DarkGray,
    surface = White,
    onSurface = DarkGray,
    error = Red,
    onError = White,
    outline = LightGray,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray
)

private val DarkColorScheme = darkColorScheme(
    primary = DeepBlue,
    onPrimary = White,
    primaryContainer = DarkBlue,
    onPrimaryContainer = LightBlue,
    secondary = Teal,
    onSecondary = White,
    secondaryContainer = DarkTeal,
    onSecondaryContainer = LightTeal,
    tertiary = Purple,
    onTertiary = White,
    tertiaryContainer = DarkPurple,
    onTertiaryContainer = Lavender,
    background = DarkGray,
    onBackground = White,
    surface = DarkGray,
    onSurface = White,
    error = Red,
    onError = White,
    outline = LightGray,
    surfaceVariant = DarkGray,
    onSurfaceVariant = LightGray
)

@Composable
fun RaptorMarkTheme(
    dynamicColor: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        },
        typography = Typography,
        content = content
    )
}