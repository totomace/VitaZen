package com.example.vitazen.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.vitazen.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import android.util.Log

@Composable
fun SplashScreen(
    userRepository: UserRepository,
    onNavigate: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        // Delay để đảm bảo Firebase Auth và Database đã load xong
        delay(500)

        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.d("SplashScreen", "=== START SPLASH CHECK ===")
        Log.d("SplashScreen", "Current Firebase user: ${currentUser?.uid}")
        Log.d("SplashScreen", "Email: ${currentUser?.email}")
        Log.d("SplashScreen", "DisplayName from Firebase: ${currentUser?.displayName}")

        // Luôn chuyển đến Welcome screen
        // User sẽ bấm nút "Bắt đầu ngay" để vào Home
        Log.d("SplashScreen", "➡️ Navigate to WELCOME")
        onNavigate(Routes.WELCOME)
        
        Log.d("SplashScreen", "=== END SPLASH CHECK ===")
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
