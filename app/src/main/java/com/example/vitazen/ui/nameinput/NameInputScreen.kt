package com.example.vitazen.ui.nameinput

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun NameInputScreen(onNameEntered: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Nền giống Login
        Image(
            painter = painterResource(id = com.example.vitazen.R.drawable.welcome),
            contentDescription = "Ảnh nền",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Nhập tên bạn muốn hiển thị",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    error = null
                },
                label = { Text("Tên hiển thị") },
                isError = error != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Vui lòng nhập tên."
                    } else {
                        onNameEntered(name.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tiếp tục", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview
@Composable
fun NameInputScreenPreview() {
    NameInputScreen(onNameEntered = {})
}
