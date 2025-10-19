package com.markopetrovic.leaflog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.markopetrovic.leaflog.data.models.ProfileDTO
import com.markopetrovic.leaflog.ui.viewmodels.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val profileRepository: ProfileRepository
) : AuthRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthenticationStatus()
    }

    override fun checkAuthenticationStatus() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _authState.value = if (user != null) {
                AuthState.Authenticated(user.uid)
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    override suspend fun signUp(email: String, password: String, username: String) {
        _authState.value = AuthState.Loading
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID not found after signup.")

            val newProfile = ProfileDTO(
                uid = uid,
                username = username,
                email = email,
                firstName = null,
                lastName = null
            )
            profileRepository.updateProfile(newProfile)

            _authState.value = AuthState.Authenticated(uid)
        } catch (e: FirebaseAuthException) {
            _authState.value = AuthState.Error(e.localizedMessage ?: "Sign up failed.")
        } catch (e: Exception) {
            _authState.value = AuthState.Error("An unexpected error occurred during profile creation.")
        }
    }

    override suspend fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID not found after login.")
            _authState.value = AuthState.Authenticated(uid)
        } catch (e: FirebaseAuthException) {
            _authState.value = AuthState.Error(e.localizedMessage ?: "Login failed.")
        }
    }

    override fun logout() {
        repositoryScope.launch {
            auth.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }

    override fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}