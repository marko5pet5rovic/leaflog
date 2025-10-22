@file:OptIn(ExperimentalMaterial3Api::class)

package com.markopetrovic.leaflog.ui.screens.location.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markopetrovic.leaflog.data.models.PlantingSpotDTO
import com.markopetrovic.leaflog.data.models.ProfileDTO
import com.markopetrovic.leaflog.ui.screens.common.DetailRow
import com.markopetrovic.leaflog.ui.screens.common.UserTile

@Composable
fun PlantingSpotDetailScreen(
    location: PlantingSpotDTO,
    publisher: ProfileDTO,
    onGivePoints: () -> Unit,
    userAlreadyInteracted: Boolean
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = location.name,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Planting spot",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Divider()
        Spacer(modifier = Modifier.height(8.dp))
        Text("Logged by:", style = MaterialTheme.typography.labelMedium)
        UserTile(publisher = publisher)
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Soil type: ${location.soilType.ifBlank { "Unknown" }}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (location.fenced) "Location is fenced!" else "Location is not fenced",
            style = MaterialTheme.typography.titleMedium,
            color = if (location.fenced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        DetailRow(title = "Description", content = location.description)

        Spacer(modifier = Modifier.height(24.dp))


        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Coordinates", style = MaterialTheme.typography.titleSmall)
                Text(text = "Lat: ${location.latitude} \nLon: ${location.longitude}", style = MaterialTheme.typography.bodyMedium)
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
                    Text(text = "${location.points} Points", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.tertiary)
                }

                Button(
                    onClick = onGivePoints,
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
