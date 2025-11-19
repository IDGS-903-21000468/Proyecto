package com.sigittuning.tuninggarage.viewmodels

import android.content.Context
import android.net.Uri // NUEVO: Import para Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Asegúrate de que tus modelos de datos de API (ApiResponse) estén importados
import com.sigittuning.tuninggarage.data.models.* import com.sigittuning.tuninggarage.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// NUEVO: Imports para subida de archivos
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// Modelos para Marketplace (Sin cambios)
data class CreateMarketplaceListingRequest(
    val titulo: String,
    val descripcion: String,
    val imagenURL: String?,
    val precioInicial: Double,
    val marca: String?,
    val modelo: String?,
    val anio: Int?,
    val kilometraje: Int?,
    val modificaciones: String?
)

data class MarketplaceListingDto(
    val listingID: Int,
    val vendedorID: Int,
    val vendedorNombre: String,
    val vendedorAvatar: String?,
    val titulo: String,
    val descripcion: String?,
    val imagenURL: String?,
    val precioInicial: Double,
    val precioActual: Double,
    val marca: String?,
    val modelo: String?,
    val anio: Int?,
    val kilometraje: Int?,
    val modificaciones: String?,
    val fechaPublicacion: String,
    val estatus: String,
    val totalOfertas: Int,
    val mejorOferta: Double?
)

data class CreateBidRequest(
    val montoOferta: Double,
    val mensaje: String?
)

data class BidDto(
    val bidID: Int,
    val compradorID: Int,
    val compradorNombre: String,
    val compradorAvatar: String?,
    val montoOferta: Double,
    val mensaje: String?,
    val fechaOferta: String,
    val aceptada: Boolean
)

sealed class MarketplaceState {
    object Loading : MarketplaceState()
    data class Success(val listings: List<MarketplaceListingDto>) : MarketplaceState()
    data class Empty(val message: String) : MarketplaceState()
    data class Error(val message: String) : MarketplaceState()
}

class MarketplaceViewModel : ViewModel() {

    private val _marketplaceState = MutableStateFlow<MarketplaceState>(MarketplaceState.Loading)
    val marketplaceState: StateFlow<MarketplaceState> = _marketplaceState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    // Cargar listings (Sin cambios)
    fun loadListings(context: Context) {
        viewModelScope.launch {
            try {
                _marketplaceState.value = MarketplaceState.Loading

                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.getMarketplaceListings()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        if (apiResponse.data.isEmpty()) {
                            _marketplaceState.value = MarketplaceState.Empty("No hay vehículos publicados")
                        } else {
                            _marketplaceState.value = MarketplaceState.Success(apiResponse.data)
                        }
                    } else {
                        _marketplaceState.value = MarketplaceState.Error(apiResponse.message)
                    }
                } else {
                    _marketplaceState.value = MarketplaceState.Error("Error al cargar marketplace")
                }
            } catch (e: Exception) {
                _marketplaceState.value = MarketplaceState.Error("Error: ${e.message}")
            }
        }
    }

    // MODIFICADO: Crear listing
    fun createListing(
        context: Context,
        titulo: String,
        descripcion: String,
        imagenUri: Uri?, // MODIFICADO: De String? a Uri?
        precioInicial: Double,
        marca: String?,
        modelo: String?,
        anio: Int?,
        kilometraje: Int?,
        modificaciones: String?
    ) {
        viewModelScope.launch {
            try {
                _actionMessage.value = "⏳ Publicando vehículo..."

                // NUEVO: Validar que la imagen no sea nula (la UI ya lo hace, pero es buena práctica)
                if (imagenUri == null) {
                    _actionMessage.value = "❌ No se seleccionó ninguna imagen."
                    kotlinx.coroutines.delay(3000)
                    _actionMessage.value = null
                    return@launch
                }

                val apiService = RetrofitClient.getApiService(context)

                // NUEVO: Subir la imagen primero
                val finalImageUrl = uploadImageAndGetUrl(context, imagenUri)

                if (finalImageUrl == null) {
                    _actionMessage.value = "❌ Error al subir la imagen"
                    kotlinx.coroutines.delay(3000)
                    _actionMessage.value = null
                    return@launch
                }

                // (Lógica anterior) Ahora creamos el listing con la URL obtenida
                val response = apiService.createMarketplaceListing(
                    CreateMarketplaceListingRequest(
                        titulo, descripcion, finalImageUrl, precioInicial, // MODIFICADO: Usamos finalImageUrl
                        marca, modelo, anio, kilometraje, modificaciones
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _actionMessage.value = "✅ Publicación creada (Se aplicará comisión del 15%)"
                        loadListings(context)
                    } else {
                        _actionMessage.value = "❌ ${apiResponse.message}"
                    }
                } else {
                    _actionMessage.value = "❌ Error al crear publicación"
                }

                kotlinx.coroutines.delay(3000)
                _actionMessage.value = null
            } catch (e: Exception) {
                _actionMessage.value = "❌ Error: ${e.message}"
                kotlinx.coroutines.delay(3000)
                _actionMessage.value = null
            }
        }
    }

    // NUEVO: Función privada para subir la imagen (copiada de SocialViewModel)
    // Reutiliza el mismo endpoint de subida
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

    // Hacer oferta (Sin cambios)
    fun makeBid(context: Context, listingId: Int, monto: Double, mensaje: String?) {
        viewModelScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.createBid(listingId, CreateBidRequest(monto, mensaje))

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _actionMessage.value = "✅ Oferta enviada exitosamente"
                        loadListings(context)
                    } else {
                        _actionMessage.value = "❌ ${apiResponse.message}"
                    }
                } else {
                    _actionMessage.value = "❌ Error al enviar oferta"
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