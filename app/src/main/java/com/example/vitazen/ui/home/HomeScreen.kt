package com.example.vitazen.ui.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.border

import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.RectangleShape

import androidx.compose.ui.tooling.preview.Preview
import com.example.vitazen.viewmodel.HomeUiState


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
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddDataForm by remember { mutableStateOf(false) }

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
                item {
                    HealthOverviewCard(
                        uiState = uiState,
                        onAddWater = { delta -> viewModel.addWaterLiters(delta) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = 200.dp)
                            .padding(vertical = 8.dp, horizontal = 0.dp)
                    )
                }

                // Quick actions row
                item {
                    QuickActionsRow(
                        onAddDataClick = { showAddDataForm = true },
                        onStatsClick = onStatsClick,
                        onHistoryClick = onHistoryClick
                    )
                }

                // (Moved) The add-data form is shown as an overlay dialog-styled card instead of an inline item.
                // Biểu đồ theo dõi
                item { ChartSection() }
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

        // Previously used a bottom sheet for add-data; now show a centered overlay card with dimmed background.
        if (showAddDataForm) {
            var selectedType by remember { mutableStateOf("Cân nặng") }
            var value by remember { mutableStateOf("") }
            var autoCalcBMI by remember { mutableStateOf(true) }
            val offsetY = remember { mutableStateOf(0f) }
            var heightInput by remember { mutableStateOf(uiState.heightCm?.toInt()?.toString() ?: "") }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(onClick = { showAddDataForm = false }),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .offset { IntOffset(0, offsetY.value.roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                val (_, dy) = dragAmount
                                offsetY.value = (offsetY.value + dy).coerceAtLeast(-500f)
                                change.consume()
                            }
                        }
                        .windowInsetsPadding(WindowInsets.ime)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Theo Dõi Sức Khỏe Hàng Ngày",
                                color = Color(0xFF1e3a8a),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .border(BorderStroke(2.dp, Color(0xFFF0F0F0)), shape = RectangleShape),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(15.dp))

                            var weightError by remember { mutableStateOf<String?>(null) }
                            var heightError by remember { mutableStateOf<String?>(null) }
                            var heartRate by remember { mutableStateOf("") }
                            var heartRateError by remember { mutableStateOf<String?>(null) }
                            var waterInput by remember { mutableStateOf("") }
                            var waterError by remember { mutableStateOf<String?>(null) }

                            // Weight
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚖️", fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
                                    Text("Cân nặng (kg):", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF333333))
                                }
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = {
                                        value = it
                                        weightError = null
                                    },
                                    placeholder = { Text("Nhập cân nặng...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    isError = weightError != null,
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                )
                                if (weightError != null) {
                                    Text(text = weightError!!, color = Color.Red, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))

                            // Height
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🟪", fontSize = 16.sp, color = Color(0xFF7C3AED), modifier = Modifier.padding(end = 4.dp))
                                    Text("Chiều cao (cm):", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF333333))
                                }
                                OutlinedTextField(
                                    value = heightInput,
                                    onValueChange = {
                                        heightInput = it
                                        heightError = null
                                    },
                                    placeholder = { Text("Nhập chiều cao...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    isError = heightError != null,
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                )
                                if (heightError != null) {
                                    Text(text = heightError!!, color = Color.Red, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))

                            // Heart rate
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("❤️", fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
                                    Text("Nhịp tim (bpm):", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF333333))
                                }
                                OutlinedTextField(
                                    value = heartRate,
                                    onValueChange = {
                                        heartRate = it
                                        heartRateError = null
                                    },
                                    placeholder = { Text("Nhập nhịp tim...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    isError = heartRateError != null,
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                )
                                if (heartRateError != null) {
                                    Text(text = heartRateError!!, color = Color.Red, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))

                            // Water intake
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💧", fontSize = 16.sp, color = Color(0xFF3B82F6), modifier = Modifier.padding(end = 4.dp))
                                    Text("Số lít nước đã uống (L):", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF333333))
                                }
                                OutlinedTextField(
                                    value = waterInput,
                                    onValueChange = {
                                        waterInput = it
                                        waterError = null
                                    },
                                    placeholder = { Text("Nhập số lít nước...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    isError = waterError != null,
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                )
                                if (waterError != null) {
                                    Text(text = waterError!!, color = Color.Red, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(15.dp))

                            Button(
                                onClick = {
                                    // Validate weight
                                    val parsedWeight = value.trim().replace(",", ".").toFloatOrNull()
                                    if (parsedWeight == null) {
                                        weightError = "Vui lòng nhập cân nặng hợp lệ (số)."
                                        return@Button
                                    }

                                    // Validate height
                                    val parsedHeight = heightInput.trim().replace("cm", "").replace(" ", "").toFloatOrNull()
                                    if (heightInput.isNotBlank() && parsedHeight == null) {
                                        heightError = "Vui lòng nhập chiều cao hợp lệ (số)."
                                        return@Button
                                    }

                                    // Validate heart rate
                                    val parsedHeart = heartRate.trim().toIntOrNull()
                                    if (heartRate.isNotBlank() && parsedHeart == null) {
                                        heartRateError = "Vui lòng nhập nhịp tim hợp lệ (số)."
                                        return@Button
                                    }

                                    // Validate water
                                    val parsedWater = waterInput.trim().replace(",", ".").toFloatOrNull()
                                    if (waterInput.isNotBlank() && parsedWater == null) {
                                        waterError = "Vui lòng nhập số lít nước hợp lệ (số)."
                                        return@Button
                                    }

                                    // Save height if user entered a value
                                    if (parsedHeight != null) {
                                        viewModel.setHeight(parsedHeight)
                                    }

                                    // Save weight and auto-calc BMI
                                    viewModel.addWeightWithOptionalBMI(parsedWeight.toString(), true)

                                    // Save heart rate
                                    if (parsedHeart != null) {
                                        viewModel.addHealthActivity("Nhịp tim", parsedHeart.toString())
                                    }

                                    // Save water
                                    if (parsedWater != null) {
                                        viewModel.addWaterLiters(parsedWater)
                                    }

                                    // reset and close
                                    value = ""
                                    heightInput = parsedHeight?.toInt()?.toString() ?: heightInput
                                    heartRate = ""
                                    waterInput = ""
                                    weightError = null
                                    heightError = null
                                    heartRateError = null
                                    waterError = null
                                    showAddDataForm = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3b82f6)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Cập Nhật & Lưu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
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
            tint = if (selected) selectedColor else unselectedColor
        )

        Spacer(modifier = Modifier.height(0.dp))

        Text(
            text = label,
            color = if (selected) selectedColor else unselectedColor,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

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
fun HealthOverviewCard(uiState: HomeUiState, onAddWater: (Float) -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 20.dp)
        ) {
            Text(
                text = "Tổng quan sức khỏe",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Compute latest values from uiState.healthActivities
                val latestWeight = uiState.healthActivities.firstOrNull { it.type == "Cân nặng" }?.value?.takeIf { it.isNotBlank() } ?: "-"
                val latestBMI = uiState.healthActivities.firstOrNull { it.type == "BMI" }?.value?.takeIf { it.isNotBlank() } ?: "-"
                val latestHR = uiState.healthActivities.firstOrNull { it.type == "Nhịp tim" }?.value?.takeIf { it.isNotBlank() } ?: "-"
                val waterLiters = uiState.waterLiters.takeIf { it > 0 }?.toString() ?: "-"

                HealthMetricItem(
                    title = "Cân nặng",
                    value = latestWeight,
                    subtitle = "",
                    color = Purple500
                )

                HealthMetricItem(
                    title = "BMI",
                    value = latestBMI,
                    subtitle = "",
                    color = Green500
                )

                HealthMetricItem(
                    title = "Nhịp tim",
                    value = latestHR,
                    subtitle = "",
                    color = Red500
                )

                HealthMetricItem(
                    title = "Nước uống",
                    value = waterLiters,
                    subtitle = "lít",
                    color = Blue500
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Water metric with small controls
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Nước (L)", fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${uiState.waterLiters}")
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = { onAddWater(0.25f) }) {
                        Text("+0.25L")
                    }
                    IconButton(onClick = { onAddWater(-0.25f) }) {
                        Text("-0.25L")
                    }
                }
            }
        }
    }
}

@Composable
fun HealthMetricItem(
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            fontSize = 11.sp,
            color = Color(0xFF718096),
            textAlign = TextAlign.Center
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
fun ChartSection() {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Biểu đồ theo dõi (7 ngày)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )

                Text(
                    text = "Cân nặng",
                    fontSize = 14.sp,
                    color = Purple500,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Giả lập biểu đồ
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF7FAFC), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Đây là biểu đồ giả - thực tế sẽ dùng MPAndroidChart
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = "Biểu đồ",
                        tint = Purple500,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Biểu đồ cân nặng 7 ngày",
                        color = Color(0xFF718096),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "68kg → 67.5kg",
                        color = Green500,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChartLegend("T2", "68kg", Purple500)
                ChartLegend("T3", "67.8kg", Purple400)
                ChartLegend("T4", "67.7kg", Purple300)
                ChartLegend("T5", "67.6kg", Purple200)
                ChartLegend("T6", "67.5kg", Green500)
                ChartLegend("T7", "67.5kg", Green500)
                ChartLegend("CN", "67.5kg", Green500)
            }
        }
    }
}

@Composable
fun ChartLegend(day: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day,
            fontSize = 10.sp,
            color = Color(0xFF718096)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 8.sp,
            color = Color(0xFF2D3748),
            fontWeight = FontWeight.Medium
        )
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


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}


