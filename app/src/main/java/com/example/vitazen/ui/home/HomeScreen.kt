package com.example.vitazen.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.*
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vitazen.viewmodel.HomeViewModel
import com.example.vitazen.viewmodel.HealthActivity
import com.example.vitazen.model.data.HealthData
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home

import androidx.compose.ui.tooling.preview.Preview
import com.example.vitazen.model.repository.HealthDataRepository
import com.example.vitazen.model.database.VitaZenDatabase
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.activity.compose.BackHandler

// Định nghĩa các màu sắc trong file này nếu chưa có trong theme
val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Green500 = Color(0xFF4CAF50)
val Red500 = Color(0xFFF44336)
val Blue500 = Color(0xFF2196F3)
val Purple400 = Color(0xFFAB47BC)
val Purple300 = Color(0xFFBA68C8)




@Composable
fun HomeScreen(
    onAddDataClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToReminder: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    // Thay đổi ở đây
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Xử lý nút back - thoát app thay vì quay lại màn hình trước
    BackHandler {
        // Thoát app
        (context as? androidx.activity.ComponentActivity)?.finish()
    }
    val healthDataRepository = remember { HealthDataRepository(VitaZenDatabase.getInstance(context).healthDataDao()) }
    val viewModel: HomeViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(
                com.example.vitazen.model.repository.UserRepository(VitaZenDatabase.getInstance(context).userDao()),
                healthDataRepository
            ) as T
        }
    })
    val uiState by viewModel.uiState.collectAsState()

    var showInputDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
    ) {
        // Overlay vàng nhạt nhẹ cho cảm giác ấm áp, hài hòa với login
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(com.example.vitazen.ui.theme.VitaZenYellow.copy(alpha = 0.08f))
        )
        Column(modifier = Modifier.fillMaxSize()) {
            // Nội dung chính
            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Header chào mừng
                item { WelcomeHeader(userName = uiState.userName) }
                // Tổng quan sức khỏe
                item { HealthOverviewCard(healthData = uiState.healthData) }
                // Quick Actions
                item {
                    QuickActionsRow(
                        onAddDataClick = { showInputDialog = true },
                        onStatsClick = onStatsClick,
                        onHistoryClick = onHistoryClick
                    )
                }
                // Biểu đồ theo dõi
                item { ChartSection(weekData = uiState.weekData) }
                // Hoạt động gần đây
                item { RecentActivitiesHeader(onHistoryClick = onHistoryClick) }
                items(uiState.healthActivities) { activity ->
                    HealthActivityItem(activity = activity)
                }
            }
            // Thanh điều hướng dưới
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }

        // Dialog nhập dữ liệu sức khỏe
        HealthDataInputDialog(
            show = showInputDialog,
            initialData = uiState.healthData,
            onDismiss = { showInputDialog = false },
            onSave = { w, h, hr, wi ->
                viewModel.saveHealthDataWithHistory(w, h, hr, wi)
                showInputDialog = false
            },
            viewModel = viewModel // Thêm tham số viewModel ở đây
        )
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val backgroundColor = com.example.vitazen.ui.theme.VitaZenYellow.copy(alpha = 0.95f)
    val selectedColor = Color(0xFFFC913A)
    val unselectedColor = Color(0xFF6D6D6D)
    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = backgroundColor,
        modifier = Modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                // Xóa .shadow để không đổ bóng
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationTabItem(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                icon = Icons.Default.Home,
                label = "Trang chủ",
                selectedColor = selectedColor,
                unselectedColor = unselectedColor
            )
            NavigationTabItem(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                icon = Icons.Default.Notifications,
                label = "Nhắc nhở",
                selectedColor = selectedColor,
                unselectedColor = unselectedColor
            )
            NavigationTabItem(
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                icon = Icons.Default.History,
                label = "Lịch sử",
                selectedColor = selectedColor,
                unselectedColor = unselectedColor
            )
            NavigationTabItem(
                selected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                icon = Icons.Default.Settings,
                label = "Cài đặt",
                selectedColor = selectedColor,
                unselectedColor = unselectedColor
            )
        }
    }
}

@Composable
fun NavigationTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selectedColor: Color,
    unselectedColor: Color
) {
    val iconColor = if (selected) selectedColor else unselectedColor
    val textColor = if (selected) selectedColor else unselectedColor.copy(alpha = 0.7f)
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
    val indicator = if (selected) Modifier
        .padding(bottom = 2.dp)
        .size(width = 32.dp, height = 4.dp)
        .background(selectedColor, RoundedCornerShape(2.dp))
    else Modifier.size(0.dp)

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = fontWeight,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = indicator)
    }
}

@Composable
fun WelcomeHeader(userName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Purple200, Purple500)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar/Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "User",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Xin chào!",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = userName,
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hôm nay bạn cảm thấy thế nào?",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun HealthOverviewCard(healthData: HealthData?) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Tổng quan sức khỏe",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly // Sửa lại cho đều
            ) {
                HealthMetricItem(
                    title = "Cân nặng",
                    value = healthData?.weight?.let { "${it} kg" } ?: "--",
                    subtitle = getWeightStatus(healthData?.weight, healthData?.height),
                    color = Purple500,
                    statusColor = getStatusColor(getWeightStatus(healthData?.weight, healthData?.height)),
                    modifier = Modifier.weight(1f)
                )
                HealthMetricItem(
                    title = "Chiều cao",
                    value = healthData?.height?.let { "${it} cm" } ?: "--",
                    subtitle = "",
                    color = Blue500,
                    modifier = Modifier.weight(1f)
                )
                HealthMetricItem(
                    title = "BMI",
                    value = healthData?.let {
                        if (it.height > 0f) {
                            val bmi = it.weight / ((it.height / 100f) * (it.height / 100f))
                            String.format("%.1f", bmi)
                        } else "--"
                    } ?: "--",
                    subtitle = getBMIStatus(healthData?.weight, healthData?.height),
                    color = Color(0xFF43A047),
                    statusColor = getStatusColor(getBMIStatus(healthData?.weight, healthData?.height)),
                    modifier = Modifier.weight(1f)
                )
                HealthMetricItem(
                    title = "Nhịp tim",
                    value = healthData?.heartRate?.let { "$it bpm" } ?: "--",
                    subtitle = "",
                    color = Red500,
                    modifier = Modifier.weight(1f)
                )
                HealthMetricItem(
                    title = "Nước uống",
                    value = healthData?.waterIntake?.let { "${it} l" } ?: "--",
                    subtitle = "",
                    color = Green500,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun HealthMetricItem(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    statusColor: Color = Color.Gray,
    modifier: Modifier = Modifier // Thêm modifier để chia đều không gian
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            color = Color(0xFF718096),
            textAlign = TextAlign.Center
        )

        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = statusColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun QuickActionsRow(
    onAddDataClick: () -> Unit,
    onStatsClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sử dụng Box với weight thay vì modifier trực tiếp trên Card
        Box(modifier = Modifier.weight(1f)) {
            QuickActionButton(
                title = "Nhập dữ liệu",
                icon = Icons.Default.Add,
                color = Purple500,
                onClick = onAddDataClick
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            QuickActionButton(
                title = "Biểu đồ",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                color = Green500,
                onClick = onStatsClick
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            QuickActionButton(
                title = "Lịch sử",
                icon = Icons.Default.MonitorHeart,
                color = Blue500,
                onClick = onHistoryClick
            )
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChartSection(weekData: List<com.example.vitazen.viewmodel.WeekData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sức khỏe tuần này",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Biểu đồ cột chồng
            if (weekData.any { it.weight != null }) {
                WeekStackedBarChart(weekData = weekData)
            } else {
                // Hiển thị empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFFF7FAFC), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Chưa có dữ liệu",
                            tint = Color(0xFF718096),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Chưa có dữ liệu tuần này",
                            color = Color(0xFF718096),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Thêm dữ liệu để xem biểu đồ",
                            color = Color(0xFF718096).copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend - chú thích màu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChartColorLegend("Cân nặng", Purple500)
                ChartColorLegend("Nhịp tim", Red500)
                ChartColorLegend("Nước", Blue500)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Labels ngày trong tuần
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekData.forEach { data ->
                    Text(
                        text = data.dayLabel,
                        fontSize = 11.sp,
                        color = if (data.weight != null) Color(0xFF2D3748) else Color(0xFF718096),
                        fontWeight = if (data.weight != null) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun ChartColorLegend(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF718096)
        )
    }
}

@Composable
fun ChartLegend(day: String, value: String, hasData: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = day,
            fontSize = 11.sp,
            color = if (hasData) Color(0xFF2D3748) else Color(0xFF718096),
            fontWeight = if (hasData) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (hasData) Purple500 else Color(0xFFE2E8F0))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 9.sp,
            color = if (hasData) Color(0xFF2D3748) else Color(0xFF718096),
            fontWeight = if (hasData) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun WeekStackedBarChart(weekData: List<com.example.vitazen.viewmodel.WeekData>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFFF7FAFC), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val barWidth = chartWidth / 7f * 0.7f // 70% của khoảng trống
            val spacing = chartWidth / 7f
            
            // Giá trị chuẩn hóa
            val maxWeight = 100f // Tối đa 100kg
            val maxHeartRate = 150f // Tối đa 150 bpm
            val maxWater = 5f // Tối đa 5 lít
            
            weekData.forEachIndexed { index, data ->
                val x = index * spacing + (spacing - barWidth) / 2f
                
                if (data.weight != null) {
                    // Vẽ 3 cột chồng lên nhau với độ cao tương ứng
                    var currentY = chartHeight
                    
                    // 1. Cân nặng (nền - màu tím)
                    val weightHeight = (data.weight / maxWeight) * chartHeight * 0.8f
                    drawRoundRect(
                        color = Purple500,
                        topLeft = androidx.compose.ui.geometry.Offset(x, currentY - weightHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, weightHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                    currentY -= weightHeight
                    
                    // 2. Nhịp tim (giả sử 70 bpm, có thể lấy từ data sau) - màu đỏ
                    val heartRate = 70f + (index * 2f) // Giả lập dao động
                    val heartHeight = (heartRate / maxHeartRate) * chartHeight * 0.3f
                    drawRoundRect(
                        color = Red500,
                        topLeft = androidx.compose.ui.geometry.Offset(x, currentY - heartHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, heartHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                    currentY -= heartHeight
                    
                    // 3. Nước uống (giả sử 2L) - màu xanh dương
                    val water = 2f + (index * 0.1f) // Giả lập dao động
                    val waterHeight = (water / maxWater) * chartHeight * 0.25f
                    drawRoundRect(
                        color = Blue500,
                        topLeft = androidx.compose.ui.geometry.Offset(x, currentY - waterHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, waterHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentActivitiesHeader(onHistoryClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hoạt động gần đây",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )

        TextButton(onClick = onHistoryClick) {
            Text(
                text = "Xem tất cả",
                fontSize = 14.sp,
                color = Purple500,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun HealthActivityItem(activity: HealthActivity) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon với màu nền
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(activity.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MonitorHeart,
                    contentDescription = activity.type,
                    tint = activity.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Thông tin - Sửa lỗi weight ở đây
            Box(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = activity.type,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D3748)
                    )
                    Text(
                        text = activity.date,
                        fontSize = 12.sp,
                        color = Color(0xFF718096)
                    )
                }
            }

            // Giá trị
            Text(
                text = activity.value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = activity.color
            )
        }
    }
}

@Composable
fun HealthDataInputDialog(
    show: Boolean,
    initialData: HealthData?,
    onDismiss: () -> Unit,
    onSave: (Float, Float, Int?, Float) -> Unit,
    viewModel: HomeViewModel
) {
    var weight by remember { mutableStateOf(initialData?.weight?.toString() ?: "") }
    var height by remember { mutableStateOf(initialData?.height?.toString() ?: "") }
    var heartRate by remember { mutableStateOf(initialData?.heartRate?.toString() ?: "") }
    var waterIntake by remember { mutableStateOf(initialData?.waterIntake?.toString() ?: "") }
    var yesterdayData by remember { mutableStateOf<HealthData?>(null) }

    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Theo Dõi Sức Khỏe Hàng Ngày",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1746A2),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black, textAlign = TextAlign.Start),
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Default.MonitorHeart),
                                    contentDescription = null,
                                    tint = Color(0xFF1746A2),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Cân nặng (kg)", color = Color(0xFF1746A2))
                            }
                        },
                        placeholder = { Text("Nhập cân nặng...", color = Color.Gray, textAlign = TextAlign.Start) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black, textAlign = TextAlign.Start),
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Default.Favorite),
                                    contentDescription = null,
                                    tint = Color(0xFF8F43FD),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Chiều cao (cm)", color = Color(0xFF8F43FD))
                            }
                        },
                        placeholder = { Text("Nhập chiều cao...", color = Color.Gray, textAlign = TextAlign.Start) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = heartRate,
                        onValueChange = { heartRate = it },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Default.Favorite),
                                    contentDescription = null,
                                    tint = Color(0xFFFF3A44),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Nhịp tim (bpm)", color = Color(0xFFFF3A44))
                            }
                        },
                        placeholder = { Text("Nhập nhịp tim...", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = waterIntake,
                        onValueChange = { waterIntake = it },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Default.MonitorHeart),
                                    contentDescription = null,
                                    tint = Color(0xFF00B2FF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Số lít nước đã uống (L)", color = Color(0xFF00B2FF))
                            }
                        },
                        placeholder = { Text("Nhập số lít nước...", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Button(
                            onClick = {
                                viewModel.loadYesterdayHealthData { data ->
                                    yesterdayData = data
                                    if (data != null) {
                                        weight = data.weight.toString()
                                        height = data.height.toString()
                                        heartRate = data.heartRate?.toString() ?: ""
                                        waterIntake = data.waterIntake.toString()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB2B2B2)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text("Lấy dữ liệu hôm qua", color = Color.White, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = weight.toFloatOrNull() ?: yesterdayData?.weight ?: 0f
                        val h = height.toFloatOrNull() ?: yesterdayData?.height ?: 0f
                        val hr = if (heartRate.isNotBlank()) heartRate.toIntOrNull() else yesterdayData?.heartRate
                        val wi = waterIntake.toFloatOrNull() ?: yesterdayData?.waterIntake ?: 0f
                        onSave(w, h, hr, wi)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Cập Nhật & Lưu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Hủy") }
            }
        )
    }
}

fun getWeightStatus(weight: Float?, height: Float?): String {
    if (weight == null || height == null || height <= 0f) return "--"
    val bmi = weight / ((height / 100f) * (height / 100f))
    return getBMIStatusByValue(bmi)
}

fun getBMIStatus(weight: Float?, height: Float?): String {
    if (weight == null || height == null || height <= 0f) return "--"
    val bmi = weight / ((height / 100f) * (height / 100f))
    return getBMIStatusByValue(bmi)
}

fun getBMIStatusByValue(bmi: Float): String {
    return when {
        bmi < 18.5f -> "Thiếu cân"
        bmi < 25f -> "Bình thường"
        bmi < 30f -> "Thừa cân"
        else -> "Béo phì"
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Thiếu cân" -> Color(0xFF42A5F5)
        "Bình thường" -> Color(0xFF43A047)
        "Thừa cân" -> Color(0xFFFFA726)
        "Béo phì" -> Color(0xFFE53935)
        else -> Color.Gray
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
