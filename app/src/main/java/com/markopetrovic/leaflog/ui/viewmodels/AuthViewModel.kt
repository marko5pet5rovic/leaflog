package com.markopetrovic.leaflog.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        if (auth.currentUser != null) {
            _authState.value = AuthState.Authenticated(auth.currentUser!!.uid)
        }
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        _isLoading.value = false
                        if (task.isSuccessful) {
                            val uid = task.result?.user?.uid ?: throw Exception("Login successful but UID is null.")
                            _authState.value = AuthState.Authenticated(uid)
                        } else {
                            _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Unknown error")
                        }
                    }.await()
            } catch (e: Exception) {
                _isLoading.value = false
                _authState.value = AuthState.Error(e.localizedMessage ?: "Login attempt failed")
            }
        }
    }
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading

            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                if (result.user != null) {
                    _authState.value = AuthState.Authenticated(result.user!!.uid)
                } else {
                    _authState.value = AuthState.Error("Регистрација није успела. Непозната грешка.")
                }

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthWeakPasswordException -> "Лозинка мора имати најмање 6 карактера."
                    is FirebaseAuthInvalidCredentialsException -> "Неисправан формат е-поште."
                    is FirebaseAuthUserCollisionException -> "Корисник са том е-поштом већ постоји."
                    else -> "Грешка при регистрацији: ${e.localizedMessage}"
                }
                _authState.value = AuthState.Error(errorMessage)
                Log.e("AuthViewModel", "Sign Up Error: $e")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun clearError() {
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val uid: String) : AuthState()
    data class Error(val message: String) : AuthState()
}