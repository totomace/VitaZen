package com.example.vitazen.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vitazen.model.database.VitaZenDatabase
import com.example.vitazen.model.repository.UserRepository
import com.example.vitazen.viewmodel.SettingsViewModel

data class SettingsItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val hasSwitch: Boolean = false,
    val hasArrow: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository(VitaZenDatabase.getInstance(context).userDao()) }
    val healthHistoryRepository = remember { com.example.vitazen.model.repository.HealthHistoryRepository(VitaZenDatabase.getInstance(context).healthHistoryDao()) }
    val healthDataRepository = remember { com.example.vitazen.model.repository.HealthDataRepository(VitaZenDatabase.getInstance(context).healthDataDao()) }
    val viewModel: SettingsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(userRepository, context, healthHistoryRepository, healthDataRepository) as T
        }
    })

    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDataDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { 
                showEditNameDialog = false
                newName = ""
            },
            title = { 
                Text(
                    "Chỉnh sửa tên",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        "Nhập tên mới của bạn:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = { Text("Tên hiển thị") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C4DFF),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF7C4DFF)
                            )
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.updateUserName(newName)
                            showEditNameDialog = false
                            newName = ""
                        } else {
                            Toast.makeText(context, "Tên không được để trống", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C4DFF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Lưu", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { 
                        showEditNameDialog = false
                        newName = ""
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hủy")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { 
                showChangePasswordDialog = false
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
            },
            title = { 
                Text(
                    "Đổi mật khẩu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Mật khẩu hiện tại") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C4DFF),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF7C4DFF)
                            )
                        }
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Mật khẩu mới") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C4DFF),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFF6E40)
                            )
                        }
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Xác nhận mật khẩu mới") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C4DFF),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF00E676)
                            )
                        }
                    )
                    Text(
                        "Mật khẩu phải có ít nhất 6 ký tự",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() -> {
                                Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                            }
                            newPassword.length < 6 -> {
                                Toast.makeText(context, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                            }
                            newPassword != confirmPassword -> {
                                Toast.makeText(context, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                viewModel.changePassword(currentPassword, newPassword)
                                showChangePasswordDialog = false
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6E40)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Đổi mật khẩu", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { 
                        showChangePasswordDialog = false
                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hủy")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        "Đăng xuất",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            },
            text = { 
                Text(
                    "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?",
                    fontSize = 16.sp,
                    color = Color(0xFF2D3748)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                        // Navigate to welcome screen
                        android.content.Intent(context, com.example.vitazen.MainActivity::class.java).also {
                            it.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(it)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Đăng xuất", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hủy")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete Data Dialog
    if (showDeleteDataDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDataDialog = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        "Xóa toàn bộ dữ liệu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            },
            text = { 
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Bạn có chắc chắn muốn xóa toàn bộ dữ liệu sức khỏe?",
                        fontSize = 16.sp,
                        color = Color(0xFF2D3748)
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Hành động này không thể hoàn tác",
                                fontSize = 13.sp,
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllData()
                        showDeleteDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Xóa", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDataDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hủy")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cài đặt",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7C4DFF),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User Info Section
            item {
                UserInfoCard(
                    userName = uiState.userName,
                    userEmail = uiState.userEmail
                )
            }

            // Notifications Section
            item {
                SectionTitle(title = "Thông báo")
            }

            item {
                SettingsSwitchCard(
                    title = "Thông báo nhắc nhở",
                    subtitle = "Nhận thông báo về các hoạt động sức khỏe",
                    icon = Icons.Default.Notifications,
                    color = Color(0xFF448AFF),
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications(it) }
                )
            }

            item {
                SettingsSwitchCard(
                    title = "Nhắc nhở uống nước",
                    subtitle = "Nhắc nhở định kỳ về việc uống nước",
                    icon = Icons.Default.WaterDrop,
                    color = Color(0xFF18FFFF),
                    checked = uiState.waterReminderEnabled,
                    onCheckedChange = { viewModel.toggleWaterReminder(it) }
                )
            }

            item {
                SettingsSwitchCard(
                    title = "Nhắc nhở tập thể dục",
                    subtitle = "Nhắc nhở về thời gian tập luyện",
                    icon = Icons.Default.FitnessCenter,
                    color = Color(0xFFFF6E40),
                    checked = uiState.exerciseReminderEnabled,
                    onCheckedChange = { viewModel.toggleExerciseReminder(it) }
                )
            }

            // Data & Privacy Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle(title = "Dữ liệu & Quyền riêng tư")
            }

            item {
                SettingsClickableCard(
                    title = "Xuất dữ liệu",
                    subtitle = "Xuất dữ liệu sức khỏe ra file",
                    icon = Icons.Default.Download,
                    color = Color(0xFF00E676),
                    onClick = { viewModel.exportData() }
                )
            }

            item {
                SettingsClickableCard(
                    title = "Xóa toàn bộ dữ liệu",
                    subtitle = "Xóa tất cả dữ liệu sức khỏe",
                    icon = Icons.Default.Delete,
                    color = Color(0xFFFF5252),
                    onClick = { showDeleteDataDialog = true }
                )
            }

            // Account Settings Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle(title = "Tài khoản")
            }

            item {
                SettingsClickableCard(
                    title = "Chỉnh sửa tên",
                    subtitle = "Thay đổi tên hiển thị",
                    icon = Icons.Default.Edit,
                    color = Color(0xFF7C4DFF),
                    onClick = { 
                        newName = uiState.userName
                        showEditNameDialog = true 
                    }
                )
            }

            item {
                SettingsClickableCard(
                    title = "Đổi mật khẩu",
                    subtitle = "Cập nhật mật khẩu đăng nhập",
                    icon = Icons.Default.Lock,
                    color = Color(0xFFFF6E40),
                    onClick = { showChangePasswordDialog = true }
                )
            }

            // App Settings Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle(title = "Cài đặt ứng dụng")
            }

            item {
                SettingsClickableCard(
                    title = "Ngôn ngữ",
                    subtitle = uiState.language,
                    icon = Icons.Default.Language,
                    color = Color(0xFFE040FB),
                    onClick = { /* TODO: Language selection */ }
                )
            }

            // About Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle(title = "Thông tin")
            }

            item {
                SettingsClickableCard(
                    title = "Giới thiệu",
                    subtitle = "Thông tin về VitaZen",
                    icon = Icons.Default.Info,
                    color = Color(0xFF448AFF),
                    onClick = { /* TODO: About screen */ }
                )
            }

            item {
                SettingsClickableCard(
                    title = "Phiên bản",
                    subtitle = "1.0.0",
                    icon = Icons.Default.AppSettingsAlt,
                    color = Color(0xFF9E9E9E),
                    onClick = { /* No action */ },
                    hasArrow = false
                )
            }

            // Logout Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                SettingsClickableCard(
                    title = "Đăng xuất",
                    subtitle = "Thoát khỏi tài khoản hiện tại",
                    icon = Icons.Default.Logout,
                    color = Color(0xFFFF5252),
                    onClick = { showLogoutDialog = true }
                )
            }

            // Footer space
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun UserInfoCard(userName: String, userEmail: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7C4DFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = userName.ifBlank { "Người dùng" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
                Text(
                    text = userEmail,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
fun SettingsSwitchCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D3748)
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = color,
                    checkedTrackColor = color.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun SettingsClickableCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    hasArrow: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D3748)
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            if (hasArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

