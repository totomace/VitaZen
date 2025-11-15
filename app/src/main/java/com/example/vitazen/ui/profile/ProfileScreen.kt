package com.example.vitazen.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vitazen.ui.home.HomeScreen

// Data class cho menu items
data class ProfileMenuItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val route: String? = null
)

@Composable
fun ProfileScreen(
    navController: NavController,
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onRemindersClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val profileMenuItems = remember {
        listOf(
            ProfileMenuItem(1, "Chỉnh sửa hồ sơ", "Cập nhật thông tin cá nhân", Icons.Default.Edit, Color(0xFF6200EE)),
            ProfileMenuItem(2, "Lịch sử sức khỏe", "Xem dữ liệu theo thời gian", Icons.Default.History, Color(0xFF4CAF50)),
            ProfileMenuItem(3, "Nhắc nhở", "Quản lý thông báo sức khỏe", Icons.Default.Notifications, Color(0xFF2196F3)),
            ProfileMenuItem(4, "Cài đặt", "Tùy chỉnh ứng dụng", Icons.Default.Settings, Color(0xFFFF9800)),
            ProfileMenuItem(5, "Giới thiệu", "Thông tin về ứng dụng", Icons.Default.Info, Color(0xFFE91E63)),
            ProfileMenuItem(6, "Đăng xuất", "Thoát khỏi tài khoản", Icons.Default.Logout, Color(0xFFF44336))
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header với ảnh đại diện
        item {
            ProfileHeader(
                onBackClick = onBackClick,
                onEditProfileClick = onEditProfileClick
            )
        }

        // Thống kê nhanh
        item {
            HealthStatsSection()
        }

        // Menu chức năng - Sửa lỗi items ở đây
        items(
            items = profileMenuItems,
            key = { it.id }
        ) { item ->
            ProfileMenuItem(
                item = item,
                onClick = {
                    when (item.id) {
                        1 -> onEditProfileClick()
                        2 -> onHistoryClick()
                        3 -> onRemindersClick()
                        4 -> onSettingsClick()
                        5 -> onAboutClick()
                        6 -> onLogoutClick()
                    }
                }
            )
        }

        // Footer
        item {
            ProfileFooter()
        }
    }
}

@Composable
fun ProfileHeader(
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6200EE), Color(0xFF3700B3))
                )
            )
    ) {
        // Nút back
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(16.dp)
                .size(24.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nguyễn Văn A",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "nguyenvana@email.com",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nút chỉnh sửa hồ sơ
            OutlinedButton(
                onClick = onEditProfileClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.8f))
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Chỉnh sửa",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chỉnh sửa hồ sơ")
            }
        }
    }
}

@Composable
fun HealthStatsSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Thống kê sức khỏe",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    value = "68 kg",
                    label = "Cân nặng",
                    color = Color(0xFF6200EE)
                )

                StatItem(
                    value = "22.5",
                    label = "BMI",
                    color = Color(0xFF4CAF50)
                )

                StatItem(
                    value = "72",
                    label = "Nhịp tim",
                    color = Color(0xFFF44336)
                )

                StatItem(
                    value = "120/80",
                    label = "Huyết áp",
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF718096),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProfileMenuItem(
    item: ProfileMenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D3748)
                )
                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF718096)
                )
            }

            // Arrow icon for non-logout items
            if (item.id != 6) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Mở",
                    tint = Color(0xFFA0AEC0),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "VitaZen - Ứng dụng theo dõi sức khỏe",
            fontSize = 14.sp,
            color = Color(0xFF718096),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Phiên bản 1.0.0",
            fontSize = 12.sp,
            color = Color(0xFFA0AEC0),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}



@Preview
@Composable
fun ProfileFooterp() { // DEFINITION 2 - The duplicate
    ProfileFooter()
}
@Preview
@Composable
fun HealthStatsSectionp() {
    HealthStatsSection()
}

