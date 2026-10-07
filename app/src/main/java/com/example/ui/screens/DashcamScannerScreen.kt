package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.camera.RoadCameraManager
import com.example.data.local.HazardSeverity
import com.example.data.local.HazardType
import com.example.ui.theme.*
import com.example.viewmodel.CruizeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

data class DashcamDetection(
    val id: String,
    val type: HazardType,
    val label: String,
    val confidence: Int,
    val boundingBox: androidx.compose.ui.geometry.Rect,
    val distanceMeters: Int
)

@Composable
fun DashcamScannerScreen(
    viewModel: CruizeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val speedMph by viewModel.currentSpeedMph.collectAsState()
    val roadName by viewModel.currentRoadName.collectAsState()
    val isAutoDashcamMapping by viewModel.isAutoDashcamMapping.collectAsState()
    val speechMsg by viewModel.speechFeedbackMessage.collectAsState()

    val cameraManager = remember(context) { RoadCameraManager(context) }
    val isStreaming by cameraManager.isStreaming.collectAsState()
    val isTorchOn by cameraManager.isTorchEnabled.collectAsState()
    val liveCameraDetection by cameraManager.latestDetection.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var isScanningFrame by remember { mutableStateOf(false) }
    var scanSuccessBanner by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.stopCamera()
        }
    }

    // Auto-map when live CameraManager detects a hazard
    LaunchedEffect(liveCameraDetection) {
        liveCameraDetection?.let { det ->
            if (isAutoDashcamMapping) {
                viewModel.scanDashcamAndAutoMap(
                    detectedType = det.hazardType,
                    title = det.label,
                    roadName = roadName,
                    distanceMeters = det.distanceMeters
                )
                scanSuccessBanner = "CameraX AI detected: ${det.label} ${det.distanceMeters}m ahead."
            }
        }
    }

    // Simulated Vision AI detected objects
    val detectedObjects = remember {
        listOf(
            DashcamDetection(
                id = "det_1",
                type = HazardType.STATIONARY_SPEED_CAM,
                label = "FIXED SPEED & RED LIGHT CAM",
                confidence = 97,
                boundingBox = androidx.compose.ui.geometry.Rect(100f, 180f, 320f, 340f),
                distanceMeters = 220
            ),
            DashcamDetection(
                id = "det_2",
                type = HazardType.MOBILE_SPEED_CAM,
                label = "MOBILE RADAR VAN SPEED CAM",
                confidence = 95,
                boundingBox = androidx.compose.ui.geometry.Rect(440f, 260f, 680f, 420f),
                distanceMeters = 340
            ),
            DashcamDetection(
                id = "det_3",
                type = HazardType.COP_TRAP,
                label = "POLICE RADAR VEHICLE",
                confidence = 96,
                boundingBox = androidx.compose.ui.geometry.Rect(180f, 480f, 420f, 620f),
                distanceMeters = 500
            )
        )
    }

    // Infinite animated scanning laser beam sweep across viewport
    val infiniteTransition = rememberInfiniteTransition(label = "dashcam_laser_sweep")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("dashcam_scanner_screen")
    ) {
        // 1. CameraX Live Feed View (if permission granted)
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        cameraManager.startCamera(
                            lifecycleOwner = lifecycleOwner,
                            previewView = this,
                            onDetection = {}
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Transparent Canvas Overlay for Bounding Boxes & Animated Laser Sweep
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val horizonY = h * 0.45f

            // Fallback synthetic road background if camera off or permission missing
            if (!hasCameraPermission || !isStreaming) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF0A0F1D))
                    )
                )

                val roadTopLeft = Offset(w * 0.42f, horizonY)
                val roadTopRight = Offset(w * 0.58f, horizonY)
                val roadBottomLeft = Offset(w * 0.05f, h * 0.95f)
                val roadBottomRight = Offset(w * 0.95f, h * 0.95f)

                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(roadTopLeft.x, roadTopLeft.y)
                    lineTo(roadTopRight.x, roadTopRight.y)
                    lineTo(roadBottomRight.x, roadBottomRight.y)
                    lineTo(roadBottomLeft.x, roadBottomLeft.y)
                    close()
                }
                drawPath(path, color = Color(0xFF161E2E).copy(alpha = 0.85f))

                for (i in 0..5) {
                    val startRatio = i / 6f
                    val endRatio = (i + 0.5f) / 6f
                    val sy = horizonY + (h * 0.95f - horizonY) * startRatio
                    val ey = horizonY + (h * 0.95f - horizonY) * endRatio
                    val sx = w * 0.5f
                    val strokeW = 2f + (i * 3f)
                    drawLine(
                        color = Color(0xFF00F0FF).copy(alpha = 0.6f),
                        start = Offset(sx, sy),
                        end = Offset(sx, ey),
                        strokeWidth = strokeW
                    )
                }
            }

            // HUD Crosshairs
            drawLine(
                color = Color(0xFF00F0FF).copy(alpha = 0.15f),
                start = Offset(w * 0.5f, 0f),
                end = Offset(w * 0.5f, h),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0xFF00F0FF).copy(alpha = 0.15f),
                start = Offset(0f, horizonY),
                end = Offset(w, horizonY),
                strokeWidth = 1f
            )

            // Animated Horizontal AI Laser Scanner Line
            val laserY = h * laserYRatio
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF00F0FF).copy(alpha = 0.8f),
                        Color(0xFF00F0FF),
                        Color(0xFF00F0FF).copy(alpha = 0.8f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, laserY),
                end = Offset(w, laserY),
                strokeWidth = 3f
            )

            // Render Bounding Boxes for Detected Hazards
            detectedObjects.forEach { det ->
                val boxColor = when (det.type) {
                    HazardType.COP_TRAP -> Color(0xFF3B82F6)
                    HazardType.STATIONARY_SPEED_CAM -> Color(0xFF00F0FF)
                    HazardType.MOBILE_SPEED_CAM -> Color(0xFFF59E0B)
                    HazardType.SPEED_BUMP -> Color(0xFFF59E0B)
                    HazardType.POTHOLE -> Color(0xFFEF4444)
                    else -> Color(0xFF00F0FF)
                }

                val rect = det.boundingBox

                // Translucent box fill
                drawRect(
                    color = boxColor.copy(alpha = 0.18f),
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height)
                )

                // Outer border stroke
                drawRect(
                    color = boxColor,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    style = Stroke(width = 3f)
                )

                // Corner Brackets for HUD feel
                val cornerL = 24f
                val strokeW = 6f

                // Top-Left
                drawLine(boxColor, Offset(rect.left, rect.top), Offset(rect.left + cornerL, rect.top), strokeWidth = strokeW)
                drawLine(boxColor, Offset(rect.left, rect.top), Offset(rect.left, rect.top + cornerL), strokeWidth = strokeW)
                // Top-Right
                drawLine(boxColor, Offset(rect.right, rect.top), Offset(rect.right - cornerL, rect.top), strokeWidth = strokeW)
                drawLine(boxColor, Offset(rect.right, rect.top), Offset(rect.right, rect.top + cornerL), strokeWidth = strokeW)
                // Bottom-Left
                drawLine(boxColor, Offset(rect.left, rect.bottom), Offset(rect.left + cornerL, rect.bottom), strokeWidth = strokeW)
                drawLine(boxColor, Offset(rect.left, rect.bottom), Offset(rect.left, rect.bottom - cornerL), strokeWidth = strokeW)
                // Bottom-Right
                drawLine(boxColor, Offset(rect.right, rect.bottom), Offset(rect.right - cornerL, rect.bottom), strokeWidth = strokeW)
                drawLine(boxColor, Offset(rect.right, rect.bottom), Offset(rect.right, rect.bottom - cornerL), strokeWidth = strokeW)
            }
        }

        // 3. Floating Transparent Labels Overlay Layer over Bounding Boxes
        detectedObjects.forEach { det ->
            val boxColor = when (det.type) {
                HazardType.COP_TRAP -> CruizeElectricBlue
                HazardType.SPEED_BUMP -> CruizeWarningAmber
                HazardType.POTHOLE -> CruizeDangerCrimson
                else -> CruizeNeonCyan
            }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = det.boundingBox.left.roundToInt().coerceAtLeast(16),
                            y = (det.boundingBox.top - 48f).roundToInt().coerceAtLeast(70)
                        )
                    }
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.88f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, boxColor)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable {
                                viewModel.scanDashcamAndAutoMap(
                                    detectedType = det.type,
                                    title = det.label,
                                    roadName = roadName,
                                    distanceMeters = det.distanceMeters
                                )
                                scanSuccessBanner = "Mapped ${det.label} from camera detection tag."
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (det.type) {
                                HazardType.COP_TRAP -> Icons.Default.Shield
                                HazardType.STATIONARY_SPEED_CAM -> Icons.Default.PhotoCamera
                                HazardType.MOBILE_SPEED_CAM -> Icons.Default.Videocam
                                HazardType.SPEED_BUMP -> Icons.Default.Warning
                                HazardType.POTHOLE -> Icons.Default.ReportProblem
                                else -> Icons.Default.PhotoCamera
                            },
                            contentDescription = null,
                            tint = boxColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = det.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = CruizeTextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${det.confidence}% AI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = boxColor
                                )
                                Text(
                                    text = " • ${det.distanceMeters}m",
                                    fontSize = 9.sp,
                                    color = CruizeTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Dashcam HUD Overlay Interface
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Bar: Telemetry & Rec Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(CruizeDangerCrimson)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REC • DASHCAM AI VISION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = CruizeTextPrimary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { cameraManager.toggleTorch() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("dashcam_torch_toggle")
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch",
                            tint = if (isTorchOn) CruizeWarningAmber else CruizeNeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = CruizeNeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$speedMph KM/H • $roadName",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CruizeTextPrimary
                            )
                        }
                    }
                }
            }

            // Speech & Scan Feedback Banner
            if (speechMsg != null || scanSuccessBanner != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CruizeDarkSurface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = CruizeNeonCyan, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = scanSuccessBanner ?: speechMsg ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CruizeTextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Detected Objects List & Instant Auto-Map Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "LIVE DASHCAM DETECTIONS (${detectedObjects.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = CruizeNeonCyan,
                    letterSpacing = 1.sp
                )

                detectedObjects.forEach { det ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (det.type) {
                                        HazardType.COP_TRAP -> Icons.Default.Shield
                                        HazardType.SPEED_BUMP -> Icons.Default.Warning
                                        HazardType.POTHOLE -> Icons.Default.ReportProblem
                                        else -> Icons.Default.Error
                                    },
                                    contentDescription = null,
                                    tint = when (det.type) {
                                        HazardType.COP_TRAP -> CruizePoliceBlue
                                        HazardType.SPEED_BUMP -> CruizeWarningAmber
                                        else -> CruizeDangerCrimson
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(det.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CruizeTextPrimary)
                                    Text("${det.distanceMeters}m ahead • ${det.confidence}% AI Confidence", fontSize = 10.sp, color = CruizeTextSecondary)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.scanDashcamAndAutoMap(
                                        detectedType = det.type,
                                        title = det.label,
                                        roadName = roadName,
                                        distanceMeters = det.distanceMeters
                                    )
                                    scanSuccessBanner = "Mapped ${det.label} to 3D Map database & played hands-free audio alert."
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CruizeNeonCyan),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("map_detected_${det.id}")
                            ) {
                                Icon(Icons.Default.AddLocation, contentDescription = null, tint = CruizeDarkBackground, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AUTO-MAP", fontSize = 10.sp, fontWeight = FontWeight.Black, color = CruizeDarkBackground)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Primary Action Row: Scan Frame & Toggle Auto-Map
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            isScanningFrame = true
                            val topDet = detectedObjects.random()
                            viewModel.scanDashcamAndAutoMap(
                                detectedType = topDet.type,
                                title = topDet.label,
                                roadName = roadName,
                                distanceMeters = topDet.distanceMeters
                            )
                            scanSuccessBanner = "Dashcam AI scanned road: ${topDet.label} detected ${topDet.distanceMeters}m ahead."
                            isScanningFrame = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("scan_dashcam_frame_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CruizeNeonCyan)
                    ) {
                        if (isScanningFrame) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CruizeDarkBackground, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.Videocam, contentDescription = null, tint = CruizeDarkBackground)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SCAN ROAD WITH AI", fontWeight = FontWeight.Black, fontSize = 12.sp, color = CruizeDarkBackground)
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.toggleAutoDashcamMapping() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("toggle_auto_dashcam_button"),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAutoDashcamMapping) CruizeSuccessGreen else CruizeDarkCardBorder
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isAutoDashcamMapping) CruizeSuccessGreen else CruizeTextPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (isAutoDashcamMapping) Icons.Default.CheckCircle else Icons.Default.AutoMode,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAutoDashcamMapping) "AUTO-MAP ON" else "AUTO-MAP OFF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
