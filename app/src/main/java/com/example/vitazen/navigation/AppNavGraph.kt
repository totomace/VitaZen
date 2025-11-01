package com.example.vitazen.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vitazen.ui.welcome.WelcomeScreen
// Giả sử bạn có một màn hình WelcomeScreen trong thư mục ui.screens
// import com.example.vitazen.ui.screens.WelcomeScreen

// Giả sử bạn có một đối tượng Routes để lưu trữ các tuyến đường
// object Routes {
//     const val WELCOME = "welcome"
//     const val LOGIN = "login"
// }

@Composable
fun AppNavGraph() {
    // 1. Tạo và ghi nhớ một NavController
    val navController = rememberNavController()

    // 2. Tạo NavHost để định nghĩa biểu đồ điều hướng
    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME // Màn hình bắt đầu
    ) {
        // 3. Đặt composable của bạn vào bên trong NavHost
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onStartClicked = { navController.navigate(Routes.LOGIN) }
            )
        }

        // TODO: Thêm các màn hình khác vào đây
        // Ví dụ:
        // composable(Routes.LOGIN) {
        //     LoginScreen(
        //         onNavigateToHome = { /* ... */ }
        //     )
        // }
    }
}
