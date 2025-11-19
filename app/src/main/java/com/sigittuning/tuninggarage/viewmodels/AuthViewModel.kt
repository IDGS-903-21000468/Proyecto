package com.sigittuning.tuninggarage.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sigittuning.tuninggarage.data.models.LoginRequest
import com.sigittuning.tuninggarage.data.models.RegisterRequest
import com.sigittuning.tuninggarage.network.RetrofitClient
import com.sigittuning.tuninggarage.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Verificar si hay sesión activa
    fun checkLoginStatus(context: Context) {
        viewModelScope.launch {
            val tokenManager = TokenManager(context)
            tokenManager.token.collect { token ->
                _isLoggedIn.value = !token.isNullOrEmpty()
            }
        }
    }

    // Login
    fun login(email: String, password: String, context: Context) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                // Validaciones básicas
                if (email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Por favor completa todos los campos")
                    return@launch
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _authState.value = AuthState.Error("Email inválido")
                    return@launch
                }

                // Llamada al API
                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.login(LoginRequest(email, password))

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    if (authResponse.success && authResponse.token != null && authResponse.usuario != null) {
                        // Guardar token y datos del usuario
                        val tokenManager = TokenManager(context)
                        tokenManager.saveToken(
                            token = authResponse.token,
                            userId = authResponse.usuario.userID,
                            name = authResponse.usuario.nombre,
                            email = authResponse.usuario.email
                        )

                        _isLoggedIn.value = true
                        _authState.value = AuthState.Success("¡Bienvenido ${authResponse.usuario.nombre}!")
                    } else {
                        _authState.value = AuthState.Error(authResponse.message)
                    }
                } else {
                    _authState.value = AuthState.Error("Error: ${response.code()} - ${response.message()}")
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // Registro
    fun register(nombre: String, email: String, password: String, telefono: String?, context: Context) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                // Validaciones
                if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Por favor completa todos los campos obligatorios")
                    return@launch
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _authState.value = AuthState.Error("Email inválido")
                    return@launch
                }

                if (password.length < 6) {
                    _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
                    return@launch
                }

                // Llamada al API
                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.register(
                    RegisterRequest(nombre, email, password, telefono)
                )

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    if (authResponse.success && authResponse.token != null && authResponse.usuario != null) {
                        // Guardar token y datos del usuario
                        val tokenManager = TokenManager(context)
                        tokenManager.saveToken(
                            token = authResponse.token,
                            userId = authResponse.usuario.userID,
                            name = authResponse.usuario.nombre,
                            email = authResponse.usuario.email
                        )

                        _isLoggedIn.value = true
                        _authState.value = AuthState.Success("¡Cuenta creada exitosamente!")
                    } else {
                        _authState.value = AuthState.Error(authResponse.message)
                    }
                } else {
                    _authState.value = AuthState.Error("Error: ${response.code()} - ${response.message()}")
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // Logout
    fun logout(context: Context) {
        viewModelScope.launch {
            val tokenManager = TokenManager(context)
            tokenManager.clearToken()
            _isLoggedIn.value = false
            _authState.value = AuthState.Idle
        }
    }

    // Reset state
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}