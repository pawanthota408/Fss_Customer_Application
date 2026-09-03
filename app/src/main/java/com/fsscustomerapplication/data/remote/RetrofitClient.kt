package com.fsscustomerapplication.data.remote

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://crm.friendssoftwaresolutions.in/api/customers/" 

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Smart Retry Interceptor for Cloudflare HTTP 521 / 502 / 503 errors
    private val retryInterceptor = Interceptor { chain ->
        val request = chain.request()
        var response: Response? = try {
            chain.proceed(request)
        } catch (_: Exception) {
            null
        }

        var tryCount = 0
        val maxRetries = 2
        while ((response == null || response.code in listOf(521, 502, 503, 504)) && tryCount < maxRetries) {
            tryCount++
            response?.close()
            try {
                Thread.sleep(600L * tryCount) // Wait 600ms, 1200ms
            } catch (_: Exception) {}
            response = try {
                chain.proceed(request)
            } catch (_: Exception) {
                null
            }
        }

        response ?: chain.proceed(request)
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(retryInterceptor)
        .addInterceptor(logging)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build()
            .create(ApiService::class.java)
    }
}
