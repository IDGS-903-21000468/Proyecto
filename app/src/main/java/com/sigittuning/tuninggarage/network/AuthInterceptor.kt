package com.sigittuning.tuninggarage.network

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import com.sigittuning.tuninggarage.utils.TokenManager

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Obtener el token de forma síncrona
        val token = runBlocking {
            tokenManager.token.first()
        }

        // Si hay token, agregarlo al header
        val newRequest = if (!token.isNullOrEmpty()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(newRequest)
    }
}