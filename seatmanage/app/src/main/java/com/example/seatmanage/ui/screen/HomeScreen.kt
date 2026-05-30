package com.example.seatmanage.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seatmanage.data.model.Seat
import com.example.seatmanage.viewmodel.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    seatViewModel: SeatViewModel,
    reservationViewModel: ReservationViewModel,
    authViewModel: AuthViewModel,
    onNavigate: (String) -> Unit,
    onNavigateSeatDetail: (String, String, String, String) -> Unit
) {
    val selectedSeat = seatViewModel.selectedSeat
    var showBookingDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    if (showBookingDialog && selectedSeat != null) {
        BookingDialog(
            seat = selectedSeat!!,
            onDismiss = {
                showBookingDialog = false
                seatViewModel.selectedSeat = null
            },
            onConfirm = { date, start, end ->
                reservationViewModel.createReservation(selectedSeat!!, date, start, end) { success, msg ->
                    if (success) {
                        seatViewModel.reloadFromReservations(reservationViewModel.reservations.value)
                        seatViewModel.notifyReservationChanged()
                    } else {
                        message = msg
                    }
                }
                showBookingDialog = false
            }
        )
    }

    message?.let { msg ->
        AlertDialog(onDismissRequest = { message = null }, title = { Text("提示") },
            text = { Text(msg) }, confirmButton = { TextButton(onClick = { message = null }) { Text("确定") } })
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF1E3A8A), shadowElevation = 4.dp) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("智慧图书馆", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp,
                        modifier = Modifier.weight(1f))
                    TextButton(onClick = { onNavigate("notifications") }) {
                        Icon(Icons.Default.Notifications, null, tint = Color.White)
                    }
                }
            }
        },
        bottomBar = {
            BottomNavBar("home", onHome = { }, onMap = { onNavigate("seat_map") },
                onReservations = { onNavigate("my_reservations") }, onProfile = { onNavigate("profile") })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {

            Card(Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(12.dp)) {
                    Text("楼层空余座位", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    seatViewModel.floorStats().forEach { stat ->
                        Row(Modifier.fillMaxWidth().clickable { seatViewModel.switchFloor(stat.floor) }
                            .padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stat.floor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text("${stat.available} / ${stat.total}", color = Color(0xFF10B981), fontSize = 14.sp)
                        }
                    }
                }
            }

            ScrollableTabRow(
                selectedTabIndex = seatViewModel.floorStats().indexOfFirst { it.floor == seatViewModel.activeFloor }.coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth(), edgePadding = 8.dp
            ) {
                seatViewModel.floorStats().forEach { stat ->
                    Tab(selected = seatViewModel.activeFloor == stat.floor,
                        onClick = { seatViewModel.switchFloor(stat.floor) },
                        text = { Text(stat.floor) })
                }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(seatViewModel.filters.window, { seatViewModel.toggleFilter("window") }, label = { Text("靠窗", fontSize = 11.sp) })
                FilterChip(seatViewModel.filters.power, { seatViewModel.toggleFilter("power") }, label = { Text("电源", fontSize = 11.sp) })
                FilterChip(seatViewModel.filters.silent, { seatViewModel.toggleFilter("silent") }, label = { Text("静音", fontSize = 11.sp) })
            }

            SeatGridView(seatViewModel, onSeatClick = { showBookingDialog = true })

            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDialog(seat: Seat, onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var startHour by remember { mutableStateOf(14) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(16) }
    var endMinute by remember { mutableStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        selectedDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) { DatePicker(state = state) }
    }

    if (showStartTimePicker) {
        TimePickerDialog({ showStartTimePicker = it }, startHour, startMinute, "选择开始时间") { h, m ->
            startHour = h; startMinute = m; error = null
        }
    }

    if (showEndTimePicker) {
        TimePickerDialog({ showEndTimePicker = it }, endHour, endMinute, "选择结束时间") { h, m ->
            endHour = h; endMinute = m; error = null
        }
    }

    fun timeStr(h: Int, m: Int) = "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("预约座位", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${seat.floor} | ${seat.area} | ${seat.seatNo}",
                    fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = selectedDate.format(fmt),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("预约日期") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.CalendarToday, null) } }
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = timeStr(startHour, startMinute),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("开始时间") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        trailingIcon = { IconButton(onClick = { showStartTimePicker = true }) { Icon(Icons.Default.Schedule, null, Modifier.size(18.dp)) } }
                    )
                    OutlinedTextField(
                        value = timeStr(endHour, endMinute),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("结束时间") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        trailingIcon = { IconButton(onClick = { showEndTimePicker = true }) { Icon(Icons.Default.Schedule, null, Modifier.size(18.dp)) } }
                    )
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val s = timeStr(startHour, startMinute)
                val e = timeStr(endHour, endMinute)
                if (s >= e) { error = "结束时间需晚于开始时间"; return@Button }
                onConfirm(selectedDate.format(fmt), s, e)
            }) { Text("确认预约") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        modifier = Modifier.widthIn(max = 360.dp)
    )
}

@Composable
private fun TimePickerDialog(
    showDialog: (Boolean) -> Unit,
    initHour: Int,
    initMinute: Int,
    title: String,
    onConfirm: (Int, Int) -> Unit
) {
    var hour by remember { mutableStateOf(initHour) }
    var minute by remember { mutableStateOf(initMinute) }

    AlertDialog(
        onDismissRequest = { showDialog(false) },
        modifier = Modifier.widthIn(max = 300.dp),
        title = { Text(title) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { hour = ((hour - 1 + 24) % 24).coerceIn(8, 21) }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                        Text("${hour.toString().padStart(2, '0')}", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { hour = ((hour + 1) % 24).coerceIn(8, 21) }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                    }
                    Text(":", fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { minute = if (minute == 0) 30 else 0 }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                        Text("${minute.toString().padStart(2, '0')}", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { minute = if (minute == 0) 30 else 0 }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                    }
                }
                Text("08:00 - 22:00", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(hour, minute); showDialog(false) }) { Text("确定") } },
        dismissButton = { TextButton(onClick = { showDialog(false) }) { Text("取消") } }
    )
}

@Composable
fun SeatGridView(seatViewModel: SeatViewModel, onSeatClick: () -> Unit = {}) {
    val selectedSeat = seatViewModel.selectedSeat
    val groups = seatViewModel.filteredSeats().groupBy { it.area }
    val areaOrder = listOf("靠窗区", "靠窗长桌区", "自习区（靠窗）", "自习区", "长桌区", "双人协作区", "大厅区", "普通区")
    val sorted = groups.entries.sortedBy { (area, _) ->
        val idx = areaOrder.indexOf(area)
        if (idx == -1) Int.MAX_VALUE else idx
    }

    sorted.forEach { (area, seatsList) ->
        Card(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
            Column(Modifier.padding(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(area, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("${seatsList.count { it.status == "available" }} / ${seatsList.size}",
                        fontSize = 11.sp, color = Color(0xFF10B981))
                }
                Spacer(Modifier.height(4.dp))
                val cols = 8
                val rows = (seatsList.size + cols - 1) / cols
                Column {
                    repeat(rows) { r ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                            repeat(cols) { c ->
                                val idx = r * cols + c
                                if (idx < seatsList.size) {
                                    val seat = seatsList[idx]
                                    val isSel = selectedSeat?.id == seat.id
                                    val bg = when {
                                        isSel -> Color(0xFF3B82F6)
                                        seat.status == "available" -> areaColor(seat.seatNo)
                                        seat.status == "booked" || seat.status == "occupied" -> Color(0xFFFACC15)
                                        else -> Color(0xFFCBD5E1)
                                    }
                                    Box(
                                        Modifier.weight(1f).aspectRatio(1f).padding(1.dp)
                                            .clip(RoundedCornerShape(3.dp)).background(bg)
                                            .then(if (seat.status == "available") Modifier.clickable {
                                                seatViewModel.selectSeat(seat)
                                                onSeatClick()
                                            } else Modifier)
                                            .then(if (isSel) Modifier.border(2.dp, Color.White, RoundedCornerShape(3.dp)) else Modifier),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(seat.seatNo.takeLast(3), fontSize = 7.sp,
                                            color = if (isSel || seat.status != "available") Color.White else Color(0xFF1E293B))
                                    }
                                } else Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    onHome: () -> Unit,
    onMap: () -> Unit,
    onReservations: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 8.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().height(52.dp).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem("首页", Icons.Default.Home, currentRoute == "home", onHome)
            BottomNavItem("地图", Icons.Default.Map, currentRoute == "seat_map", onMap)
            BottomNavItem("预约", Icons.Default.CalendarMonth, currentRoute == "my_reservations", onReservations)
            BottomNavItem("我的", Icons.Default.Person, currentRoute == "profile", onProfile)
        }
    }
}

@Composable
private fun BottomNavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(Modifier.clickable { onClick() }.padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, label, modifier = Modifier.size(22.dp),
            tint = if (selected) Color(0xFF1E3A8A) else Color(0xFF94A3B8))
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 10.sp,
            color = if (selected) Color(0xFF1E3A8A) else Color(0xFF94A3B8),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

fun areaColor(seatNo: String): Color = when {
    seatNo.startsWith("WL-") || seatNo.startsWith("WR-") || seatNo.startsWith("WBL-") || seatNo.startsWith("WBR-") -> Color(0xFF7AD63A)
    seatNo.startsWith("SL-") || seatNo.startsWith("SR-") || seatNo.startsWith("SBL-") || seatNo.startsWith("SBR-") -> Color(0xFFDBEAFE)
    seatNo.startsWith("PTH-") || seatNo.startsWith("PBH-") || seatNo.startsWith("PLV-") || seatNo.startsWith("PRV-") -> Color(0xFFF9A8D4)
    seatNo.startsWith("YV-") -> Color(0xFFFDE047)
    seatNo.startsWith("DTL-") || seatNo.startsWith("DTR-") || seatNo.startsWith("DBL-") || seatNo.startsWith("DBR-") -> Color(0xFF2BB7B3)
    seatNo.startsWith("H-") -> Color(0xFF7C3AED)
    else -> Color(0xFFDBEAFE)
}
