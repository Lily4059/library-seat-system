package com.example.seatmanage.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seatmanage.data.model.Seat
import com.example.seatmanage.viewmodel.SeatViewModel

@Composable
fun SeatMapScreen(seatViewModel: SeatViewModel, onBack: () -> Unit, onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            Surface(color = Color(0xFF1E3A8A), shadowElevation = 4.dp) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                    Text("座位地图", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp,
                        modifier = Modifier.weight(1f))
                    TextButton(onClick = { onNavigate("home") }) { Text("去预约", color = Color.White, fontSize = 12.sp) }
                }
            }
        },
        bottomBar = {
            BottomNavBar("seat_map",
                onHome = { onNavigate("home") },
                onMap = { },
                onReservations = { onNavigate("my_reservations") },
                onProfile = { onNavigate("profile") })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Floor tabs
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

            // Legend
            Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendDot(Color(0xFF7AD63A), "可预约")
                LegendDot(Color(0xFFFACC15), "已约")
                LegendDot(Color(0xFFCBD5E1), "占用")
                LegendDot(Color(0xFF3B82F6), "已选")
            }

            // Floor plan - scrollable both directions
            val hScroll = rememberScrollState()
            val vScroll = rememberScrollState()
            Box(Modifier.fillMaxSize().horizontalScroll(hScroll).verticalScroll(vScroll).padding(8.dp),
                contentAlignment = Alignment.TopCenter) {
                FloorPlan(seatViewModel)
            }
        }
    }
}

@Composable
private fun FloorPlan(seatViewModel: SeatViewModel) {
    val seats = seatViewModel.activeSeats()
    val seatMap = seats.associateBy { it.seatNo }
    val selNo = seatViewModel.selectedSeat?.seatNo

    fun bg(seatNo: String): Color {
        val s = seatMap[seatNo] ?: return Color(0xFFCBD5E1)
        if (selNo == seatNo) return Color(0xFF3B82F6)
        return when (s.status) {
            "available" -> areaColor(seatNo)
            "booked", "occupied" -> Color(0xFFFACC15)
            else -> Color(0xFFCBD5E1)
        }
    }

    fun click(seatNo: String) {
        seatMap[seatNo]?.let { if (it.status == "available") seatViewModel.selectSeat(it) }
    }

    val S = 9  // dot size dp

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp)).padding(8.dp)) {

        // ===== TOP SECTION =====
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Top) {
            // Left Corner: SL study + WL window
            Column {
                StudyBlock("SL", S, ::bg, ::click)
                WindowRow("WL", S, ::bg, ::click)
            }
            // Service desk (top)
            Box(Modifier.width(54.dp).height(28.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color(0xFF8B6B23)), contentAlignment = Alignment.Center) {
                Text("服务台", fontSize = 9.sp, color = Color.White)
            }
            // Right Corner: WR window + SR study
            Column {
                WindowRow("WR", S, ::bg, ::click)
                StudyBlock("SR", S, ::bg, ::click)
            }
        }

        Spacer(Modifier.height(4.dp))

        // ===== CORRIDOR 1: DTL + PTH + DTR =====
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            DuoBlock("DTL", S, ::bg, ::click)
            LongRow("PTH", S * 2, 8, ::bg, ::click)
            DuoBlock("DTR", S, ::bg, ::click)
        }

        Spacer(Modifier.height(4.dp))

        // ===== MIDDLE: Leisure + PLV + spacer + PRV + YV =====
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            // Leisure zone
            Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFE8EEFC)),
                contentAlignment = Alignment.Center) {
                Text("休闲区", fontSize = 8.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
            }
            LongCol("PLV", S * 2, 8, ::bg, ::click)
            Spacer(Modifier.width(28.dp))
            LongCol("PRV", S * 2, 8, ::bg, ::click)
            LongCol("YV", S * 2, 8, ::bg, ::click)
        }

        Spacer(Modifier.height(4.dp))

        // ===== CORRIDOR 2: DBL + PBH + DBR =====
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            DuoBlock("DBL", S, ::bg, ::click)
            LongRow("PBH", S * 2, 8, ::bg, ::click)
            DuoBlock("DBR", S, ::bg, ::click)
        }

        Spacer(Modifier.height(4.dp))

        // ===== BOTTOM SECTION =====
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
            // Bottom Left: SBL study + WBL window
            Column {
                StudyBlock("SBL", S, ::bg, ::click)
                WindowRow("WBL", S, ::bg, ::click)
            }
            // Bottom Center: Hall + Service desk
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HallBlock(3, S, ::bg, ::click)
                Box(Modifier.width(54.dp).height(28.dp).clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(Color(0xFF8B6B23)), contentAlignment = Alignment.Center) {
                    Text("服务台", fontSize = 9.sp, color = Color.White)
                }
            }
            // Bottom Right: WBR window + SBR study
            Column {
                WindowRow("WBR", S, ::bg, ::click)
                StudyBlock("SBR", S, ::bg, ::click)
            }
        }
    }
}

// ===== BLOCK BUILDERS =====

@Composable
private fun WindowRow(prefix: String, s: Int, bg: (String) -> Color, onClick: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        for (t in 1..7) Table4("$prefix-$t", s, bg, onClick)
    }
}

@Composable
private fun StudyBlock(prefix: String, s: Int, bg: (String) -> Color, onClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        for (r in 1..4) {
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                for (t in 1..4) Table4("$prefix-$r-$t", s, bg, onClick)
            }
        }
    }
}

@Composable
private fun Table4(prefix: String, s: Int, bg: (String) -> Color, onClick: (String) -> Unit) {
    Surface(shape = RoundedCornerShape(2.dp), color = Color(0xFFF1F5F9).copy(alpha = 0.3f),
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))) {
        Column(Modifier.padding(1.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                SeatDot("$prefix-1", s, bg, onClick)
                SeatDot("$prefix-2", s, bg, onClick)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                SeatDot("$prefix-3", s, bg, onClick)
                SeatDot("$prefix-4", s, bg, onClick)
            }
        }
    }
}

@Composable
private fun DuoBlock(prefix: String, s: Int, bg: (String) -> Color, onClick: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..4) {
            Surface(shape = RoundedCornerShape(2.dp), color = Color(0xFFF1F5F9).copy(alpha = 0.3f),
                border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))) {
                Row(Modifier.padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    SeatDot("$prefix-$i-1", s, bg, onClick)
                    SeatDot("$prefix-$i-2", s, bg, onClick)
                }
            }
        }
    }
}

@Composable
private fun LongRow(prefix: String, sz: Int, count: Int, bg: (String) -> Color, onClick: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
        for (i in 1..count) SeatDot("$prefix-$i", sz, bg, onClick)
    }
}

@Composable
private fun LongCol(prefix: String, sz: Int, count: Int, bg: (String) -> Color, onClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..count) SeatDot("$prefix-$i", sz, bg, onClick)
    }
}

@Composable
private fun HallBlock(rows: Int, s: Int, bg: (String) -> Color, onClick: (String) -> Unit) {
    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF1F5F9).copy(alpha = 0.4f),
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))) {
        Column(Modifier.padding(3.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            for (r in 1..rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    for (t in 1..4) Table4("H-$r-$t", s, bg, onClick)
                }
            }
        }
    }
}

@Composable
private fun SeatDot(seatNo: String, size: Int, bg: (String) -> Color, onClick: (String) -> Unit) {
    val px = (size / 2).coerceAtLeast(5).dp
    val isSel = bg(seatNo) == Color(0xFF3B82F6)
    Box(
        Modifier.size(px).clip(RoundedCornerShape(2.dp)).background(bg(seatNo))
            .then(if (isSel) Modifier.border(1.5.dp, Color.White, RoundedCornerShape(2.dp)) else Modifier)
            .clickable { onClick(seatNo) }
    )
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(3.dp))
        Text(label, fontSize = 10.sp, color = Color(0xFF64748B))
    }
}
