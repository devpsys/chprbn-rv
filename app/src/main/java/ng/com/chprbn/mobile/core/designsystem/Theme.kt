package ng.com.chprbn.mobile.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Surface,
    background = Background,
    onBackground = NeutralText,
    surface = Surface,
    onSurface = NeutralText,
    error = ErrorRed,
    onError = Surface
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = Surface,
    background = NeutralText,
    onBackground = Surface,
    surface = NeutralText,
    onSurface = Surface,
    error = ErrorRed,
    onError = Surface
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

