package com.sigittuning.tuninggarage.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sigittuning.tuninggarage.data.models.ApiResponse
import com.sigittuning.tuninggarage.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

// DTOs para Chat
data class ChatDto(
    val chatID: Int,
    val listingID: Int,
    val listingTitulo: String,
    val otroUsuarioID: Int,
    val otroUsuarioNombre: String,
    val otroUsuarioAvatar: String?,
    val mensajes: List<ChatMessageDto>
)

data class ChatMessageDto(
    val messageID: Int,
    val senderUserID: Int,
    val senderNombre: String,
    val mensaje: String,
    val fechaEnvio: Date,
    val esPropio: Boolean
)

data class CreateChatMessageDto(
    val mensaje: String
)

sealed class ChatState {
    object Loading : ChatState()
    data class Success(val chat: ChatDto) : ChatState()
    data class Error(val message: String) : ChatState()
}

class ChatViewModel : ViewModel() {

    private val _chatState = MutableStateFlow<ChatState>(ChatState.Loading)
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    // Iniciar o cargar chat
    fun initializeChat(context: Context, listingId: Int) {
        viewModelScope.launch {
            try {
                _chatState.value = ChatState.Loading

                val apiService = RetrofitClient.getApiService(context)

                // Primero, intentar iniciar el chat
                val initResponse = apiService.initiateChat(listingId)

                if (initResponse.isSuccessful && initResponse.body() != null) {
                    val apiResponse = initResponse.body()!!

                    if (apiResponse.success && apiResponse.data != null) {
                        val chatId = apiResponse.data

                        // Luego cargar los mensajes
                        loadChatMessages(context, chatId)
                    } else {
                        _chatState.value = ChatState.Error(apiResponse.message)
                    }
                } else {
                    _chatState.value = ChatState.Error("Error al iniciar chat")
                }
            } catch (e: Exception) {
                _chatState.value = ChatState.Error("Error: ${e.message}")
            }
        }
    }

    // Cargar mensajes del chat
    private suspend fun loadChatMessages(context: Context, chatId: Int) {
        try {
            val apiService = RetrofitClient.getApiService(context)
            val response = apiService.getChatMessages(chatId)

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!

                if (apiResponse.success && apiResponse.data != null) {
                    _chatState.value = ChatState.Success(apiResponse.data)
                } else {
                    _chatState.value = ChatState.Error(apiResponse.message)
                }
            } else {
                _chatState.value = ChatState.Error("Error al cargar mensajes")
            }
        } catch (e: Exception) {
            _chatState.value = ChatState.Error("Error: ${e.message}")
        }
    }

    // Enviar mensaje
    fun sendMessage(context: Context, chatId: Int, mensaje: String) {
        viewModelScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.sendChatMessage(chatId, CreateChatMessageDto(mensaje))

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!

                    if (apiResponse.success) {
                        // Recargar mensajes
                        loadChatMessages(context, chatId)
                        _actionMessage.value = "✅ Mensaje enviado"
                    } else {
                        _actionMessage.value = "❌ ${apiResponse.message}"
                    }
                } else {
                    _actionMessage.value = "❌ Error al enviar mensaje"
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
}