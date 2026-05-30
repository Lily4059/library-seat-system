package com.example.seatmanage.viewmodel

import com.example.seatmanage.data.api.RetrofitClient
import com.example.seatmanage.data.api.SeatItem
import com.example.seatmanage.data.model.Seat

object SeatRepository {
    private var cachedSeats: Map<String, List<Seat>> = emptyMap()

    suspend fun fetchSeatsByFloor(floor: String): List<Seat> {
        if (cachedSeats.containsKey(floor)) return cachedSeats[floor]!!
        try {
            val response = RetrofitClient.apiService.getSeats(floor)
            if (response.isSuccessful && response.body()?.code == 200) {
                val seats = response.body()!!.data!!.map { it.toSeat() }
                cachedSeats = cachedSeats + (floor to seats)
                return seats
            }
        } catch (_: Exception) {}
        return emptyList()
    }

    suspend fun fetchAllFloorsSeats(floors: List<String>): Map<String, List<Seat>> {
        try {
            val response = RetrofitClient.apiService.getSeats(null)
            if (response.isSuccessful && response.body()?.code == 200) {
                val grouped = response.body()!!.data!!.map { it.toSeat() }.groupBy { it.floor }
                cachedSeats = grouped
                return grouped
            }
        } catch (_: Exception) {}
        return cachedSeats
    }

    fun getCachedSeats(floor: String): List<Seat> = cachedSeats[floor] ?: emptyList()

    fun reset() { cachedSeats = emptyMap() }

    private fun SeatItem.toSeat() = Seat(
        id = id,
        floor = floor,
        seatNo = seatNo,
        area = area,
        status = status,
        window = window,
        power = power,
        silent = silent
    )
}

object ReservationRepository {
    private var cachedReservations = emptyList<com.example.seatmanage.data.model.Reservation>()

    suspend fun fetchMyReservations(): List<com.example.seatmanage.data.model.Reservation> {
        try {
            val response = RetrofitClient.apiService.getMyReservations()
            if (response.isSuccessful && response.body()?.code == 200) {
                cachedReservations = response.body()!!.data!!.map { it.toReservation() }
                return cachedReservations
            }
        } catch (_: Exception) {}
        return cachedReservations
    }

    fun getCachedReservations() = cachedReservations

    fun reset() { cachedReservations = emptyList() }

    private fun com.example.seatmanage.data.api.ReservationItem.toReservation() =
        com.example.seatmanage.data.model.Reservation(
            id = id,
            seatId = seatId,
            seatNo = seatNo,
            seatType = seatType ?: "",
            startTime = startTime,
            endTime = endTime,
            status = status,
            checkInTime = checkInTime,
            checkOutTime = checkOutTime,
            createdAt = createdAt
        )
}
