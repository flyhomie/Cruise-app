package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RoadHazardEntity
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun Cruize3DMapView(
    hazards: List<RoadHazardEntity>,
    selectedHazard: RoadHazardEntity?,
    onSelectHazard: (RoadHazardEntity) -> Unit,
    is3dPerspective: Boolean,
    onToggle3d: () -> Unit,
    driverSpeedMph: Int,
    isSimulatingDrive: Boolean,
    onToggleSimulateDrive: () -> Unit,
    onManualPinDropped: ((lat: Double, lng: Double, roadName: String) -> Unit)? = null,
    onMapCctvDropped: ((lat: Double, lng: Double, roadName: String) -> Unit)? = null,
    onInspectCctvFeed: ((RoadHazardEntity) -> Unit)? = null,
    showCctvLayer: Boolean = true,
    onToggleCctvLayer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Map view state
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var tiltAngleDeg by remember { mutableFloatStateOf(if (is3dPerspective) 55f else 0f) }
    var rotationDeg by remember { mutableFloatStateOf(15f) }

    // Manual map pin state
    var manualPinPos by remember { mutableStateOf<Offset?>(null) }

    // Synchronize tiltAngle when is3dPerspective parameter changes
    LaunchedEffect(is3dPerspective) {
        tiltAngleDeg = if (is3dPerspective) 55f else 0f
    }

    // Animated pulsating radar ring for driver position
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Simulation offset along the main road axis
    val simProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "simProgress"
    )

    val currentDriverYOffset = if (isSimulatingDrive) -simProgress else 0f

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CruizeDarkBackground)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    panOffset += pan
                    zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.0f)
                    rotationDeg = (rotationDeg + rotation) % 360f
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    if (change.pressed) {
                        panOffset += dragAmount
                    }
                }
            }
            .testTag("cruize_3d_map_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            manualPinPos = tapOffset
                        },
                        onLongPress = { longPressOffset ->
                            manualPinPos = longPressOffset
                        }
                    )
                }
        ) {
            val center = Offset(size.width / 2f, size.height * 0.6f) + panOffset
            val is3d = tiltAngleDeg > 15f

            // 1. Render Atmospheric Horizon Glow in 3D Mode
            if (is3d) {
                val horizonY = center.y - (300f * zoomScale)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CruizeDarkBackground,
                            CruizeDarkSurfaceVariant.copy(alpha = 0.8f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = max(1f, horizonY)
                    ),
                    size = Size(size.width, max(1f, horizonY))
                )
            }

            // 2. Render 3D Perspective Road Grid & Lanes
            val scaleFactor = zoomScale
            val tiltRatio = cos(Math.toRadians(tiltAngleDeg.toDouble())).toFloat()

            // Main Highway Route (3D perspective converging lines)
            val roadWidth = 140f * scaleFactor
            val horizonY = if (is3d) center.y - 450f * scaleFactor else center.y - 1200f
            val topWidth = if (is3d) roadWidth * 0.25f else roadWidth

            // Highway pavement polynomial path
            val roadPath = Path().apply {
                moveTo(center.x - topWidth, horizonY)
                lineTo(center.x + topWidth, horizonY)
                lineTo(center.x + roadWidth * 1.8f, center.y + 800f)
                lineTo(center.x - roadWidth * 1.8f, center.y + 800f)
                close()
            }

            drawPath(
                path = roadPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CruizeDarkSurfaceVariant.copy(alpha = 0.4f),
                        CruizeDarkSurface,
                        CruizeDarkSurfaceVariant
                    )
                )
            )

            // Road Edge Neon Glow Lines
            drawLine(
                color = CruizeNeonCyan.copy(alpha = 0.8f),
                start = Offset(center.x - topWidth, horizonY),
                end = Offset(center.x - roadWidth * 1.8f, center.y + 800f),
                strokeWidth = 4f * scaleFactor
            )
            drawLine(
                color = CruizeNeonCyan.copy(alpha = 0.8f),
                start = Offset(center.x + topWidth, horizonY),
                end = Offset(center.x + roadWidth * 1.8f, center.y + 800f),
                strokeWidth = 4f * scaleFactor
            )

            // Dashed Center Lanes (Moving effect during drive simulation)
            val numLaneSegment = 12
            for (i in 0 until numLaneSegment) {
                val t1 = (i.toFloat() / numLaneSegment + (if (isSimulatingDrive) simProgress / 600f else 0f)) % 1.0f
                val t2 = (t1 + 0.04f).coerceAtMost(0.99f)

                val y1 = horizonY + (center.y + 800f - horizonY) * t1
                val y2 = horizonY + (center.y + 800f - horizonY) * t2

                val w1 = topWidth + (roadWidth * 1.8f - topWidth) * t1
                val x1 = center.x

                drawLine(
                    color = CruizeWarningAmber.copy(alpha = (1.0f - t1).coerceIn(0.1f, 0.9f)),
                    start = Offset(x1, y1),
                    end = Offset(x1, y2),
                    strokeWidth = (2f + 6f * t1) * scaleFactor
                )
            }

            // Cross Roads Grid Lines
            for (j in 1..8) {
                val t = j.toFloat() / 8f
                val y = horizonY + (center.y + 800f - horizonY) * t
                val span = topWidth + (roadWidth * 2.2f - topWidth) * t

                drawLine(
                    color = CruizeDarkCardBorder.copy(alpha = 0.4f),
                    start = Offset(center.x - span, y),
                    end = Offset(center.x + span, y),
                    strokeWidth = 1.5f
                )
            }

            // 3. Render 3D Hazard Pins, CCTV Cameras & FOV Cones
            val baseVehiclePos = Offset(center.x, center.y + 250f + currentDriverYOffset)

            hazards.forEachIndexed { index, hazard ->
                val isCctv = hazard.type == "CCTV_CAMERA"
                val laneOffset = when (index % 3) {
                    0 -> -80f * scaleFactor
                    1 -> 0f
                    else -> 90f * scaleFactor
                }
                val distY = -180f * scaleFactor - (index * 160f * scaleFactor)
                val rawPos = Offset(center.x + laneOffset, center.y + distY)

                val isSelected = selectedHazard?.id == hazard.id
                val distMeters = ((baseVehiclePos - rawPos).getDistance() * 0.85f).roundToInt()

                // Render 3D Pin Icon & Glow
                val pinColor = when (hazard.type) {
                    "COP_TRAP" -> CruizePoliceBlue
                    "STATIONARY_SPEED_CAM" -> CruizeElectricBlue
                    "MOBILE_SPEED_CAM" -> CruizeWarningAmber
                    "SPEED_CAMERA" -> CruizeElectricBlue
                    "CCTV_CAMERA" -> CruizeNeonCyan
                    "SPEED_BUMP" -> CruizeWarningAmber
                    "POTHOLE" -> CruizeDangerCrimson
                    else -> CruizeNeonCyan
                }

                // 3D Translucent Field-of-View (FOV) Radar Coverage Cone for CCTV & Speed Cameras
                if (showCctvLayer && (isCctv || hazard.type == "STATIONARY_SPEED_CAM")) {
                    val fovPath = Path().apply {
                        moveTo(rawPos.x, rawPos.y - 45f * scaleFactor)
                        lineTo(rawPos.x - 70f * scaleFactor, rawPos.y + 110f * scaleFactor)
                        lineTo(rawPos.x + 70f * scaleFactor, rawPos.y + 110f * scaleFactor)
                        close()
                    }
                    drawPath(
                        path = fovPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                (if (isCctv) CruizeNeonCyan else CruizeElectricBlue).copy(alpha = if (isSelected) 0.45f else 0.22f),
                                CruizeCobaltBlue.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            startY = rawPos.y - 45f * scaleFactor,
                            endY = rawPos.y + 120f * scaleFactor
                        )
                    )
                    // Arc across FOV cone base
                    drawArc(
                        color = (if (isCctv) CruizeNeonCyan else CruizeElectricBlue).copy(alpha = 0.4f),
                        startAngle = 45f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(rawPos.x - 75f * scaleFactor, rawPos.y + 50f * scaleFactor),
                        size = Size(150f * scaleFactor, 120f * scaleFactor),
                        style = Stroke(width = 1.5f)
                    )
                }

                // Shadow under 3D pin/mast
                drawOval(
                    color = Color.Black.copy(alpha = 0.5f),
                    topLeft = Offset(rawPos.x - 24f, rawPos.y + 10f),
                    size = Size(48f, 16f)
                )

                if (isCctv) {
                    // 3D Metallic Mast Pole
                    drawLine(
                        color = Color(0xFF64748B),
                        start = rawPos,
                        end = Offset(rawPos.x, rawPos.y - 55f * scaleFactor),
                        strokeWidth = 4f * scaleFactor
                    )
                    // Horizontal bracket arm
                    drawLine(
                        color = Color(0xFF94A3B8),
                        start = Offset(rawPos.x, rawPos.y - 50f * scaleFactor),
                        end = Offset(rawPos.x + 18f * scaleFactor, rawPos.y - 50f * scaleFactor),
                        strokeWidth = 3f * scaleFactor
                    )
                    // Camera Head Box
                    drawRoundRect(
                        color = CruizeDarkSurface,
                        topLeft = Offset(rawPos.x + 8f * scaleFactor, rawPos.y - 58f * scaleFactor),
                        size = Size(20f * scaleFactor, 14f * scaleFactor),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = if (isSelected) CruizeTextPrimary else CruizeNeonCyan,
                        topLeft = Offset(rawPos.x + 8f * scaleFactor, rawPos.y - 58f * scaleFactor),
                        size = Size(20f * scaleFactor, 14f * scaleFactor),
                        cornerRadius = CornerRadius(4f, 4f),
                        style = Stroke(width = 1.5f)
                    )
                    // Optical Lens LED
                    drawCircle(
                        color = CruizeDangerCrimson.copy(alpha = pulseAlpha),
                        radius = 3.5f * scaleFactor,
                        center = Offset(rawPos.x + 24f * scaleFactor, rawPos.y - 51f * scaleFactor)
                    )
                } else {
                    // Standard 3D Pin Stem
                    drawLine(
                        color = pinColor,
                        start = rawPos,
                        end = Offset(rawPos.x, rawPos.y - 50f * scaleFactor),
                        strokeWidth = 3f * scaleFactor
                    )
                    // Pin Top Crest Circle
                    drawCircle(
                        color = pinColor,
                        radius = if (isSelected) 22f * scaleFactor else 16f * scaleFactor,
                        center = Offset(rawPos.x, rawPos.y - 50f * scaleFactor)
                    )
                    drawCircle(
                        color = CruizeTextPrimary,
                        radius = if (isSelected) 8f * scaleFactor else 5f * scaleFactor,
                        center = Offset(rawPos.x, rawPos.y - 50f * scaleFactor)
                    )
                }

                // Distance & Title Callout Badge
                val badgePrefix = when(hazard.type) {
                    "CCTV_CAMERA" -> "📹 "
                    "STATIONARY_SPEED_CAM" -> "📸 "
                    "MOBILE_SPEED_CAM" -> "🚐 "
                    "COP_TRAP" -> "🚨 "
                    else -> ""
                }
                val badgeText = "$badgePrefix${hazard.title.take(16)} (${distMeters}m)"
                val textLayout = textMeasurer.measure(
                    text = badgeText,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CruizeTextPrimary
                    )
                )

                val badgeWidth = textLayout.size.width + 24f
                val badgeHeight = textLayout.size.height + 12f
                val badgeTopLeft = Offset(rawPos.x - badgeWidth / 2f, rawPos.y - 95f * scaleFactor)

                // Badge Container
                drawRoundRect(
                    color = if (isSelected) pinColor else CruizeDarkSurface.copy(alpha = 0.9f),
                    topLeft = badgeTopLeft,
                    size = Size(badgeWidth, badgeHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Badge Border
                drawRoundRect(
                    color = if (isSelected) CruizeTextPrimary else pinColor,
                    topLeft = badgeTopLeft,
                    size = Size(badgeWidth, badgeHeight),
                    cornerRadius = CornerRadius(12f, 12f),
                    style = Stroke(width = 2f)
                )

                // Badge Text
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(badgeTopLeft.x + 12f, badgeTopLeft.y + 6f)
                )
            }

            // 4. Render Manual Map Pin Drop if user tapped map
            manualPinPos?.let { pin ->
                // Beacon ring around dropped pin
                drawCircle(
                    color = CruizeNeonCyan.copy(alpha = pulseAlpha),
                    radius = (pulseRadius * 0.8f).coerceAtLeast(15f),
                    center = pin,
                    style = Stroke(width = 2.5f)
                )

                // Vertical beacon ray
                drawLine(
                    color = CruizeNeonCyan,
                    start = pin,
                    end = Offset(pin.x, pin.y - 60f),
                    strokeWidth = 3.5f
                )

                // Pin head diamond
                val diamondPath = Path().apply {
                    moveTo(pin.x, pin.y - 75f)
                    lineTo(pin.x - 12f, pin.y - 60f)
                    lineTo(pin.x, pin.y - 45f)
                    lineTo(pin.x + 12f, pin.y - 60f)
                    close()
                }
                drawPath(path = diamondPath, color = CruizeNeonCyan)

                // Pinned Badge
                val pinBadgeText = "📍 MANUAL PIN DROPPED"
                val pinLayout = textMeasurer.measure(
                    text = pinBadgeText,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = CruizeDarkBackground
                    )
                )
                val pWidth = pinLayout.size.width + 16f
                val pHeight = pinLayout.size.height + 8f
                val pPos = Offset(pin.x - pWidth / 2f, pin.y - 105f)

                drawRoundRect(
                    color = CruizeNeonCyan,
                    topLeft = pPos,
                    size = Size(pWidth, pHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                drawText(
                    textLayoutResult = pinLayout,
                    topLeft = Offset(pPos.x + 8f, pPos.y + 4f)
                )
            }

            // 5. Render Driver Vehicle Indicator & Pulsating Radar
            // Pulsating Radar Ring
            drawCircle(
                color = CruizeNeonCyan.copy(alpha = pulseAlpha),
                radius = pulseRadius * scaleFactor,
                center = baseVehiclePos,
                style = Stroke(width = 2f)
            )

            // Vehicle Glow Base
            drawCircle(
                color = CruizeNeonCyan.copy(alpha = 0.25f),
                radius = 32f * scaleFactor,
                center = baseVehiclePos
            )

            // Vehicle Arrow
            val vehiclePath = Path().apply {
                moveTo(baseVehiclePos.x, baseVehiclePos.y - 24f * scaleFactor)
                lineTo(baseVehiclePos.x - 18f * scaleFactor, baseVehiclePos.y + 18f * scaleFactor)
                lineTo(baseVehiclePos.x, baseVehiclePos.y + 8f * scaleFactor)
                lineTo(baseVehiclePos.x + 18f * scaleFactor, baseVehiclePos.y + 18f * scaleFactor)
                close()
            }

            drawPath(
                path = vehiclePath,
                brush = Brush.verticalGradient(
                    colors = listOf(CruizeNeonCyan, CruizeElectricBlue)
                )
            )

            // Driver Label
            val driverTextLayout = textMeasurer.measure(
                text = "YOU • $driverSpeedMph KM/H",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = CruizeDarkBackground
                )
            )

            val dWidth = driverTextLayout.size.width + 16f
            val dHeight = driverTextLayout.size.height + 8f
            val dPos = Offset(baseVehiclePos.x - dWidth / 2f, baseVehiclePos.y + 30f * scaleFactor)

            drawRoundRect(
                color = CruizeNeonCyan,
                topLeft = dPos,
                size = Size(dWidth, dHeight),
                cornerRadius = CornerRadius(10f, 10f)
            )

            drawText(
                textLayoutResult = driverTextLayout,
                topLeft = Offset(dPos.x + 8f, dPos.y + 4f)
            )
        }

        // Manual Pin Action Bar Overlay at Bottom Center
        manualPinPos?.let { pin ->
            val pinnedLat = -1.2863 + ((pin.y - 400f) / 10000.0)
            val pinnedLng = 36.8172 + ((pin.x - 400f) / 10000.0)
            val roadEstimate = "Pinned Location @ Thika Superhighway"

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                shape = RoundedCornerShape(18.dp),
                color = CruizeDarkSurface.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CruizeNeonCyan),
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PinDrop,
                        contentDescription = "Pin Drop",
                        tint = CruizeNeonCyan,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LOCATION PINNED ON MAP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = CruizeNeonCyan
                        )
                        Text(
                            text = "${pinnedLat.toString().take(7)}° N, ${pinnedLng.toString().take(8)}° W",
                            fontSize = 11.sp,
                            color = CruizeTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { manualPinPos = null },
                        modifier = Modifier.size(32.dp).testTag("clear_map_pin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Pin",
                            tint = CruizeTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Button(
                        onClick = {
                            onMapCctvDropped?.invoke(pinnedLat, pinnedLng, roadEstimate)
                            manualPinPos = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CruizeElectricBlue,
                            contentColor = CruizeTextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("map_cctv_at_pin_button")
                    ) {
                        Text(
                            text = "📹 + CCTV",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            onManualPinDropped?.invoke(pinnedLat, pinnedLng, roadEstimate)
                            manualPinPos = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CruizeNeonCyan,
                            contentColor = CruizeDarkBackground
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("report_at_pin_button")
                    ) {
                        Text(
                            text = "+ Hazard",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Selected CCTV Camera Quick Preview Floating Card (if selected hazard is CCTV)
        if (selectedHazard != null && (selectedHazard.type == "CCTV_CAMERA" || selectedHazard.type == "STATIONARY_SPEED_CAM")) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                shape = RoundedCornerShape(14.dp),
                color = CruizeDarkSurface.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CruizeNeonCyan),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = CruizeNeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedHazard.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CruizeTextPrimary
                        )
                        Text(
                            text = "${selectedHazard.cameraStatus.replace('_', ' ')} • Limit ${selectedHazard.speedLimit} KM/H",
                            fontSize = 10.sp,
                            color = CruizeNeonCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = { onInspectCctvFeed?.invoke(selectedHazard) },
                        colors = ButtonDefaults.buttonColors(containerColor = CruizeNeonCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("open_cctv_feed_button")
                    ) {
                        Text(
                            text = "Live Cam 🎥",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = CruizeDarkBackground
                        )
                    }
                }
            }
        }

        // Floating Map Controls Overlay (Top Right / Bottom Right)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // CCTV Layer Toggle Button
            FloatingMapControlButton(
                icon = Icons.Default.Videocam,
                label = "CCTV",
                isActive = showCctvLayer,
                onClick = { onToggleCctvLayer?.invoke() },
                testTag = "map_control_toggle_cctv_layer"
            )

            // Manual Pin Drop Button
            FloatingMapControlButton(
                icon = Icons.Default.PinDrop,
                label = "Pin",
                isActive = manualPinPos != null,
                onClick = {
                    manualPinPos = Offset(300f, 400f)
                },
                testTag = "map_control_drop_pin"
            )

            // 3D Perspective Tilt Button
            FloatingMapControlButton(
                icon = if (is3dPerspective) Icons.Default.ViewInAr else Icons.Default.Layers,
                label = if (is3dPerspective) "3D" else "2D",
                isActive = is3dPerspective,
                onClick = onToggle3d,
                testTag = "map_control_3d_tilt"
            )

            // Zoom In (+) Button
            FloatingMapControlButton(
                icon = Icons.Default.Add,
                label = "Zoom",
                isActive = false,
                onClick = { zoomScale = (zoomScale + 0.25f).coerceAtMost(3.0f) },
                testTag = "map_control_zoom_in"
            )

            // Zoom Out (-) Button
            FloatingMapControlButton(
                icon = Icons.Default.Remove,
                label = "Out",
                isActive = false,
                onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.6f) },
                testTag = "map_control_zoom_out"
            )

            // Recenter Driver Button
            FloatingMapControlButton(
                icon = Icons.Default.MyLocation,
                label = "Center",
                isActive = false,
                onClick = {
                    panOffset = Offset.Zero
                    zoomScale = 1.0f
                    manualPinPos = null
                },
                testTag = "map_control_recenter"
            )

            // Drive Simulation Toggle
            FloatingMapControlButton(
                icon = if (isSimulatingDrive) Icons.Default.Pause else Icons.Default.PlayArrow,
                label = if (isSimulatingDrive) "Pause" else "Drive",
                isActive = isSimulatingDrive,
                onClick = onToggleSimulateDrive,
                testTag = "map_control_simulate"
            )
        }
    }
}

@Composable
private fun FloatingMapControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) CruizeNeonCyan else CruizeDarkSurface.copy(alpha = 0.9f),
        border = SurfaceBorder(),
        shadowElevation = 6.dp,
        modifier = Modifier
            .size(48.dp)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) CruizeDarkBackground else CruizeNeonCyan,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) CruizeDarkBackground else CruizeTextSecondary
            )
        }
    }
}
