package com.example.seatmanage.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seatmanage.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(authViewModel: AuthViewModel, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    val user = authViewModel.currentUser
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("退出登录") },
            text = { Text("确定要退出当前账号吗？") },
            confirmButton = {
                TextButton(onClick = { authViewModel.logout(); onLogout() }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("取消") } }
        )
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF1E3A8A), shadowElevation = 4.dp) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("个人中心", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp,
                        modifier = Modifier.weight(1f).padding(start = 16.dp))
                    IconButton(onClick = { onNavigate("settings") }) {
                        Icon(Icons.Default.Settings, "设置", tint = Color.White)
                    }
                }
            }
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "profile",
                onHome = { onNavigate("home") },
                onMap = { onNavigate("seat_map") },
                onReservations = { onNavigate("my_reservations") },
                onProfile = { }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(64.dp).clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primaryContainer) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        user.name.take(1).ifEmpty { "U" },
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(user.name.ifEmpty { "张三" }, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("学号 ${user.studentNo.ifEmpty { "20261001" }}", fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        statItem(user.creditScore.toString(), "信用积分", onNavigate, "credit_log")
                        statItem("${user.totalStudyTime}分钟", "学习时长", onNavigate, "study_time")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("信用等级：良好", fontSize = 13.sp, color = Color(0xFF67C23A))
                }
            }

            Text("功能菜单", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                menuItem("我的预约", Icons.Default.CalendarMonth) { onNavigate("my_reservations") }
                menuItem("历史记录", Icons.Default.History) { onNavigate("history") }
                menuItem("积分明细", Icons.Default.Stars) { onNavigate("credit_log") }
                menuItem("信用攻略", Icons.Default.TipsAndUpdates) { onNavigate("credit_strategy") }
                menuItem("消息通知", Icons.Default.Notifications) { onNavigate("notifications") }
                menuItem("账号管理", Icons.Default.ManageAccounts) { onNavigate("settings") }
                menuItem("帮助与反馈", Icons.Default.Help) { onNavigate("help_feedback") }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("退出登录")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun statItem(value: String, label: String, onNavigate: (String) -> Unit, route: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onNavigate(route) }.padding(8.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun menuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(title, fontSize = 15.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
