package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RoadHazardEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CctvInspectionDialog(
    hazard: RoadHazardEntity,
    onDismiss: () -> Unit,
    onUpvote: (Long) -> Unit,
    onDownvote: (Long) -> Unit,
    onTriggerVoiceAlert: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cctv_feed_anim")
    val recAlpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_blink"
    )
    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline"
    )

    val timeFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val currentTimeStr = remember { timeFormat.format(Date()) }

    val cameraTypeName = when (hazard.cameraType) {
        "ANPR_PLATE_SCANNER" -> "ANPR Automated Plate Recognition CCTV"
        "RED_LIGHT" -> "Red Light & Intersection Violation CCTV"
        "SPEED_GANTRY" -> "Overhead Highway Speed Gantry CCTV"
        "MUNICIPAL_SECURITY" -> "City Municipal Security CCTV"
        else -> "Smart Highway Traffic Surveillance CCTV"
    }

    val cameraTypeIcon = when (hazard.cameraType) {
        "ANPR_PLATE_SCANNER" -> Icons.Default.QrCodeScanner
        "RED_LIGHT" -> Icons.Default.Traffic
        "SPEED_GANTRY" -> Icons.Default.Speed
        else -> Icons.Default.Videocam
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CruizeDarkBackground,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        modifier = Modifier.testTag("cctv_inspection_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Header: Camera ID & Live status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CruizeDangerCrimson.copy(alpha = recAlpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE CCTV FEED • CAM-0${hazard.id}",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = CruizeTextPrimary,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CruizeNeonCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan)
                ) {
                    Text(
                        text = hazard.cameraStatus.replace("_", " "),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = CruizeNeonCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simulated Live CCTV Video Screen Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF06090F))
                    .border(1.5.dp, CruizeDarkCardBorder, RoundedCornerShape(12.dp))
            ) {
                // Background simulated road scene canvas with scanlines & AI bounding boxes
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Perspective Road asphalt in CCTV angle
                    val roadPath = Path().apply {
                        moveTo(w * 0.4f, h * 0.2f)
                        lineTo(w * 0.6f, h * 0.2f)
                        lineTo(w * 0.95f, h)
                        lineTo(w * 0.05f, h)
                        close()
                    }
                    drawPath(
                        path = roadPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF151D2A), Color(0xFF0D121B))
                        )
                    )

                    // Lane dividing markings
                    drawLine(
                        color = Color(0x6600E5FF),
                        start = Offset(w * 0.5f, h * 0.2f),
                        end = Offset(w * 0.5f, h),
                        strokeWidth = 2f
                    )

                    // 2. Scanline moving effect
                    drawLine(
                        color = Color(0x3300E5FF),
                        start = Offset(0f, scanlineY % h),
                        end = Offset(w, scanlineY % h),
                        strokeWidth = 3f
                    )

                    // 3. Simulated AI Vehicle Bounding Boxes & Plate Scanner Overlays
                    // Box 1: Left lane vehicle
                    val box1X = w * 0.22f
                    val box1Y = h * 0.48f
                    val box1W = w * 0.22f
                    val box1H = h * 0.28f
                    drawRect(
                        color = CruizeNeonCyan.copy(alpha = 0.85f),
                        topLeft = Offset(box1X, box1Y),
                        size = Size(box1W, box1H),
                        style = Stroke(width = 1.5f)
                    )
                    // Corner targeting brackets
                    drawLine(CruizeNeonCyan, Offset(box1X, box1Y), Offset(box1X + 12f, box1Y), 3f)
                    drawLine(CruizeNeonCyan, Offset(box1X, box1Y), Offset(box1X, box1Y + 12f), 3f)

                    // Box 2: Right lane vehicle
                    val box2X = w * 0.58f
                    val box2Y = h * 0.38f
                    val box2W = w * 0.18f
                    val box2H = h * 0.24f
                    drawRect(
                        color = CruizeWarningAmber.copy(alpha = 0.85f),
                        topLeft = Offset(box2X, box2Y),
                        size = Size(box2W, box2H),
                        style = Stroke(width = 1.5f)
                    )

                    // 4. CCTV Reticle & Crosshair
                    val cx = w / 2f
                    val cy = h / 2f
                    drawLine(Color(0x5500E5FF), Offset(cx - 20f, cy), Offset(cx + 20f, cy), 1.5f)
                    drawLine(Color(0x5500E5FF), Offset(cx, cy - 20f), Offset(cx, cy + 20f), 1.5f)
                    drawCircle(Color(0x3300E5FF), radius = 30f, center = Offset(cx, cy), style = Stroke(1.5f))
                }

                // Top Overlay Info inside video feed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "[REC ●] $currentTimeStr\nGPS: ${hazard.latitude.toString().take(7)}, ${hazard.longitude.toString().take(7)}",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CruizeTextPrimary.copy(alpha = 0.9f),
                        lineHeight = 12.sp
                    )

                    Text(
                        text = "FPS: 59.94\nFOV: 110° AZ: 042°",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CruizeNeonCyan,
                        lineHeight = 12.sp
                    )
                }

                // AI Detection Tags inside feed
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan)
                    ) {
                        Text(
                            text = "AI PLATE SCAN: KDA 391B • 74 KM/H [OK]",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = CruizeNeonCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CruizeWarningAmber)
                    ) {
                        Text(
                            text = "AI TRACKING: KCM 882X • 82 KM/H [MONITORED]",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = CruizeWarningAmber,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Camera Location & Details Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CruizeDarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = cameraTypeIcon,
                            contentDescription = null,
                            tint = CruizeNeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = hazard.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = CruizeTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = hazard.roadName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CruizeElectricBlue
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = hazard.description,
                        fontSize = 12.sp,
                        color = CruizeTextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = CruizeDarkCardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // 4-Quadrant Telemetry Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CAMERA TYPE", fontSize = 10.sp, color = CruizeTextSecondary, fontWeight = FontWeight.Bold)
                            Text(cameraTypeName.take(24), fontSize = 11.sp, color = CruizeTextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SPEED LIMIT", fontSize = 10.sp, color = CruizeTextSecondary, fontWeight = FontWeight.Bold)
                            Text("${hazard.speedLimit} KM/H", fontSize = 11.sp, color = CruizeWarningAmber, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DIRECTION COVERAGE", fontSize = 10.sp, color = CruizeTextSecondary, fontWeight = FontWeight.Bold)
                            Text(hazard.cameraDirection.replace("_", " "), fontSize = 11.sp, color = CruizeNeonCyan, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("REPORTED BY", fontSize = 10.sp, color = CruizeTextSecondary, fontWeight = FontWeight.Bold)
                            Text(hazard.reportedBy, fontSize = 11.sp, color = CruizeTextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Bar: Verify, Upvote, Voice Alert
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onUpvote(hazard.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CruizeNeonCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("cctv_verify_upvote_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint = CruizeDarkBackground,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Verify Active (${hazard.upvotes})",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = CruizeDarkBackground
                    )
                }

                OutlinedButton(
                    onClick = {
                        onTriggerVoiceAlert(
                            "Approaching ${hazard.title} on ${hazard.roadName}. Speed limit is ${hazard.speedLimit} kilometers per hour. Camera is actively enforcing."
                        )
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CruizeNeonCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("cctv_voice_alert_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Test Voice Alert",
                        tint = CruizeNeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
