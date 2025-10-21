@file:OptIn(ExperimentalMaterial3Api::class)

package com.markopetrovic.leaflog.ui.screens.location.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.markopetrovic.leaflog.di.AppContainer
import com.markopetrovic.leaflog.data.models.PlantDTO
import com.markopetrovic.leaflog.data.models.MushroomDTO
import com.markopetrovic.leaflog.data.models.PlantingSpotDTO
import com.markopetrovic.leaflog.ui.viewmodels.PlantDetailViewModel
import com.markopetrovic.leaflog.ui.viewmodels.PlantDetailViewModelFactory

@Composable
fun LocationDetailScreen(
    navController: NavController,
    locationId: String
) {
    val locationViewModel: PlantDetailViewModel = viewModel(
        key = locationId,
        factory = PlantDetailViewModelFactory(
            locationId = locationId,
            locationRepository = AppContainer.locationRepository,
            profileRepository = AppContainer.profileRepository,
            authRepository = AppContainer.authRepository
        )
    )

    val location by locationViewModel.location.collectAsState()
    val publisher by locationViewModel.publisher.collectAsState()
    val isLoading by locationViewModel.isLoading.collectAsState()
    val userAlreadyInteracted by locationViewModel.userAlreadyInteracted.collectAsState()

    val title = if (isLoading) "Loading Details..." else location?.name ?: "Location Not Found"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                location is PlantDTO && publisher != null -> {
                    PlantDetailScreen(
                        location = location as PlantDTO,
                        publisher = publisher!!,
                        onGivePoints = locationViewModel::givePoints,
                        userAlreadyInteracted = userAlreadyInteracted
                    )
                }

                location is MushroomDTO && publisher != null -> {
                    MushroomDetailScreen(
                        location = location as MushroomDTO,
                        publisher = publisher!!,
                        onGivePoints = locationViewModel::givePoints,
                        userAlreadyInteracted = userAlreadyInteracted
                    )
                }

                location is PlantingSpotDTO && publisher != null -> {
                    PlantingSpotDetailScreen(
                        location = location as PlantingSpotDTO,
                        publisher = publisher!!,
                        onGivePoints = locationViewModel::givePoints,
                        userAlreadyInteracted = userAlreadyInteracted
                    )
                }

                else -> {
                    Text(
                        "Location details unavailable or ID mismatch. Type: ${location?.type ?: "Unknown"}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
