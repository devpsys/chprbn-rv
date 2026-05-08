package ng.com.chprbn.mobile.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Surface,
    secondary = LightOnSurfaceVariant,
    onSecondary = Surface,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = PrimaryGreen,
    onTertiary = Surface,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = Background,
    onBackground = NeutralText,
    surface = Surface,
    onSurface = NeutralText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = ErrorRed,
    onError = Surface
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = DarkOnSurface,
    secondary = DarkOnSurfaceVariant,
    onSecondary = DarkSurface,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkOnSurface,
    tertiary = PrimaryGreen,
    onTertiary = DarkOnSurface,
    tertiaryContainer = PrimaryGreen.copy(alpha = 0.22f),
    onTertiaryContainer = DarkOnSurface,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = ErrorRed,
    onError = DarkOnSurface
)

@Composable
fun ChprbnTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ChprbnTypography,
        shapes = ChprbnShapes,
        content = content
    )
}

