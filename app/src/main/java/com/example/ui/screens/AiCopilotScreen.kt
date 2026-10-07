package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.CruizeViewModel
import kotlinx.coroutines.launch

@Composable
fun AiCopilotScreen(
    viewModel: CruizeViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    val roadName by viewModel.currentRoadName.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "🔍 NTSA speed limit on $roadName",
        "🚨 Kenya Traffic Police traps nearby?",
        "📰 East Africa highway transit updates",
        "⚠️ Report NTSA Speed Cam / Hump"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CruizeDarkBackground)
            .testTag("ai_copilot_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CruizeDarkSurface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(CruizeNeonCyan, CruizeElectricBlue)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Co-Pilot",
                                tint = CruizeDarkBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CRUIZE CO-PILOT AI",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = CruizeNeonCyan
                            )
                            Text(
                                text = "Powered by Gemini & Live Search Data",
                                fontSize = 11.sp,
                                color = CruizeTextSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CruizeDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CruizeSuccessGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Active On-Road", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CruizeSuccessGreen)
                        }
                    }
                }
            }

            // Quick Suggestion Chips Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CruizeDarkSurface.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    SuggestionChip(
                        onClick = {
                            viewModel.sendCopilotMessage(prompt.replace(Regex("^[^\b]+\\s"), ""))
                        },
                        label = { Text(prompt, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = CruizeDarkSurfaceVariant,
                            labelColor = CruizeTextPrimary
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            borderColor = CruizeDarkCardBorder,
                            enabled = true
                        )
                    )
                }
            }

            // Chat Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == "You"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (isUser) 18.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 18.dp
                            ),
                            color = if (isUser) CruizeElectricBlue.copy(alpha = 0.3f) else CruizeDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isUser) CruizeElectricBlue else CruizeNeonCyan
                            ),
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = msg.sender,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isUser) CruizeElectricBlue else CruizeNeonCyan
                                    )
                                    if (!isUser) {
                                        IconButton(
                                            onClick = { viewModel.speakCurrentAlert() },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = "Read aloud",
                                                tint = CruizeTextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = CruizeTextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = CruizeNeonCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cruize AI consulting Google search & road radar...",
                                fontSize = 11.sp,
                                color = CruizeTextSecondary
                            )
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                color = CruizeDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask Co-Pilot or fact check traffic...", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CruizeNeonCyan,
                            unfocusedBorderColor = CruizeDarkCardBorder,
                            focusedTextColor = CruizeTextPrimary,
                            unfocusedTextColor = CruizeTextPrimary,
                            focusedPlaceholderColor = CruizeTextDim,
                            unfocusedPlaceholderColor = CruizeTextDim
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copilot_chat_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendCopilotMessage(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(CruizeNeonCyan)
                            .testTag("copilot_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = CruizeDarkBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
