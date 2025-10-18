@file:OptIn(ExperimentalMaterial3Api::class)

package com.markopetrovic.leaflog.ui.screens.location.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.markopetrovic.leaflog.data.models.LocationBase
import com.markopetrovic.leaflog.data.models.PlantDTO
import com.markopetrovic.leaflog.data.models.MushroomDTO
import com.markopetrovic.leaflog.data.models.PlantingSpotDTO

@Composable
fun LocationDetailScreen(
    location: LocationBase,
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(location.name) },
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            when (location) {
                is PlantDTO -> {
                    PlantDetailScreen(plant = location)
                }
                is MushroomDTO -> {
                    MushroomDetailScreen(mushroom = location)
                }
                is PlantingSpotDTO -> {
                    PlantingSpotDetailScreen(plantingSpot = location)
                }
            }
        }
    }
}