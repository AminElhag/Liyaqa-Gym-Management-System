package com.liyaqa.android.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.liyaqa.android.ui.theme.LiyaqaBrand

/**
 * Edit Profile Screen
 * Allows users to edit their profile information and upload a profile photo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val editProfileState by viewModel.editProfileState.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactPhone by remember { mutableStateOf("") }
    var profilePhotoUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Initialize form with current profile data
    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val member = (profileState as ProfileState.Success).member
            name = member.name
            phone = member.contactInfo.phone
            emergencyContactName = member.emergencyContactName ?: ""
            emergencyContactPhone = member.emergencyContactPhone ?: ""
            profilePhotoUrl = member.profilePhotoUrl
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        // TODO: Upload image to server and get URL
        // For now, just use the URI string
        profilePhotoUrl = uri?.toString()
    }

    // Show success snackbar
    LaunchedEffect(editProfileState.successMessage) {
        if (editProfileState.successMessage != null) {
            // Navigate back after successful update
            kotlinx.coroutines.delay(1000)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Photo Section
                Box(
                    modifier = Modifier.padding(vertical = 24.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    // Profile Image
                    if (selectedImageUri != null || profilePhotoUrl != null) {
                        AsyncImage(
                            model = selectedImageUri ?: profilePhotoUrl,
                            contentDescription = "Profile photo",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Camera button
                    FloatingActionButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.size(40.dp),
                        containerColor = LiyaqaBrand,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change photo",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Personal Information Section
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Emergency Contact Section
                Text(
                    text = "Emergency Contact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Emergency Contact Name Field
                OutlinedTextField(
                    value = emergencyContactName,
                    onValueChange = { emergencyContactName = it },
                    label = { Text("Emergency Contact Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Emergency Contact Phone Field
                OutlinedTextField(
                    value = emergencyContactPhone,
                    onValueChange = { emergencyContactPhone = it },
                    label = { Text("Emergency Contact Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Error Message
                if (editProfileState.error != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = editProfileState.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Success Message
                if (editProfileState.successMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = LiyaqaBrand.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = editProfileState.successMessage!!,
                            color = LiyaqaBrand,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Save Button
                Button(
                    onClick = {
                        viewModel.updateProfile(
                            name = name,
                            phone = phone,
                            emergencyContactName = emergencyContactName.takeIf { it.isNotBlank() },
                            emergencyContactPhone = emergencyContactPhone.takeIf { it.isNotBlank() },
                            profilePhotoUrl = profilePhotoUrl
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !editProfileState.isSaving && name.isNotBlank() && phone.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LiyaqaBrand
                    )
                ) {
                    if (editProfileState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Save Changes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Clear messages when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearEditProfileMessages()
        }
    }
}
