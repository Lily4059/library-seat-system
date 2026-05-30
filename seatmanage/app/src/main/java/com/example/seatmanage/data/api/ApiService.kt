package com.example.seatmanage.data.api

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiResponse<TokenInfo>>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<ApiResponse<TokenInfo>>

    @GET("user/profile")
    suspend fun getProfile(): Response<ApiResponse<ProfileResponse>>

    @PUT("user/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): Response<ApiResponse<Unit>>

    @GET("seats")
    suspend fun getSeats(@Query("floor") floor: String? = null): Response<ApiResponse<List<SeatItem>>>

    @GET("seats/{id}")
    suspend fun getSeat(@Path("id") id: String): Response<ApiResponse<SeatItem>>

    @POST("reservations")
    suspend fun createReservation(@Body body: ReservationRequest): Response<ApiResponse<ReservationItem>>

    @GET("reservations/my")
    suspend fun getMyReservations(): Response<ApiResponse<List<ReservationItem>>>

    @POST("reservations/{id}/cancel")
    suspend fun cancelReservation(@Path("id") id: Int, @Body body: CancelBody = CancelBody()): Response<ApiResponse<Unit>>

    @POST("reservations/checkin")
    suspend fun checkIn(@Body body: CheckInRequest): Response<ApiResponse<CheckInResult>>

    @POST("reservations/checkout")
    suspend fun checkOut(@Body body: CheckOutRequest): Response<ApiResponse<CheckOutResult>>

    @GET("notifications")
    suspend fun getNotifications(
        @Query("type") type: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<ApiResponse<NotificationListResponse>>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<ApiResponse<UnreadCountResponse>>

    @PUT("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @PUT("notifications/read-all")
    suspend fun markAllRead(): Response<ApiResponse<Unit>>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @GET("history")
    suspend fun getHistory(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<ApiResponse<HistoryListResponse>>
}
