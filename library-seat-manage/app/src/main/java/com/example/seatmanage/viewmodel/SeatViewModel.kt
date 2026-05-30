package com.example.seatmanage.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seatmanage.data.AppConfig
import com.example.seatmanage.data.MockData
import com.example.seatmanage.data.model.*
import kotlinx.coroutines.launch

class SeatViewModel : ViewModel() {
    var activeFloor by mutableStateOf("1F")
    var selectedSeat by mutableStateOf<Seat?>(null)
    var filters by mutableStateOf(SeatFilters())
    var refreshTrigger by mutableIntStateOf(0)

    private var allSeats: Map<String, List<Seat>> = emptyMap()
    private var bookedSeatIds: Set<String> = emptySet()

    init { loadAllSeats() }

    private fun loadAllSeats() {
        if (AppConfig.USE_API) {
            viewModelScope.launch {
                allSeats = SeatRepository.fetchAllFloorsSeats(listOf("1F", "2F", "3F", "4F", "5F"))
                refreshTrigger++
            }
        } else {
            allSeats = MockData.seatsByFloor
            refreshTrigger++
        }
    }

    fun reloadFromReservations(reservations: List<Reservation>) {
        bookedSeatIds = reservations
            .filter { it.status == "pending" || it.status == "checked_in" }
            .map { it.seatId }
            .toSet()
        refreshTrigger++
    }

    fun activeSeats(): List<Seat> {
        refreshTrigger
        return allSeats[activeFloor]?.map { seat ->
            if (seat.id in bookedSeatIds) seat.copy(status = "booked") else seat
        } ?: emptyList()
    }

    fun filteredSeats(): List<Seat> = activeSeats().filter { seat ->
        if (filters.window && !seat.window) return@filter false
        if (filters.power && !seat.power) return@filter false
        if (filters.silent && !seat.silent) return@filter false
        true
    }

    fun floorStats(): List<FloorStat> {
        refreshTrigger
        return listOf("1F", "2F", "3F", "4F", "5F").map { calcFloorStat(it) }
    }

    fun switchFloor(floor: String) {
        activeFloor = floor
        if (selectedSeat?.floor != floor) selectedSeat = null
    }

    fun selectSeat(seat: Seat) {
        if (seat.status != "available") return
        selectedSeat = seat
    }

    fun toggleFilter(filter: String) {
        filters = when (filter) {
            "window" -> filters.copy(window = !filters.window)
            "power" -> filters.copy(power = !filters.power)
            "silent" -> filters.copy(silent = !filters.silent)
            else -> filters
        }
    }

    fun notifyReservationChanged() {
        selectedSeat = null
        refreshTrigger++
    }

    private fun calcFloorStat(floor: String): FloorStat {
        val seats = allSeats[floor] ?: return FloorStat(floor, 0, 0, 0)
        val available = seats.count { it.status == "available" && it.id !in bookedSeatIds }
        val total = seats.size
        return FloorStat(floor, available, total, if (total > 0) available * 100 / total else 0)
    }
}

data class SeatFilters(
    val window: Boolean = false,
    val power: Boolean = false,
    val silent: Boolean = false
)
