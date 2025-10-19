package com.markopetrovic.leaflog.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.markopetrovic.leaflog.navigation.Screen
import com.markopetrovic.leaflog.ui.viewmodels.AuthViewModel
import com.markopetrovic.leaflog.ui.viewmodels.AuthState

@Composable
fun SignUpScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordError by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            navController.navigate(Screen.BottomNavContainer.route) {
                popUpTo(Screen.Welcome.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = if (it != confirmPassword && confirmPassword.isNotEmpty()) {
                    "Passwords do not match"
                } else {
                    ""
                }
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                passwordError = if (password != it) {
                    "Passwords do not match"
                } else {
                    ""
                }
            },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            isError = passwordError.isNotEmpty(),
            enabled = !isLoading
        )

        if (passwordError.isNotEmpty()) {
            Text(
                text = passwordError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                if (password != confirmPassword || password.isEmpty() || confirmPassword.isEmpty()) {
                    passwordError = "Passwords must match."
                } else {
                    passwordError = ""
                    authViewModel.signUp(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isLoading && passwordError.isEmpty() && email.isNotEmpty() && password.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Sign Up")
            }
        }

        TextButton(
            onClick = { navController.navigate(Screen.Login.route) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Already have an account? Log In")
        }

        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { authViewModel.clearError() }) { Text("Clear Error") }
        }
    }
}
