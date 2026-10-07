package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CruizeBrandLogo
import com.example.ui.components.CruizeLogoSize
import com.example.ui.theme.*
import com.example.viewmodel.CruizeViewModel

@Composable
fun SettingsScreen(
    viewModel: CruizeViewModel,
    modifier: Modifier = Modifier
) {
    val appSettings by viewModel.appSettings.collectAsState()
    val allHazards by viewModel.allHazards.collectAsState()
    val tripLogs by viewModel.tripLogs.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val is3dMode = appSettings?.is3dPerspective ?: true
    val isAiVoiceOn = appSettings?.isAiVoiceOn ?: true
    val proximityRange = appSettings?.proximityAlertRangeMeters ?: 500
    val speedUnitMph = appSettings?.speedUnitMph ?: false
    val nightDriveTheme = appSettings?.nightDriveTheme ?: true
    val currentRegion = appSettings?.selectedRegion ?: "Kenya (Nairobi / Mombasa Corridors)"
    val currentAccent = appSettings?.brandAccentTheme ?: "NEON_CYAN"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CruizeDarkBackground)
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Branding Header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = CruizeDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CruizeBrandLogo(
                        logoSize = CruizeLogoSize.LARGE,
                        subtitle = "PERSISTENT RADAR & STORAGE",
                        animated = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Customize AI driver voice warnings, radar sensitivity, highway corridors, and persistent local SQLite database settings.",
                        fontSize = 12.sp,
                        color = CruizeTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 1. AI Radar Alerts Section
        item {
            SettingSectionHeader("AI DRIVER WARNING SYSTEM")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CruizeDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingSwitchRow(
                        title = "AI Voice Audio Warning",
                        subtitle = "Synthesize spoken audio warnings for upcoming cops & bumps",
                        checked = isAiVoiceOn,
                        onCheckedChange = { viewModel.updateAiVoiceSetting(it) },
                        testTag = "switch_ai_voice"
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CruizeDarkCardBorder)

                    Text(text = "Proximity Alert Distance (${proximityRange}m)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CruizeTextPrimary)
                    Text(text = "Warn driver when approaching within this radius", fontSize = 11.sp, color = CruizeTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(100, 300, 500, 1000).forEach { range ->
                            FilterChip(
                                selected = proximityRange == range,
                                onClick = { viewModel.setProximityRange(range) },
                                label = { Text("${range}m", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CruizeNeonCyan,
                                    selectedLabelColor = CruizeDarkBackground,
                                    containerColor = CruizeDarkSurfaceVariant,
                                    labelColor = CruizeTextSecondary
                                ),
                                modifier = Modifier.weight(1f).testTag("range_chip_$range")
                            )
                        }
                    }
                }
            }
        }

        // 2. Map & Display Settings
        item {
            SettingSectionHeader("3D MAP & DISPLAY PREFERENCES")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CruizeDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingSwitchRow(
                        title = "3D Camera Perspective Tilt",
                        subtitle = "Render roads with 3D driver horizon view",
                        checked = is3dMode,
                        onCheckedChange = { viewModel.update3dPerspectiveSetting(it) },
                        testTag = "switch_3d_mode"
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CruizeDarkCardBorder)

                    SettingSwitchRow(
                        title = "Night Drive HUD Palette",
                        subtitle = "High contrast midnight canvas for night driving safety",
                        checked = nightDriveTheme,
                        onCheckedChange = { viewModel.updateThemeModeSetting(it) },
                        testTag = "switch_night_theme"
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CruizeDarkCardBorder)

                    SettingSwitchRow(
                        title = "Speedometer Unit (MPH / KMH)",
                        subtitle = if (speedUnitMph) "Imperial Speed (MPH)" else "Metric Speed (KM/H - East Africa Standard)",
                        checked = speedUnitMph,
                        onCheckedChange = { viewModel.updateSpeedUnitSetting(it) },
                        testTag = "switch_speed_units"
                    )
                }
            }
        }

        // 3. Corridor & Region Branding
        item {
            SettingSectionHeader("HIGHWAY CORRIDOR REGION")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CruizeDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Transit Corridor",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CruizeTextPrimary
                    )
                    Text(
                        text = "Cruize dynamically routes traffic alerts and police radar traps for this corridor.",
                        fontSize = 11.sp,
                        color = CruizeTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val regions = listOf(
                        "Kenya (Nairobi / Mombasa Corridors)",
                        "Uganda (Kampala / Jinja Corridors)",
                        "Tanzania (Dar es Salaam / Arusha)",
                        "Rwanda (Kigali Corridors)"
                    )

                    regions.forEach { region ->
                        val isSelected = currentRegion == region
                        Surface(
                            onClick = { viewModel.updateRegionSetting(region) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CruizeNeonCyan.copy(alpha = 0.15f) else CruizeDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CruizeNeonCyan else CruizeDarkCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateRegionSetting(region) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CruizeNeonCyan,
                                        unselectedColor = CruizeTextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = region,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CruizeNeonCyan else CruizeTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Room Database & Local Storage Status
        item {
            SettingSectionHeader("PERSISTENT LOCAL STORAGE (ROOM DATABASE)")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CruizeDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = CruizeNeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SQLite Room Database v3",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CruizeTextPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CruizeEmeraldSafe.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CruizeEmeraldSafe)
                        ) {
                            Text(
                                text = "OFFLINE SYNCED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = CruizeEmeraldSafe,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StorageMetricBadge(
                            label = "Road Hazards",
                            count = "${allHazards.size}",
                            modifier = Modifier.weight(1f)
                        )
                        StorageMetricBadge(
                            label = "Trip Logs",
                            count = "${tripLogs.size}",
                            modifier = Modifier.weight(1f)
                        )
                        StorageMetricBadge(
                            label = "Karma",
                            count = "${currentUser?.karmaPoints ?: 0}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (tripLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.clearTripLogs() },
                            modifier = Modifier.fillMaxWidth().testTag("btn_clear_logs"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CruizeCrimsonAlert),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CruizeCrimsonAlert.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Stored Dashcam Trip Logs (${tripLogs.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. About Cruize East Africa
        item {
            SettingSectionHeader("ABOUT CRUIZE EAST AFRICA")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CruizeDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Cruize Kenya & East Africa v2.5.0", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CruizeTextPrimary)
                    Text(
                        text = "Community-driven road warning system for NTSA Speed Cameras, Traffic Police Traps, Speed Humps & Highway Hazards with persistent local SQLite storage.",
                        fontSize = 12.sp,
                        color = CruizeTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageMetricBadge(
    label: String,
    count: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = CruizeDarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count, fontSize = 16.sp, fontWeight = FontWeight.Black, color = CruizeNeonCyan)
            Text(text = label, fontSize = 10.sp, color = CruizeTextSecondary)
        }
    }
}

@Composable
private fun SettingSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = CruizeNeonCyan,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CruizeTextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = CruizeTextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CruizeDarkBackground,
                checkedTrackColor = CruizeNeonCyan,
                uncheckedThumbColor = CruizeTextSecondary,
                uncheckedTrackColor = CruizeDarkSurfaceVariant
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

