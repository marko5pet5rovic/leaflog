package com.markopetrovic.leaflog.ui.screens.map.components

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapPermissions(
    onPermissionsGranted: () -> Unit
): Boolean {
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionState) {
        if (locationPermissionState.allPermissionsGranted) {
            onPermissionsGranted()
        } else if (locationPermissionState.shouldShowRationale) {
            locationPermissionState.launchMultiplePermissionRequest()
        } else if (!locationPermissionState.allPermissionsGranted && !locationPermissionState.shouldShowRationale) {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

    return locationPermissionState.allPermissionsGranted
}