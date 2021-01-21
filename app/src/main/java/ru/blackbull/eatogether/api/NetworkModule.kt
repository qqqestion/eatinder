package ru.blackbull.eatogether.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import ru.blackbull.eatogether.other.Constants.BASE_GOOGLE_API_URL
import java.util.concurrent.TimeUnit

object NetworkModule {

    // Перехватывает запросы
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addNetworkInterceptor(loggingInterceptor)
        .connectTimeout(10 , TimeUnit.SECONDS)
        .writeTimeout(30 , TimeUnit.SECONDS)
        .readTimeout(30 , TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_GOOGLE_API_URL)
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val googlePlaceApiService: GooglePlaceApiService = retrofit.create()

    val firebaseApiService = FirebaseApiService()
}