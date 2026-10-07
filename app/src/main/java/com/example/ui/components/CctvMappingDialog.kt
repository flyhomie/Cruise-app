package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CctvMappingDialog(
    pinnedLocation: Pair<Double, Double>?,
    pinnedRoadName: String?,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        roadName: String,
        cameraType: String,
        cameraDirection: String,
        cameraStatus: String,
        speedLimit: Int,
        description: String
    ) -> Unit
) {
    var titleInput by remember { mutableStateOf("Smart Highway Traffic Surveillance CCTV") }
    var roadInput by remember { mutableStateOf(pinnedRoadName ?: "Thika Superhighway & Roysambu Junction") }
    var selectedCameraType by remember { mutableStateOf("ANPR_PLATE_SCANNER") }
    var selectedDirection by remember { mutableStateOf("ALL_LANES") }
    var selectedStatus by remember { mutableStateOf("AI_ENFORCING") }
    var speedLimitInput by remember { mutableStateOf("80") }
    var notesInput by remember { mutableStateOf("Overhead mast mounted CCTV scanning both lanes.") }

    LaunchedEffect(selectedCameraType) {
        when (selectedCameraType) {
            "ANPR_PLATE_SCANNER" -> {
                titleInput = "ANPR Automatic Plate Recognition CCTV"
                notesInput = "Overhead multi-lane camera reading vehicle plates and monitoring speeds."
                speedLimitInput = "80"
            }
            "RED_LIGHT" -> {
                titleInput = "Red Light & Intersection Violation CCTV"
                notesInput = "High-definition intersection surveillance recording red light violations."
                speedLimitInput = "50"
            }
            "SPEED_GANTRY" -> {
                titleInput = "High-Speed Gantry Enforcement CCTV"
                notesInput = "Overhead smart gantry camera scanning all express lanes."
                speedLimitInput = "100"
            }
            "TRAFFIC_SURVEILLANCE" -> {
                titleInput = "Smart Highway Traffic Surveillance CCTV"
                notesInput = "Traffic management camera monitoring congestion and lane discipline."
                speedLimitInput = "70"
            }
            "MUNICIPAL_SECURITY" -> {
                titleInput = "Municipal & Police Highway Security CCTV"
                notesInput = "High-mast panoramic 360 degree security camera system."
                speedLimitInput = "80"
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CruizeDarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        modifier = Modifier.testTag("cctv_mapping_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MAP CCTV SURVEILLANCE CAMERA",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = CruizeNeonCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Pin a new traffic, ANPR, or speed CCTV on Cruize 3D Radar",
                        fontSize = 11.sp,
                        color = CruizeTextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = CruizeNeonCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (pinnedLocation != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CruizeNeonCyan.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = CruizeNeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GPS: ${pinnedLocation.first.toString().take(8)}, ${pinnedLocation.second.toString().take(8)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CruizeTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. CCTV Type Selection
            Text(
                text = "CCTV ENFORCEMENT & CAMERA TYPE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CruizeTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CctvTypeOptionChip(
                        label = "ANPR Plate CCTV 🔍",
                        icon = Icons.Default.QrCodeScanner,
                        selected = selectedCameraType == "ANPR_PLATE_SCANNER",
                        onSelect = { selectedCameraType = "ANPR_PLATE_SCANNER" },
                        modifier = Modifier.weight(1f)
                    )
                    CctvTypeOptionChip(
                        label = "Red Light CCTV 🚦",
                        icon = Icons.Default.Traffic,
                        selected = selectedCameraType == "RED_LIGHT",
                        onSelect = { selectedCameraType = "RED_LIGHT" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CctvTypeOptionChip(
                        label = "Speed Gantry ⚡",
                        icon = Icons.Default.Speed,
                        selected = selectedCameraType == "SPEED_GANTRY",
                        onSelect = { selectedCameraType = "SPEED_GANTRY" },
                        modifier = Modifier.weight(1f)
                    )
                    CctvTypeOptionChip(
                        label = "Traffic Flow 📹",
                        icon = Icons.Default.Videocam,
                        selected = selectedCameraType == "TRAFFIC_SURVEILLANCE",
                        onSelect = { selectedCameraType = "TRAFFIC_SURVEILLANCE" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Camera Direction Coverage
            Text(
                text = "MONITORED LANE DIRECTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CruizeTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DirectionOptionChip("ALL LANES", "All Lanes ↕️", selectedDirection == "ALL_LANES") {
                    selectedDirection = "ALL_LANES"
                }
                DirectionOptionChip("INBOUND", "Inbound ⬆️", selectedDirection == "INBOUND") {
                    selectedDirection = "INBOUND"
                }
                DirectionOptionChip("OUTBOUND", "Outbound ⬇️", selectedDirection == "OUTBOUND") {
                    selectedDirection = "OUTBOUND"
                }
                DirectionOptionChip("INTERSECTION_360", "360° 🔄", selectedDirection == "INTERSECTION_360") {
                    selectedDirection = "INTERSECTION_360"
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Inputs
            OutlinedTextField(
                value = roadInput,
                onValueChange = { roadInput = it },
                label = { Text("Highway / Road Name") },
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
                modifier = Modifier.fillMaxWidth().testTag("input_cctv_road_name")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                label = { Text("CCTV Camera Title") },
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
                modifier = Modifier.fillMaxWidth().testTag("input_cctv_title")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = speedLimitInput,
                    onValueChange = { speedLimitInput = it.filter { ch -> ch.isDigit() }.take(3) },
                    label = { Text("Speed Limit (KM/H)") },
                    leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, tint = CruizeWarningAmber) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CruizeNeonCyan,
                        unfocusedBorderColor = CruizeDarkCardBorder,
                        focusedLabelColor = CruizeNeonCyan,
                        unfocusedLabelColor = CruizeTextSecondary,
                        focusedTextColor = CruizeTextPrimary,
                        unfocusedTextColor = CruizeTextPrimary
                    ),
                    modifier = Modifier.weight(1f).testTag("input_cctv_speed_limit")
                )

                OutlinedTextField(
                    value = selectedStatus.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Camera Status") },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CruizeSuccessGreen) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CruizeNeonCyan,
                        unfocusedBorderColor = CruizeDarkCardBorder,
                        focusedLabelColor = CruizeNeonCyan,
                        unfocusedLabelColor = CruizeTextSecondary,
                        focusedTextColor = CruizeTextPrimary,
                        unfocusedTextColor = CruizeTextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Camera Details & Mounting Notes") },
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
                modifier = Modifier.fillMaxWidth().testTag("input_cctv_notes")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    val limit = speedLimitInput.toIntOrNull() ?: 80
                    onSubmit(
                        titleInput.ifBlank { "Smart Highway Traffic CCTV" },
                        roadInput.ifBlank { "Thika Superhighway & Roysambu" },
                        selectedCameraType,
                        selectedDirection,
                        selectedStatus,
                        limit,
                        notesInput.ifBlank { "Community mapped CCTV surveillance camera." }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CruizeNeonCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_cctv_mapping_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AddLocationAlt,
                    contentDescription = null,
                    tint = CruizeDarkBackground,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MAP CCTV TO 3D RADAR (+20 KARMA)",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = CruizeDarkBackground,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun CctvTypeOptionChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) CruizeNeonCyan.copy(alpha = 0.2f) else CruizeDarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) CruizeNeonCyan else CruizeDarkCardBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) CruizeNeonCyan else CruizeTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                color = if (selected) CruizeNeonCyan else CruizeTextPrimary
            )
        }
    }
}

@Composable
private fun RowScope.DirectionOptionChip(
    key: String,
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) CruizeElectricBlue.copy(alpha = 0.25f) else CruizeDarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) CruizeElectricBlue else CruizeDarkCardBorder
        ),
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) CruizeElectricBlue else CruizeTextSecondary
            )
        }
    }
}
