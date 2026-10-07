package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiDriverAlert
import com.example.ui.theme.*

@Composable
fun AiWarningCard(
    alert: AiDriverAlert?,
    isLoadingAi: Boolean,
    onSpeakAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (alert == null && !isLoadingAi) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_warning_card"),
        shape = RoundedCornerShape(20.dp),
        color = CruizeDarkSurface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if ((alert?.riskScore ?: 0) > 70) CruizeDangerCrimson else CruizeWarningAmber
        ),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Title & Status Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if ((alert?.riskScore ?: 0) > 70)
                                    Brush.linearGradient(listOf(CruizeDangerCrimson, CruizeWarningAmber))
                                else
                                    Brush.linearGradient(listOf(CruizeWarningAmber, CruizeNeonCyan))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Hazard Warning",
                            tint = CruizeDarkBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI DRIVER RADAR WARNING",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = if ((alert?.riskScore ?: 0) > 70) CruizeDangerCrimson else CruizeWarningAmber,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isLoadingAi) "Analyzing road ahead with Gemini AI..." else "Proximity Alert",
                            fontSize = 11.sp,
                            color = CruizeTextSecondary
                        )
                    }
                }

                // Speak Audio Button
                if (alert != null) {
                    IconButton(
                        onClick = onSpeakAlert,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CruizeDarkSurfaceVariant)
                            .testTag("speak_alert_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Read Alert Aloud",
                            tint = CruizeNeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoadingAi) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = CruizeNeonCyan,
                    trackColor = CruizeDarkSurfaceVariant
                )
            } else if (alert != null) {
                // Voice Message Text
                Text(
                    text = alert.voiceWarning,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CruizeTextPrimary,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Risk Meter & Target Speed Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Risk Score Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Risk Level: ",
                            fontSize = 11.sp,
                            color = CruizeTextSecondary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (alert.riskScore > 70) CruizeDangerCrimson.copy(alpha = 0.2f) else CruizeWarningAmber.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${alert.riskScore} / 100",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (alert.riskScore > 70) CruizeDangerCrimson else CruizeWarningAmber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Recommended Target Speed
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CruizeDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Target: ",
                                fontSize = 11.sp,
                                color = CruizeTextSecondary
                            )
                            Text(
                                text = "${alert.recommendedSpeed} KM/H",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CruizeNeonCyan
                            )
                        }
                    }
                }
            }
        }
    }
}
