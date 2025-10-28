package com.sigittuning.tuninggarage.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sigittuning.tuninggarage.pantallas.Mensaje
import com.sigittuning.tuninggarage.servicios.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AsistenteViewModel : ViewModel() {

    private val geminiService = GeminiService()

    private val _mensajes = MutableStateFlow<List<Mensaje>>(
        listOf(
            Mensaje(
                texto = "¡Hola! Soy AVT, tu Asistente Virtual de Tuning. " +
                        "Dime la Marca, Modelo y Año de tu vehículo y qué modificaciones " +
                        "quieres hacer. ¡También puedes enviarme fotos!",
                esUsuario = false
            )
        )
    )
    val mensajes: StateFlow<List<Mensaje>> = _mensajes.asStateFlow()

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    private val historialChat = mutableListOf<Pair<String, String>>()
    // Ahora pide el Context para procesar la imagen
    fun enviarMensaje(texto: String, imagenUri: Uri?, context: Context) {
        val mensajeUsuario = Mensaje(
            texto = texto,
            esUsuario = true,
            imagenUri = imagenUri
        )

        _mensajes.value = _mensajes.value + mensajeUsuario
        _estaCargando.value = true

        val mensajeCargando = Mensaje(
            texto = "",
            esUsuario = false,
            esCargando = true
        )
        _mensajes.value = _mensajes.value + mensajeCargando

        viewModelScope.launch {
            try {
                var respuestaCompleta = ""

                // Lógica para decidir qué servicio llamar
                if (imagenUri == null) {
                    // --- Caso 1: Solo Texto ---
                    geminiService.enviarMensaje(texto, historialChat).collect { chunk ->
                        respuestaCompleta += chunk
                        actualizarUltimoMensaje(respuestaCompleta)
                    }
                } else {
                    // --- Caso 2: Texto + Imagen ---
                    val bitmap = uriToBitmap(context, imagenUri)
                    geminiService.enviarMensaje(texto, bitmap, historialChat).collect { chunk ->
                        respuestaCompleta += chunk
                        actualizarUltimoMensaje(respuestaCompleta)
                    }
                }

                // Guardar en historial
                historialChat.add(Pair(texto, respuestaCompleta))

            } catch (e: Exception) {
                // Quitar mensaje de cargando y mostrar error
                _mensajes.value = _mensajes.value.dropLast(1) + Mensaje(
                    texto = "Lo siento, ocurrió un error: ${e.message}. " +
                            "Verifica tu conexión a internet y tu API Key.",
                    esUsuario = false,
                    esError = true
                )
            } finally {
                _estaCargando.value = false
            }
        }
    }

    // Función de ayuda para actualizar el mensaje de "escribiendo..."
    private fun actualizarUltimoMensaje(textoChunk: String) {
        _mensajes.value = _mensajes.value.dropLast(1) + Mensaje(
            texto = textoChunk,
            esUsuario = false
        )
    }

    private fun uriToBitmap(context: Context, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    fun limpiarChat() {
        _mensajes.value = listOf(
            Mensaje(
                texto = "Chat limpiado. ¿En qué más puedo ayudarte con tu vehículo?",
                esUsuario = false
            )
        )
        historialChat.clear()
    }
}