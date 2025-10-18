package com.markopetrovic.leaflog.ui.screens.location.create

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.markopetrovic.leaflog.di.AppContainer
import com.markopetrovic.leaflog.ui.viewmodels.AddNewPlantingSpotViewModel
import com.markopetrovic.leaflog.ui.viewmodels.AddNewPlantingSpotViewModelFactory
import com.markopetrovic.leaflog.ui.viewmodels.MapViewModel
import com.markopetrovic.leaflog.ui.viewmodels.NavigationEvent
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewPlantingSpotScreen(
    navController: NavController,
    mapViewModel: MapViewModel
) {
    val viewModel: AddNewPlantingSpotViewModel = viewModel(
        factory = AddNewPlantingSpotViewModelFactory(
            AppContainer.locationRepository,
            mapViewModel
        )
    )

    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val isFenced by viewModel.isFenced.collectAsState()
    val soilType by viewModel.soilType.collectAsState()

    val isReadyToSave = name.isNotBlank() && description.isNotBlank() && soilType.isNotBlank()

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collectLatest { event ->
            if (event is NavigationEvent.PopBackStack) {
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Planting Spot 🌳") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = viewModel::updateName,
                label = { Text("Spot Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Description / Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            OutlinedTextField(
                value = soilType,
                onValueChange = viewModel::updateSoilType,
                label = { Text("Soil Type (e.g., Clay, Sandy)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isFenced,
                    onCheckedChange = { viewModel.toggleIsFenced() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Is the spot fenced/protected?")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = viewModel::savePlantingSpot,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isReadyToSave
            ) {
                Text("Save New Planting Spot")
            }
        }
    }
}