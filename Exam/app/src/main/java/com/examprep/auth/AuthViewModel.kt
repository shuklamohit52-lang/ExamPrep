package com.examprep.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object LoggedOut : AuthUiState
    data object Loading : AuthUiState
    data class LoggedIn(val uid: String, val email: String?) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.LoggedOut)
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        auth.currentUser?.let { user ->
            _state.value = AuthUiState.LoggedIn(user.uid, user.email)
        }
    }

    fun login(email: String, password: String) {
        _state.value = AuthUiState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                _state.value = AuthUiState.LoggedIn(result.user?.uid.orEmpty(), result.user?.email)
            }
            .addOnFailureListener {
                _state.value = AuthUiState.Error(it.message ?: "Login failed")
            }
    }

    fun signup(email: String, password: String) {
        _state.value = AuthUiState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                _state.value = AuthUiState.LoggedIn(result.user?.uid.orEmpty(), result.user?.email)
            }
            .addOnFailureListener {
                _state.value = AuthUiState.Error(it.message ?: "Signup failed")
            }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            _state.value = AuthUiState.LoggedOut
        }
    }
}
