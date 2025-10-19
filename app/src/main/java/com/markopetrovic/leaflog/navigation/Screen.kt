package com.markopetrovic.leaflog.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val title: String = "",
    val icon: ImageVector? = null,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object SignUp : Screen("sign_up")

    object BottomNavContainer : Screen("bottom_nav_container")

    object AddNewPlant : Screen("add_new_plant")
    object AddNewMushroom : Screen("add_new_mushroom")
    object AddNewPlantingSpot : Screen("add_new_planting_spot")

    // Novo: ruta za ekran za uređivanje profila
    object ProfileEdit : Screen("profile_edit")

    object LocationDetail : Screen(
        route = "location_detail/{locationId}",
        arguments = listOf(
            navArgument("locationId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(locationId: String) = "location_detail/$locationId"
    }

    sealed class BottomNavScreen(
        route: String,
        title: String,
        icon: ImageVector
    ) : Screen(route, title, icon) {
        object Map : BottomNavScreen("map", "Map", Icons.Filled.LocationOn)
        object Ranking : BottomNavScreen("ranking", "Ranking", Icons.Filled.Star)
        object Profile : BottomNavScreen("profile", "Profile", Icons.Filled.Person)
    }
}

val bottomNavItems = listOf(
    Screen.BottomNavScreen.Map,
    Screen.BottomNavScreen.Ranking,
    Screen.BottomNavScreen.Profile,
)
