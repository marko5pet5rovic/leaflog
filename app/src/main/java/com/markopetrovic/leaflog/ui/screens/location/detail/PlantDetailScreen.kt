@file:OptIn(ExperimentalMaterial3Api::class)

package com.markopetrovic.leaflog.ui.screens.location.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.markopetrovic.leaflog.data.models.PlantDTO
import com.markopetrovic.leaflog.data.models.ProfileDTO
import com.markopetrovic.leaflog.ui.screens.common.DetailRow
import com.markopetrovic.leaflog.ui.screens.common.UserTile

@Composable
fun PlantDetailScreen(
    location: PlantDTO,
    publisher: ProfileDTO,
    onGivePoints: () -> Unit,
    userAlreadyInteracted: Boolean
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 120.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        ) {
            val painter = rememberAsyncImagePainter(model = location.imageUrl)
            Image(
                painter = painter,
                contentDescription = "Photo of ${location.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (location.imageUrl.isNullOrBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Image Available", color = Color.White)
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = location.name, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            location.scientificName.takeIf { it.isNotBlank() }?.let {
                Text(text = "(${it})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "Total Points", tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${location.points} Points",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Logged by:", style = MaterialTheme.typography.labelMedium)
            UserTile(publisher = publisher)
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(title = "Description", content = location.description)
            Spacer(modifier = Modifier.height(12.dp))
            if (location.careTips.isNotBlank()) {
                DetailRow(title = "Care Tips", content = location.careTips)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 5. Coordinates
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Coordinates", style = MaterialTheme.typography.titleSmall)
                    Text(text = "Lat: ${location.latitude}, Lon: ${location.longitude}", style = MaterialTheme.typography.bodyMedium)
                }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Total Points", tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${location.points} Points", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                }

                Button(
                    onClick = onGivePoints,
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
