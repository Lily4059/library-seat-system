package com.example.seatmanage.data.api

data class ApiResponse<T>(
    val code: Int,
    val data: T?,
    val message: String? = null
)

data class LoginRequest(val username: String, val password: String)

data class RegisterRequest(val username: String, val password: String, val name: String, val studentNo: String? = null)

data class TokenInfo(
    val token: String,
    val userInfo: UserInfo
)

data class UserInfo(
    val id: Int,
    val name: String,
    val studentNo: String?,
    val credit: Int,
    val role: String? = null
)

data class ProfileResponse(
    val id: Int,
    val name: String,
    val studentNo: String?,
    val creditScore: Int,
    val totalStudyTime: Int,
    val phone: String?,
    val avatar: String?
)

data class SeatItem(
    val id: String,
    val floor: String,
    val seatNo: String,
    val status: String,
    val area: String,
    val window: Boolean,
    val power: Boolean,
    val silent: Boolean
)

data class ReservationRequest(
    val seatId: String,
    val seatNo: String,
    val seatType: String? = null,
    val startTime: String,
    val endTime: String
)

data class ReservationItem(
    val id: Int,
    val seatId: String,
    val seatNo: String,
    val seatType: String?,
    val startTime: String,
    val endTime: String,
    val status: String,
    val checkInTime: String?,
    val checkOutTime: String?,
    val cancelReason: String?,
    val createdAt: String
)

data class CheckInRequest(val reservationId: Int)
data class CheckOutRequest(val reservationId: Int)
data class CheckInResult(val checkInTime: String)
data class CheckOutResult(val checkOutTime: String, val studyTime: Int)

data class CancelBody(val reason: String? = null)

data class NotificationItem(
    val id: Int,
    val type: String,
    val title: String,
    val summary: String,
    val content: String,
    val actionLink: String?,
    val actionText: String?,
    val isRead: Boolean,
    val time: String
)

data class NotificationListResponse(
    val list: List<NotificationItem>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

data class UnreadCountResponse(val unreadCount: Int)

data class HistoryItem(
    val id: String,
    val seatNo: String,
    val floor: Int?,
    val area: String?,
    val features: List<String>?,
    val date: String,
    val time: String,
    val status: String,
    val checkInTime: String?,
    val reason: String?
)

data class HistoryListResponse(
    val list: List<HistoryItem>,
    val stats: HistoryStats,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

data class HistoryStats(
    val totalCount: Int,
    val completedCount: Int,
    val cancelledCount: Int,
    val expiredCount: Int,
    val totalStudyTime: Int
)
