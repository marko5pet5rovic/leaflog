package com.markopetrovic.leaflog.views.profile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.markopetrovic.leaflog.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
) {
    // Definisanje ID-a za prosleđivanje (koristi se u Koin modulu)
    val dummyUserId = "CURRENT_USER_ID_PLACEHOLDER"
    
    // Povezivanje: Koin automatski instancira ProfileViewModel i injektuje Repozitorijum.
    val viewModel: ProfileViewModel = koinViewModel { 
        parametersOf(dummyUserId) 
    }
    
    // Pokretanje metode za učitavanje profila kada se ekran prvi put učita
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // Prikaz minimalnog teksta
    Text(text = "Profile Screen je spreman i povezan.")
}
