package edu.cit.ochavillo.schedease.network

import com.google.gson.annotations.SerializedName
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// ==========================================
// 1. DATA TRANSFER OBJECTS (DTOs)
// ==========================================

data class AuthRequest(val username: String, val password: String)

data class AuthResponse(
    @SerializedName("accessToken") val token: String, // 🚨 FIX: Tells Gson to map 'accessToken' here!
    val message: String?
)

data class RegisterRequest(
    val username: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val address: String,
    val email: String,
    val password: String,
    val role: String
)

// 🚨 Added these so your getEstablishments endpoint compiles!
data class Establishment(
    val id: String,
    val name: String,
    val description: String
)

data class PaginatedResponse<T>(
    val data: List<T>,
    val nextCursor: String?,
    val hasMore: Boolean
)

data class AppointmentRequest(
    val establishmentId: String,
    val date: String,
    val startTime: String
)

data class AppointmentResponse(
    val id: String,
    val establishmentName: String?,
    val appointmentDate: String,
    val startTime: String,
    val status: String
)

data class UpdateStatusRequest(val status: String)

data class RescheduleRequest(val date: String, val startTime: String)

data class ProviderAppointmentResponse(
    val id: String,
    val clientName: String?,
    val appointmentDate: String,
    val startTime: String,
    val status: String
)

data class AvailabilityDto(
    val dayOfWeek: String,
    val startTime: String?,
    val endTime: String?
)

data class SettingsEstablishmentDto(
    val name: String,
    val description: String,
    val address: String,
    val contactEmail: String,
    val phone: String,
    val slotDurationMinutes: Int,
    val bufferMinutes: Int,
    val bookingCutoffHours: Int
)

data class SettingsUserDto(
    val username: String,
    val firstName: String,
    val lastName: String
)

data class OverrideDto(
    val id: String,
    val date: String,
    val unavailable: Boolean,
    val startTime: String?,
    val endTime: String?
)

data class AddOverrideRequest(
    val date: String,
    val unavailable: Boolean,
    val startTime: String?,
    val endTime: String?
)

data class UserProfileDto(
    val firstName: String,
    val lastName: String,
    val username: String,
    val role: String?,
    val email: String?,
    val phoneNumber: String?,
    val address: String?,
    val enabled: Boolean?,
    val createdAt: String?
)

data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val username: String
)

data class PasswordChangeRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

data class CreateEstablishmentRequest(
    val name: String,
    val description: String,
    val address: String,
    val contactEmail: String,
    val slotDurationMinutes: Int
)

// ==========================================
// 2. THE API INTERFACE
// ==========================================
interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // 🚨 Added Refresh and Logout endpoints for your SessionManager
    @POST("api/auth/refresh")
    suspend fun refresh(): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/establishments")
    suspend fun getEstablishments(
        @Query("search") search: String?,
        @Query("limit") limit: Int,
        @Query("cursor") cursor: String?
    ): Response<PaginatedResponse<Establishment>>

    @GET("api/establishments/{id}")
    suspend fun getEstablishment(@Path("id") id: String): Response<Establishment>

    @GET("api/establishments/{id}/slots")
    suspend fun getAvailableSlots(
        @Path("id") id: String,
        @Query("date") date: String
    ): Response<List<String>>

    @POST("api/appointments")
    suspend fun bookAppointment(@Body request: AppointmentRequest): Response<Unit>

    @GET("api/appointments/my")
    suspend fun getMyAppointments(): Response<List<AppointmentResponse>>

    @PUT("api/appointments/{id}/status")
    suspend fun updateAppointmentStatus(
        @Path("id") id: String,
        @Body request: UpdateStatusRequest
    ): Response<Unit>

    @PUT("api/appointments/{id}/reschedule")
    suspend fun rescheduleAppointment(
        @Path("id") id: String,
        @Body request: RescheduleRequest
    ): Response<Unit>

    @GET("api/appointments/provider")
    suspend fun getProviderAppointments(): Response<List<ProviderAppointmentResponse>>

    @GET("api/providers/availability")
    suspend fun getAvailability(): Response<List<AvailabilityDto>>

    @PUT("api/providers/availability")
    suspend fun updateAvailability(@Body payload: List<AvailabilityDto>): Response<Unit>

    @GET("api/establishments/me")
    suspend fun getMyEstablishment(): Response<SettingsEstablishmentDto>

    @GET("api/users/me")
    suspend fun getMyUser(): Response<SettingsUserDto>

    @PUT("api/establishments/me")
    suspend fun updateMyEstablishment(@Body payload: SettingsEstablishmentDto): Response<Unit>

    @PUT("api/users/me")
    suspend fun updateMyUser(@Body payload: SettingsUserDto): Response<Unit>

    @DELETE("api/establishments/me")
    suspend fun deleteMyEstablishment(): Response<Unit>

    @GET("api/availability/overrides")
    suspend fun getOverrides(): Response<List<OverrideDto>>

    @POST("api/availability/overrides")
    suspend fun addOverride(@Body payload: AddOverrideRequest): Response<Unit>

    @DELETE("api/availability/overrides/{id}")
    suspend fun deleteOverride(@Path("id") id: String): Response<Unit>

    @GET("api/users/me")
    suspend fun getProfile(): Response<UserProfileDto>

    @PUT("api/users/me")
    suspend fun updateProfile(@Body payload: UpdateProfileRequest): Response<UserProfileDto>

    @POST("api/auth/resend-verification")
    suspend fun resendVerification(@Query("email") email: String): Response<Unit>

    @PUT("api/users/me/password")
    suspend fun changePassword(@Body payload: PasswordChangeRequest): Response<Unit>

    @DELETE("/api/users/me")
    suspend fun deleteAccount() : Response<Unit>

    @POST("api/establishments") // Make sure this matches your Spring Boot controller!
    suspend fun createEstablishment(@Body request: CreateEstablishmentRequest): Response<Unit>
}


// ==========================================
// 3. THE RETROFIT BUILDER
// ==========================================
object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // A simple in-memory CookieJar to store the refresh token cookie
    private val cookieJar = object : CookieJar {
        private val cookies = mutableListOf<Cookie>()

        override fun saveFromResponse(url: HttpUrl, newCookies: List<Cookie>) {
            cookies.addAll(newCookies)
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookies
        }
    }

    // 🚨 NEW: The Interceptor that adds your JWT token to every request
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // Grab the token from wherever you saved it during Login (e.g., SessionManager)
        // Adjust this line to match exactly how you store your token!
        val token = SessionManager.accessToken

        if (!token.isNullOrBlank()) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }

    // Attach the CookieJar AND the Interceptor to OkHttp
    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(authInterceptor) // 🚨 ADDED HERE
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}