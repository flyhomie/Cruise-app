package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HazardType
import com.example.data.local.RoadHazardEntity
import com.example.ui.components.AiWarningCard
import com.example.ui.components.CctvInspectionDialog
import com.example.ui.components.CctvMappingDialog
import com.example.ui.components.Cruize3DMapView
import com.example.ui.components.RoadHazardCard
import com.example.ui.components.SurfaceBorder
import com.example.ui.theme.*
import com.example.viewmodel.CruizeViewModel

@Composable
fun HomeScreen(
    viewModel: CruizeViewModel,
    modifier: Modifier = Modifier
) {
    val hazards by viewModel.allHazards.collectAsState()
    val selectedHazard by viewModel.selectedHazard.collectAsState()
    val is3dMode by viewModel.is3dPerspective.collectAsState()
    val speedMph by viewModel.currentSpeedMph.collectAsState()
    val isSimulating by viewModel.isSimulatingDrive.collectAsState()
    val aiAlert by viewModel.aiAlertState.collectAsState()
    val isLoadingAi by viewModel.isLoadingAiAlert.collectAsState()
    val speechMsg by viewModel.speechFeedbackMessage.collectAsState()
    val showCctvLayer by viewModel.showCctvLayer.collectAsState()
    val inspectedCctvHazard by viewModel.inspectedCctvHazard.collectAsState()
    val showCctvMappingDialog by viewModel.showCctvMappingDialog.collectAsState()
    val pinnedLocation by viewModel.pinnedLocation.collectAsState()
    val pinnedRoadName by viewModel.pinnedRoadName.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CruizeDarkBackground)
            .testTag("home_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top Section: Interactive 3D Radar Map Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.1f)
            ) {
                Cruize3DMapView(
                    hazards = hazards,
                    selectedHazard = selectedHazard,
                    onSelectHazard = { viewModel.selectHazard(it) },
                    is3dPerspective = is3dMode,
                    onToggle3d = { viewModel.toggle3dPerspective() },
                    driverSpeedMph = speedMph,
                    isSimulatingDrive = isSimulating,
                    onToggleSimulateDrive = { viewModel.toggleSimulateDrive() },
                    onManualPinDropped = { lat, lng, road ->
                        viewModel.setPinnedLocation(lat, lng, road)
                        viewModel.setShowReportDialog(true)
                    },
                    onMapCctvDropped = { lat, lng, road ->
                        viewModel.setPinnedLocation(lat, lng, road)
                        viewModel.setShowCctvMappingDialog(true)
                    },
                    onInspectCctvFeed = { viewModel.inspectCctvFeed(it) },
                    showCctvLayer = showCctvLayer,
                    onToggleCctvLayer = { viewModel.toggleCctvLayer() },
                    modifier = Modifier.fillMaxSize()
                )

                // Quick Action Floating Buttons on Map Bottom Left
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickReportPillButton(
                        label = "+ CCTV",
                        icon = Icons.Default.Videocam,
                        accentColor = CruizeNeonCyan,
                        onClick = { viewModel.setShowCctvMappingDialog(true) },
                        testTag = "quick_report_cctv"
                    )

                    QuickReportPillButton(
                        label = "+ COP",
                        icon = Icons.Default.Shield,
                        accentColor = CruizePoliceBlue,
                        onClick = { viewModel.setShowReportDialog(true) },
                        testTag = "quick_report_cop"
                    )

                    QuickReportPillButton(
                        label = "+ BUMP",
                        icon = Icons.Default.Warning,
                        accentColor = CruizeWarningAmber,
                        onClick = { viewModel.setShowReportDialog(true) },
                        testTag = "quick_report_bump"
                    )
                }
            }

            // 2. Bottom Section: AI Alert Card & Nearby Hazard Feed
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .background(CruizeDarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Speech feedback banner toast
                if (speechMsg != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CruizeNeonCyan.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = speechMsg ?: "",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CruizeNeonCyan
                                )
                                IconButton(
                                    onClick = { viewModel.clearSpeechFeedback() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = CruizeNeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // AI Realtime Driver Warning Card
                item {
                    AiWarningCard(
                        alert = aiAlert,
                        isLoadingAi = isLoadingAi,
                        onSpeakAlert = { viewModel.speakCurrentAlert() }
                    )
                }

                // Header for Nearby Hazards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NEARBY ROAD & CCTV RADAR ALERTS",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = CruizeNeonCyan,
                            letterSpacing = 0.5.sp
                        )
                        TextButton(
                            onClick = { viewModel.setShowReportDialog(true) },
                            modifier = Modifier.testTag("report_new_hazard_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // List of active road hazard and CCTV cards
                items(hazards, key = { it.id }) { hazard ->
                    RoadHazardCard(
                        hazard = hazard,
                        isSelected = selectedHazard?.id == hazard.id,
                        onClick = { viewModel.selectHazard(hazard) },
                        onReadAlert = { viewModel.readIncomingAlert(hazard) },
                        onUpvote = { viewModel.upvoteHazard(hazard.id) },
                        onDownvote = { viewModel.downvoteHazard(hazard.id) },
                        onInspectCctv = if (hazard.type == "CCTV_CAMERA" || hazard.type == "STATIONARY_SPEED_CAM") {
                            { viewModel.inspectCctvFeed(hazard) }
                        } else null
                    )
                }
            }
        }

        // Live CCTV Camera Video Inspection Modal Dialog
        inspectedCctvHazard?.let { cctv ->
            CctvInspectionDialog(
                hazard = cctv,
                onDismiss = { viewModel.inspectCctvFeed(null) },
                onUpvote = { viewModel.upvoteHazard(it) },
                onDownvote = { viewModel.downvoteHazard(it) },
                onTriggerVoiceAlert = { viewModel.speakText(it) }
            )
        }

        // CCTV Camera Radar Mapping Dialog
        if (showCctvMappingDialog) {
            CctvMappingDialog(
                pinnedLocation = pinnedLocation,
                pinnedRoadName = pinnedRoadName,
                onDismiss = { viewModel.setShowCctvMappingDialog(false) },
                onSubmit = { title, road, camType, direction, status, limit, desc ->
                    viewModel.mapCctvCamera(
                        title = title,
                        roadName = road,
                        cameraType = camType,
                        cameraDirection = direction,
                        cameraStatus = status,
                        speedLimit = limit,
                        description = desc
                    )
                }
            )
        }
    }
}

@Composable
private fun QuickReportPillButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = accentColor,
        shadowElevation = 8.dp,
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = CruizeDarkBackground,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = CruizeDarkBackground
            )
        }
    }
}
