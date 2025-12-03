package com.example.vitazen.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ComboChart(weekData: List<com.example.vitazen.viewmodel.WeekData>) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color(0xFFF7FAFC), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val chartWidth = size.width - 40.dp.toPx()
                val chartHeight = size.height - 40.dp.toPx()
                val barWidth = (chartWidth / 7f) * 0.5f
                val spacing = chartWidth / 7f
                val offsetX = 20.dp.toPx()

                // Giới hạn giá trị cho từng trục
                val weightMin = 50f
                val weightMax = 80f
                val waterMax = 4f
                val sleepMax = 10f

                // Vẽ lưới nền
                for (i in 0..5) {
                    val y = (chartHeight / 5) * i
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = androidx.compose.ui.geometry.Offset(offsetX, y),
                        end = androidx.compose.ui.geometry.Offset(chartWidth + offsetX, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Thu thập các điểm cho line charts
                val weightPoints = mutableListOf<androidx.compose.ui.geometry.Offset>()
                val sleepPoints = mutableListOf<androidx.compose.ui.geometry.Offset>()

                weekData.forEachIndexed { index, data ->
                    val centerX = offsetX + index * spacing + spacing / 2f

                    // VẼ CỘT NƯỚC (Column) - Trục phải
                    data.waterIntake?.let { water ->
                        val waterPercent = (water / waterMax).coerceIn(0f, 1f)
                        val waterHeight = waterPercent * chartHeight
                        
                        // Vẽ cột nước với màu xanh dương
                        drawRoundRect(
                            color = Color(0xFF3498DB),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                centerX - barWidth / 2f,
                                chartHeight - waterHeight
                            ),
                            size = androidx.compose.ui.geometry.Size(barWidth, waterHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }

                    // Thu thập điểm cho ĐƯỜNG CÂN NẶNG - Trục trái
                    data.weight?.let { weight ->
                        val weightPercent = ((weight - weightMin) / (weightMax - weightMin)).coerceIn(0f, 1f)
                        val y = chartHeight - (weightPercent * chartHeight)
                        weightPoints.add(androidx.compose.ui.geometry.Offset(centerX, y))
                    }

                    // Thu thập điểm cho ĐƯỜNG NGỦ - Trục phải
                    data.sleepHours?.let { sleep ->
                        val sleepPercent = (sleep / sleepMax).coerceIn(0f, 1f)
                        val y = chartHeight - (sleepPercent * chartHeight)
                        sleepPoints.add(androidx.compose.ui.geometry.Offset(centerX, y))
                    }
                }

                // VẼ ĐƯỜNG CÂN NẶNG (Line) với điểm tròn
                if (weightPoints.size > 1) {
                    drawPath(
                        path = Path().apply {
                            moveTo(weightPoints[0].x, weightPoints[0].y)
                            for (i in 1 until weightPoints.size) {
                                lineTo(weightPoints[i].x, weightPoints[i].y)
                            }
                        },
                        color = Color(0xFF2D3E50),
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }
                // Vẽ điểm tròn cho cân nặng
                weightPoints.forEach { point ->
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = Color(0xFF2D3E50),
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }

                // VẼ ĐƯỜNG NGỦ (Line) với điểm tròn
                if (sleepPoints.size > 1) {
                    drawPath(
                        path = Path().apply {
                            moveTo(sleepPoints[0].x, sleepPoints[0].y)
                            for (i in 1 until sleepPoints.size) {
                                lineTo(sleepPoints[i].x, sleepPoints[i].y)
                            }
                        },
                        color = Color(0xFF27AE60),
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }
                // Vẽ điểm tròn cho giấc ngủ
                sleepPoints.forEach { point ->
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = Color(0xFF27AE60),
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }

            // Vẽ nhãn trục Y bên trái (Cân nặng)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-4).dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "80",
                    fontSize = 9.sp,
                    color = Color(0xFF2D3E50),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(180.dp))
                Text(
                    text = "50",
                    fontSize = 9.sp,
                    color = Color(0xFF2D3E50),
                    fontWeight = FontWeight.Bold
                )
            }

            // Vẽ nhãn trục Y bên phải (Nước/Ngủ)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "10h",
                    fontSize = 9.sp,
                    color = Color(0xFF27AE60),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(180.dp))
                Text(
                    text = "4L",
                    fontSize = 9.sp,
                    color = Color(0xFF3498DB),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nhãn ngày trong tuần
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekData.forEach { data ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = data.dayLabel,
                        fontSize = 11.sp,
                        color = if (data.weight != null) Color(0xFF2D3748) else Color(0xFF718096),
                        fontWeight = if (data.weight != null) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = formatDateLabel(data.timestamp),
                        fontSize = 9.sp,
                        color = Color(0xFF718096)
                    )
                }
            }
        }
    }
}

private fun formatDateLabel(timestamp: Long): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val month = calendar.get(java.util.Calendar.MONTH) + 1
    return String.format("%02d/%02d", day, month)
}
