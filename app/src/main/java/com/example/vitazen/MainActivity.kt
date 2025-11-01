package com.example.vitazen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.vitazen.navigation.AppNavGraph // <--- 1. Import AppNavGraph
import com.example.vitazen.ui.theme.VitaZenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VitaZenTheme {
                // Chúng ta sẽ dùng Surface thay vì Scaffold để AppNavGraph kiểm soát toàn bộ màn hình
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph() // <--- 2. Gọi AppNavGraph ở đây
                }
            }
        }
    }
}
