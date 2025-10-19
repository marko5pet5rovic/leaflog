package com.markopetrovic.leaflog.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.markopetrovic.leaflog.ui.viewmodels.MapViewModel
import com.markopetrovic.leaflog.navigation.Screen
import com.markopetrovic.leaflog.navigation.bottomNavItems
import com.markopetrovic.leaflog.ui.screens.location.create.AddNewPlantScreen
import com.markopetrovic.leaflog.ui.screens.location.detail.LocationDetailScreen
import com.markopetrovic.leaflog.ui.screens.map.MapScreen
import com.markopetrovic.leaflog.ui.screens.profile.ProfileEditScreen
import com.markopetrovic.leaflog.ui.screens.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavContainer(
    rootNavController: NavController,
    mapViewModel: MapViewModel
) {
    val localNavController = androidx.navigation.compose.rememberNavController()
    val navBackStackEntry by localNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentTitle = bottomNavItems.firstOrNull { it.route == currentRoute }?.title ?: "LeafLog"

    val isBottomBarVisible = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                navigationIcon = {
                    if (localNavController.previousBackStackEntry != null) {
                        IconButton(onClick = { localNavController.navigateUp() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (currentRoute == Screen.BottomNavScreen.Map.route) {
                        IconButton(onClick = { mapViewModel.toggleFilterSheet(true) }) {
                            Icon(Icons.Default.List, contentDescription = "Filter Locations")
                        }
                    }
                    if (currentRoute == Screen.BottomNavScreen.Profile.route) {
                        IconButton(onClick = {
                            localNavController.navigate(Screen.ProfileEdit.route)
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
                        }
                    }
                }

            )
        },
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                localNavController.navigate(screen.route) {
                                    popUpTo(localNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = localNavController,
            startDestination = Screen.BottomNavScreen.Map.route,
            modifier = Modifier
        ) {
            composable(Screen.BottomNavScreen.Map.route) {
                MapScreen(
                    navController = rootNavController,
                    mapViewModel = mapViewModel,
                    paddingValues = paddingValues,
                    localNavController = localNavController
                )
            }

            composable(Screen.BottomNavScreen.Ranking.route) { backStackEntry ->
                RankingScreen(
                    navController = rootNavController,
                    paddingValues = paddingValues,
                    localNavController = localNavController
                )
            }

            composable(Screen.BottomNavScreen.Profile.route) {
                ProfileScreen(
                    navController = rootNavController,
                    paddingValues = paddingValues
                )
            }

            composable(
                route = Screen.LocationDetail.route,
                arguments = Screen.LocationDetail.arguments
            ) { backStackEntry ->
                val locationId = backStackEntry.arguments?.getString("locationId") ?: return@composable

                LocationDetailScreen(navController = localNavController, locationId = locationId)

            }

            composable(Screen.AddNewPlant.route) {
                AddNewPlantScreen(
                    navController = localNavController,
                    mapViewModel = mapViewModel
                )
            }

            composable(Screen.ProfileEdit.route) {
                ProfileEditScreen(navController = localNavController)
            }
        }
    }
}
