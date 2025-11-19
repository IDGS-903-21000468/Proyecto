package com.sigittuning.tuninggarage.viewmodels

// Usamos Gson, ya que lo tienes en tu build.gradle
import com.google.gson.annotations.SerializedName

/**
 * Este data class representa la respuesta JSON que tu backend
 * debe enviar después de subir una imagen.
 */
data class ImageUploadResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("url") // Este campo debe coincidir con el JSON que envía tu backend
    val url: String
)