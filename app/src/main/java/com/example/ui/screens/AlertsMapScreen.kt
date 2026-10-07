package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RoadHazardEntity
import com.example.ui.components.CctvInspectionDialog
import com.example.ui.components.CctvMappingDialog
import com.example.ui.components.Cruize3DMapView
import com.example.ui.components.RoadHazardCard
import com.example.ui.theme.*
import com.example.viewmodel.CruizeViewModel

@Composable
fun AlertsMapScreen(
    viewModel: CruizeViewModel,
    modifier: Modifier = Modifier
) {
    val allHazards by viewModel.allHazards.collectAsState()
    val filterType by viewModel.selectedMapFilter.collectAsState()
    val selectedHazard by viewModel.selectedHazard.collectAsState()
    val is3dMode by viewModel.is3dPerspective.collectAsState()
    val speedMph by viewModel.currentSpeedMph.collectAsState()
    val isSimulating by viewModel.isSimulatingDrive.collectAsState()
    val showCctvLayer by viewModel.showCctvLayer.collectAsState()
    val inspectedCctvHazard by viewModel.inspectedCctvHazard.collectAsState()
    val showCctvMappingDialog by viewModel.showCctvMappingDialog.collectAsState()
    val pinnedLocation by viewModel.pinnedLocation.collectAsState()
    val pinnedRoadName by viewModel.pinnedRoadName.collectAsState()

    var showListView by remember { mutableStateOf(false) }

    val severityFilter by viewModel.selectedSeverityFilter.collectAsState()
    val radiusFilterMeters by viewModel.selectedRadiusFilterMeters.collectAsState()

    val filteredHazards = remember(allHazards, filterType, severityFilter, radiusFilterMeters) {
        allHazards.filter { h ->
            val matchesType = if (filterType == "ALL") true else h.type == filterType
            val matchesSeverity = if (severityFilter == "ALL") true else h.severity == severityFilter
            val matchesRadius = if (radiusFilterMeters == 0) true else true
            matchesType && matchesSeverity && matchesRadius
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CruizeDarkBackground)
            .testTag("alerts_map_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Category Filter Chips Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CruizeDarkSurface,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CRUIZE 3D RADAR MAP",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = CruizeNeonCyan,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Quick Map CCTV Button
                            FilledTonalButton(
                                onClick = { viewModel.setShowCctvMappingDialog(true) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = CruizeNeonCyan.copy(alpha = 0.2f),
                                    contentColor = CruizeNeonCyan
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("button_map_cctv_quick")
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Map CCTV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            FilterChip(
                                selected = showListView,
                                onClick = { showListView = !showListView },
                                label = { Text(if (showListView) "Map" else "Feed", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (showListView) Icons.Default.Map else Icons.Default.List,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CruizeNeonCyan,
                                    selectedLabelColor = CruizeDarkBackground,
                                    containerColor = CruizeDarkSurfaceVariant,
                                    labelColor = CruizeTextPrimary
                                ),
                                modifier = Modifier.testTag("toggle_list_map_view")
                            )
                        }
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = filterType == "ALL",
                                onClick = { viewModel.setMapFilter("ALL") },
                                label = { Text("All Types (${allHazards.size})") },
                                modifier = Modifier.testTag("filter_all")
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterType == "CCTV_CAMERA",
                                onClick = { viewModel.setMapFilter("CCTV_CAMERA") },
                                label = { Text("CCTV Cams 📹") },
                                modifier = Modifier.testTag("filter_cctv_cams")
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterType == "COP_TRAP",
                                onClick = { viewModel.setMapFilter("COP_TRAP") },
                                label = { Text("Cop Traps 🚨") },
                                modifier = Modifier.testTag("filter_cops")
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterType == "STATIONARY_SPEED_CAM",
                                onClick = { viewModel.setMapFilter("STATIONARY_SPEED_CAM") },
                                label = { Text("Fixed Cams 📸") },
                                modifier = Modifier.testTag("filter_stationary_cams")
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterType == "MOBILE_SPEED_CAM",
                                onClick = { viewModel.setMapFilter("MOBILE_SPEED_CAM") },
                                label = { Text("Mobile Cams 🚐") },
                                modifier = Modifier.testTag("filter_mobile_cams")
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterType == "SPEED_BUMP",
                                onClick = { viewModel.setMapFilter("SPEED_BUMP") },
                                label = { Text("Speed Bumps ⚠️") },
                                modifier = Modifier.testTag("filter_bumps")
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterType == "SPEED_CAMERA",
                                onClick = { viewModel.setMapFilter("SPEED_CAMERA") },
                                label = { Text("Speed Cams 📷") },
                                modifier = Modifier.testTag("filter_cams")
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterType == "POTHOLE",
                                onClick = { viewModel.setMapFilter("POTHOLE") },
                                label = { Text("Potholes 🕳️") },
                                modifier = Modifier.testTag("filter_potholes")
                            )
                        }
                        item {
                            FilterChip(
                                selected = severityFilter == "CRITICAL",
                                onClick = { viewModel.setSeverityFilter(if (severityFilter == "CRITICAL") "ALL" else "CRITICAL") },
                                label = { Text("⚡ Critical Only") },
                                modifier = Modifier.testTag("filter_critical")
                            )
                        }
                        item {
                            FilterChip(
                                selected = radiusFilterMeters == 500,
                                onClick = { viewModel.setRadiusFilter(if (radiusFilterMeters == 500) 0 else 500) },
                                label = { Text("📍 Within 500m") },
                                modifier = Modifier.testTag("filter_radius_500m")
                            )
                        }
                    }
                }
            }

            // Main View Area: Either 3D Interactive Map or List View
            if (!showListView) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Cruize3DMapView(
                        hazards = filteredHazards,
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

                    // Selected Hazard Inspector Bottom Sheet Card
                    if (selectedHazard != null) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = CruizeDarkSurface.copy(alpha = 0.95f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, CruizeNeonCyan),
                            shadowElevation = 12.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedHazard?.title ?: "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = CruizeNeonCyan
                                    )
                                    IconButton(
                                        onClick = { viewModel.selectHazard(null) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = CruizeTextSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = "${selectedHazard?.roadName} • Speed Limit: ${selectedHazard?.speedLimit} KM/H",
                                    fontSize = 12.sp,
                                    color = CruizeTextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = selectedHazard?.description ?: "",
                                    fontSize = 12.sp,
                                    color = CruizeTextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (selectedHazard?.type == "CCTV_CAMERA" || selectedHazard?.type == "STATIONARY_SPEED_CAM") {
                                        Button(
                                            onClick = { viewModel.inspectCctvFeed(selectedHazard) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CruizeElectricBlue),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("View Live Cam", fontSize = 11.sp, color = CruizeTextPrimary)
                                        }
                                    } else {
                                        Text(
                                            text = "Reported by ${selectedHazard?.reportedBy}",
                                            fontSize = 10.sp,
                                            color = CruizeTextDim
                                        )
                                    }
                                    Button(
                                        onClick = { viewModel.upvoteHazard(selectedHazard!!.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CruizeNeonCyan),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Confirm Active (${selectedHazard?.upvotes})", fontSize = 11.sp, color = CruizeDarkBackground)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHazards, key = { it.id }) { h ->
                        RoadHazardCard(
                            hazard = h,
                            isSelected = selectedHazard?.id == h.id,
                            onClick = { viewModel.selectHazard(h) },
                            onReadAlert = { viewModel.readIncomingAlert(h) },
                            onUpvote = { viewModel.upvoteHazard(h.id) },
                            onDownvote = { viewModel.downvoteHazard(h.id) },
                            onInspectCctv = if (h.type == "CCTV_CAMERA" || h.type == "STATIONARY_SPEED_CAM") {
                                { viewModel.inspectCctvFeed(h) }
                            } else null
                        )
                    }
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
