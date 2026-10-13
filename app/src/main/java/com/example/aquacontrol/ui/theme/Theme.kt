package com.example.aquacontrol.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Red,
    onPrimary = Color.White,
    primaryContainer = FondoCriticoClaro,
    onPrimaryContainer = ContenidoCriticoClaro,

    secondary = Blue,
    onSecondary = Color.White,
    secondaryContainer = Sky,
    onSecondaryContainer = Navy,

    tertiary = AzulPanelAqua,
    onTertiary = Color.White,
    tertiaryContainer = Sky,
    onTertiaryContainer = Navy,

    background = Paper,
    onBackground = Navy,

    surface = Color.White,
    onSurface = Navy,
    surfaceVariant = Paper,
    onSurfaceVariant = Muted,
    surfaceTint = Color.Transparent,

    outline = Muted,
    outlineVariant = Line,

    error = ContenidoCriticoClaro,
    onError = Color.White,
    errorContainer = FondoCriticoClaro,
    onErrorContainer = ContenidoCriticoClaro,

    inverseSurface = Navy,
    inverseOnSurface = Color.White,
    inversePrimary = Color(0xFFFFB4AA),

    scrim = Color.Black,

    surfaceDim = Color(0xFFE8EEED),
    surfaceBright = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Paper,
    surfaceContainerHighest = Color(0xFFEDF2F1)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB4AA),
    onPrimary = Color(0xFF650E0C),
    primaryContainer = Color(0xFF713330),
    onPrimaryContainer = Color(0xFFFFDAD5),

    secondary = Color(0xFFA4D8EF),
    onSecondary = Navy,
    secondaryContainer = Color(0xFF164B60),
    onSecondaryContainer = Sky,

    tertiary = Color(0xFF9ED5E4),
    onTertiary = Navy,
    tertiaryContainer = Color(0xFF164B60),
    onTertiaryContainer = Sky,

    background = Color(0xFF101D25),
    onBackground = Color(0xFFE5EDF1),

    surface = Color(0xFF172630),
    onSurface = Color(0xFFE5EDF1),
    surfaceVariant = Color(0xFF293D48),
    onSurfaceVariant = Color(0xFFBDCDD5),
    surfaceTint = Color.Transparent,

    outline = Color(0xFF8EA5B1),
    outlineVariant = Color(0xFF3B515D),

    error = ContenidoCriticoOscuro,
    onError = Color(0xFF690005),
    errorContainer = FondoCriticoOscuro,
    onErrorContainer = ContenidoCriticoOscuro,

    inverseSurface = Paper,
    inverseOnSurface = Navy,
    inversePrimary = Red,

    scrim = Color.Black,

    surfaceDim = Color(0xFF0C171E),
    surfaceBright = Color(0xFF304551),
    surfaceContainerLowest = Color(0xFF0C171E),
    surfaceContainerLow = Color(0xFF172630),
    surfaceContainer = Color(0xFF1C2E39),
    surfaceContainerHigh = Color(0xFF243844),
    surfaceContainerHighest = Color(0xFF2D4450)
)

private val AquaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AquaControlTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = AquaShapes,
        content = content
    )
}