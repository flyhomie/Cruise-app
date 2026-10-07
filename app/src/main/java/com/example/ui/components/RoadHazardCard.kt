package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RoadHazardEntity
import com.example.ui.theme.*

@Composable
fun RoadHazardCard(
    hazard: RoadHazardEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onReadAlert: () -> Unit,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
    onInspectCctv: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isCctv = hazard.type == "CCTV_CAMERA" || hazard.type == "STATIONARY_SPEED_CAM"

    val iconVector = when (hazard.type) {
        "CCTV_CAMERA" -> Icons.Default.Videocam
        "COP_TRAP" -> Icons.Default.LocalPolice
        "STATIONARY_SPEED_CAM" -> Icons.Default.PhotoCamera
        "MOBILE_SPEED_CAM" -> Icons.Default.DirectionsCar
        "SPEED_CAMERA" -> Icons.Default.CameraAlt
        "SPEED_BUMP" -> Icons.Default.Warning
        "POTHOLE" -> Icons.Default.ReportProblem
        else -> Icons.Default.LocationOn
    }

    val accentColor = when (hazard.type) {
        "CCTV_CAMERA" -> CruizeNeonCyan
        "COP_TRAP" -> CruizePoliceBlue
        "STATIONARY_SPEED_CAM" -> CruizeElectricBlue
        "MOBILE_SPEED_CAM" -> CruizeWarningAmber
        "SPEED_CAMERA" -> CruizeElectricBlue
        "SPEED_BUMP" -> CruizeWarningAmber
        "POTHOLE" -> CruizeDangerCrimson
        else -> CruizeNeonCyan
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CruizeDarkSurfaceVariant else CruizeDarkSurface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) CruizeNeonCyan else CruizeDarkCardBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("road_hazard_card_${hazard.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Type Icon, Title, and Speed/Severity Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, accentColor),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hazard.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = CruizeTextPrimary
                    )
                    Text(
                        text = hazard.roadName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CruizeElectricBlue
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CruizeWarningAmber.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CruizeWarningAmber)
                ) {
                    Text(
                        text = "${hazard.speedLimit} KM/H",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = CruizeWarningAmber,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = hazard.description,
                fontSize = 12.sp,
                color = CruizeTextSecondary,
                lineHeight = 16.sp
            )

            // CCTV Metadata if applicable
            if (hazard.type == "CCTV_CAMERA") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CruizeNeonCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "📸 ${hazard.cameraType.replace('_', ' ')}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CruizeNeonCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CruizeElectricBlue.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "↕️ ${hazard.cameraDirection.replace('_', ' ')}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CruizeElectricBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = CruizeDarkCardBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Verification & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Reported by ${hazard.reportedBy}",
                        fontSize = 10.sp,
                        color = CruizeTextDim
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCctv && onInspectCctv != null) {
                        FilledTonalButton(
                            onClick = onInspectCctv,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = CruizeNeonCyan.copy(alpha = 0.2f),
                                contentColor = CruizeNeonCyan
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("card_cctv_inspect_button_${hazard.id}")
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Feed", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = onReadAlert,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Read Alert",
                            tint = CruizeNeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onUpvote,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Upvote",
                                tint = CruizeSuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${hazard.upvotes}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CruizeSuccessGreen
                            )
                        }
                    }
                }
            }
        }
    }
}
