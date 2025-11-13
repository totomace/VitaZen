package com.example.vitazen.ui.history


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.vitazen.R

@Composable
fun HistoryScreen() {
	Box(modifier = Modifier.fillMaxSize()) {
		Image(
			painter = painterResource(id = R.drawable.welcome),
			contentDescription = "Ảnh nền lịch sử",
			modifier = Modifier.fillMaxSize()
		)
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(Color.Black.copy(alpha = 0.55f))
		)
		Text(
			text = "Lịch sử (History)",
			color = Color.White,
			modifier = Modifier.align(Alignment.Center)
		)
	}
}

@Preview
@Composable
fun HistoryScreenPreview() {
	HistoryScreen()
}
