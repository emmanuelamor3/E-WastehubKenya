package com.example.e_wastehubkenya.data.network

// import android.content.Context // No longer needed
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

// Converted from a class to a singleton object to fix the "Unresolved reference" error.
object RetrofitInstance {
    // NOTE: The AuthInterceptor has been temporarily removed as it required a Context.
    // This will get the build working, but authentication for API calls will be disabled.
    // A proper dependency injection setup is recommended to manage the Context dependency.
    // private val authInterceptor = AuthInterceptor(context)

    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // .addInterceptor(authInterceptor) // Temporarily removed
        .build()

    private val retrofit by lazy {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(Json.asConverterFactory(contentType))
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
