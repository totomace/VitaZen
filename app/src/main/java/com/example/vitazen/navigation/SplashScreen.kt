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

@Composable
fun SplashScreen(
    userRepository: UserRepository,
    onNavigate: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onNavigate(Routes.WELCOME)
        } else {
            val user = userRepository.getUserById(currentUser.uid)
            if (user?.username.isNullOrBlank()) {
                onNavigate(Routes.NAME_INPUT)
            } else {
                onNavigate(Routes.HOME)
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
