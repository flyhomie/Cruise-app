package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HazardSeverity
import com.example.data.local.HazardType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HazardReportDialog(
    onDismiss: () -> Unit,
    pinnedLocation: Pair<Double, Double>? = null,
    pinnedRoadName: String? = null,
    onSubmit: (
        type: HazardType,
        title: String,
        description: String,
        roadName: String,
        severity: HazardSeverity
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf(HazardType.STATIONARY_SPEED_CAM) }
    var titleInput by remember { mutableStateOf("") }
    var roadInput by remember { mutableStateOf(pinnedRoadName ?: "Thika Superhighway & Survey, Nairobi") }
    var descInput by remember { mutableStateOf("") }
    var selectedSeverity by remember { mutableStateOf(HazardSeverity.HIGH) }

    // Preset suggested title based on selected type
    LaunchedEffect(selectedType) {
        if (titleInput.isBlank()) {
            titleInput = when (selectedType) {
                HazardType.CCTV_CAMERA -> "Live ANPR / Surveillance CCTV"
                HazardType.COP_TRAP -> "Kenya Traffic Police Speed Trap"
                HazardType.STATIONARY_SPEED_CAM -> "NTSA Fixed Speed Camera (80 KM/H)"
                HazardType.MOBILE_SPEED_CAM -> "NTSA Mobile Speed Radar Van"
                HazardType.SPEED_CAMERA -> "Fixed Overhead Speed Cam"
                HazardType.SPEED_BUMP -> "Steep Unmarked Speed Hump"
                HazardType.POTHOLE -> "Deep Rim Damage Pothole"
                HazardType.ACCIDENT -> "Road Traffic Accident"
                HazardType.ROADWORK -> "Roadworks & Matatu Diversion"
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CruizeDarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        modifier = Modifier.testTag("hazard_report_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "REPORT ROAD HAZARD",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = CruizeNeonCyan,
                letterSpacing = 1.sp
            )
            Text(
                text = "Help fellow Cruize drivers stay warned & safe. Earn +15 Karma points!",
                fontSize = 12.sp,
                color = CruizeTextSecondary
            )

            if (pinnedLocation != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CruizeNeonCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PinDrop,
                            contentDescription = "Pinned Location",
                            tint = CruizeNeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "MANUAL MAP PIN DROPPED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = CruizeNeonCyan
                            )
                            Text(
                                text = "Lat: ${pinnedLocation.first.toString().take(8)} • Lng: ${pinnedLocation.second.toString().take(8)}",
                                fontSize = 10.sp,
                                color = CruizeTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Select Hazard Type
            Text(
                text = "SELECT HAZARD TYPE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CruizeTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Row 1: CCTV & Speed Cameras
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HazardTypeOptionButton(
                    type = HazardType.CCTV_CAMERA,
                    label = "CCTV Cam 📹",
                    icon = Icons.Default.Videocam,
                    selected = selectedType == HazardType.CCTV_CAMERA,
                    accentColor = CruizeNeonCyan,
                    onSelect = { selectedType = HazardType.CCTV_CAMERA },
                    modifier = Modifier.weight(1f)
                )

                HazardTypeOptionButton(
                    type = HazardType.STATIONARY_SPEED_CAM,
                    label = "Fixed Cam 📸",
                    icon = Icons.Default.PhotoCamera,
                    selected = selectedType == HazardType.STATIONARY_SPEED_CAM,
                    accentColor = CruizeElectricBlue,
                    onSelect = { selectedType = HazardType.STATIONARY_SPEED_CAM },
                    modifier = Modifier.weight(1f)
                )

                HazardTypeOptionButton(
                    type = HazardType.COP_TRAP,
                    label = "Cop Trap 🚨",
                    icon = Icons.Default.Shield,
                    selected = selectedType == HazardType.COP_TRAP,
                    accentColor = CruizePoliceBlue,
                    onSelect = { selectedType = HazardType.COP_TRAP },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Bumps, Potholes, Accidents
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HazardTypeOptionButton(
                    type = HazardType.SPEED_BUMP,
                    label = "Speed Bump ⚠️",
                    icon = Icons.Default.Warning,
                    selected = selectedType == HazardType.SPEED_BUMP,
                    accentColor = CruizeWarningAmber,
                    onSelect = { selectedType = HazardType.SPEED_BUMP },
                    modifier = Modifier.weight(1f)
                )

                HazardTypeOptionButton(
                    type = HazardType.POTHOLE,
                    label = "Pothole 🕳️",
                    icon = Icons.Default.ReportProblem,
                    selected = selectedType == HazardType.POTHOLE,
                    accentColor = CruizeDangerCrimson,
                    onSelect = { selectedType = HazardType.POTHOLE },
                    modifier = Modifier.weight(1f)
                )

                HazardTypeOptionButton(
                    type = HazardType.ACCIDENT,
                    label = "Accident 💥",
                    icon = Icons.Default.CarCrash,
                    selected = selectedType == HazardType.ACCIDENT,
                    accentColor = CruizeDangerCrimson,
                    onSelect = { selectedType = HazardType.ACCIDENT },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Road Name & Title Input
            OutlinedTextField(
                value = roadInput,
                onValueChange = { roadInput = it },
                label = { Text("Road / Intersection") },
                leadingIcon = { Icon(Icons.Default.AddLocation, contentDescription = null, tint = CruizeNeonCyan) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CruizeNeonCyan,
                    unfocusedBorderColor = CruizeDarkCardBorder,
                    focusedLabelColor = CruizeNeonCyan,
                    unfocusedLabelColor = CruizeTextSecondary,
                    focusedTextColor = CruizeTextPrimary,
                    unfocusedTextColor = CruizeTextPrimary
                ),
                modifier = Modifier.fillMaxWidth().testTag("input_road_name")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                label = { Text("Hazard Title") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, tint = CruizeNeonCyan) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CruizeNeonCyan,
                    unfocusedBorderColor = CruizeDarkCardBorder,
                    focusedLabelColor = CruizeNeonCyan,
                    unfocusedLabelColor = CruizeTextSecondary,
                    focusedTextColor = CruizeTextPrimary,
                    unfocusedTextColor = CruizeTextPrimary
                ),
                modifier = Modifier.fillMaxWidth().testTag("input_hazard_title")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = descInput,
                onValueChange = { descInput = it },
                label = { Text("Details for Drivers (e.g. hidden behind bridge, left lane)") },
                minLines = 2,
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CruizeNeonCyan,
                    unfocusedBorderColor = CruizeDarkCardBorder,
                    focusedLabelColor = CruizeNeonCyan,
                    unfocusedLabelColor = CruizeTextSecondary,
                    focusedTextColor = CruizeTextPrimary,
                    unfocusedTextColor = CruizeTextPrimary
                ),
                modifier = Modifier.fillMaxWidth().testTag("input_hazard_desc")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Severity Selector
            Text(
                text = "SEVERITY LEVEL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CruizeTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HazardSeverity.values().forEach { sev ->
                    FilterChip(
                        selected = selectedSeverity == sev,
                        onClick = { selectedSeverity = sev },
                        label = { Text(sev.name, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (sev) {
                                HazardSeverity.CRITICAL, HazardSeverity.HIGH -> CruizeDangerCrimson
                                HazardSeverity.MEDIUM -> CruizeWarningAmber
                                else -> CruizeElectricBlue
                            },
                            selectedLabelColor = CruizeTextPrimary,
                            containerColor = CruizeDarkSurfaceVariant,
                            labelColor = CruizeTextSecondary
                        ),
                        modifier = Modifier.weight(1f).testTag("severity_chip_${sev.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Action Button
            Button(
                onClick = {
                    if (titleInput.isNotBlank() && roadInput.isNotBlank()) {
                        onSubmit(
                            selectedType,
                            titleInput,
                            if (descInput.isBlank()) "Driver reported $selectedType alert." else descInput,
                            roadInput,
                            selectedSeverity
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CruizeNeonCyan,
                    contentColor = CruizeDarkBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_report_button")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "POST HAZARD ALERT (+15 Karma)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun HazardTypeOptionButton(
    type: HazardType,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    accentColor: Color,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) accentColor.copy(alpha = 0.25f) else CruizeDarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (selected) accentColor else CruizeDarkCardBorder
        ),
        modifier = modifier
            .height(60.dp)
            .testTag("select_hazard_type_${type.name}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) accentColor else CruizeTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) CruizeTextPrimary else CruizeTextSecondary
            )
        }
    }
}
