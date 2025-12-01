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

        if (currentUser == null) {
            Log.d("SplashScreen", "❌ No user logged in -> WELCOME")
            onNavigate(Routes.WELCOME)
        } else {
            // Lấy user từ database
            val user = userRepository.getUserById(currentUser.uid)
            Log.d("SplashScreen", "User from database: uid=${user?.uid}, username='${user?.username}', email=${user?.email}")

            if (user == null) {
                Log.d("SplashScreen", "❌ User not found in DB -> NAME_INPUT")
                onNavigate(Routes.NAME_INPUT)
            } else if (user.username.isBlank()) {
                Log.d("SplashScreen", "❌ User has no username -> NAME_INPUT")
                onNavigate(Routes.NAME_INPUT)
            } else {
                Log.d("SplashScreen", "✅ User logged in with name '${user.username}' -> HOME")
                onNavigate(Routes.HOME)
            }
        }
        Log.d("SplashScreen", "=== END SPLASH CHECK ===")
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
