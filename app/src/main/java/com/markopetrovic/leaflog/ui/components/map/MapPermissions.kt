// file: ui/components/map/MapPermissions.kt (ИСПРАВЉЕНА ВЕРЗИЈА)

package com.markopetrovic.leaflog.ui.components.map

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Рукује тражењем дозвола за локацију и враћа стање дозвола.
 */
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

    // Кључна логика за тражење дозволе:
    LaunchedEffect(locationPermissionState) {
        if (locationPermissionState.allPermissionsGranted) {
            // СВЕ ДОЗВОЛЕ СУ ДОДЕЛЕНЕ
            onPermissionsGranted()
        } else if (locationPermissionState.shouldShowRationale) {
            // ПОТРЕБНО ЈЕ ОБЈАШЊЕЊЕ (Корисник је одбио, али није кликнуо "Never ask again")
            locationPermissionState.launchMultiplePermissionRequest()
        } else if (!locationPermissionState.allPermissionsGranted && !locationPermissionState.shouldShowRationale) {
            // ПРВО ПОКРЕТАЊЕ ИЛИ "Never ask again"
            // У већини случајева, лансирање у овом тренутку покрива почетно тражење.
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

    return locationPermissionState.allPermissionsGranted
}