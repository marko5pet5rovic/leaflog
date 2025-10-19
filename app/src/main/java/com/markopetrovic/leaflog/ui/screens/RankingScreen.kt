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
import com.markopetrovic.leaflog.data.models.ProfileDTO
import com.markopetrovic.leaflog.ui.viewmodels.AuthViewModel
import com.markopetrovic.leaflog.ui.viewmodels.AuthState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color

private enum class LeaderboardType(val title: String) {LOCATIONS("Locations"), USERS("Users") }

@Composable
fun RankingScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    localNavController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUserId: String? = when (val state = authState) {
        is AuthState.Authenticated -> state.uid
        else -> null
    }

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
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            LeaderboardType.entries.forEachIndexed { index, type ->
                Tab(
                    selected = selectedTab.ordinal == index,
                    onClick = { selectedTab = type },
                    text = { Text(type.title, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (selectedTab) {
                    LeaderboardType.LOCATIONS -> {
                        itemsIndexed(locationRankings) { index, location ->
                            RankingCard(
                                index = index,
                                title = location.name,
                                points = location.points,
                                isTop3 = index < 3,
                                isCurrentUser = false,
                                onClick = {
                                    val route = Screen.LocationDetail.createRoute(location.id)
                                    localNavController.navigate(route)
                                }
                            )
                        }
                    }
                    LeaderboardType.USERS -> {
                        itemsIndexed(userRankings) { index, profile ->
                            if (profile is ProfileDTO) {
                                val isCurrentUser = profile.uid == currentUserId

                                RankingCard(
                                    index = index,
                                    title = if (isCurrentUser) "${profile.username} (You)" else profile.username,
                                    points = profile.totalPoints,
                                    isTop3 = index < 3,
                                    isCurrentUser = isCurrentUser,
                                    onClick = null
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
    isCurrentUser: Boolean,
    onClick: (() -> Unit)? = null
) {
    val rank1Color = Color(0xFFFFC107)
    val rank2Color = Color(0xFF90A4AE)
    val rank3Color = Color(0xFFA1887F)

    val rankColor = when (index) {
        0 -> rank1Color
        1 -> rank2Color
        2 -> rank3Color
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val containerColor = when {
        isCurrentUser -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        isTop3 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    val titleTextColor = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser || isTop3) 6.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${index + 1}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = if (isTop3) rankColor else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.width(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Medium,
                color = titleTextColor,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "$points pts",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}