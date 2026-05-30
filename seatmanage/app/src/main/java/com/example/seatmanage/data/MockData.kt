package com.example.seatmanage.data

import com.example.seatmanage.data.model.*
import kotlin.math.abs

object MockData {

    fun createFloorSeats(floor: String): List<Seat> {
        val seats = mutableListOf<Seat>()

        fun pushSeat(seatNo: String) {
            val meta = getSeatMeta(seatNo)
            seats.add(
                Seat(
                    id = "$floor-$seatNo",
                    floor = floor,
                    seatNo = seatNo,
                    area = meta.area,
                    status = statusByHash("$floor-$seatNo"),
                    window = meta.window,
                    power = meta.power,
                    silent = meta.silent
                )
            )
        }

        fun addWindowBlock(prefix: String) {
            for (t in 1..7) for (s in 1..4) pushSeat("$prefix-$t-$s")
        }

        fun addStudyBlock(prefix: String) {
            for (r in 1..4) for (t in 1..4) for (s in 1..4) pushSeat("$prefix-$r-$t-$s")
        }

        fun addDuoBlock(prefix: String) {
            for (i in 1..4) for (s in 1..2) pushSeat("$prefix-$i-$s")
        }

        fun addLongBlock(prefix: String) {
            for (i in 1..8) pushSeat("$prefix-$i")
        }

        fun addHallBlock() {
            for (r in 1..3) for (t in 1..4) for (s in 1..4) pushSeat("H-$r-$t-$s")
        }

        addWindowBlock("WL")
        addStudyBlock("SL")
        addWindowBlock("WR")
        addStudyBlock("SR")
        addDuoBlock("DTL")
        addLongBlock("PTH")
        addDuoBlock("DTR")
        addLongBlock("PLV")
        addLongBlock("PRV")
        addLongBlock("YV")
        addDuoBlock("DBL")
        addLongBlock("PBH")
        addDuoBlock("DBR")
        addStudyBlock("SBL")
        addWindowBlock("WBL")
        addHallBlock()
        addStudyBlock("SBR")
        addWindowBlock("WBR")

        return seats
    }

    private fun statusByHash(text: String): String {
        var hash = 0
        for (c in text) hash = (hash shl 5) - hash + c.code
        return when (abs(hash) % 10) {
            in 0..5 -> "available"
            in 6..7 -> "booked"
            else -> "unavailable"
        }
    }

    private fun parseStudySeat(seatNo: String): SeatMeta {
        val parts = seatNo.split("-")
        val tableIndex = parts.getOrNull(2)?.toIntOrNull() ?: 1
        val window = tableIndex == 1 || tableIndex == 4
        return SeatMeta(
            area = if (window) "自习区（靠窗）" else "自习区",
            window = window,
            power = !window,
            silent = true
        )
    }

    private fun getSeatMeta(seatNo: String): SeatMeta {
        if (seatNo.startsWith("WL-") || seatNo.startsWith("WR-") ||
            seatNo.startsWith("WBL-") || seatNo.startsWith("WBR-")
        ) return SeatMeta("靠窗区", true, false, false)
        if (seatNo.startsWith("SL-") || seatNo.startsWith("SR-") ||
            seatNo.startsWith("SBL-") || seatNo.startsWith("SBR-")
        ) return parseStudySeat(seatNo)
        if (seatNo.startsWith("PTH-") || seatNo.startsWith("PBH-") ||
            seatNo.startsWith("PLV-") || seatNo.startsWith("PRV-")
        ) return SeatMeta("长桌区", false, true, false)
        if (seatNo.startsWith("YV-")) return SeatMeta("靠窗长桌区", true, true, false)
        if (seatNo.startsWith("DTL-") || seatNo.startsWith("DTR-") ||
            seatNo.startsWith("DBL-") || seatNo.startsWith("DBR-")
        ) return SeatMeta("双人协作区", false, true, false)
        if (seatNo.startsWith("H-")) return SeatMeta("大厅区", false, true, false)
        return SeatMeta("普通区", false, false, false)
    }

    data class SeatMeta(
        val area: String,
        val window: Boolean,
        val power: Boolean,
        val silent: Boolean
    )

    val allFloors = listOf("1F", "2F", "3F", "4F", "5F")
    val seatsByFloor: Map<String, List<Seat>> = allFloors.associateWith { createFloorSeats(it) }

    val testUser = User(
        id = 1,
        username = "test",
        name = "张诗琪",
        studentNo = "20261001",
        phone = "138****1234",
        creditScore = 95,
        totalStudyTime = 360
    )

    val notifications = mutableListOf(
        Notification(
            id = "1", type = "booking", title = "预约签到提醒",
            summary = "您预约的 WL-1-1 座位即将开始，请及时签到。",
            content = "您预约的 1F 靠窗区 WL-1-1 座位将于今天 12:30 开始，请在 12:15 - 12:45 之间完成签到。",
            time = "2026-04-27 12:30:00", isRead = false, actionLink = "reservations", actionText = "查看预约"
        ),
        Notification(
            id = "2", type = "system", title = "系统通知",
            summary = "图书馆将于 5 月 1 日闭馆一天。",
            content = "各位读者：接学校通知，图书馆将于 5 月 1 日闭馆一天。",
            time = "2026-04-26 18:00:00", isRead = true
        ),
        Notification(
            id = "3", type = "points", title = "积分变动提醒",
            summary = "您已成功签到，积分 +5。",
            content = "您预约的座位已成功签到，按时签到奖励积分 +5。",
            time = "2026-04-25 14:30:00", isRead = false, actionLink = "credit_log", actionText = "查看积分"
        ),
        Notification(
            id = "4", type = "booking", title = "座位释放提醒",
            summary = "您关注的 WR-1-1 座位已空闲。",
            content = "您关注的座位目前已空闲，快去预约吧！",
            time = "2026-04-24 09:15:00", isRead = true, actionLink = "home", actionText = "去预约"
        ),
        Notification(
            id = "5", type = "system", title = "占座违规警告",
            summary = "系统检测到您离开座位超时。",
            content = "系统检测到您离开座位已超过 30 分钟，已自动释放座位并扣除积分 10 分。",
            time = "2026-04-20 15:40:00", isRead = true, actionLink = "credit_log", actionText = "查看详情"
        )
    )

    val creditRecords = listOf(
        CreditRecord(1, "按时签到奖励", 5, "2026-04-25 14:30:00", "按时签到"),
        CreditRecord(2, "按时签退奖励", 3, "2026-04-24 18:00:00", "按时签退"),
        CreditRecord(3, "连续预约奖励", 2, "2026-04-23 10:00:00", "连续3天预约"),
        CreditRecord(4, "取消预约扣除", -2, "2026-04-22 15:00:00", "取消预约"),
        CreditRecord(5, "占座违规扣除", -10, "2026-04-20 15:40:00", "离开座位超时")
    )

    val reservations = mutableListOf<Reservation>()
    private var nextResId = 1001
    var currentReservationStatus: String = "已结束"
    var currentReservationSeatNo: String = "WL-1-1"
    var currentReservationArea: String = "靠窗区"
    var currentReservationStart: String = "12:00"
    var currentReservationEnd: String = "13:30"

    fun createReservation(seat: Seat, date: String, startTime: String, endTime: String): Reservation {
        val newId = nextResId++
        val reservation = Reservation(
            id = newId,
            seatId = seat.id,
            seatNo = seat.seatNo,
            seatType = seat.area,
            startTime = "${date}T${startTime}:00",
            endTime = "${date}T${endTime}:00",
            status = "pending",
            createdAt = date
        )
        reservations.add(0, reservation)
        return reservation
    }

    fun getMyReservations(): List<Reservation> = reservations.toList()

    fun cancelReservation(id: Int): Boolean {
        val idx = reservations.indexOfFirst { it.id == id }
        if (idx == -1) return false
        reservations[idx] = reservations[idx].copy(status = "cancelled")
        return true
    }

    fun checkIn(reservationId: Int): Boolean {
        val idx = reservations.indexOfFirst { it.id == reservationId }
        if (idx == -1) return false
        reservations[idx] = reservations[idx].copy(status = "checked_in", checkInTime = "now")
        return true
    }

    fun checkOut(reservationId: Int): Boolean {
        val idx = reservations.indexOfFirst { it.id == reservationId }
        if (idx == -1) return false
        reservations[idx] = reservations[idx].copy(status = "completed", checkOutTime = "now")
        return true
    }
}
