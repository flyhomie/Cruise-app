package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class NavDestination {
    ALERTS_MAP,
    AI_COPILOT,
    HOME,
    DASHCAM,
    SETTINGS,
    PROFILE
}

@Composable
fun FloatingBottomBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(16.dp, RoundedCornerShape(34.dp), ambientColor = CruizeNeonCyan, spotColor = CruizeElectricBlue)
                .testTag("floating_bottom_bar"),
            shape = RoundedCornerShape(34.dp),
            color = CruizeDarkSurface.copy(alpha = 0.95f),
            border = SurfaceBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Left Side: Map / Alerts
                NavPillItem(
                    selected = currentDestination == NavDestination.ALERTS_MAP,
                    icon = Icons.Outlined.Radar,
                    selectedIcon = Icons.Filled.Radar,
                    label = "Radar Map",
                    onClick = { onNavigate(NavDestination.ALERTS_MAP) },
                    testTag = "nav_map"
                )

                // 2. Left-Center: AI Co-Pilot Bot
                NavPillItem(
                    selected = currentDestination == NavDestination.AI_COPILOT,
                    icon = Icons.Outlined.AutoAwesome,
                    selectedIcon = Icons.Filled.AutoAwesome,
                    label = "AI Bot",
                    onClick = { onNavigate(NavDestination.AI_COPILOT) },
                    testTag = "nav_ai_copilot"
                )

                // 3. Center Anchor: HOME (Large Circular Drive Dashboard Button)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                if (currentDestination == NavDestination.HOME)
                                    listOf(CruizeNeonCyan, CruizeElectricBlue)
                                else
                                    listOf(CruizeDarkSurfaceVariant, CruizeDarkCardBorder)
                            )
                        )
                        .clickable { onNavigate(NavDestination.HOME) }
                        .testTag("nav_home_middle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Home Drive Dashboard",
                        tint = if (currentDestination == NavDestination.HOME) CruizeDarkBackground else CruizeNeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // 4. Right-Center: Dashcam AI Camera Vision Scanner
                NavPillItem(
                    selected = currentDestination == NavDestination.DASHCAM,
                    icon = Icons.Outlined.Videocam,
                    selectedIcon = Icons.Filled.Videocam,
                    label = "Dashcam",
                    onClick = { onNavigate(NavDestination.DASHCAM) },
                    testTag = "nav_dashcam"
                )

                // 5. Right Side: PROFILE
                NavPillItem(
                    selected = currentDestination == NavDestination.PROFILE,
                    icon = Icons.Outlined.Person,
                    selectedIcon = Icons.Filled.Person,
                    label = "Profile",
                    onClick = { onNavigate(NavDestination.PROFILE) },
                    testTag = "nav_profile_right"
                )
            }
        }
    }
}

@Composable
private fun NavPillItem(
    selected: Boolean,
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    val tintColor by animateColorAsState(
        targetValue = if (selected) CruizeNeonCyan else CruizeTextSecondary,
        label = "tintColor"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = tintColor
        )
    }
}
