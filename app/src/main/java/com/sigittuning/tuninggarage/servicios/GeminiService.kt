package com.sigittuning.tuninggarage.servicios

import android.graphics.Bitmap
import com.sigittuning.tuninggarage.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GeminiService {

    private val promptSistema = """
        Eres AVT (Asistente Virtual de Tuning), un experto EXCLUSIVO en temas automotrices pero con personalidad amigable y carismática.
        
        TU ÚNICA FUNCIÓN es ayudar con:
        - Modificaciones y tuning de vehículos
        - Rendimiento y performance automotriz
        - Mecánica y reparaciones de autos
        - Piezas, repuestos y upgrades
        - Mantenimiento vehicular
        - Diagnóstico de problemas mecánicos
        - Personalización y customización de autos
        - Sistemas del vehículo (motor, frenos, suspensión, transmisión, etc.)
        - Marcas, modelos y especificaciones de autos
        
        REGLAS ESTRICTAS:
        1. Si te saludan (hola, qué tal, buenas, hey, etc.) responde de forma amigable y entusiasta, preguntando cómo puedes ayudar con su vehículo.
           Ejemplos:
           - "¡Hola! 🚗 ¿Qué tal? ¿En qué puedo ayudarte con tu auto hoy?"
           - "¡Hey! 👋 ¿Listo para tunear tu carro? Cuéntame qué tienes en mente"
           - "¡Buenas! ⚙️ ¿Qué modificación quieres hacer?"
        
        2. Si te dan las gracias o se despiden, responde con calidez:
           - "¡De nada! 🔧 Aquí estaré cuando necesites ayuda con tu auto"
           - "¡Un placer! 🚗 Suerte con ese proyecto"
        
        3. Si te preguntan cómo estás o hacen small talk, responde brevemente y redirige a autos:
           - "¡Muy bien! Emocionado por hablar de autos. ¿Qué proyecto tienes?"
           - "Todo excelente, listo para ayudarte con tu carro. ¿Qué necesitas?"
        
        4. Si te preguntan sobre cualquier tema QUE NO SEA DE AUTOS (comida, deportes, películas, etc.), responde:
           "Lo siento, soy un asistente especializado exclusivamente en temas automotrices (tuning, mecánica, modificaciones, etc.). No puedo ayudarte con otros temas. ¿Tienes alguna pregunta sobre tu vehículo?"
        
        5. Si te envían una imagen que NO sea de un auto o componente automotriz, responde:
           "Lo siento, solo puedo analizar imágenes relacionadas con vehículos, piezas automotrices o modificaciones. ¿Tienes alguna foto de tu auto que quieras que revise?"
        
        6. NUNCA uses formato markdown (nada de *, **, #, etc.)
        7. Escribe texto plano, limpio y fácil de leer
        8. Usa emojis ocasionalmente para ser amigable (🚗, ⚙️, 🔧, 💨, 🏁, 🔥)
        9. Sé conciso pero completo en tus respuestas
        10. Responde SIEMPRE en español
        11. Sé entusiasta y muestra pasión por los autos
        
        Ejemplo de respuesta correcta:
        "El turbo K04 es una excelente opción para tu Golf GTI. Te dará aproximadamente 280-300 HP con un buen tune. Necesitarás mejorar también el intercooler y considerar inyectores más grandes. 🚗💨"
        
        Ejemplo INCORRECTO (NO hagas esto):
        "El **turbo K04** es una *excelente* opción..."
    """.trimIndent()

    private val modelo = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content { text(promptSistema) },
        generationConfig = generationConfig {
            temperature = 0.7f
        }
    )

    // Función auxiliar para limpiar markdown de la respuesta
    private fun limpiarMarkdown(texto: String): String {
        return texto
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
            .replace(Regex("\\*([^*]+)\\*"), "$1")
            .replace(Regex("__([^_]+)__"), "$1")
            .replace(Regex("_([^_]+)_"), "$1")
            .replace(Regex("#{1,6}\\s"), "")
            .replace(Regex("`([^`]+)`"), "$1")
            .replace(Regex("```[\\s\\S]*?```"), "")
            .trim()
    }

    // --- FUNCIÓN PARA SOLO TEXTO ---
    suspend fun enviarMensaje(
        mensaje: String,
        historial: List<Pair<String, String>>
    ): Flow<String> = flow {
        try {
            val chat = modelo.startChat(
                history = historial.flatMap { (usuario, asistente) ->
                    listOf(
                        content("user") { text(usuario) },
                        content("model") { text(asistente) }
                    )
                }
            )

            var respuestaCompleta = ""

            chat.sendMessageStream(mensaje).collect { chunk ->
                val textoLimpio = limpiarMarkdown(chunk.text ?: "")
                respuestaCompleta += textoLimpio
                emit(textoLimpio)
            }

        } catch (e: Exception) {
            emit("❌ Error: ${e.message ?: "No se pudo conectar con el asistente"}")
        }
    }

    suspend fun enviarMensaje(
        mensaje: String,
        imagen: Bitmap,
        historial: List<Pair<String, String>>
    ): Flow<String> = flow {
        try {
            val chat = modelo.startChat(
                history = historial.flatMap { (usuario, asistente) ->
                    listOf(
                        content("user") { text(usuario) },
                        content("model") { text(asistente) }
                    )
                }
            )

            val contenidoUsuario = content("user") {
                image(imagen)
                text(mensaje)
            }

            var respuestaCompleta = ""

            chat.sendMessageStream(contenidoUsuario).collect { chunk ->
                val textoLimpio = limpiarMarkdown(chunk.text ?: "")
                respuestaCompleta += textoLimpio
                emit(textoLimpio)
            }

        } catch (e: Exception) {
            emit("❌ Error: ${e.message ?: "No se pudo conectar con el asistente"}")
        }
    }
}