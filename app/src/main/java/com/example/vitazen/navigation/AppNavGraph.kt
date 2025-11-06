package com.example.vitazen.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
// THAY ĐỔI IMPORT: Sử dụng thư viện chính thức của AndroidX
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vitazen.ui.home.HomeScreen
import com.example.vitazen.ui.login.LoginScreen
import com.example.vitazen.ui.register.RegisterScreen
import com.example.vitazen.ui.welcome.WelcomeScreen
import com.example.vitazen.viewmodel.LoginViewModel
import com.example.vitazen.viewmodel.RegisterViewModel
import com.example.vitazen.viewmodel.WelcomeViewModel

// BỎ ANNOTATION @OptIn(ExperimentalAnimationApi::class) vì không cần nữa
@Composable
fun AppNavGraph() {
    // SỬ DỤNG LẠI rememberNavController thông thường
    val navController = rememberNavController()

    // SỬ DỤNG LẠI NavHost thông thường, hiệu ứng được định nghĩa trong composable
    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        // Màn hình Chào mừng (Welcome)
        composable(
            route = Routes.WELCOME,
            // Hiệu ứng được định nghĩa trực tiếp ở đây
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            val welcomeViewModel: WelcomeViewModel = viewModel()
            WelcomeScreen(
                viewModel = welcomeViewModel,
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        // Màn hình Đăng nhập (Login)
        composable(
            route = Routes.LOGIN,
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) { popUpTo(0) }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        // MÀN HÌNH ĐĂNG KÝ (MỚI)
        composable(
            route = Routes.REGISTER,
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            val registerViewModel: RegisterViewModel = viewModel()
            RegisterScreen(
                viewModel = registerViewModel,
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) { popUpTo(0) }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Màn hình Chính (Home)
        composable(
            route = Routes.HOME,
            enterTransition = { fadeIn(animationSpec = tween(500)) }
        ) {
            HomeScreen()
        }
    }
}
