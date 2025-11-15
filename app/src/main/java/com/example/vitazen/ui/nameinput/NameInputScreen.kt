package com.example.vitazen.ui.nameinput

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vitazen.viewmodel.NameInputViewModel
import com.example.vitazen.viewmodel.NameInputUiState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.vitazen.ui.theme.VitaZenYellow

@Composable
fun NameInputModalScreen(
    viewModel: NameInputViewModel,
    onSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    var visible by remember { mutableStateOf(true) }

    // Xử lý khi thành công
    LaunchedEffect(uiState) {
        if (uiState is NameInputUiState.Success) {
            visible = false
            kotlinx.coroutines.delay(350)
            onSuccess((uiState as NameInputUiState.Success).userName)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.9f, animationSpec = tween(350))
        ) {
            Card(
                modifier = Modifier
                    .widthIn(min = 300.dp, max = 420.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Load Firebase avatar if present
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val photoUrl = currentUser?.photoUrl?.toString()

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(VitaZenYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = Color.Black.copy(alpha = 0.85f),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tên hiển thị",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Chọn tên để mọi người dễ nhận biết bạn",
                        fontSize = 13.sp,
                        color = Color.Black.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (uiState is NameInputUiState.Error) viewModel.resetState()
                        },
                        label = { Text("Tên hiển thị", color = Color.Black) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.Black)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black,
                            cursorColor = VitaZenYellow,
                            focusedBorderColor = VitaZenYellow,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = VitaZenYellow,
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    if (uiState is NameInputUiState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text((uiState as NameInputUiState.Error).message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            viewModel.submitName(name)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VitaZenYellow, contentColor = Color.Black)
                    ) {
                        if (uiState is NameInputUiState.Loading) {
                            CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Tiếp tục", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Preview không dùng ViewModel thật
