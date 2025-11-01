package com.example.vitazen.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// CẬP NHẬT DARKCOLORSCHEME VỚI CÁC MÀU MỚI
private val DarkColorScheme = darkColorScheme(
    primary = ButtonBlue, // Màu chính cho các thành phần tương tác
    background = DarkPurpleBlue, // Màu nền của ứng dụng
    surface = DarkPurpleBlue,    // Màu của các bề mặt như Card, Sheet
    onPrimary = Color.White,     // Màu chữ/icon trên nền màu `primary`
    onBackground = Color.White,  // Màu chữ/icon trên nền `background`
    onSurface = Color.White      // Màu chữ/icon trên nền `surface`
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
    // Bạn có thể giữ nguyên hoặc tùy chỉnh LightColorScheme cho các màn hình sau này
)

@Composable
fun VitaZenTheme(
    // THAY ĐỔI GIÁ TRỊ MẶC ĐỊNH Ở ĐÂY
    darkTheme: Boolean = true, // Luôn dùng dark theme
    dynamicColor: Boolean = false, // Tắt màu động để đảm bảo màu sắc nhất quán
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Typography sẽ được dùng trong bước tiếp theo
        content = content
    )
}
