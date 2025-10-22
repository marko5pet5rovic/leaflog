package com.markopetrovic.leaflog.ui.screens.profile

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import com.markopetrovic.leaflog.ui.viewmodels.AuthState
import com.markopetrovic.leaflog.ui.viewmodels.AuthViewModel
import com.markopetrovic.leaflog.ui.viewmodels.ProfileViewModel
import com.markopetrovic.leaflog.ui.viewmodels.ProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    val currentUserId: String = when (val state = authState) {
        is AuthState.Authenticated -> state.uid
        else -> ""
    }

    if (authState is AuthState.Unauthenticated) {
        navController.navigate(Screen.Welcome.route) {
            popUpTo(Screen.BottomNavContainer.route) { inclusive = true }
        }
        return
    }

    val profileViewModel = viewModel<ProfileViewModel>(
        viewModelStoreOwner = LocalContext.current as ComponentActivity,
        key = currentUserId,
        factory = ProfileViewModelFactory(
            currentUserId = currentUserId,
            profileRepository = AppContainer.profileRepository,
            storageRepository = AppContainer.storageRepository,
            locationRepository = AppContainer.locationRepository
        )
    )

    val profileData = profileViewModel?.profile?.collectAsState()?.value
    val displayProfile = profileData ?: profileViewModel?.editableProfile?.collectAsState()?.value

    val totalLocationsCount by profileViewModel?.totalLocationsCount?.collectAsState(initial = 0) ?: remember { mutableIntStateOf(0) }
    val dynamicTotalPoints by profileViewModel?.totalPoints?.collectAsState(initial = 0) ?: remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .padding(bottom = 16.dp)
            ) {
                if (!displayProfile?.avatarUrl.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = displayProfile.avatarUrl),
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

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Points Total: $dynamicTotalPoints",
                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(16.dp))

            ProfileDetailRow(title = "Locations Logged", value = totalLocationsCount)
            ProfileDetailRow(title = "Badges Earned", value = displayProfile?.badgesEarned ?: 0)
        }

        Button(
            onClick = { authViewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Log Out")
        }
    }
}

@Composable
private fun ProfileDetailRow(title: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
