package edu.cit.ochavillo.schedease.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

// 1. The DTOs (Must match your Spring Boot backend exactly!)
data class AuthRequest(val username: String, val password: String)
data class AuthResponse(val token: String, val message: String?)

data class RegisterRequest(
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

// 2. The API Interface
interface ApiService {
    @POST("api/auth/login") // Update this to match your actual backend URL
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/auth/register") // Update this to match your actual backend URL
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}

// 3. The Retrofit Builder
object ApiClient {
    // 🚨 Use 10.0.2.2 for Android Emulator. Use your computer's IPv4 address if testing on a physical phone.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}