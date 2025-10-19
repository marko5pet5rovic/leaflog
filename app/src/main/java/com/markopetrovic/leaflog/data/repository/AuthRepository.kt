package com.markopetrovic.leaflog.data.repository
import com.markopetrovic.leaflog.ui.viewmodels.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun signUp(email: String, password: String, username: String)

    suspend fun login(email: String, password: String)

    fun logout()

    fun clearError()

    fun checkAuthenticationStatus()
}