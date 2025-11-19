package com.sigittuning.tuninggarage.network

import android.content.Context
import com.sigittuning.tuninggarage.BuildConfig
import com.sigittuning.tuninggarage.utils.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ⭐ CAMBIADO: Usa la URL desde BuildConfig
    private const val BASE_URL = BuildConfig.API_BASE_URL

    private var apiService: ApiService? = null

    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            val tokenManager = TokenManager(context)

            // Logging Interceptor (para ver las peticiones en Logcat)
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // OkHttp Client con interceptores
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(tokenManager))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            // Retrofit Instance
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }

        return apiService!!
    }
}