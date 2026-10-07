package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

enum class CruizeLogoSize {
    SMALL,
    MEDIUM,
    LARGE
}

@Composable
fun CruizeBrandLogo(
    modifier: Modifier = Modifier,
    logoSize: CruizeLogoSize = CruizeLogoSize.MEDIUM,
    showText: Boolean = true,
    animated: Boolean = true,
    subtitle: String = "EAST AFRICA ROAD RADAR"
) {
    val logoDimension: Dp = when (logoSize) {
        CruizeLogoSize.SMALL -> 34.dp
        CruizeLogoSize.MEDIUM -> 54.dp
        CruizeLogoSize.LARGE -> 84.dp
    }

    val titleFontSize = when (logoSize) {
        CruizeLogoSize.SMALL -> 14.sp
        CruizeLogoSize.MEDIUM -> 20.sp
        CruizeLogoSize.LARGE -> 28.sp
    }

    val subtitleFontSize = when (logoSize) {
        CruizeLogoSize.SMALL -> 8.sp
        CruizeLogoSize.MEDIUM -> 10.sp
        CruizeLogoSize.LARGE -> 12.sp
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cruize_logo_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logo_sweep"
    )

    Row(
        modifier = modifier.testTag("cruize_brand_logo"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo Emblem Badge
        Box(
            modifier = Modifier
                .size(logoDimension)
                .shadow(
                    elevation = if (logoSize == CruizeLogoSize.LARGE) 12.dp else 6.dp,
                    shape = CircleShape,
                    ambientColor = CruizeNeonCyan,
                    spotColor = CruizeCobaltBlue
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0F172A),
                            Color(0xFF030712)
                        )
                    )
                )
                .border(
                    width = if (logoSize == CruizeLogoSize.SMALL) 1.dp else 1.5.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            CruizeNeonCyan,
                            CruizeCobaltBlue,
                            CruizeAmberAlert,
                            CruizeNeonCyan
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Background Image Asset if present, with canvas vector shield
            Image(
                painter = painterResource(id = R.drawable.cruize_brand_logo_1786957704253),
                contentDescription = "Cruize Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // Radar sweep vector overlay
            if (animated) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = (size.width / 2f) * 0.85f

                    // Subtle radar beacon rings
                    drawCircle(
                        color = CruizeNeonCyan.copy(alpha = 0.35f),
                        radius = radius * (if (animated) pulseScale * 0.7f else 0.7f),
                        center = center,
                        style = Stroke(width = 1.2f)
                    )

                    // Sweeping beam
                    val sweepRad = Math.toRadians(radarSweepAngle.toDouble())
                    val endX = center.x + radius * Math.cos(sweepRad).toFloat()
                    val endY = center.y + radius * Math.sin(sweepRad).toFloat()

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(CruizeNeonCyan.copy(alpha = 0.8f), Color.Transparent),
                            start = center,
                            end = Offset(endX, endY)
                        ),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 2.5f
                    )
                }
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.width(if (logoSize == CruizeLogoSize.SMALL) 8.dp else 12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CRUIZE",
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Black,
                        color = CruizeTextPrimary,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CruizeNeonCyan.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "AI",
                            fontSize = (subtitleFontSize.value + 1).sp,
                            fontWeight = FontWeight.Black,
                            color = CruizeNeonCyan,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = subtitleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = CruizeTextSecondary,
                    letterSpacing = 1.1.sp
                )
            }
        }
    }
}

@Composable
fun CruizeBrandBanner(
    modifier: Modifier = Modifier,
    activeHazardsCount: Int = 0,
    speedUnitLabel: String = "KM/H",
    onReportClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cruize_brand_banner"),
        shape = RoundedCornerShape(20.dp),
        color = CruizeDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                colors = listOf(
                    CruizeNeonCyan.copy(alpha = 0.5f),
                    CruizeCobaltBlue.copy(alpha = 0.3f),
                    Color.Transparent
                )
            )
        )
    ) {
        Box {
            // Background glow
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CruizeNeonCyan.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.5f),
                        radius = size.width * 0.4f
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CruizeBrandLogo(
                    logoSize = CruizeLogoSize.MEDIUM,
                    subtitle = "EAST AFRICA ROAD RADAR",
                    animated = true
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CruizeNeonCyan.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CruizeNeonCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$activeHazardsCount Active Live",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CruizeNeonCyan
                        )
                    }
                }
            }
        }
    }
}
