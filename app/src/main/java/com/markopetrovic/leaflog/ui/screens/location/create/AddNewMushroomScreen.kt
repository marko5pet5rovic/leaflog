package com.markopetrovic.leaflog.ui.screens.location.create

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.markopetrovic.leaflog.di.AppContainer
import com.markopetrovic.leaflog.ui.viewmodels.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewMushroomScreen(
    navController: NavController,
    mapViewModel: MapViewModel
) {
    val viewModel: AddNewMushroomViewModel = viewModel(
        factory = AddNewMushroomViewModelFactory(
            AppContainer.locationRepository,
            AppContainer.storageRepository,
            mapViewModel
        )
    )

    val imagePath by viewModel.imagePath.collectAsState()
    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val isEdible by viewModel.isEdible.collectAsState()
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
                title = { Text("New Mushroom 🍄") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, enabled = !isSaving) {
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
                label = { Text("Mushroom Name") },
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isEdible,
                    onCheckedChange = { viewModel.toggleIsEdible() },
                    enabled = !isSaving
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Is Edible?")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.saveMushroom(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isReadyToSave && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save New Mushroom")
                }
            }
        }
    }
}