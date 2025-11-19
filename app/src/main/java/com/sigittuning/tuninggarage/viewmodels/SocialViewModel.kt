package com.sigittuning.tuninggarage.viewmodels

import android.content.Context
import android.net.Uri // NUEVO: Import para Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sigittuning.tuninggarage.data.models.* // Asegúrate que ApiResponse esté aquí
import com.sigittuning.tuninggarage.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// NUEVO: Imports para subida de archivos
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// Modelos para Social (como los tenías en tu archivo original)
data class CreateSocialPostRequest(
    val titulo: String? = null,
    val descripcion: String,
    val imagenURL: String? = null
)

data class SocialPostDto(
    val postID: Int,
    val userID: Int,
    val usuarioNombre: String,
    val usuarioAvatar: String?,
    val titulo: String?,
    val descripcion: String?,
    val imagenURL: String?,
    val fechaPublicacion: String,
    val totalLikes: Int,
    val totalComentarios: Int,
    val usuarioLeDioLike: Boolean,
    val tiempoTranscurrido: String
)

data class CreateCommentRequest(
    val textoComentario: String
)

data class CommentDto(
    val commentID: Int,
    val userID: Int,
    val usuarioNombre: String,
    val usuarioAvatar: String?,
    val textoComentario: String,
    val fechaComentario: String,
    val tiempoTranscurrido: String
)

sealed class SocialPostsState {
    object Loading : SocialPostsState()
    data class Success(val posts: List<SocialPostDto>) : SocialPostsState()
    data class Error(val message: String) : SocialPostsState()
}

class SocialViewModel : ViewModel() {

    private val _postsState = MutableStateFlow<SocialPostsState>(SocialPostsState.Loading)
    val postsState: StateFlow<SocialPostsState> = _postsState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    // Cargar publicaciones
    fun loadPosts(context: Context) {
        viewModelScope.launch {
            try {
                _postsState.value = SocialPostsState.Loading

                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.getSocialPosts()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        _postsState.value = SocialPostsState.Success(apiResponse.data)
                    } else {
                        _postsState.value = SocialPostsState.Error(apiResponse.message)
                    }
                } else {
                    _postsState.value = SocialPostsState.Error("Error al cargar publicaciones")
                }
            } catch (e: Exception) {
                _postsState.value = SocialPostsState.Error("Error: ${e.message}")
            }
        }
    }

    // MODIFICADO: Crear publicación (ahora acepta Uri?)
    fun createPost(context: Context, titulo: String?, descripcion: String, imagenUri: Uri?) {
        viewModelScope.launch {
            try {
                _actionMessage.value = "⏳ Publicando..." // Mensaje inicial
                val apiService = RetrofitClient.getApiService(context)

                var finalImageUrl: String? = null

                // NUEVO: Lógica para subir la imagen PRIMERO
                if (imagenUri != null) {
                    // 1. Llamar a la nueva función de subida
                    finalImageUrl = uploadImageAndGetUrl(context, imagenUri)

                    // 2. Comprobar si la subida de imagen falló
                    if (finalImageUrl == null) {
                        _actionMessage.value = "❌ Error al subir la imagen"
                        kotlinx.coroutines.delay(2000)
                        _actionMessage.value = null
                        return@launch // Detener si la imagen no se subió
                    }
                }

                // 3. (Lógica anterior) Crear el post con la URL de la imagen (si existe)
                val response = apiService.createSocialPost(
                    CreateSocialPostRequest(
                        titulo,
                        descripcion,
                        finalImageUrl
                    ) // Se usa la URL devuelta
                )

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _actionMessage.value = "✅ Publicación creada"
                        loadPosts(context) // Recargar posts
                    } else {
                        _actionMessage.value = "❌ ${apiResponse.message}"
                    }
                } else {
                    _actionMessage.value = "❌ Error al crear publicación"
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

    // NUEVO: Función privada para subir la imagen y obtener la URL
    private suspend fun uploadImageAndGetUrl(context: Context, imageUri: Uri): String? {
        return try {
            val apiService = RetrofitClient.getApiService(context)

            // 1. Convertir Uri a bytes
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val fileBytes = inputStream?.readBytes()
            inputStream?.close()

            if (fileBytes == null) {
                _actionMessage.value = "❌ No se pudo leer el archivo"
                return null
            }

            // 2. Crear el RequestBody para la subida
            val requestFile = fileBytes.toRequestBody(
                context.contentResolver.getType(imageUri)?.toMediaTypeOrNull()
            )

            // 3. Crear el MultipartBody.Part (el backend C# espera "file")
            val bodyPart = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)

            // 4. Llamar al endpoint de subida de imagen
            val uploadResponse = apiService.uploadImage(bodyPart)

            if (uploadResponse.isSuccessful && uploadResponse.body() != null) {
                // 5. Devolver la URL si la subida fue exitosa
                uploadResponse.body()?.url
            } else {
                _actionMessage.value = "❌ Error del servidor al subir imagen"
                null
            }
        } catch (e: Exception) {
            _actionMessage.value = "❌ Error: ${e.message}"
            null
        }
    }


    // Toggle like (Sin cambios)
    fun toggleLike(context: Context, postId: Int) {
        viewModelScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.toggleLike(postId)

                if (response.isSuccessful && response.body() != null) {
                    loadPosts(context) // Recargar posts para actualizar el like
                }
            } catch (e: Exception) {
                _actionMessage.value = "❌ Error: ${e.message}"
                kotlinx.coroutines.delay(2000)
                _actionMessage.value = null
            }
        }
    }

    // Agregar comentario (Sin cambios)
    fun addComment(context: Context, postId: Int, texto: String) {
        viewModelScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.createComment(postId, CreateCommentRequest(texto))

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _actionMessage.value = "✅ Comentario agregado"
                        loadPosts(context) // Recargar para mostrar el nuevo comentario
                    } else {
                        _actionMessage.value = "❌ ${apiResponse.message}"
                    }
                } else {
                    _actionMessage.value = "❌ Error al comentar"
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

    // AGREGAR estos estados y funciones a tu SocialViewModel.kt existente:

    // Estados para comentarios
    sealed class CommentsState {
        object Loading : CommentsState()
        data class Success(val comments: List<CommentDto>) : CommentsState()
        data class Error(val message: String) : CommentsState()
    }

    private val _commentsState = MutableStateFlow<CommentsState>(CommentsState.Loading)
    val commentsState: StateFlow<CommentsState> = _commentsState.asStateFlow()

    // Cargar comentarios de un post
    fun loadComments(context: Context, postId: Int) {
        viewModelScope.launch {
            try {
                _commentsState.value = CommentsState.Loading

                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.getComments(postId)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        _commentsState.value = CommentsState.Success(apiResponse.data)
                    } else {
                        _commentsState.value = CommentsState.Error(apiResponse.message)
                    }
                } else {
                    _commentsState.value = CommentsState.Error("Error al cargar comentarios")
                }
            } catch (e: Exception) {
                _commentsState.value = CommentsState.Error("Error: ${e.message}")
            }
        }
    }
}