package com.markopetrovic.leaflog.ui.screens.location.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.markopetrovic.leaflog.data.models.PlantingSpotDTO
import com.markopetrovic.leaflog.di.AppContainer
import com.markopetrovic.leaflog.ui.screens.common.DetailRow
import com.markopetrovic.leaflog.ui.screens.common.UserTile
import com.markopetrovic.leaflog.ui.viewmodels.PlantingSpotDetailViewModel
import com.markopetrovic.leaflog.ui.viewmodels.PlantingSpotDetailViewModelFactory

@Composable
fun PlantingSpotDetailScreen(
    plantingSpot: PlantingSpotDTO
) {
    val viewModel: PlantingSpotDetailViewModel = viewModel(
        factory = PlantingSpotDetailViewModelFactory(
            initialLocation = plantingSpot,
            locationRepository = AppContainer.locationRepository,
            authRepository = AppContainer.authRepository,
            profileRepository = AppContainer.profileRepository
        )
    )

    val currentSpot by viewModel.location.collectAsState()
    val userAlreadyInteracted by viewModel.userAlreadyInteracted.collectAsState()
    val currentPublisher by viewModel.publisher.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = currentSpot.name,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Planting Spot",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Divider()
        Spacer(modifier = Modifier.height(8.dp))
        Text("Logged by:", style = MaterialTheme.typography.labelMedium)

        currentPublisher?.let {
            UserTile(publisher = it)
        } ?: Text("Loading publisher...", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Soil Type: ${currentSpot.soilType.ifBlank { "Not Defined" }}",
            style = MaterialTheme.typography.titleMedium
        )
        DetailRow(title = "Location Description", content = currentSpot.description)

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Coordinates", style = MaterialTheme.typography.titleSmall)
                Text(text = "Lat: ${currentSpot.latitude}, Lon: ${currentSpot.longitude}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Total Points", tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${currentSpot.points} Points", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.tertiary)
                }

                Button(
                    onClick = viewModel::givePoints,
                    enabled = !userAlreadyInteracted,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.ThumbUp, contentDescription = "Give Points")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Support")
                }
            }
        }
    }
}