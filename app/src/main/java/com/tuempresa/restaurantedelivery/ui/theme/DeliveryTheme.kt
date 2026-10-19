package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography as MaterialTypography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DeliveryColors = lightColorScheme(
    primary = Color(0xFFC63F2A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE9E1),
    onPrimaryContainer = Color(0xFF702415),

    secondary = Color(0xFF72513F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5E5D8),
    onSecondaryContainer = Color(0xFF362318),

    background = Color(0xFFFFF8F3),
    onBackground = Color(0xFF292320),

    surface = Color.White,
    onSurface = Color(0xFF292320),
    surfaceVariant = Color(0xFFF3EBE5),
    onSurfaceVariant = Color(0xFF756A63),

    outline = Color(0xFF92847B),
    outlineVariant = Color(0xFFE9DED6),

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    surfaceTint = Color.Transparent
)

private val DeliveryTypography = MaterialTypography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun DeliveryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DeliveryColors,
        typography = DeliveryTypography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(12.dp),
            small = RoundedCornerShape(16.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}