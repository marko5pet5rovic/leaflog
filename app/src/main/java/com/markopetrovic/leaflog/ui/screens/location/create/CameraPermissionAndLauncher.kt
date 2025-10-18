package com.markopetrovic.leaflog.ui.screens.location.create

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionAndLauncher(
    onImageTaken: (String) -> Unit,
    isSaving: Boolean,
    imagePath: String?
) {
    val context = LocalContext.current
    val tempFile = remember { File(context.cacheDir, "temp_photo_mushroom.jpg") }
    val tempUri = remember {
        FileProvider.getUriForFile(context, context.packageName + ".fileprovider", tempFile)
    }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                onImageTaken(tempUri.toString())
            }
        }
    )

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
        Text(if (imagePath == null) "Сликај гљиву (Обавезно)" else "Слика поново сачувана")
    }
}