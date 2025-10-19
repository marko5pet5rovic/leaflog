@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.markopetrovic.leaflog.ui.screens.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.LocationServices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import android.location.Location
import com.google.maps.android.compose.*
import com.markopetrovic.leaflog.navigation.Screen
import com.markopetrovic.leaflog.ui.viewmodels.MapViewModel
import com.markopetrovic.leaflog.ui.screens.map.components.MapComponent
import com.markopetrovic.leaflog.ui.screens.map.components.MapPermissions
import com.markopetrovic.leaflog.ui.screens.map.components.LocationTypeSelectorSheet
import com.markopetrovic.leaflog.ui.screens.map.components.LocationType
import com.markopetrovic.leaflog.ui.screens.map.components.MapFilterBottomSheet
import com.markopetrovic.leaflog.data.models.PlantDTO
import com.markopetrovic.leaflog.data.models.MushroomDTO
import com.markopetrovic.leaflog.data.models.PlantingSpotDTO

@Composable
fun MapScreen(
    navController: NavController,
    mapViewModel: MapViewModel,
    paddingValues: PaddingValues,
    localNavController: NavController
) {
    val currentGpsLocation by mapViewModel.currentGpsLocation.collectAsState()
    val showBottomSheet by mapViewModel.showBottomSheet.collectAsState()
    val locations by mapViewModel.locations.collectAsState()
    val isLoading by mapViewModel.isLoading.collectAsState()
    val cameraPositionState by mapViewModel.cameraPosition.collectAsState()

    val activeFilter by mapViewModel.activeFilter.collectAsState()
    val showFilterSheet by mapViewModel.showFilterSheet.collectAsState()
    val radiusFilter by mapViewModel.radiusFilter.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val colorScheme = MaterialTheme.colorScheme

    val mapCameraState = rememberCameraPositionState {
        position = cameraPositionState
    }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isLocationPermissionGranted by remember { mutableStateOf(false) }

    val gpsMarkerState = rememberMarkerState(position = currentGpsLocation)

    LaunchedEffect(currentGpsLocation) {
        if (currentGpsLocation.latitude != 0.0 || currentGpsLocation.longitude != 0.0) {
            gpsMarkerState.position = currentGpsLocation
        }
    }

    val allPermissionsGranted = MapPermissions {
        isLocationPermissionGranted = true
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    mapViewModel.updateGpsLocation(LatLng(it.latitude, it.longitude))
                }
            }
        } catch (e: SecurityException) { /* Handle security exception */ }
    }
    LaunchedEffect(allPermissionsGranted) {
        if (allPermissionsGranted) isLocationPermissionGranted = true
    }

    LaunchedEffect(mapViewModel.uiEvent) {
        mapViewModel.uiEvent.collect { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        MapComponent(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = mapCameraState,
            isLocationEnabled = isLocationPermissionGranted,
            content = {
                if (currentGpsLocation.latitude != 0.0 || currentGpsLocation.longitude != 0.0) {

                    key("RadiusCircle") {
                        Circle(
                            center = currentGpsLocation,
                            radius = radiusFilter.toDouble(),
                            strokeColor = colorScheme.primary,
                            strokeWidth = 4f,
                            fillColor = colorScheme.primary.copy(alpha = 0.15f)
                        )
                    }

                    Marker(
                        state = gpsMarkerState,
                        title = "Your location (GPS)",
                        snippet = "Current position",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                locations.forEach { location ->
                    key(location.id) {
                        val latLng = LatLng(location.latitude, location.longitude)

                        val markerColor = when (location) {
                            is PlantDTO -> BitmapDescriptorFactory.HUE_GREEN
                            is MushroomDTO -> BitmapDescriptorFactory.HUE_YELLOW
                            is PlantingSpotDTO -> BitmapDescriptorFactory.HUE_BLUE
                            else -> BitmapDescriptorFactory.HUE_RED
                        }

                        Marker(
                            state = rememberMarkerState(position = latLng),
                            title = location.name,
                            snippet = "Details: ${location.description}",
                            icon = BitmapDescriptorFactory.defaultMarker(markerColor),
                            onClick = {
                                val route = Screen.LocationDetail.createRoute(location.id)
                                localNavController.navigate(route)
                                true
                            }
                        )
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = { mapViewModel.toggleFilterSheet(true) },
                modifier = Modifier.padding(bottom = 8.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Filled.FilterList, contentDescription = "Toggle Filters")
            }

            FloatingActionButton(
                onClick = { mapViewModel.toggleBottomSheet(true) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add New Location")
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }
    }


    LocationTypeSelectorSheet(
        isVisible = showBottomSheet,
        onDismiss = {
            mapViewModel.toggleBottomSheet(false)
        },
        onTypeSelected = { type ->
            mapViewModel.setTempPlantLocation(currentGpsLocation)

            val route = when (type) {
                LocationType.PLANT -> Screen.AddNewPlant.route
                LocationType.MUSHROOM -> Screen.AddNewMushroom.route
                LocationType.PLANTING_SPOT -> Screen.AddNewPlantingSpot.route
            }

            navController.navigate(route)
            mapViewModel.toggleBottomSheet(false)
        }
    )

    if (showFilterSheet) {
        MapFilterBottomSheet(
            activeFilter = activeFilter,
            onFilterSelected = { filter ->
                mapViewModel.setFilter(filter)
            },
            currentRadius = radiusFilter,
            onRadiusChanged = { newRadius ->
                mapViewModel.setRadiusFilter(newRadius)
            },
            onDismiss = {
                mapViewModel.toggleFilterSheet(false)
            }
        )
    }
}
