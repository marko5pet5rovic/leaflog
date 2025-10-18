package com.markopetrovic.leaflog.ui.screens.location.detail

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.markopetrovic.leaflog.data.models.PlantDTO
import com.markopetrovic.leaflog.di.AppContainer
import com.markopetrovic.leaflog.ui.screens.common.DetailRow
import com.markopetrovic.leaflog.ui.screens.common.UserTile // <--- Uključen import za UserTile
import com.markopetrovic.leaflog.ui.viewmodels.PlantOnlyDetailViewModel
import com.markopetrovic.leaflog.ui.viewmodels.PlantOnlyDetailViewModelFactory

@Composable
fun PlantDetailScreen(
    plant: PlantDTO // Samo jedan input: PlantDTO
) {
    val viewModel: PlantOnlyDetailViewModel = viewModel(
        factory = PlantOnlyDetailViewModelFactory(
            initialLocation = plant,
            locationRepository = AppContainer.locationRepository,
            authRepository = AppContainer.authRepository,
            profileRepository = AppContainer.profileRepository
        )
    )

    val currentPlant by viewModel.location.collectAsState()
    val userAlreadyInteracted by viewModel.userAlreadyInteracted.collectAsState()
    val currentPublisher by viewModel.publisher.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 120.dp)
    ) {
        // 1. Plant Image (Header)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        ) {
            val painter = rememberAsyncImagePainter(model = currentPlant.imageUrl)
            Image(
                painter = painter,
                contentDescription = "Photo of ${currentPlant.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (currentPlant.imageUrl.isNullOrBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Image Available", color = Color.White)
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // 2. Name and Scientific Name
            Text(text = currentPlant.name, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            currentPlant.scientificName.takeIf { it.isNotBlank() }?.let {
                Text(text = "(${it})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Current Points Display
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "Total Points", tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${currentPlant.points} Points",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Publisher ListTile (User who posted it)
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Logged by:", style = MaterialTheme.typography.labelMedium)

            // Koristi se UserTile sa kontrolisanim stanjem učitavanja
            currentPublisher?.let {
                UserTile(publisher = it)
            } ?: Text("Loading publisher...", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Description and Care Tips
            DetailRow(title = "Description", content = currentPlant.description)
            Spacer(modifier = Modifier.height(12.dp))
            if (currentPlant.careTips.isNotBlank()) {
                DetailRow(title = "Care Tips", content = currentPlant.careTips)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 5. Coordinates
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Coordinates", style = MaterialTheme.typography.titleSmall)
                    Text(text = "Lat: ${currentPlant.latitude}, Lon: ${currentPlant.longitude}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    // 6. Sticky Bottom Bar for Points Action
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // Points Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Current Points
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Total Points", tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${currentPlant.points} Points", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                }

                // Give Points Button
                Button(
                    onClick = viewModel::givePoints,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    enabled = !userAlreadyInteracted
                ) {
                    Icon(Icons.Default.ThumbUp, contentDescription = "Give Points")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Support")
                }
            }
        }
    }
}