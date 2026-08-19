package com.lonaloto.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SchemeClair = lightColorScheme(
    primary = OrangeLonaloto,
    onPrimary = SurfaceClair,
    primaryContainer = OrangeLonalotoClair,
    onPrimaryContainer = OrangeLonalotoFonce,

    secondary = VertLonaloto,
    onSecondary = SurfaceClair,
    secondaryContainer = VertLonalotoClair,
    onSecondaryContainer = VertLonalotoFonce,

    background = FondClair,
    onBackground = TexteClair,
    surface = SurfaceClair,
    onSurface = TexteClair,

    error = RougeErreur
)

private val SchemeSombre = darkColorScheme(
    primary = OrangeLonaloto,
    onPrimary = TexteClair,
    primaryContainer = OrangeLonalotoFonce,
    onPrimaryContainer = OrangeLonalotoClair,

    secondary = VertLonaloto,
    onSecondary = TexteClair,
    secondaryContainer = VertLonalotoFonce,
    onSecondaryContainer = VertLonalotoClair,

    background = FondSombre,
    onBackground = TexteSombre,
    surface = SurfaceSombre,
    onSurface = TexteSombre,

    error = RougeErreur
)

/**
 * Thème racine de l'app.
 * `dynamicColor` est désactivé par défaut : sur Android 12+, le "Material You"
 * remplacerait sinon les couleurs LONALOTO par celles du fond d'écran de
 * l'utilisateur — on garde une identité de marque cohérente sur tous les
 * appareils, ce qui compte davantage pour une app professionnelle terrain.
 */
@Composable
fun LonalotoTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> SchemeSombre
        else -> SchemeClair
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TypographieLonaloto,
        content = content
    )
}
