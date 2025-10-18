@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.markopetrovic.leaflog.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.LocationServices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import android.location.Location
import com.google.maps.android.compose.*
import com.markopetrovic.leaflog.di.AppContainer
import com.markopetrovic.leaflog.navigation.AppNavigator
import com.markopetrovic.leaflog.navigation.Screen
import com.markopetrovic.leaflog.ui.viewmodels.MapViewModel
import com.markopetrovic.leaflog.ui.viewmodels.MapViewModelFactory
import com.markopetrovic.leaflog.ui.viewmodels.LocationFilter
import com.markopetrovic.leaflog.ui.components.map.MapComponent
import com.markopetrovic.leaflog.ui.components.map.MapPermissions
import com.markopetrovic.leaflog.ui.components.map.LocationTypeSelectorSheet
import com.markopetrovic.leaflog.ui.components.map.LocationType
import com.markopetrovic.leaflog.ui.components.MapFilterBottomSheet
import com.markopetrovic.leaflog.data.models.PlantDTO
import com.markopetrovic.leaflog.data.models.MushroomDTO
import com.markopetrovic.leaflog.data.models.PlantingSpotDTO

@Composable
fun MapScreen(
    navController: NavController,
    paddingValues: PaddingValues
) {
    val mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(AppContainer.locationRepository)
    )

    val appNavigator: AppNavigator = AppContainer.appNavigator

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
        } catch (e: SecurityException) { }
    }
    LaunchedEffect(allPermissionsGranted) {
        if (allPermissionsGranted) isLocationPermissionGranted = true
    }

    LaunchedEffect(mapViewModel.uiEvent) {
        mapViewModel.uiEvent.collect { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }

    val filteredLocations = remember(locations, activeFilter, radiusFilter, currentGpsLocation) {
        locations.filter { location ->
            val typeMatches = when (activeFilter) {
                LocationFilter.ALL -> true
                LocationFilter.PLANTS -> location is PlantDTO
                LocationFilter.MUSHROOMS -> location is MushroomDTO
                LocationFilter.PLANTING_SPOTS -> location is PlantingSpotDTO
            }
            val radiusMatches = mapViewModel.isLocationWithinRadius(location)
            typeMatches && radiusMatches
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(paddingValues)
    ) {
        MapComponent(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = mapCameraState,
            isLocationEnabled = isLocationPermissionGranted,
            content = {
                if (currentGpsLocation.latitude != 0.0 || currentGpsLocation.longitude != 0.0) {
                    Circle(
                        center = currentGpsLocation,
                        radius = radiusFilter.toDouble(),
                        strokeColor = colorScheme.primary,
                        strokeWidth = 4f,
                        fillColor = colorScheme.primary.copy(alpha = 0.15f)
                    )
                    Marker(
                        state = gpsMarkerState,
                        title = "Your location (GPS)",
                        snippet = "Current position",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                filteredLocations.forEach { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    val markerColor = when (location) {
                        is PlantDTO -> BitmapDescriptorFactory.HUE_GREEN
                        is MushroomDTO -> BitmapDescriptorFactory.HUE_YELLOW
                        is PlantingSpotDTO -> BitmapDescriptorFactory.HUE_BLUE
                    }

                    Marker(
                        state = rememberMarkerState(position = latLng),
                        title = location.name,
                        snippet = "Details: ${location.description}",
                        icon = BitmapDescriptorFactory.defaultMarker(markerColor),
                        onClick = {
                            val route = Screen.LocationDetail.createRoute(location.id)
                            navController.navigate(route)
                            true
                        }
                    )
                }
            }
        )

        FloatingActionButton(
            onClick = { mapViewModel.toggleBottomSheet(true) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add New Location")
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
            )
        }
    }

    LocationTypeSelectorSheet(
        isVisible = showBottomSheet,
        onDismiss = { mapViewModel.toggleBottomSheet(false) },
        onTypeSelected = { type ->
            mapViewModel.setTempPlantLocation(currentGpsLocation)
            val route = when (type) {
                LocationType.PLANT -> Screen.AddNewPlant.route
                LocationType.MUSHROOM -> Screen.AddNewMushroom.route
                LocationType.PLANTING_SPOT -> Screen.AddNewPlantingSpot.route
            }
            appNavigator.navigate(route)
            mapViewModel.toggleBottomSheet(false)
        }
    )

    if (showFilterSheet) {
        MapFilterBottomSheet(
            activeFilter = activeFilter,
            onFilterSelected = { filter -> mapViewModel.setFilter(filter) },
            currentRadius = radiusFilter,
            onRadiusChanged = { newRadius -> mapViewModel.setRadiusFilter(newRadius) },
            onDismiss = { mapViewModel.toggleFilterSheet(false) }
        )
    }
}
