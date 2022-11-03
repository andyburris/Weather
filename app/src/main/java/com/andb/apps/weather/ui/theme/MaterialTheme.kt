package com.andb.apps.weather.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andb.apps.weather.R

val weatherColors = lightColors(

)

val weatherTypography
    @Composable get() = Typography(
        defaultFontFamily = FontFamily(
            fonts = listOf(
                Font(R.font.interregular, weight = FontWeight.Normal),
                Font(R.font.intermedium, weight = FontWeight.Medium),
                Font(R.font.intersemibold, weight = FontWeight.SemiBold),
                Font(R.font.interbold, weight = FontWeight.Bold)
            )
        ),
        h1 = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 48.sp,
        ),
        h6 = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
        ),
        subtitle1 = TextStyle(
            fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    body2 = TextStyle(
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontSize = 10.sp
    ),
)

val weatherShapes = Shapes(
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

val ContentAlpha.low: Float @Composable get() = ContentAlpha.disabled
val ContentAlpha.divider: Float
    @Composable get() = when (MaterialTheme.colors.isLight) {
        true -> .12f
        false -> .25f
    }
val ContentAlpha.overlay: Float
    @Composable get() = when (MaterialTheme.colors.isLight) {
        true -> .05f
        false -> .10f
    }

val Colors.onBackgroundSecondary @Composable get() = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium)
val Colors.onBackgroundTertiary @Composable get() = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.low)
val Colors.onBackgroundDivider @Composable get() = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.divider)
val Colors.onBackgroundOverlay @Composable get() = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.overlay)
val Colors.onPrimarySecondary @Composable get() = MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.medium)
