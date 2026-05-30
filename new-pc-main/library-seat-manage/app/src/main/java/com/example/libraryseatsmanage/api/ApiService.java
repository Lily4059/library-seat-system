package com.example.libraryseatsmanage.api;

import com.example.libraryseatsmanage.model.ApiResponse;
import com.example.libraryseatsmanage.model.CheckInRequest;
import com.example.libraryseatsmanage.model.LoginRequest;
import com.example.libraryseatsmanage.model.LoginResponse;
import com.example.libraryseatsmanage.model.RegisterRequest;
import com.example.libraryseatsmanage.model.Reservation;
import com.example.libraryseatsmanage.model.ReservationRequest;
import com.example.libraryseatsmanage.model.Seat;
import com.example.libraryseatsmanage.model.UserProfile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("register")
    Call<ApiResponse<Object>> register(@Body RegisterRequest request);

    @POST("logout")
    Call<ApiResponse<Object>> logout();

    @GET("user/profile")
    Call<ApiResponse<UserProfile>> getUserProfile(@Header("x-user-id") int userId);

    @GET("seats")
    Call<ApiResponse<List<Seat>>> getSeats(@Query("floor") String floor);

    @POST("reservations")
    Call<ApiResponse<Reservation>> createReservation(
            @Header("x-user-id") int userId,
            @Body ReservationRequest request
    );

    @GET("reservations/my")
    Call<ApiResponse<List<Reservation>>> getMyReservations(@Header("x-user-id") int userId);

    @POST("reservations/{id}/cancel")
    Call<ApiResponse<Object>> cancelReservation(
            @Header("x-user-id") int userId,
            @Path("id") int reservationId
    );

    @POST("checkin")
    Call<ApiResponse<Object>> checkIn(
            @Header("x-user-id") int userId,
            @Body CheckInRequest request
    );

    @POST("checkout")
    Call<ApiResponse<Object>> checkOut(
            @Header("x-user-id") int userId,
            @Body CheckInRequest request
    );
}
