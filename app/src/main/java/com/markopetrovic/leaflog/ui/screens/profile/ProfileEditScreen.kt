@file:OptIn(ExperimentalMaterial3Api::class)

package com.markopetrovic.leaflog.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.markopetrovic.leaflog.di.AppContainer
import com.markopetrovic.leaflog.navigation.Screen
import com.markopetrovic.leaflog.services.auth.AuthState
import com.markopetrovic.leaflog.services.auth.AuthViewModel
import com.markopetrovic.leaflog.ui.viewmodels.AuthState
import com.markopetrovic.leaflog.ui.viewmodels.AuthViewModel
import com.markopetrovic.leaflog.ui.viewmodels.ProfileViewModel
import com.markopetrovic.leaflog.ui.viewmodels.ProfileViewModelFactory

@Composable
fun ProfileEditScreen(
    navController: NavController,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = AppContainer.authViewModel
    val appNavigator = AppContainer.appNavigator

    val authState by authViewModel.authState.collectAsState()

    val currentUserId: String = when (val state = authState) {
        is AuthState.Authenticated -> state.uid
        else -> ""
    }

    val profileViewModel: ProfileViewModel = viewModel(
        key = currentUserId,
        factory = ProfileViewModelFactory(
            currentUserId = currentUserId,
            profileRepository = AppContainer.profileRepository,
            storageRepository = AppContainer.storageRepository,
            locationRepository = AppContainer.locationRepository
        )
    )

    profileViewModel.setEditMode(true)

    val editableProfileData by profileViewModel.editableProfile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val saveStatus by profileViewModel.saveStatus.collectAsState()
    val selectedImageUri: Uri? by profileViewModel.selectedImageUri.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            profileViewModel.setSelectedImageUri(uri)
        }
    )

    if (authState is AuthState.Unauthenticated) {
        appNavigator.navigate(Screen.Welcome.route) {
            popUpTo(Screen.BottomNavContainer.route) { inclusive = true }
        }
        return
    }

    val imageSource = selectedImageUri ?: editableProfileData.avatarUrl
    val hasImage = (selectedImageUri != null) || !(editableProfileData.avatarUrl).isNullOrBlank()


    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp).clip(CircleShape).clickable(enabled = !isLoading) {
                    imagePickerLauncher.launch("image/*")
                }.padding(bottom = 16.dp)
            ) {
                if (hasImage) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageSource),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize().clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Default Avatar",
                        modifier = Modifier.matchParentSize(),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Filled.AddAPhoto,
                    contentDescription = "Change Avatar",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(32.dp).clip(CircleShape).align(Alignment.BottomEnd).padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        profileViewModel.saveProfile(context)
                        navController.popBackStack()
                    },
                    enabled = !isLoading,
                    modifier = Modifier.wrapContentSize()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Save, contentDescription = "Save")
                        Spacer(Modifier.width(4.dp))
                        Text("Save")
                    }
                }

                Spacer(Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {
                        profileViewModel.setEditMode(false)
                        navController.popBackStack()
                    },
                    enabled = !isLoading,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancel")
                    Spacer(Modifier.width(4.dp))
                    Text("Cancel")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            saveStatus?.let { status ->
                Text(
                    status,
                    color = if (status.contains("success")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            ProfileEditField(
                label = "First Name",
                value = editableProfileData.firstName ?: "",
                onValueChange = profileViewModel::updateFirstName,
                enabled = !isLoading
            )
            ProfileEditField(
                label = "Last Name",
                value = editableProfileData.lastName ?: "",
                onValueChange = profileViewModel::updateLastName,
                enabled = !isLoading
            )
            ProfileEditField(
                label = "Username",
                value = editableProfileData.username,
                onValueChange = profileViewModel::updateUsername,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = true,
        enabled = enabled
    )
}