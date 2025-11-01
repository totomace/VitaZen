package com.example.vitazen.ui.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale // <-- Thêm import này
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitazen.R
import com.example.vitazen.ui.theme.VitaZenYellow
import kotlinx.coroutines.async // <-- Thêm import này
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch // <-- Thêm import này

@Composable
fun WelcomeScreen(
    onStartClicked: () -> Unit
) {
    // ---- 1. TẠO CÁC STATE ANIMATION CHO TỪNG PHẦN TỬ RIÊNG BIỆT ----
    val welcomeTextAlpha = remember { Animatable(0f) }
    val welcomeTextOffsetY = remember { Animatable(-100f) } // Bắt đầu từ trên, trượt xuống

    val vitazenScale = remember { Animatable(0.5f) } // Bắt đầu từ kích thước nhỏ, phóng to ra
    val vitazenAlpha = remember { Animatable(0f) }

    val sloganAlpha = remember { Animatable(0f) }
    val sloganOffsetY = remember { Animatable(100f) } // Bắt đầu từ dưới, trượt lên

    val buttonAlpha = remember { Animatable(0f) }

    // ---- 2. KHỞI CHẠY CHUỖI ANIMATION ĐA DẠNG ----
    LaunchedEffect(key1 = true) {
        // Sử dụng coroutine scope để chạy một số animation song song
        launch {
            // Animation 1: "Chào mừng đến với" trượt xuống
            launch {
                welcomeTextAlpha.animateTo(1f, tween(800))
                welcomeTextOffsetY.animateTo(0f, tween(800))
            }

            // Animation 2: "VitaZen" phóng to, chạy gần như song song
            launch {
                delay(200) // Trễ một chút để tạo hiệu ứng nối tiếp
                vitazenAlpha.animateTo(1f, tween(800))
                vitazenScale.animateTo(1f, tween(800))
            }
        }

        delay(1000) // Đợi cho 2 animation trên hoàn thành

        // Animation 3: Slogan trượt lên
        launch {
            sloganAlpha.animateTo(1f, tween(durationMillis = 800))
            sloganOffsetY.animateTo(0f, tween(durationMillis = 800))
        }

        delay(400) // Đợi một chút

        // Animation 4: Nút hiện dần
        buttonAlpha.animateTo(1f, tween(durationMillis = 500))
    }

    // ---- PHẦN GIAO DIỆN ----
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // LỚP 1: ẢNH NỀN
        Image(
            painter = painterResource(id = R.drawable.welcome),
            contentDescription = "Ảnh nền chào mừng",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // LỚP 2: LỚP PHỦ MÀU TỐI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        // LỚP 3: NỘI DUNG (CHỮ VÀ NÚT)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Phần nội dung chính
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ---- 3. ÁP DỤNG CÁC MODIFIER ANIMATION MỚI ----
                // "Chào mừng đến với"
                Text(
                    modifier = Modifier
                        .offset(y = welcomeTextOffsetY.value.dp) // Trượt từ trên
                        .alpha(welcomeTextAlpha.value),
                    textAlign = TextAlign.Center,
                    text = "Chào mừng đến với",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Normal
                )

                // "VitaZen"
                Text(
                    modifier = Modifier
                        .scale(vitazenScale.value) // Phóng to
                        .alpha(vitazenAlpha.value),
                    text = "VitaZen",
                    color = VitaZenYellow,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 60.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Slogan và mô tả
                Column(
                    modifier = Modifier
                        .offset(y = sloganOffsetY.value.dp) // Trượt từ dưới
                        .alpha(sloganAlpha.value),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sống Khỏe, Sống Cân Bằng.",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Hành trình sức khỏe của bạn bắt đầu từ đây.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Nút kêu gọi hành động
            Button(
                onClick = onStartClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .alpha(buttonAlpha.value), // Chỉ hiện dần
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitaZenYellow,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    "Bắt đầu ngay",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}
