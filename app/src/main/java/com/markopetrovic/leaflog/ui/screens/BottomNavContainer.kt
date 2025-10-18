// file: ui/screens/BottomNavContainer.kt (ФИНАЛНА ИЗМЕНА)

package com.markopetrovic.leaflog.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavContainer(
    rootNavController: NavController,
    mapViewModel: MapViewModel
) {
    // localNavController за навигацију унутар Bottom Nav-а
    val localNavController = androidx.navigation.compose.rememberNavController()
    val navBackStackEntry by localNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentTitle = bottomNavItems.firstOrNull { it.route == currentRoute }?.title ?: "LeafLog"

    // Decide whether to show the Bottom Bar (it should be visible on primary routes)
    val isBottomBarVisible = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                // Navigation icon logic remains, allowing back navigation within the BottomNav stack
                navigationIcon = {
                    if (localNavController.previousBackStackEntry != null) {
                        IconButton(onClick = { localNavController.navigateUp() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (currentRoute == Screen.BottomNavScreen.Map.route) {
                        // Само на MapScreen-у приказујемо филтер
                        IconButton(onClick = { mapViewModel.toggleFilterSheet(true) }) {
                            Icon(Icons.Default.List, contentDescription = "Filter Locations")
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
        // NavHost for Bottom Navigation routes
        NavHost(
            navController = localNavController, // Користимо локални контролер
            startDestination = Screen.BottomNavScreen.Map.route,
            modifier = Modifier
        ) {
            composable(Screen.BottomNavScreen.Map.route) {
                MapScreen(
                    navController = rootNavController, // Главни контролер
                    mapViewModel = mapViewModel,
                    paddingValues = paddingValues,
                    localNavController = localNavController // Прослеђујемо локални контролер
                )
            }

            composable(Screen.BottomNavScreen.Ranking.route) { backStackEntry -> // КЉУЧНА ИСПРАВКА 1: Експлицитно ухвати backStackEntry
                RankingScreen(
                    navController = rootNavController,
                    paddingValues = paddingValues,
                    // КЉУЧНА ИСПРАВКА 2: Користимо backStackEntry за dobijanje NavController-а
                    localNavController = localNavController // localNavController је исти као backStackEntry.navController
                )
            }

            composable(Screen.BottomNavScreen.Profile.route) {
                ProfileScreen(
                    navController = rootNavController,
                    paddingValues = paddingValues
                )
            }

            // Routes that are not in the bottom bar, but are called from within it
            composable(
                route = Screen.LocationDetail.route, // "location_detail/{locationId}"
                // ОВДЕ СЕ РЕШАВА ГРЕШКА: arguments сада постоји у Screen.LocationDetail
                arguments = Screen.LocationDetail.arguments
            ) { backStackEntry ->
                val locationId = backStackEntry.arguments?.getString("locationId") ?: return@composable

                // Користимо localNavController за излаз из BottomNavContainer-а
                LocationDetailScreen(navController = localNavController, locationId = locationId)

            }

            composable(Screen.AddNewPlant.route) {
                AddNewPlantScreen(
                    navController = localNavController,
                    mapViewModel = mapViewModel
                ) // КЉУЧНА ИСПРАВКА 4: Користимо localNavController
            }
        }
    }
}