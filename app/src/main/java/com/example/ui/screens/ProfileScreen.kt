package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.ui.components.CruizeBrandLogo
import com.example.ui.components.CruizeLogoSize
import com.example.ui.theme.*
import com.example.viewmodel.CruizeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: CruizeViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CruizeDarkBackground)
            .testTag("profile_screen")
    ) {
        if (user != null && user?.isLoggedIn == true) {
            LoggedInProfileContent(
                user = user!!,
                viewModel = viewModel,
                onSignOut = { viewModel.signOut() }
            )
        } else {
            AuthContent(
                onSignIn = { email, pass, onRes -> viewModel.signIn(email, pass, onRes) },
                onSignUp = { u, e, p, v, onRes -> viewModel.signUp(u, e, p, v, onRes) },
                onGuestLogin = { viewModel.loginAsGuest() }
            )
        }
    }
}

@Composable
private fun LoggedInProfileContent(
    user: UserEntity,
    viewModel: CruizeViewModel,
    onSignOut: () -> Unit
) {
    val userHazards by viewModel.userReportingHistory.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US) }
    val signInFormatted = remember(user.lastSignInTime) {
        dateFormat.format(Date(user.lastSignInTime))
    }
    val signOutFormatted = remember(user.lastSignOutTime) {
        dateFormat.format(Date(user.lastSignOutTime))
    }

    // Reputation Tier Label
    val (repTierLabel, repTierColor) = remember(user.reputationScore) {
        when {
            user.reputationScore >= 300 -> "⚡ ELITE SENTINEL" to CruizeNeonCyan
            user.reputationScore >= 150 -> "🛡️ MASTER CRUISER" to CruizeSuccessGreen
            user.reputationScore >= 50 -> "🚗 VERIFIED DRIVER" to CruizeElectricBlue
            else -> "🐣 NOVICE CRUISER" to CruizeTextSecondary
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Profile Header Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = CruizeDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(CruizeNeonCyan, CruizeElectricBlue)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Avatar",
                            tint = CruizeDarkBackground,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user.username,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = CruizeTextPrimary
                    )

                    Text(
                        text = user.email,
                        fontSize = 12.sp,
                        color = CruizeTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bio
                    Text(
                        text = "\"${user.bio}\"",
                        fontSize = 12.sp,
                        color = CruizeTextPrimary,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reputation & Tier Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CruizeDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, repTierColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = repTierColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$repTierLabel • ${user.reputationScore} REP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = repTierColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Edit Profile Button
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CruizeNeonCyan),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CruizeNeonCyan),
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EDIT PROFILE & BIO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sign In & Sign Out Timestamps Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CruizeDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SESSION & LOGIN ACTIVITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = CruizeNeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Login, contentDescription = null, tint = CruizeSuccessGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Current Sign-In:", fontSize = 12.sp, color = CruizeTextSecondary)
                        }
                        Text(signInFormatted, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CruizeTextPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = CruizeWarningAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Last Sign-Out:", fontSize = 12.sp, color = CruizeTextSecondary)
                        }
                        Text(signOutFormatted, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CruizeTextPrimary)
                    }
                }
            }
        }

        // Driver Stats Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DriverStatCard(
                    title = "HAZARDS POSTED",
                    value = "${userHazards.size + user.reportsCount}",
                    icon = Icons.Default.AddAlert,
                    modifier = Modifier.weight(1f)
                )
                DriverStatCard(
                    title = "DRIVER VEHICLE",
                    value = user.preferredVehicle.take(14),
                    icon = Icons.Default.MinorCrash,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Reputation Logic Explanation Tooltip Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CruizeDarkSurfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = CruizeNeonCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Reputation Score Rules", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CruizeNeonCyan)
                        Text(
                            "+15 REP for reporting real hazards, +10 REP when other drivers confirm your alert. -5 REP if report is disproved.",
                            fontSize = 11.sp,
                            color = CruizeTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // User's Reporting History Log Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "MY REPORTING HISTORY LOG (${userHazards.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = CruizeNeonCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (userHazards.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CruizeDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
                    ) {
                        Text(
                            text = "No reports submitted yet. Tap the + Floating button on Home to report speed traps, bumps, or potholes!",
                            fontSize = 12.sp,
                            color = CruizeTextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    userHazards.forEach { hazard ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = CruizeDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(hazard.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CruizeTextPrimary)
                                    Text("${hazard.roadName} • ${hazard.type}", fontSize = 11.sp, color = CruizeTextSecondary)
                                    Text("Validated • ${hazard.upvotes} Confirmations", fontSize = 10.sp, color = CruizeSuccessGreen)
                                }
                                IconButton(
                                    onClick = { viewModel.deleteReport(hazard) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Retract Report", tint = CruizeDangerCrimson, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sign Out Button
        item {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDangerCrimson),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CruizeDangerCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("sign_out_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SIGN OUT", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        var editUsername by remember { mutableStateOf(user.username) }
        var editBio by remember { mutableStateOf(user.bio) }
        var editVehicle by remember { mutableStateOf(user.preferredVehicle) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Driver Profile", fontWeight = FontWeight.Bold, color = CruizeNeonCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CruizeNeonCyan,
                            unfocusedBorderColor = CruizeDarkCardBorder,
                            focusedTextColor = CruizeTextPrimary,
                            unfocusedTextColor = CruizeTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Driver Bio") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CruizeNeonCyan,
                            unfocusedBorderColor = CruizeDarkCardBorder,
                            focusedTextColor = CruizeTextPrimary,
                            unfocusedTextColor = CruizeTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = editVehicle,
                        onValueChange = { editVehicle = it },
                        label = { Text("Preferred Vehicle") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CruizeNeonCyan,
                            unfocusedBorderColor = CruizeDarkCardBorder,
                            focusedTextColor = CruizeTextPrimary,
                            unfocusedTextColor = CruizeTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(editUsername, editBio, editVehicle, user.avatarUri)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CruizeNeonCyan)
                ) {
                    Text("Save Changes", color = CruizeDarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = CruizeTextSecondary)
                }
            },
            containerColor = CruizeDarkSurface
        )
    }
}

@Composable
private fun DriverStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = CruizeDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CruizeDarkCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = CruizeNeonCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 10.sp, color = CruizeTextSecondary, fontWeight = FontWeight.Bold)
            Text(text = value, fontSize = 15.sp, color = CruizeTextPrimary, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun AuthContent(
    onSignIn: (email: String, pass: String, onResult: (String?) -> Unit) -> Unit,
    onSignUp: (username: String, email: String, pass: String, vehicle: String, onResult: (String?) -> Unit) -> Unit,
    onGuestLogin: () -> Unit
) {
    var isSignUpTab by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("driver@cruize.app") }
    var passwordInput by remember { mutableStateOf("demo123") }
    var usernameInput by remember { mutableStateOf("SpeedyRider") }
    var vehicleInput by remember { mutableStateOf("Sports Coupe") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            CruizeBrandLogo(
                logoSize = CruizeLogoSize.LARGE,
                subtitle = "EAST AFRICA ROAD RADAR",
                animated = true,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "CRUIZE DRIVER PORTAL",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = CruizeNeonCyan,
                letterSpacing = 1.sp
            )
            Text(
                text = "Sign in or create account to save radar alerts & earn Karma",
                fontSize = 12.sp,
                color = CruizeTextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CruizeDarkSurfaceVariant)
                    .padding(4.dp)
            ) {
                TabButton(
                    selected = !isSignUpTab,
                    label = "Sign In",
                    onClick = { isSignUpTab = false },
                    modifier = Modifier.weight(1f).testTag("tab_sign_in")
                )
                TabButton(
                    selected = isSignUpTab,
                    label = "Sign Up",
                    onClick = { isSignUpTab = true },
                    modifier = Modifier.weight(1f).testTag("tab_sign_up")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = CruizeDangerCrimson,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isSignUpTab) {
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it },
                    label = { Text("Driver Username") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CruizeNeonCyan) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CruizeNeonCyan, unfocusedBorderColor = CruizeDarkCardBorder,
                        focusedLabelColor = CruizeNeonCyan, unfocusedLabelColor = CruizeTextSecondary,
                        focusedTextColor = CruizeTextPrimary, unfocusedTextColor = CruizeTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("auth_username_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CruizeNeonCyan) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CruizeNeonCyan, unfocusedBorderColor = CruizeDarkCardBorder,
                    focusedLabelColor = CruizeNeonCyan, unfocusedLabelColor = CruizeTextSecondary,
                    focusedTextColor = CruizeTextPrimary, unfocusedTextColor = CruizeTextPrimary
                ),
                modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CruizeNeonCyan) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CruizeNeonCyan, unfocusedBorderColor = CruizeDarkCardBorder,
                    focusedLabelColor = CruizeNeonCyan, unfocusedLabelColor = CruizeTextSecondary,
                    focusedTextColor = CruizeTextPrimary, unfocusedTextColor = CruizeTextPrimary
                ),
                modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
            )

            if (isSignUpTab) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = vehicleInput,
                    onValueChange = { vehicleInput = it },
                    label = { Text("Vehicle Type (e.g., Coupe, EV, Motorcycle)") },
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = CruizeNeonCyan) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CruizeNeonCyan, unfocusedBorderColor = CruizeDarkCardBorder,
                        focusedLabelColor = CruizeNeonCyan, unfocusedLabelColor = CruizeTextSecondary,
                        focusedTextColor = CruizeTextPrimary, unfocusedTextColor = CruizeTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("auth_vehicle_input")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (isSignUpTab) {
                        onSignUp(usernameInput, emailInput, passwordInput, vehicleInput) { err -> errorMessage = err }
                    } else {
                        onSignIn(emailInput, passwordInput) { err -> errorMessage = err }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CruizeNeonCyan, contentColor = CruizeDarkBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_submit_button")
            ) {
                Text(if (isSignUpTab) "CREATE DRIVER ACCOUNT" else "SIGN IN TO CRUIZE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onGuestLogin,
                modifier = Modifier.testTag("auth_guest_button")
            ) {
                Text("Continue as Guest Cruiser", color = CruizeTextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TabButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) CruizeNeonCyan else Color.Transparent,
        modifier = modifier.height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) CruizeDarkBackground else CruizeTextSecondary
            )
        }
    }
}
