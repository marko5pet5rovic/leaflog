package com.markopetrovic.leaflog.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.markopetrovic.leaflog.navigation.Screen
import com.markopetrovic.leaflog.ui.viewmodels.AuthState
import com.markopetrovic.leaflog.ui.viewmodels.AuthViewModel

@Composable
fun WelcomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            navController.navigate(Screen.BottomNavContainer.route) {
                popUpTo(Screen.Welcome.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Leaflog",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 64.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            OutlinedButton(
                onClick = { navController.navigate(Screen.SignUp.route) },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Register")
            }

            Button(
                onClick = { navController.navigate(Screen.Login.route) },
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("Login")
            }
        }
    }
}
