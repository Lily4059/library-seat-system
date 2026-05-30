package com.example.seatmanage.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seatmanage.data.AppConfig
import com.example.seatmanage.data.MockData
import com.example.seatmanage.data.api.*
import com.example.seatmanage.data.model.*
import kotlinx.coroutines.launch

class ReservationViewModel : ViewModel() {
    val reservations = mutableStateOf(emptyList<Reservation>())
    val isLoading = mutableStateOf(false)

    val stats: Triple<Int, Int, Int>
        get() {
            val list = reservations.value
            return Triple(
                list.count { it.status == "pending" },
                list.count { it.status == "checked_in" },
                list.count { it.status == "completed" }
            )
        }

    fun loadMyReservations() {
        if (!AppConfig.USE_API) {
            reservations.value = MockData.getMyReservations()
            return
        }
        isLoading.value = true
        viewModelScope.launch {
            reservations.value = ReservationRepository.fetchMyReservations()
            isLoading.value = false
        }
    }

    fun createReservation(seat: Seat, date: String, startTime: String, endTime: String, onResult: (Boolean, String) -> Unit) {
        if (!AppConfig.USE_API) {
            val r = MockData.createReservation(seat, date, startTime, endTime)
            reservations.value = MockData.getMyReservations()
            onResult(true, "预约成功（本地数据）")
            return
        }
        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.createReservation(
                    ReservationRequest(seatId = seat.id, seatNo = seat.seatNo, seatType = seat.area,
                        startTime = "${date}T${startTime}:00", endTime = "${date}T${endTime}:00"))
                if (response.isSuccessful && response.body()?.code == 200) {
                    reservations.value = ReservationRepository.fetchMyReservations()
                    isLoading.value = false
                    onResult(true, "预约成功")
                } else {
                    isLoading.value = false
                    onResult(false, response.body()?.message ?: "预约失败")
                }
            } catch (e: Exception) {
                isLoading.value = false
                onResult(false, "网络错误：${e.message}")
            }
        }
    }

    fun cancelReservation(id: Int, onResult: (Boolean, String) -> Unit) {
        if (!AppConfig.USE_API) {
            if (MockData.cancelReservation(id)) {
                reservations.value = MockData.getMyReservations()
                onResult(true, "取消成功（本地数据）")
            } else onResult(false, "取消失败")
            return
        }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.cancelReservation(id)
                if (response.isSuccessful && response.body()?.code == 200) {
                    reservations.value = ReservationRepository.fetchMyReservations()
                    onResult(true, "取消成功")
                } else onResult(false, response.body()?.message ?: "取消失败")
            } catch (e: Exception) { onResult(false, "网络错误：${e.message}") }
        }
    }

    fun checkIn(id: Int, onResult: (Boolean, String) -> Unit) {
        if (!AppConfig.USE_API) {
            if (MockData.checkIn(id)) {
                reservations.value = MockData.getMyReservations()
                onResult(true, "签到成功（本地数据）")
            } else onResult(false, "签到失败")
            return
        }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.checkIn(CheckInRequest(id))
                if (response.isSuccessful && response.body()?.code == 200) {
                    reservations.value = ReservationRepository.fetchMyReservations()
                    onResult(true, "签到成功")
                } else onResult(false, response.body()?.message ?: "签到失败")
            } catch (e: Exception) { onResult(false, "网络错误：${e.message}") }
        }
    }

    fun checkOut(id: Int, onResult: (Boolean, String) -> Unit) {
        if (!AppConfig.USE_API) {
            if (MockData.checkOut(id)) {
                reservations.value = MockData.getMyReservations()
                onResult(true, "签退成功（本地数据）")
            } else onResult(false, "签退失败")
            return
        }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.checkOut(CheckOutRequest(id))
                if (response.isSuccessful && response.body()?.code == 200) {
                    reservations.value = ReservationRepository.fetchMyReservations()
                    onResult(true, "签退成功")
                } else onResult(false, response.body()?.message ?: "签退失败")
            } catch (e: Exception) { onResult(false, "网络错误：${e.message}") }
        }
    }

    fun refresh() { loadMyReservations() }
}
