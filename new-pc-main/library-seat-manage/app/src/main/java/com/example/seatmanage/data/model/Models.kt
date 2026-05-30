package com.example.seatmanage.data.model

data class User(
    val id: Int = 0,
    val username: String = "",
    val name: String = "",
    val studentNo: String = "",
    val phone: String = "",
    val creditScore: Int = 100,
    val totalStudyTime: Int = 360,
    val avatar: String = ""
)

data class Seat(
    val id: String = "",
    val floor: String = "",
    val seatNo: String = "",
    val area: String = "",
    val status: String = "available",
    val window: Boolean = false,
    val power: Boolean = false,
    val silent: Boolean = false
)

data class Reservation(
    val id: Int = 0,
    val seatId: String = "",
    val seatNo: String = "",
    val seatType: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val status: String = "pending",
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val createdAt: String = ""
)

data class Notification(
    val id: String = "",
    val type: String = "system",
    val title: String = "",
    val summary: String = "",
    val content: String = "",
    val time: String = "",
    val isRead: Boolean = false,
    val actionLink: String = "",
    val actionText: String = ""
)

data class CreditRecord(
    val id: Int = 0,
    val title: String = "",
    val points: Int = 0,
    val time: String = "",
    val reason: String = ""
)

data class FloorStat(
    val floor: String = "",
    val available: Int = 0,
    val total: Int = 0,
    val availableRate: Int = 0
)

enum class SeatStatus(val label: String) {
    AVAILABLE("可预约"),
    BOOKED("已约"),
    OCCUPIED("占用"),
    UNAVAILABLE("不可用"),
    TEMP_RESERVED("暂离")
}
