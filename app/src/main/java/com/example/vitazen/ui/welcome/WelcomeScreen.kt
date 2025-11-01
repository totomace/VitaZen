package com.example.vitazen.ui.welcome

import androidx.compose.animation.core.Animatable
// Thêm các import cần thiết cho hiệu ứng nhấn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight // <-- THÊM IMPORT
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitazen.R
import com.example.vitazen.ui.theme.VitaZenYellow
import com.example.vitazen.viewmodel.WelcomeEffect
import com.example.vitazen.viewmodel.WelcomeEvent
import com.example.vitazen.viewmodel.WelcomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel,
    onNavigateToLogin: () -> Unit
) {
    // ... (Phần lắng nghe ViewModel và animation giữ nguyên)
    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WelcomeEffect.NavigateToLogin -> {
                    onNavigateToLogin()
                }
            }
        }
    }
    val welcomeTextAlpha = remember { Animatable(0f) }
    val welcomeTextOffsetY = remember { Animatable(-100f) }
    val vitazenScale = remember { Animatable(0.5f) }
    val vitazenAlpha = remember { Animatable(0f) }
    val sloganAlpha = remember { Animatable(0f) }
    val sloganOffsetY = remember { Animatable(100f) }
    val buttonAlpha = remember { Animatable(0f) }
    LaunchedEffect(key1 = true) {
        launch {
            launch {
                welcomeTextAlpha.animateTo(1f, tween(800))
                welcomeTextOffsetY.animateTo(0f, tween(800))
            }
            launch {
                delay(200)
                vitazenAlpha.animateTo(1f, tween(800))
                vitazenScale.animateTo(1f, tween(800))
            }
        }
        delay(1000)
        launch {
            sloganAlpha.animateTo(1f, tween(durationMillis = 800))
            sloganOffsetY.animateTo(0f, tween(durationMillis = 800))
        }
        delay(400)
        buttonAlpha.animateTo(1f, tween(durationMillis = 500))
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "buttonScale")

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.welcome),
            contentDescription = "Ảnh nền chào mừng",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // "Chào mừng đến với" - Giữ nguyên, không cần quá nổi bật
                Text(
                    modifier = Modifier
                        .offset(y = welcomeTextOffsetY.value.dp)
                        .alpha(welcomeTextAlpha.value),
                    textAlign = TextAlign.Center,
                    text = "Chào mừng đến với",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Normal // <-- Độ đậm bình thường
                )

                // "VitaZen" - Tên thương hiệu, CẦN NỔI BẬT NHẤT
                Text(
                    modifier = Modifier
                        .scale(vitazenScale.value)
                        .alpha(vitazenAlpha.value),
                    text = "VitaZen",
                    color = VitaZenYellow,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold, // <-- SỬA: Tăng độ đậm lên tối đa
                    lineHeight = 60.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier
                        .offset(y = sloganOffsetY.value.dp)
                        .alpha(sloganAlpha.value),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Slogan - Cần rõ ràng, dễ đọc
                    Text(
                        text = "Sống Khỏe, Sống Cân Bằng.",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium, // <-- SỬA: Tăng nhẹ độ đậm
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Câu mô tả - Làm nền, không cần quá đậm
                    Text(
                        text = "Hành trình sức khỏe của bạn bắt đầu từ đây.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light, // <-- SỬA: Giảm độ đậm để tạo sự tương phản
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Nút kêu gọi hành động - Cần rõ ràng, thu hút
            Button(
                onClick = { viewModel.handleEvent(WelcomeEvent.StartButtonClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .scale(buttonScale)
                    .alpha(buttonAlpha.value),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitaZenYellow,
                    contentColor = Color.Black
                ),
                interactionSource = interactionSource
            ) {
                Text(
                    "Bắt đầu ngay",
                    fontWeight = FontWeight.Bold, // <-- SỬA: Tăng độ đậm
                    fontSize = 18.sp // <-- Giữ nguyên hoặc có thể tăng lên 20.sp nếu muốn
                )
            }
        }
    }
}
