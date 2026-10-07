package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SlidingTopBar(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    currentSpeedMph: Int,
    currentRoadName: String,
    is3dMode: Boolean,
    onToggle3d: () -> Unit,
    isAiVoiceOn: Boolean,
    onToggleAiVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .testTag("sliding_top_bar")
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(animationSpec = spring()) { -it } + expandVertically() + fadeIn(),
            exit = slideOutVertically(animationSpec = spring()) { -it } + shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                color = CruizeDarkSurface.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                border = SurfaceBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Cruize Logo & Live Road
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            CruizeBrandLogo(
                                logoSize = CruizeLogoSize.SMALL,
                                showText = false,
                                animated = true
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CRUIZE RADAR",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = CruizeNeonCyan,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = currentRoadName,
                                    fontSize = 11.sp,
                                    color = CruizeTextSecondary,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right: Live Speedometer Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CruizeDarkSurfaceVariant,
                            border = SurfaceBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$currentSpeedMph",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = CruizeTextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "KM/H",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = CruizeNeonCyan
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // AI Status Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CruizeDarkSurfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sensors,
                                contentDescription = "AI Active",
                                tint = CruizeSuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Radar Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CruizeSuccessGreen
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // 3D Mode Toggle
                            FilterChip(
                                selected = is3dMode,
                                onClick = onToggle3d,
                                label = { Text(if (is3dMode) "3D Perspective" else "2D Map", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (is3dMode) Icons.Default.ViewInAr else Icons.Default.Map,
                                        contentDescription = "Toggle 3D",
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CruizeNeonCyan,
                                    selectedLabelColor = CruizeDarkBackground,
                                    containerColor = CruizeDarkSurfaceVariant,
                                    labelColor = CruizeTextPrimary
                                ),
                                modifier = Modifier.testTag("toggle_3d_button")
                            )

                            // AI Voice Toggle
                            IconButton(
                                onClick = onToggleAiVoice,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isAiVoiceOn) CruizeNeonCyan.copy(alpha = 0.2f) else CruizeDarkSurfaceVariant)
                                    .testTag("toggle_voice_button")
                            ) {
                                Icon(
                                    imageVector = if (isAiVoiceOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = "Voice Warning Toggle",
                                    tint = if (isAiVoiceOn) CruizeNeonCyan else CruizeTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Slide Off Button
                            IconButton(
                                onClick = onToggleExpand,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CruizeDarkSurfaceVariant)
                                    .testTag("slide_up_top_bar")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Slide Up Top Bar",
                                    tint = CruizeTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Toggle Handle Pill when top bar is collapsed or at bottom edge of bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggleExpand() }
                    .testTag("top_bar_toggle_pill"),
                shape = RoundedCornerShape(16.dp),
                color = CruizeDarkSurface.copy(alpha = 0.9f),
                border = SurfaceBorder()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Top Bar",
                        tint = CruizeNeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isExpanded) "Hide Radar Top Bar" else "CRUIZE RADAR • $currentSpeedMph KM/H",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CruizeNeonCyan,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SurfaceBorder() = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
