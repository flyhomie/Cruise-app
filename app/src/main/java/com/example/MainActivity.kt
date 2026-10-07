package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FloatingBottomBar
import com.example.ui.components.HazardReportDialog
import com.example.ui.components.NavDestination
import com.example.ui.components.SlidingTopBar
import com.example.ui.screens.*
import com.example.ui.theme.CruizeDarkBackground
import com.example.ui.theme.CruizeTheme
import com.example.viewmodel.CruizeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CruizeTheme {
                CruizeAppContent()
            }
        }
    }
}

@Composable
fun CruizeAppContent(
    viewModel: CruizeViewModel = viewModel()
) {
    val currentDest by viewModel.currentDestination.collectAsState()
    val isTopBarExpanded by viewModel.isTopBarExpanded.collectAsState()
    val currentSpeed by viewModel.currentSpeedMph.collectAsState()
    val currentRoad by viewModel.currentRoadName.collectAsState()
    val is3dMode by viewModel.is3dPerspective.collectAsState()
    val isAiVoiceOn by viewModel.isAiVoiceOn.collectAsState()
    val showReportDialog by viewModel.showReportDialog.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CruizeDarkBackground)
            .testTag("cruize_app_root")
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = CruizeDarkBackground,
            topBar = {
                SlidingTopBar(
                    isExpanded = isTopBarExpanded,
                    onToggleExpand = { viewModel.toggleTopBar() },
                    currentSpeedMph = currentSpeed,
                    currentRoadName = currentRoad,
                    is3dMode = is3dMode,
                    onToggle3d = { viewModel.toggle3dPerspective() },
                    isAiVoiceOn = isAiVoiceOn,
                    onToggleAiVoice = { viewModel.toggleAiVoice() }
                )
            },
            bottomBar = {
                FloatingBottomBar(
                    currentDestination = currentDest,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentDest,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { destination ->
                    when (destination) {
                        NavDestination.HOME -> HomeScreen(viewModel = viewModel)
                        NavDestination.ALERTS_MAP -> AlertsMapScreen(viewModel = viewModel)
                        NavDestination.AI_COPILOT -> AiCopilotScreen(viewModel = viewModel)
                        NavDestination.DASHCAM -> DashcamScannerScreen(viewModel = viewModel)
                        NavDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        NavDestination.PROFILE -> ProfileScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Modal Bottom Sheet for Posting Road Hazards (Bumps, Cop Traps, Cameras, Potholes)
        val pinnedLocation by viewModel.pinnedLocation.collectAsState()
        val pinnedRoadName by viewModel.pinnedRoadName.collectAsState()

        if (showReportDialog) {
            HazardReportDialog(
                onDismiss = { viewModel.setShowReportDialog(false) },
                pinnedLocation = pinnedLocation,
                pinnedRoadName = pinnedRoadName,
                onSubmit = { type, title, desc, road, severity ->
                    viewModel.postHazardReport(type, title, desc, road, severity)
                }
            )
        }
    }
}
