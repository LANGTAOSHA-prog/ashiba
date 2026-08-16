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
    primary = BlueprintBlueDark,
    onPrimary = BlueprintBlueOnDark,
    primaryContainer = BlueprintBlueContainerDark,
    onPrimaryContainer = BlueprintBlueOnContainerDark,
    secondary = SafetyOrangeDark,
    onSecondary = SafetyOrangeOnDark,
    secondaryContainer = SafetyOrangeContainerDark,
    onSecondaryContainer = SafetyOrangeOnContainerDark,
    tertiary = SteelTealDark,
    onTertiary = SteelTealOnDark,
    tertiaryContainer = SteelTealContainerDark,
    onTertiaryContainer = SteelTealOnContainerDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
)

private val LightColorScheme = lightColorScheme(
    primary = BlueprintBlueLight,
    onPrimary = BlueprintBlueOnLight,
    primaryContainer = BlueprintBlueContainerLight,
    onPrimaryContainer = BlueprintBlueOnContainerLight,
    secondary = SafetyOrangeLight,
    onSecondary = SafetyOrangeOnLight,
    secondaryContainer = SafetyOrangeContainerLight,
    onSecondaryContainer = SafetyOrangeOnContainerLight,
    tertiary = SteelTealLight,
    onTertiary = SteelTealOnLight,
    tertiaryContainer = SteelTealContainerLight,
    onTertiaryContainer = SteelTealOnContainerLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our distinct blueprint & construction theme by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
