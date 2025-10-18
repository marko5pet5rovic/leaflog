@file:OptIn(ExperimentalMaterial3Api::class)

package com.markopetrovic.leaflog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.markopetrovic.leaflog.di.AppContainer
import com.markopetrovic.leaflog.navigation.Screen
import com.markopetrovic.leaflog.ui.viewmodels.RankingViewModel
import com.markopetrovic.leaflog.data.models.LocationBase
import com.markopetrovic.leaflog.data.models.ProfileDTO
import com.markopetrovic.leaflog.services.auth.AuthState
import com.markopetrovic.leaflog.services.auth.AuthViewModel

private enum class LeaderboardType(val title: String) {
    LOCATIONS("Locations"),
    USERS("Users")
}

@Composable
fun RankingScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    localNavController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUserId: String? = (authState as? AuthState.Authenticated)?.uid

    val viewModel: RankingViewModel = viewModel(
        factory = RankingViewModel.RankingViewModelFactory(
            AppContainer.locationRepository,
            AppContainer.profileRepository,
            currentUserId
        )
    )

    val locationRankings by viewModel.locationRankings.collectAsState()
    val userRankings by viewModel.userRankings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableStateOf(LeaderboardType.LOCATIONS) }

    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues)
    ) {
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            LeaderboardType.entries.forEachIndexed { index, type ->
                Tab(
                    selected = selectedTab.ordinal == index,
                    onClick = { selectedTab = type },
                    text = { Text(type.title) }
                )
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text("Loading rankings...", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (selectedTab) {
                    LeaderboardType.LOCATIONS -> {
                        itemsIndexed(locationRankings) { index, location ->
                            RankingCard(
                                index = index,
                                title = location.name,
                                points = location.points,
                                isTop3 = index < 3,
                                onClick = {
                                    localNavController.navigate(Screen.LocationDetail.createRoute(location.id))
                                }
                            )
                        }
                    }
                    LeaderboardType.USERS -> {
                        itemsIndexed(userRankings) { index, profile ->
                            if (profile is ProfileDTO) {
                                val isCurrentUser = profile.uid == currentUserId
                                val title = if (isCurrentUser) "${profile.username} (You)" else profile.username

                                RankingCard(
                                    index = index,
                                    title = title,
                                    points = profile.totalPoints,
                                    isTop3 = index < 3,
                                    onClick = if (!isCurrentUser) {
                                        { /* localNavController.navigate(Screen.ProfileDetail.createRoute(profile.uid)) */ }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingCard(
    index: Int,
    title: String,
    points: Int,
    isTop3: Boolean,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth(),
        colors = if (isTop3) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${index + 1}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.width(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$points pts",
                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary)
            )
        }
    }
}