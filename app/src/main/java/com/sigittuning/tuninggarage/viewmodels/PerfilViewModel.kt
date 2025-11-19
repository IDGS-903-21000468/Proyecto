package com.sigittuning.tuninggarage.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sigittuning.tuninggarage.data.models.UserDto
import com.sigittuning.tuninggarage.network.RetrofitClient
import com.sigittuning.tuninggarage.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: UserDto) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

data class ActualizarPerfilRequest(
    val nombre: String,
    val telefono: String?
)

data class CambiarPasswordRequest(
    val passwordActual: String,
    val passwordNueva: String
)

class PerfilViewModel : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun cargarPerfil(context: Context) {
        viewModelScope.launch {
            try {
                _profileState.value = ProfileState.Loading

                val tokenManager = TokenManager(context)
                val userId = tokenManager.userId.first()
                val userName = tokenManager.userName.first()
                val userEmail = tokenManager.userEmail.first()

                if (userId != null && userName != null && userEmail != null) {
                    // TODO: Llamar al API para obtener datos completos del usuario
                    // val apiService = RetrofitClient.getApiService(context)
                    // val response = apiService.getMyProfile()

                    // Por ahora, usar datos del TokenManager
                    val user = UserDto(
                        userID = userId.toInt(),
                        nombre = userName,
                        email = userEmail,
                        telefono = null,
                        avatarURL = null,
                        fechaRegistro = "2024-01-01T00:00:00"
                    )
                    _profileState.value = ProfileState.Success(user)
                } else {
                    _profileState.value = ProfileState.Error("No se pudo cargar el perfil")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Error: ${e.message}")
            }
        }
    }

    fun actualizarPerfil(context: Context, nombre: String, telefono: String) {
        viewModelScope.launch {
            try {
                _actionMessage.value = "⏳ Actualizando perfil..."

                // TODO: Implementar llamada al API
                // val apiService = RetrofitClient.getApiService(context)
                // val response = apiService.updateProfile(ActualizarPerfilRequest(nombre, telefono))

                // Por ahora, simular éxito
                kotlinx.coroutines.delay(1000)

                _actionMessage.value = "✅ Perfil actualizado"
                cargarPerfil(context)

                kotlinx.coroutines.delay(2000)
                _actionMessage.value = null
            } catch (e: Exception) {
                _actionMessage.value = "❌ Error: ${e.message}"
                kotlinx.coroutines.delay(2000)
                _actionMessage.value = null
            }
        }
    }

    fun actualizarAvatar(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            try {
                _actionMessage.value = "⏳ Subiendo foto..."

                val apiService = RetrofitClient.getApiService(context)

                // Convertir Uri a bytes
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val fileBytes = inputStream?.readBytes()
                inputStream?.close()

                if (fileBytes != null) {
                    val requestFile = fileBytes.toRequestBody(
                        context.contentResolver.getType(imageUri)?.toMediaTypeOrNull()
                    )
                    val bodyPart = MultipartBody.Part.createFormData("file", "avatar.jpg", requestFile)

                    // Subir imagen
                    val uploadResponse = apiService.uploadImage(bodyPart)

                    if (uploadResponse.isSuccessful && uploadResponse.body() != null) {
                        // TODO: Actualizar avatar en el perfil del usuario
                        _actionMessage.value = "✅ Foto actualizada"
                        cargarPerfil(context)
                    } else {
                        _actionMessage.value = "❌ Error al subir foto"
                    }
                } else {
                    _actionMessage.value = "❌ No se pudo leer la imagen"
                }

                kotlinx.coroutines.delay(2000)
                _actionMessage.value = null
            } catch (e: Exception) {
                _actionMessage.value = "❌ Error: ${e.message}"
                kotlinx.coroutines.delay(2000)
                _actionMessage.value = null
            }
        }
    }

    fun cambiarPassword(context: Context, passwordActual: String, passwordNueva: String) {
        viewModelScope.launch {
            try {
                _actionMessage.value = "⏳ Cambiando contraseña..."

                // TODO: Implementar llamada al API
                // val apiService = RetrofitClient.getApiService(context)
                // val response = apiService.changePassword(CambiarPasswordRequest(passwordActual, passwordNueva))

                kotlinx.coroutines.delay(1000)

                _actionMessage.value = "✅ Contraseña actualizada"

                kotlinx.coroutines.delay(2000)
                _actionMessage.value = null
            } catch (e: Exception) {
                _actionMessage.value = "❌ Error: ${e.message}"
                kotlinx.coroutines.delay(2000)
                _actionMessage.value = null
            }
        }
    }

    fun cerrarSesion(context: Context) {
        viewModelScope.launch {
            val tokenManager = TokenManager(context)
            tokenManager.clearToken()
        }
    }
}