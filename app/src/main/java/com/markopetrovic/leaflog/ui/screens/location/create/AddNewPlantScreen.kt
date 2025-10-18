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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.markopetrovic.leaflog.di.AppContainer
import com.markopetrovic.leaflog.ui.viewmodels.AddNewPlantViewModel
import com.markopetrovic.leaflog.ui.viewmodels.AddNewPlantViewModelFactory
import com.markopetrovic.leaflog.ui.viewmodels.MapViewModel
import com.markopetrovic.leaflog.ui.viewmodels.NavigationEvent
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewPlantScreen(
    navController: NavController,
    mapViewModel: MapViewModel
) {
    val viewModel: AddNewPlantViewModel = viewModel(
        factory = AddNewPlantViewModelFactory(
            AppContainer.locationRepository,
            mapViewModel,
            AppContainer.storageRepository
        )
    )

    val imagePath by viewModel.imagePath.collectAsState()
    val name by viewModel.name.collectAsState()
    val scientificName by viewModel.scientificName.collectAsState()
    val description by viewModel.description.collectAsState()
    val careTips by viewModel.careTips.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val isReadyToSave = name.isNotBlank() && description.isNotBlank() && imagePath != null
    val context = LocalContext.current

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
                title = { Text("New Rare Plant") },
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
            CameraPermissionAndLauncher(
                onImageTaken = viewModel::updateImagePath,
                isSaving = isSaving,
                imagePath = imagePath
            )

            OutlinedTextField(
                value = name,
                onValueChange = viewModel::updateName,
                label = { Text("Plant Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            )

            OutlinedTextField(
                value = scientificName,
                onValueChange = viewModel::updateScientificName,
                label = { Text("Scientific Name (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            )

            OutlinedTextField(
                value = description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                enabled = !isSaving
            )

            OutlinedTextField(
                value = careTips,
                onValueChange = viewModel::updateCareTips,
                label = { Text("Care Tips") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                enabled = !isSaving
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.savePlant(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isReadyToSave && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save New Plant")
                }
            }
        }
    }
}