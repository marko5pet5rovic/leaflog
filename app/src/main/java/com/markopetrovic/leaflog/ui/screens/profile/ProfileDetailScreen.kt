@file:OptIn(ExperimentalMaterial3Api::class)

package com.markopetrovic.leaflog.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
fun ProfileDetailScreen(
    navController: NavController,
    paddingValues: PaddingValues
) {
    val authViewModel: AuthViewModel = AppContainer.authViewModel
    val appNavigator = AppContainer.appNavigator
    val authState by authViewModel.authState.collectAsState()

    val currentUserId: String = when (val state = authState) {
        is AuthState.Authenticated -> state.uid
        else -> ""
    }

    val profileViewModel: ProfileViewModel? = if (currentUserId.isNotEmpty()) {
        viewModel(
            key = currentUserId,
            factory = ProfileViewModelFactory(
                currentUserId = currentUserId,
                profileRepository = AppContainer.profileRepository,
                storageRepository = AppContainer.storageRepository,
                locationRepository = AppContainer.locationRepository
            )
        )
    } else null

    val profileData by profileViewModel?.profile?.collectAsState() ?: remember { mutableStateOf(null) }
    val totalLocationsCount by profileViewModel?.totalLocationsCount?.collectAsState(initial = 0) ?: remember { mutableStateOf(0) }
    val dynamicTotalPoints by profileViewModel?.totalPoints?.collectAsState(initial = 0) ?: remember { mutableStateOf(0) }

    if (authState is AuthState.Unauthenticated) {
        appNavigator.navigate(Screen.Welcome.route) {
            popUpTo(Screen.BottomNavContainer.route) { inclusive = true }
        }
        return
    }

    val displayProfile = profileData

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp).clip(CircleShape).padding(bottom = 16.dp)
                ) {
                    if (!displayProfile?.avatarUrl.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = displayProfile?.avatarUrl),
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
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${displayProfile?.firstName ?: ""} ${displayProfile?.lastName ?: ""}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = displayProfile?.username ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Points Total: $dynamicTotalPoints",
                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(24.dp))

                ProfileDetailRow(title = "Locations Logged", value = totalLocationsCount)
                ProfileDetailRow(title = "Badges Earned", value = displayProfile?.badgesEarned ?: 0)
            }

            Button(
                onClick = { authViewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Log Out")
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate(Screen.ProfileEdit.route) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp + paddingValues.calculateBottomPadding())
        ) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
        }
    }
}

@Composable
fun ProfileDetailRow(title: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.secondary)
        )
    }
    Divider()
}