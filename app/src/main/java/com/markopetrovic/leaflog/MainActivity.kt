package com.markopetrovic.leaflog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.markopetrovic.leaflog.di.AppContainer
import com.markopetrovic.leaflog.navigation.Screen
import com.markopetrovic.leaflog.ui.screens.BottomNavContainer
import com.markopetrovic.leaflog.ui.screens.location.detail.LocationDetailScreen
import com.markopetrovic.leaflog.ui.screens.LoginScreen
import com.markopetrovic.leaflog.ui.screens.SignUpScreen
import com.markopetrovic.leaflog.ui.screens.WelcomeScreen
import com.markopetrovic.leaflog.ui.screens.location.create.AddNewPlantScreen
import com.markopetrovic.leaflog.ui.screens.location.create.AddNewMushroomScreen
import com.markopetrovic.leaflog.ui.screens.location.create.AddNewPlantingSpotScreen
import com.markopetrovic.leaflog.ui.theme.LeafLogTheme
import com.markopetrovic.leaflog.ui.viewmodels.MapViewModel
import com.markopetrovic.leaflog.ui.viewmodels.MapViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LeafLogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LeafLogAppNavigation()
                }
            }
        }
    }
}

@Composable
fun LeafLogAppNavigation() {
    val navController = rememberNavController()
    val mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(AppContainer.locationRepository)
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }

        composable(Screen.BottomNavContainer.route) {
            BottomNavContainer(rootNavController = navController, mapViewModel = mapViewModel)
        }

        composable(Screen.AddNewPlant.route) {
            AddNewPlantScreen(
                navController = navController,
                mapViewModel = mapViewModel
            )
        }

        composable(Screen.AddNewMushroom.route) {
            AddNewMushroomScreen(
                navController = navController,
                mapViewModel = mapViewModel
            )
        }

        composable(Screen.AddNewPlantingSpot.route) {
            AddNewPlantingSpotScreen(
                navController = navController,
                mapViewModel = mapViewModel
            )
        }

        composable(
            route = Screen.LocationDetail.route,
            arguments = Screen.LocationDetail.arguments
        ) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: return@composable
            LocationDetailScreen(navController = navController, locationId = locationId)
        }
    }
}