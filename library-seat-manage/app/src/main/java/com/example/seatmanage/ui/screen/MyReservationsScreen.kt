package com.example.seatmanage.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seatmanage.data.model.Reservation
import com.example.seatmanage.viewmodel.ReservationViewModel
import com.example.seatmanage.viewmodel.SeatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(
    reservationViewModel: ReservationViewModel,
    seatViewModel: SeatViewModel,
    onBack: () -> Unit,
    onCheckIn: () -> Unit
) {
    val activeReservations = reservationViewModel.reservations.value.filter {
        it.status == "pending" || it.status == "checked_in"
    }
    val stats = reservationViewModel.stats
    var message by remember { mutableStateOf<String?>(null) }
    var cancelTargetId by remember { mutableStateOf(-1) }
    var checkOutTargetId by remember { mutableStateOf(-1) }

    message?.let {
        AlertDialog(onDismissRequest = { message = null }, title = { Text("提示") },
            text = { Text(it) }, confirmButton = { TextButton(onClick = { message = null }) { Text("确定") } })
    }

    if (cancelTargetId >= 0) {
        AlertDialog(
            onDismissRequest = { cancelTargetId = -1 },
            title = { Text("取消预约") },
            text = { Text("确定要取消该预约吗？取消后座位将释放。") },
            confirmButton = {
                Button(onClick = {
                    reservationViewModel.cancelReservation(cancelTargetId) { success, msg ->
                        message = msg
                        if (success) {
                            seatViewModel.reloadFromReservations(reservationViewModel.reservations.value)
                            seatViewModel.notifyReservationChanged()
                        }
                    }
                    cancelTargetId = -1
                }) { Text("确定取消") }
            },
            dismissButton = { TextButton(onClick = { cancelTargetId = -1 }) { Text("返回") } }
        )
    }

    if (checkOutTargetId >= 0) {
        AlertDialog(
            onDismissRequest = { checkOutTargetId = -1 },
            title = { Text("签退确认") },
            text = { Text("确定要签退吗？签退后该座位将释放。") },
            confirmButton = {
                Button(onClick = {
                    reservationViewModel.checkOut(checkOutTargetId) { _, _ ->
                        seatViewModel.reloadFromReservations(reservationViewModel.reservations.value)
                        seatViewModel.notifyReservationChanged()
                    }
                    checkOutTargetId = -1
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) { Text("确认签退") }
            },
            dismissButton = { TextButton(onClick = { checkOutTargetId = -1 }) { Text("返回") } }
        )
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF1E3A8A), shadowElevation = 4.dp) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Text("我的预约", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp,
                        modifier = Modifier.weight(1f))
                    TextButton(onClick = { reservationViewModel.refresh() }) {
                        Text("刷新", color = Color.White)
                    }
                }
            }
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "my_reservations",
                onHome = { onBack() },
                onMap = { onBack() },
                onReservations = { },
                onProfile = { onBack() }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                statCard("待签到", stats.first, Color(0xFFF59E0B), Modifier.weight(1f))
                statCard("已签到", stats.second, Color(0xFF3B82F6), Modifier.weight(1f))
                statCard("已完成", stats.third, Color(0xFF10B981), Modifier.weight(1f))
            }

            if (activeReservations.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("暂无进行中的预约", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(activeReservations) { reservation ->
                        ReservationCard(
                            reservation,
                            onCancel = { cancelTargetId = reservation.id },
                            onCheckIn = {
                                val now = System.currentTimeMillis()
                                val startMs = parseToEpoch(reservation.startTime)
                                val earliestCheckIn = startMs - 30 * 60 * 1000L
                                if (now < earliestCheckIn) {
                                    message = "未到可签到时间，最早可提前30分钟签到"
                                } else {
                                    reservationViewModel.checkIn(reservation.id) { success, msg ->
                                        message = msg
                                        if (success) seatViewModel.notifyReservationChanged()
                                    }
                                }
                            },
                            onCheckOut = { checkOutTargetId = reservation.id }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun statCard(label: String, count: Int, color: Color, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 12.sp, color = color)
        }
    }
}

@Composable
fun ReservationCard(reservation: Reservation, onCancel: () -> Unit, onCheckIn: () -> Unit, onCheckOut: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Chair, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(reservation.seatNo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                AssistChip(
                    onClick = {},
                    label = { Text(statusText(reservation.status), fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = statusColor(reservation.status).copy(alpha = 0.15f)
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("${extractFloor(reservation.seatId)} | ${reservation.seatType}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(extractDate(reservation.startTime), fontSize = 12.sp, color = Color(0xFF64748B))
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("开始", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(extractTime(reservation.startTime), fontSize = 13.sp)
                }
                Column {
                    Text("结束", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(extractTime(reservation.endTime), fontSize = 13.sp)
                }
            }
            if (reservation.status == "pending") {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("取消") }
                    Button(onClick = onCheckIn, modifier = Modifier.weight(1f)) { Text("签到") }
                }
            }
            if (reservation.status == "checked_in") {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onCheckOut, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) { Text("签退") }
            }
        }
    }
}

private fun statusText(status: String) = when (status) {
    "pending" -> "待签到"
    "checked_in" -> "已签到"
    "completed" -> "已完成"
    "cancelled" -> "已取消"
    else -> status
}

private fun statusColor(status: String) = when (status) {
    "pending" -> Color(0xFFF59E0B)
    "checked_in" -> Color(0xFF3B82F6)
    "completed" -> Color(0xFF10B981)
    "cancelled" -> Color(0xFFEF4444)
    else -> Color.Gray
}

private fun extractTime(isoStr: String): String {
    val idx = isoStr.indexOf('T')
    if (idx == -1) return isoStr
    return isoStr.substring(idx + 1, (idx + 6).coerceAtMost(isoStr.length))
}

private fun extractFloor(seatId: String): String {
    val idx = seatId.indexOf('-')
    if (idx == -1) return seatId
    return seatId.substring(0, idx)
}

private fun extractDate(isoStr: String): String {
    val idx = isoStr.indexOf('T')
    if (idx == -1) return isoStr
    return isoStr.substring(0, idx)
}

private fun parseToEpoch(isoStr: String): Long {
    return try {
        val cleaned = isoStr.substringBefore("+").substringBefore("Z").trim()
        java.time.LocalDateTime.parse(cleaned)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (_: Exception) { 0L }
}
