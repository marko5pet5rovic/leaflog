import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.markopetrovic.leaflog.views.theme.LeafLogTheme
import com.markopetrovic.leaflog.views.map.MapScreen
import com.markopetrovic.leaflog.views.leaderboard.LeaderboardScreen
import com.markopetrovic.leaflog.views.profile.ProfileScreen
import com.markopetrovic.leaflog.views.addlocation.AddLocationScreen

enum class Destination {
    Map,
    Add,
    Leaderboard,
    Profile,
}

@Composable
fun LeafLogApp() {
    var currentDestination by remember { mutableStateOf(Destination.Map) }

    Scaffold(
        bottomBar = {
            LeafLogBottomBar(
                currentDestination = currentDestination,
                onDestinationSelected = { currentDestination = it }
            )
        }
    ) { paddingValues ->
        when (currentDestination) {
            Destination.Map -> MapScreen(Modifier.padding(paddingValues))
            Destination.Add -> AddLocationScreen(Modifier.padding(paddingValues))
            Destination.Leaderboard -> LeaderboardScreen(Modifier.padding(paddingValues))
            Destination.Profile -> ProfileScreen(Modifier.padding(paddingValues))
        }
    }
}

@Composable
fun LeafLogBottomBar(
    currentDestination: Destination,
    onDestinationSelected: (Destination) -> Unit
) {
    BottomAppBar {
        val items = listOf(
            Pair(Destination.Map, Icons.Filled.LocationOn),
            Pair(Destination.Add, Icons.Filled.AddCircle),
            Pair(Destination.Leaderboard, Icons.AutoMirrored.Filled.List),
            Pair(Destination.Profile, Icons.Filled.Person)
        )

        items.forEach { (destination, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = destination.name) },
                label = { Text(destination.name },
                selected = currentDestination == destination,
                onClick = { onDestinationSelected(destination) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LeafLogAppPreview() {
    LeafLogTheme {
        LeafLogApp()
    }
}
