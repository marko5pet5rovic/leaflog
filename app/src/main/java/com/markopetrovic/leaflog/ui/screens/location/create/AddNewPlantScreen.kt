@file:OptIn(ExperimentalPermissionsApi::class)

package com.markopetrovic.leaflog.ui.screens.location.create

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.rememberPermissionState
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import java.io.File
import androidx.lifecycle.viewmodel.compose.viewModel
import com.markopetrovic.leaflog.di.AppContainer
import com.markopetrovic.leaflog.ui.viewmodels.location.create.AddNewPlantViewModel
import com.markopetrovic.leaflog.ui.viewmodels.location.create.AddNewPlantViewModelFactory
import com.markopetrovic.leaflog.ui.viewmodels.MapViewModel
import androidx.compose.ui.Alignment

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
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

    val tempFile = remember { File(context.cacheDir, "temp_photo.jpg") }
    val tempUri = remember {
        FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            tempFile
        )
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                viewModel.updateImagePath(tempUri.toString())
            }
        }
    )

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

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
            OutlinedButton(
                onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        cameraLauncher.launch(tempUri)
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text(
                    if (imagePath == null) "Take Plant Photo (Required)" else "Photo Saved (Retake)"
                )
            }

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
                modifier = Modifier.fillMaxWidth().height(100.dp),
                enabled = !isSaving
            )

            OutlinedTextField(
                value = careTips,
                onValueChange = viewModel::updateCareTips,
                label = { Text("Care Tips") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                enabled = !isSaving
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.savePlant(context) {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isReadyToSave && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save New Plant")
                }
            }
        }
    }
}
