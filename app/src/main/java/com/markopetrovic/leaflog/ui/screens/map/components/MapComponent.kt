package com.markopetrovic.leaflog.ui.screens.map.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.*

@Composable
fun MapComponent(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
    isLocationEnabled: Boolean,
    content: @Composable () -> Unit
) {
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false
        ),
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = isLocationEnabled
        ),
        content = content
    )
}
