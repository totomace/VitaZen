package com.example.vitazen.ui.reminder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vitazen.model.data.Reminder
import com.example.vitazen.model.data.ReminderType
import com.example.vitazen.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel = viewModel()
) {
    val reminders by viewModel.reminders.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val editingReminder by viewModel.editingReminder.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show error message
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it, duration = SnackbarDuration.Short
                )
            }
            viewModel.clearMessages()
        }
    }

    // Show success message
    LaunchedEffect(successMessage) {
        successMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it, duration = SnackbarDuration.Short
                )
            }
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nhắc nhở uống nước",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF2196F3)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm nhắc nhở"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when {
                isLoading && reminders.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2196F3)
                    )
                }

                reminders.isEmpty() -> {
                    EmptyReminderState(
                        onAddClick = { viewModel.showAddDialog() }
                    )
                }

                else -> {
                    ReminderList(
                        reminders = reminders,
                        onToggle = { viewModel.toggleReminder(it) },
                        onEdit = { viewModel.startEditing(it) },
                        onDelete = { viewModel.deleteReminder(it) }
                    )
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { title, type, interval, amount, start, end, days ->
                viewModel.addReminder(title, type, interval, amount, start, end, days)
            }
        )
    }

    // Edit Dialog
    editingReminder?.let { reminder ->
        EditReminderDialog(
            reminder = reminder,
            onDismiss = { viewModel.cancelEditing() },
            onConfirm = { updated ->
                viewModel.updateReminder(updated)
            }
        )
    }
}

@Composable
fun ReminderList(
    reminders: List<Reminder>,
    onToggle: (Reminder) -> Unit,
    onEdit: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 88.dp  // THÊM PADDING ĐỂ FAB KHÔNG CHE NÚT XÓA
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = reminders, key = { it.id }) { reminder ->
            ReminderCard(
                reminder = reminder,
                onToggle = { onToggle(reminder) },
                onEdit = { onEdit(reminder) },
                onDelete = { onDelete(reminder) }
            )
        }
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Icon + Title + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (reminder.isEnabled) Color(0xFF212121)
                        else Color(0xFF757575)
                    )
                }

                // Switch
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2196F3)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Lượng nước
                InfoRow(
                    icon = Icons.Default.WaterDrop,
                    label = "Lượng nước",
                    value = "${reminder.waterAmountMl} ml",
                    color = Color(0xFF2196F3)
                )

                // Khoảng cách
                InfoRow(
                    icon = Icons.Default.Schedule,
                    label = "Khoảng cách",
                    value = formatInterval(reminder.intervalMinutes),
                    color = Color(0xFF4CAF50)
                )

                // Thời gian
                InfoRow(
                    icon = Icons.Default.AccessTime,
                    label = "Thời gian",
                    value = "${reminder.startTime} - ${reminder.endTime}",
                    color = Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Edit button
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Chỉnh sửa",
                        tint = Color(0xFF2196F3)
                    )
                }

                // Delete button
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = Color(0xFFF44336)
                    )
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
            text = { Text("Bạn có chắc muốn xóa nhắc nhở '${reminder.title}' không?") },
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
fun InfoRow(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, ReminderType, Int, Int, String, String, List<Int>) -> Unit
) {
    var title by remember { mutableStateOf("Uống nước") }
    var waterAmount by remember { mutableStateOf("250") }
    var intervalValue by remember { mutableStateOf("30") }
    var intervalUnit by remember { mutableStateOf("minutes") }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("22:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Thêm nhắc nhở mới",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                HorizontalDivider(color = Color(0xFFE0E0E0))

                // Tiêu đề
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Title, contentDescription = null)
                    }
                )

                // Lượng nước
                OutlinedTextField(
                    value = waterAmount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) waterAmount = it },
                    label = { Text("Lượng nước (ml)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.WaterDrop, contentDescription = null)
                    }
                )

                // Khoảng cách
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Khoảng cách mỗi lần",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = intervalValue,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) intervalValue = it
                            },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                            }
                        )

                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = intervalUnit == "minutes",
                                    onClick = { intervalUnit = "minutes" }
                                )
                                Text("Phút")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = intervalUnit == "hours",
                                    onClick = { intervalUnit = "hours" }
                                )
                                Text("Giờ")
                            }
                        }
                    }
                }

                // Thời gian bắt đầu và kết thúc
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Thời gian trong ngày",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Bắt đầu") },
                            placeholder = { Text("08:00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                            }
                        )

                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("Kết thúc") },
                            placeholder = { Text("22:00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                            }
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE0E0E0))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = Color(0xFF757575))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) return@Button

                            val intervalMinutes = if (intervalUnit == "hours") {
                                (intervalValue.toIntOrNull() ?: 1) * 60
                            } else {
                                intervalValue.toIntOrNull() ?: 30
                            }

                            onConfirm(
                                title,
                                ReminderType.WATER,
                                intervalMinutes,
                                waterAmount.toIntOrNull() ?: 250,
                                startTime,
                                endTime,
                                listOf(1, 2, 3, 4, 5, 6, 7)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thêm")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderDialog(
    reminder: Reminder,
    onDismiss: () -> Unit,
    onConfirm: (Reminder) -> Unit
) {
    var title by remember { mutableStateOf(reminder.title) }
    var waterAmount by remember { mutableStateOf(reminder.waterAmountMl.toString()) }
    var intervalValue by remember {
        mutableStateOf(
            if (reminder.intervalMinutes >= 60 && reminder.intervalMinutes % 60 == 0) {
                (reminder.intervalMinutes / 60).toString()
            } else {
                reminder.intervalMinutes.toString()
            }
        )
    }
    var intervalUnit by remember {
        mutableStateOf(
            if (reminder.intervalMinutes >= 60 && reminder.intervalMinutes % 60 == 0) "hours" else "minutes"
        )
    }
    var startTime by remember { mutableStateOf(reminder.startTime) }
    var endTime by remember { mutableStateOf(reminder.endTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Chỉnh sửa nhắc nhở",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                HorizontalDivider(color = Color(0xFFE0E0E0))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) }
                )

                OutlinedTextField(
                    value = waterAmount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) waterAmount = it },
                    label = { Text("Lượng nước (ml)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.WaterDrop, contentDescription = null) }
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Khoảng cách mỗi lần",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = intervalValue,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) intervalValue = it
                            },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                        )

                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = intervalUnit == "minutes",
                                    onClick = { intervalUnit = "minutes" }
                                )
                                Text("Phút")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = intervalUnit == "hours",
                                    onClick = { intervalUnit = "hours" }
                                )
                                Text("Giờ")
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Thời gian trong ngày",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Bắt đầu") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                        )

                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("Kết thúc") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE0E0E0))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = Color(0xFF757575))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) return@Button

                            val intervalMinutes = if (intervalUnit == "hours") {
                                (intervalValue.toIntOrNull() ?: 1) * 60
                            } else {
                                intervalValue.toIntOrNull() ?: 30
                            }

                            onConfirm(
                                reminder.copy(
                                    title = title,
                                    intervalMinutes = intervalMinutes,
                                    waterAmountMl = waterAmount.toIntOrNull() ?: 250,
                                    startTime = startTime,
                                    endTime = endTime
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lưu")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyReminderState(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.WaterDrop,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color(0xFF2196F3).copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Chưa có nhắc nhở nào",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Thêm nhắc nhở uống nước để giữ gìn sức khỏe",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF757575)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            ),
            modifier = Modifier.size(width = 200.dp, height = 56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thêm nhắc nhở", style = MaterialTheme.typography.titleMedium)
        }
    }
}

// Helper functions
fun formatInterval(minutes: Int): String {
    return if (minutes >= 60) {
        if (minutes % 60 == 0) {
            "${minutes / 60} giờ"
        } else {
            "${minutes / 60}h ${minutes % 60}p"
        }
    } else {
        "$minutes phút"
    }
}

@Preview(showBackground = true)
@Composable
fun ReminderScreenPreview() {
    MaterialTheme {
        ReminderScreen()
    }
}