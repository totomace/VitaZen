package com.example.vitazen.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.example.vitazen.ui.home.HomeScreen
import com.example.vitazen.ui.login.LoginScreen
import com.example.vitazen.ui.welcome.WelcomeScreen
import com.example.vitazen.viewmodel.LoginViewModel
import com.example.vitazen.viewmodel.WelcomeViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph() {
    // Sử dụng AnimatedNavController để hỗ trợ hiệu ứng
    val navController = rememberAnimatedNavController()

    // Sử dụng AnimatedNavHost thay cho NavHost thông thường
    AnimatedNavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        // Màn hình Chào mừng (Welcome)
        composable(
            route = Routes.WELCOME,
            // Hiệu ứng khi thoát khỏi màn Welcome: Mờ dần đi
            exitTransition = {
                fadeOut(animationSpec = tween(500)) // Thời gian 500ms
            }
        ) {
            val welcomeViewModel: WelcomeViewModel = viewModel()
            WelcomeScreen(
                viewModel = welcomeViewModel,
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        // Xóa màn hình Welcome khỏi backstack
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        // Màn hình Đăng nhập (Login)
        composable(
            route = Routes.LOGIN,
            // Hiệu ứng khi vào màn Login: Từ từ hiện ra
            enterTransition = {
                fadeIn(animationSpec = tween(500))
            },
            // Hiệu ứng khi thoát khỏi màn Login (để sang Home): Mờ dần đi
            exitTransition = {
                fadeOut(animationSpec = tween(500))
            }
        ) {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToHome = {
                    // Điều hướng đến Home và xóa toàn bộ lịch sử (Welcome, Login)
                    navController.navigate(Routes.HOME) {
                        popUpTo(0)
                    }
                }
            )
        }

        // Màn hình Chính (Home)
        composable(
            route = Routes.HOME,
            // Hiệu ứng khi vào màn Home: Từ từ hiện ra
            enterTransition = {
                fadeIn(animationSpec = tween(500))
            }
        ) {
            HomeScreen()
            // Sau này bạn có thể thêm HomeViewModel tại đây
        }
    }
}
