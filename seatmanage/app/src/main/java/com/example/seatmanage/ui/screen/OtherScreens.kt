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
import com.example.seatmanage.data.MockData
import com.example.seatmanage.data.model.Reservation
import com.example.seatmanage.viewmodel.AuthViewModel
import com.example.seatmanage.viewmodel.ReservationViewModel
import com.example.seatmanage.viewmodel.SeatViewModel
import java.time.format.DateTimeFormatter

// ---------- Auth / BindStudent ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BindStudentScreen(authViewModel: AuthViewModel, onBindSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var studentNo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("绑定学号") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(32.dp))
            Icon(Icons.Default.School, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("身份认证", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("绑定学号后方可使用", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(name, { name = it }, label = { Text("真实姓名") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(studentNo, { studentNo = it }, label = { Text("学号/工号") },
                leadingIcon = { Icon(Icons.Default.Badge, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (name.isBlank() || studentNo.isBlank()) { error = "请填写完整信息"; return@Button }
                    authViewModel.bindStudent(name, studentNo) { success, msg ->
                        if (success) onBindSuccess() else error = msg
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)
            ) { Text("确认绑定") }
        }
    }
}

// ---------- History ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(reservationViewModel: ReservationViewModel, onBack: () -> Unit) {
    val historyList = reservationViewModel.reservations.value.filter {
        it.status == "completed" || it.status == "cancelled"
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF1E3A8A), shadowElevation = 4.dp) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                    Text("历史记录", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp,
                        modifier = Modifier.weight(1f))
                }
            }
        }
    ) { padding ->
        if (historyList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("暂无历史记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(historyList) { reservation ->
                    HistoryItem(reservation)
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(reservation: Reservation) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Chair, null, Modifier.size(18.dp), tint = Color(0xFF64748B))
                    Spacer(Modifier.width(4.dp))
                    Text("${extractFloor(reservation.seatId)} ${reservation.seatNo}",
                        fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
                Surface(shape = RoundedCornerShape(4.dp),
                    color = when (reservation.status) {
                        "completed" -> Color(0xFF10B981).copy(alpha = 0.12f)
                        else -> Color(0xFFEF4444).copy(alpha = 0.12f)
                    }) {
                    Text(
                        if (reservation.status == "completed") "已完成" else "已取消",
                        fontSize = 11.sp,
                        color = if (reservation.status == "completed") Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(reservation.seatType, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(extractDate(reservation.startTime), fontSize = 11.sp, color = Color(0xFF94A3B8))
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column {
                    Text("开始", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(extractTime(reservation.startTime), fontSize = 12.sp)
                }
                Column {
                    Text("结束", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(extractTime(reservation.endTime), fontSize = 12.sp)
                }
            }
        }
    }
}

// ---------- CreditLog ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditLogScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("积分明细") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(MockData.creditRecords) { record ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(record.title, fontWeight = FontWeight.Medium)
                            Text(record.time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            if (record.points > 0) "+${record.points}" else "${record.points}",
                            fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = if (record.points > 0) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

// ---------- CreditStrategy ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditStrategyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("信用攻略") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("如何获取积分", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            strategyItem("按时签到", "+3分", "在预约时段开始前后15分钟内完成签到")
            strategyItem("按时签退", "+2分", "在预约时段结束时正常签退")
            strategyItem("连续预约", "+2分/天", "连续多天预约并完成签到")
            strategyItem("取消预约", "-2分", "在预约时段开始前取消预约")
            strategyItem("占座违规", "-10分", "离开座位超时未返回")
            strategyItem("爽约", "-15分", "预约后未按时签到")
            strategyItem("违规冻结", "冻结7天", "累计爽约3次将冻结预约权限7天")
        }
    }
}

@Composable
private fun strategyItem(title: String, points: String, desc: String) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(points, fontWeight = FontWeight.Bold,
                color = if (points.startsWith("+")) Color(0xFF10B981) else Color(0xFFEF4444))
        }
    }
}

// ---------- Notifications ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("消息通知") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(MockData.notifications) { notification ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!notification.isRead)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(notification.title, fontWeight = FontWeight.Medium)
                            Text(notification.time.take(10), fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(notification.summary, fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ---------- Settings ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(authViewModel: AuthViewModel, onBack: () -> Unit, onLogout: () -> Unit) {
    var showLogout by remember { mutableStateOf(false) }

    if (showLogout) {
        AlertDialog(onDismissRequest = { showLogout = false }, title = { Text("退出登录") },
            text = { Text("确定退出？") },
            confirmButton = { TextButton(onClick = { authViewModel.logout(); onLogout() }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showLogout = false }) { Text("取消") } })
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("账号管理") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            menuItem("修改密码", Icons.Default.Lock) {}
            menuItem("绑定学号", Icons.Default.School) {}
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { showLogout = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) { Text("退出登录") }
        }
    }
}

// ---------- Favorites ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("收藏座位") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Star, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("暂无收藏座位", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ---------- HelpFeedback ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpFeedbackScreen(onBack: () -> Unit) {
    var feedback by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("帮助与反馈") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("常见问题", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("如何预约座位？", fontWeight = FontWeight.Medium)
                    Text("在首页选择楼层和座位，选择日期和时间段后确认预约即可。",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(feedback, { feedback = it }, label = { Text("反馈意见") },
                modifier = Modifier.fillMaxWidth().height(120.dp))
            Spacer(Modifier.height(12.dp))
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("提交反馈") }
        }
    }
}

// ---------- StudyTime ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyTimeScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("学习时长") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(32.dp))
            Text("本周学习时长", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("360 分钟", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("本月", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("1200分钟", fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("累计", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("3600分钟", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ---------- CheckIn ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(reservationViewModel: ReservationViewModel, onBack: () -> Unit) {
    val pendingReservations = reservationViewModel.reservations.value.filter { it.status == "pending" }
    var message by remember { mutableStateOf<String?>(null) }

    message?.let {
        AlertDialog(onDismissRequest = { message = null }, title = { Text("提示") },
            text = { Text(it) }, confirmButton = { TextButton(onClick = { message = null }) { Text("确定") } })
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("一键签到") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (pendingReservations.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("当前没有需要签到的预约")
                    }
                }
            } else {
                pendingReservations.forEach { res ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("座位：${res.seatNo}", fontWeight = FontWeight.Bold)
                            Text("时段：${res.startTime.takeLast(5)} - ${res.endTime.takeLast(5)}",
                                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val now = System.currentTimeMillis()
                                    val startMs = try {
                                        val cleaned = res.startTime.substringBefore("+").substringBefore("Z").trim()
                                        java.time.LocalDateTime.parse(cleaned)
                                            .atZone(java.time.ZoneId.systemDefault())
                                            .toInstant()
                                            .toEpochMilli()
                                    } catch (_: Exception) { 0L }
                                    val earliestCheckIn = startMs - 30 * 60 * 1000L
                                    if (now < earliestCheckIn) {
                                        message = "未到可签到时间，最早可提前30分钟签到"
                                    } else {
                                        reservationViewModel.checkIn(res.id) { _, msg -> message = msg }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("一键签到") }
                        }
                    }
                }
            }
        }
    }
}

// ---------- Confirm ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmScreen(
    seatId: String, date: String, startTime: String, endTime: String,
    seatViewModel: SeatViewModel, reservationViewModel: ReservationViewModel,
    onBack: () -> Unit, onSuccess: () -> Unit
) {
    val seat = seatViewModel.activeSeats().find { it.id == seatId }
    var message by remember { mutableStateOf<String?>(null) }

    message?.let {
        AlertDialog(onDismissRequest = { message = null }, title = { Text("提示") },
            text = { Text(it) }, confirmButton = { TextButton(onClick = { message = null; onSuccess() }) { Text("确定") } })
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("确认预约") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (seat == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("座位信息不存在")
                }
            } else {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("预约信息确认", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(16.dp))
                        infoRow("楼层", seat.floor)
                        infoRow("区域", seat.area)
                        infoRow("座位号", seat.seatNo)
                        infoRow("日期", date)
                        infoRow("开始时间", startTime)
                        infoRow("结束时间", endTime)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    reservationViewModel.createReservation(seat, date, startTime, endTime) { success, msg ->
                        if (success) {
                            seatViewModel.reloadFromReservations(reservationViewModel.reservations.value)
                            seatViewModel.notifyReservationChanged()
                        }
                        message = if (success) "预约成功！" else msg
                    }
                }, modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("确认预约", fontSize = 16.sp) }
            }
        }
    }
}

@Composable
private fun infoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, Modifier.width(80.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
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

// ---------- CurrentSeat ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentSeatScreen(reservationViewModel: ReservationViewModel, onBack: () -> Unit) {
    val current = reservationViewModel.reservations.value.firstOrNull {
        it.status == "checked_in"
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("当前座位") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (current == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("当前没有正在使用的座位", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("当前预约", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(12.dp))
                        infoRow("座位", current.seatNo)
                        infoRow("类型", current.seatType)
                        infoRow("开始", current.startTime)
                        infoRow("结束", current.endTime)
                    }
                }
            }
        }
    }
}
