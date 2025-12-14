package com.example.vitazen.ui.history

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vitazen.model.data.HealthHistory
import com.example.vitazen.model.database.VitaZenDatabase
import com.example.vitazen.model.repository.HealthHistoryRepository
import com.example.vitazen.ui.home.ComboChart
import com.example.vitazen.viewmodel.FilterType
import com.example.vitazen.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember {
        HealthHistoryRepository(VitaZenDatabase.getInstance(context).healthHistoryDao())
    }
    val viewModel: HistoryViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(repository) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lịch sử sức khỏe",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                actions = {
                    // Filter button
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Lọc",
                            tint = Color(0xFF2196F3)
                        )
                    }
                    
                    // Filter menu
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tất cả") },
                            onClick = {
                                viewModel.setFilterType(FilterType.ALL)
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.List, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Tuần này") },
                            onClick = {
                                viewModel.setFilterType(FilterType.WEEK)
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Tháng này") },
                            onClick = {
                                viewModel.setFilterType(FilterType.MONTH)
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchHistory(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Tìm kiếm...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.searchHistory("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Weekly Chart Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Week navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.navigateToPreviousWeek() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Tuần trước",
                                tint = Color(0xFF2196F3)
                            )
                        }

                        Text(
                            text = uiState.weekLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )

                        IconButton(
                            onClick = { viewModel.navigateToNextWeek() },
                            enabled = uiState.canNavigateToNextWeek
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Tuần sau",
                                tint = if (uiState.canNavigateToNextWeek)
                                    Color(0xFF2196F3) else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chart
if (uiState.weekData.isNotEmpty()) {
    ComboChart(
        weekData = uiState.weekData  // Truyền trực tiếp, không cần convert
    )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Không có dữ liệu trong tuần này",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterType == FilterType.ALL,
                    onClick = { viewModel.setFilterType(FilterType.ALL) },
                    label = { Text("Tất cả") }
                )
                FilterChip(
                    selected = uiState.filterType == FilterType.WEEK,
                    onClick = { viewModel.setFilterType(FilterType.WEEK) },
                    label = { Text("7 ngày") }
                )
                FilterChip(
                    selected = uiState.filterType == FilterType.MONTH,
                    onClick = { viewModel.setFilterType(FilterType.MONTH) },
                    label = { Text("30 ngày") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // History list header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Danh sách lịch sử (${uiState.filteredHistoryList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // History list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2196F3))
                }
            } else if (uiState.filteredHistoryList.isEmpty()) {
                EmptyHistoryState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.filteredHistoryList,
                        key = { it.id }
                    ) { item ->
                        HistoryItem(
                            item = item,
                            onDelete = { viewModel.deleteHistoryItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    item: HealthHistory,
    onDelete: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateFormat.format(Date(item.timestamp)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                }

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Health data grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item.weight?.let {
                    HealthDataRow(
                        icon = Icons.Default.MonitorWeight,
                        label = "Cân nặng",
                        value = "$it kg",
                        color = Color(0xFFE91E63)
                    )
                }
                item.height?.let {
                    HealthDataRow(
                        icon = Icons.Default.Height,
                        label = "Chiều cao",
                        value = "$it cm",
                        color = Color(0xFF9C27B0)
                    )
                }
                item.heartRate?.let {
                    HealthDataRow(
                        icon = Icons.Default.Favorite,
                        label = "Nhịp tim",
                        value = "$it bpm",
                        color = Color(0xFFF44336)
                    )
                }
                item.waterIntake?.let {
                    HealthDataRow(
                        icon = Icons.Default.WaterDrop,
                        label = "Nước uống",
                        value = "$it lít",
                        color = Color(0xFF2196F3)
                    )
                }
                item.sleepHours?.let {
                    HealthDataRow(
                        icon = Icons.Default.Bedtime,
                        label = "Giờ ngủ",
                        value = "$it giờ",
                        color = Color(0xFF673AB7)
                    )
                }
                item.bloodPressureSystolic?.let { sys ->
                    item.bloodPressureDiastolic?.let { dia ->
                        HealthDataRow(
                            icon = Icons.Default.MonitorHeart,
                            label = "Huyết áp",
                            value = "$sys/$dia mmHg",
                            color = Color(0xFFFF5722)
                        )
                    }
                }
                item.steps?.let {
                    HealthDataRow(
                        icon = Icons.Default.DirectionsWalk,
                        label = "Số bước",
                        value = "$it bước",
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            // Notes
            item.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF44336)
                )
            },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa mục lịch sử này không?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun HealthDataRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
    }
}

@Composable
fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color(0xFF2196F3).copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Chưa có lịch sử",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bắt đầu nhập dữ liệu sức khỏe của bạn",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF757575)
        )
    }
}



@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    MaterialTheme {
        HistoryScreen()
    }
}

