// file: ui/components/map/MapComponent.kt (Понављам исправну верзију)

package com.markopetrovic.leaflog.ui.components.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.* // Обавезно укључите
import com.google.android.gms.maps.model.LatLng // Обавезно укључите

@Composable
fun MapComponent(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState, // МОРА БИТИ CameraPositionState
    isLocationEnabled: Boolean,
    content: @Composable () -> Unit // Садржај мапе за Маркере
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