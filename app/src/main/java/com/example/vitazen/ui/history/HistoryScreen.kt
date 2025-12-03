package com.example.vitazen.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vitazen.model.data.HealthHistory
import com.example.vitazen.model.database.VitaZenDatabase
import com.example.vitazen.model.repository.HealthHistoryRepository
import com.example.vitazen.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val healthHistoryRepository = remember {
        HealthHistoryRepository(VitaZenDatabase.getInstance(context).healthHistoryDao())
    }
    val viewModel: HistoryViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(healthHistoryRepository) as T
        }
    })

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lịch sử sức khỏe",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Week navigation card
            WeekNavigationCard(
                weekLabel = uiState.weekLabel,
                onPreviousWeek = { viewModel.navigateToPreviousWeek() },
                onNextWeek = { viewModel.navigateToNextWeek() },
                canNavigateNext = uiState.canNavigateToNextWeek
            )

            // Chart section
            if (uiState.weekData.isNotEmpty()) {
                WeekChartCard(weekData = uiState.weekData)
            }

            // History list
            Text(
                text = "Chi tiết theo ngày",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748),
                modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.historyList) { history ->
                    HistoryItemCard(history = history)
                }

                if (uiState.historyList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chưa có dữ liệu trong tuần này",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekNavigationCard(
    weekLabel: String,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    canNavigateNext: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousWeek,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6200EE))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Tuần trước",
                    tint = Color.White
                )
            }

            Text(
                text = weekLabel,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onNextWeek,
                enabled = canNavigateNext,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (canNavigateNext) Color(0xFF6200EE) else Color.LightGray)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Tuần sau",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun WeekChartCard(weekData: List<com.example.vitazen.viewmodel.WeekData>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Biểu đồ tuần",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Chart with stacked bars
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF7FAFC), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val chartWidth = size.width
                    val chartHeight = size.height
                    val barWidth = chartWidth / 7f * 0.7f
                    val spacing = chartWidth / 7f

                    val maxWeight = 100f
                    val maxHeartRate = 150f
                    val maxWater = 5f
                    val maxSleep = 12f

                    weekData.forEachIndexed { index, data ->
                        val x = index * spacing + (spacing - barWidth) / 2f

                        if (data.weight != null) {
                            var currentY = chartHeight

                            // Weight
                            val weightHeight = (data.weight / maxWeight) * chartHeight * 0.4f
                            drawRect(
                                color = Color(0xFF6200EE),
                                topLeft = androidx.compose.ui.geometry.Offset(x, currentY - weightHeight),
                                size = androidx.compose.ui.geometry.Size(barWidth, weightHeight)
                            )
                            currentY -= weightHeight

                            // Heart rate
                            data.heartRate?.let { hr ->
                                val heartHeight = (hr / maxHeartRate) * chartHeight * 0.25f
                                drawRect(
                                    color = Color(0xFFF44336),
                                    topLeft = androidx.compose.ui.geometry.Offset(x, currentY - heartHeight),
                                    size = androidx.compose.ui.geometry.Size(barWidth, heartHeight)
                                )
                                currentY -= heartHeight
                            }

                            // Water
                            data.waterIntake?.let { water ->
                                val waterHeight = (water / maxWater) * chartHeight * 0.2f
                                drawRect(
                                    color = Color(0xFF2196F3),
                                    topLeft = androidx.compose.ui.geometry.Offset(x, currentY - waterHeight),
                                    size = androidx.compose.ui.geometry.Size(barWidth, waterHeight)
                                )
                                currentY -= waterHeight
                            }

                            // Sleep
                            data.sleepHours?.let { sleep ->
                                val sleepHeight = (sleep / maxSleep) * chartHeight * 0.15f
                                drawRect(
                                    color = Color(0xFF9C27B0),
                                    topLeft = androidx.compose.ui.geometry.Offset(x, currentY - sleepHeight),
                                    size = androidx.compose.ui.geometry.Size(barWidth, sleepHeight)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekData.forEach { data ->
                    Text(
                        text = data.dayLabel,
                        fontSize = 12.sp,
                        color = if (data.weight != null) Color(0xFF2D3748) else Color.Gray,
                        fontWeight = if (data.weight != null) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(history: HealthHistory) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(history.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateString,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Health metrics in a grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                history.weight?.let {
                    MetricChip(
                        label = "Cân nặng",
                        value = "${String.format("%.1f", it)} kg",
                        color = Color(0xFF6200EE)
                    )
                }
                history.heartRate?.let {
                    MetricChip(
                        label = "Nhịp tim",
                        value = "$it bpm",
                        color = Color(0xFFF44336)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                history.waterIntake?.let {
                    MetricChip(
                        label = "Nước uống",
                        value = "${String.format("%.1f", it)} L",
                        color = Color(0xFF2196F3)
                    )
                }
                history.sleepHours?.let {
                    MetricChip(
                        label = "Giờ ngủ",
                        value = "${String.format("%.1f", it)}h",
                        color = Color(0xFF9C27B0)
                    )
                }
            }

            history.notes?.let {
                if (it.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ghi chú: $it",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun MetricChip(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
