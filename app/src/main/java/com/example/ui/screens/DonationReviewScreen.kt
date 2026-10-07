package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HazardReviewEntity
import com.example.ui.theme.*
import com.example.viewmodel.CruizeViewModel

@Composable
fun DonationReviewScreen(
    viewModel: CruizeViewModel,
    modifier: Modifier = Modifier
) {
    val reviews by viewModel.allReviews.collectAsState()
    val hazards by viewModel.allHazards.collectAsState()

    var donationSuccessMsg by remember { mutableStateOf<String?>(null) }
    var reviewCommentInput by remember { mutableStateOf("") }
    var reviewRating by remember { mutableFloatStateOf(5.0f) }
    var selectedHazardForReview by remember { mutableStateOf(hazards.firstOrNull()?.id ?: 1L) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CruizeDarkBackground)
            .padding(16.dp)
            .testTag("donation_review_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "SUPPORT CRUIZE & DRIVER REVIEWS",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = CruizeNeonCyan,
                letterSpacing = 1.sp
            )
            Text(
                text = "Keep the community radar online. Leave reviews on reported road hazards.",
                fontSize = 12.sp,
                color = CruizeTextSecondary
            )
        }

        // 1. Tip Jar / Support Section
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = CruizeDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Coffee, contentDescription = null, tint = CruizeWarningAmber, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SUPPORT CRUIZE DEVELOPMENT",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = CruizeTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Cruize is free & community powered. Fuel our servers and AI radar updates!",
                        fontSize = 11.sp,
                        color = CruizeTextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (donationSuccessMsg != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CruizeSuccessGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CruizeSuccessGreen)
                        ) {
                            Text(
                                text = donationSuccessMsg!!,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CruizeSuccessGreen,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DonationPillButton(
                            title = "☕ KSh 200 Chai",
                            onClick = { donationSuccessMsg = "🎉 Asante sana! Thank you for the KSh 200 Chai support! +50 Karma awarded!" },
                            modifier = Modifier.weight(1f).testTag("donate_200")
                        )
                        DonationPillButton(
                            title = "⚡ KSh 500 Fuel",
                            onClick = { donationSuccessMsg = "🚀 Asante sana! Thank you for the KSh 500 Fuel support! +150 Karma awarded!" },
                            modifier = Modifier.weight(1f).testTag("donate_500")
                        )
                        DonationPillButton(
                            title = "🔥 KSh 1,000 Nitro",
                            onClick = { donationSuccessMsg = "👑 Legendary KSh 1,000 Supercharge! +400 Karma awarded!" },
                            modifier = Modifier.weight(1f).testTag("donate_1000")
                        )
                    }
                }
            }
        }

        // 2. Submit Hazard Review Section
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = CruizeDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "LEAVE A DRIVER REVIEW",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = CruizeNeonCyan
                    )
                    Text(
                        text = "Rate accuracy of reported bumps and cop traps",
                        fontSize = 11.sp,
                        color = CruizeTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rating Star Picker
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { reviewRating = star.toFloat() },
                                modifier = Modifier.size(32.dp).testTag("star_rate_$star")
                            ) {
                                Icon(
                                    imageVector = if (star <= reviewRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star $star",
                                    tint = CruizeWarningAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Text(
                            text = "${reviewRating.toInt()} / 5 Stars",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CruizeWarningAmber,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = reviewCommentInput,
                        onValueChange = { reviewCommentInput = it },
                        label = { Text("Driver feedback (e.g., Cop still there, bump painted yellow)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CruizeNeonCyan, unfocusedBorderColor = CruizeDarkCardBorder,
                            focusedLabelColor = CruizeNeonCyan, unfocusedLabelColor = CruizeTextSecondary,
                            focusedTextColor = CruizeTextPrimary, unfocusedTextColor = CruizeTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("review_comment_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (reviewCommentInput.isNotBlank()) {
                                viewModel.addReview(selectedHazardForReview, reviewRating, reviewCommentInput)
                                reviewCommentInput = ""
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CruizeNeonCyan, contentColor = CruizeDarkBackground),
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("submit_review_button")
                    ) {
                        Text("POST DRIVER REVIEW", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Recent Driver Reviews Feed
        item {
            Text(
                text = "COMMUNITY DRIVER REVIEWS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CruizeNeonCyan
            )
        }

        items(reviews, key = { it.id }) { rev ->
            ReviewFeedCard(review = rev)
        }
    }
}

@Composable
private fun DonationPillButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = CruizeDarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan),
        modifier = modifier.height(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CruizeNeonCyan
            )
        }
    }
}

@Composable
private fun ReviewFeedCard(review: HazardReviewEntity) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CruizeDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = review.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CruizeTextPrimary)
                Row {
                    repeat(review.rating.toInt()) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = CruizeWarningAmber, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = review.comment, fontSize = 12.sp, color = CruizeTextSecondary)
        }
    }
}
