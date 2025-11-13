package com.example.vitazen.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vitazen.model.database.VitaZenDatabase
import com.example.vitazen.model.repository.UserRepository
import com.example.vitazen.ui.home.HomeScreen
import com.example.vitazen.ui.login.LoginScreen
import com.example.vitazen.ui.register.RegisterScreen
import com.example.vitazen.ui.welcome.WelcomeScreen
import com.example.vitazen.viewmodel.LoginViewModel
import com.example.vitazen.viewmodel.RegisterViewModel
import com.example.vitazen.viewmodel.WelcomeViewModel
import com.google.firebase.auth.FirebaseAuth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.runtime.LaunchedEffect

/**
 * Hằng số cho animation duration để tái sử dụng
 */
private const val ANIMATION_DURATION = 400

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    // Khởi tạo UserRepository từ database
    val context = LocalContext.current
    val database = VitaZenDatabase.getInstance(context)
    val userRepository = UserRepository(database.userDao())

    // Factory để tạo ViewModels với dependencies
    val loginViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(FirebaseAuth.getInstance(), userRepository) as T
        }
    }

    val registerViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RegisterViewModel(FirebaseAuth.getInstance(), userRepository) as T
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        // Màn hình Welcome - Slide right to left khi thoát
        composable(
            route = Routes.WELCOME,
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
            }
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

        // Màn hình Login - Slide in from right
        composable(
            route = Routes.LOGIN,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Routes.REGISTER -> slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                    else -> fadeOut(animationSpec = tween(ANIMATION_DURATION))
                }
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
            }
        ) {
            val loginViewModel: LoginViewModel = viewModel(factory = loginViewModelFactory)
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToHome = {
                    navController.navigate(Routes.NAME_INPUT) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        // Màn hình nhập tên sau đăng nhập (đưa ra ngoài, cùng cấp các route khác)
        composable(route = Routes.NAME_INPUT) {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                // No-op, chỉ để Compose imports sẵn sàng
            }
            var pendingName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
            if (pendingName != null) {
                androidx.compose.runtime.LaunchedEffect(pendingName) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        val user = userRepository.getUserById(currentUser.uid)
                        if (user != null) {
                            val updatedUser = user.copy(username = pendingName!!)
                            userRepository.updateUser(updatedUser)
                        }
                    }
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            com.example.vitazen.ui.nameinput.NameInputScreen(
                onNameEntered = { name ->
                    pendingName = name
                }
            )
        }

        // Màn hình Register - Slide in from right, slide out to right khi back
        composable(
            route = Routes.REGISTER,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(ANIMATION_DURATION))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
            }
        ) {
            val registerViewModel: RegisterViewModel = viewModel(factory = registerViewModelFactory)
            RegisterScreen(
                viewModel = registerViewModel,
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Trang chủ (Home Main)
        composable(
            route = Routes.HOME_MAIN,
            enterTransition = {
                fadeIn(animationSpec = tween(ANIMATION_DURATION))
            }
        ) {
            HomeScreen(
                onNavigateToHome = {},
                onNavigateToReminder = { navController.navigate(Routes.REMINDER) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                selectedTab = 0,
                onTabSelected = { index ->
                    when (index) {
                        0 -> {} // Đã ở trang chủ
                        1 -> navController.navigate(Routes.REMINDER)
                        2 -> navController.navigate(Routes.HISTORY)
                        3 -> navController.navigate(Routes.SETTINGS)
                    }
                }
            )
        }

        // Màn hình Home (cũ, dùng cho chuyển hướng sau login/register)
        composable(
            route = Routes.HOME,
            enterTransition = {
                fadeIn(animationSpec = tween(ANIMATION_DURATION))
            }
        ) {
            // Khi vào HOME, chuyển sang HOME_MAIN và clear backstack
            LaunchedEffect(Unit) {
                navController.navigate(Routes.HOME_MAIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        // Màn hình Nhắc nhở
        composable(route = Routes.REMINDER) {
            com.example.vitazen.ui.reminder.ReminderScreen()
        }

        // Màn hình Lịch sử
        composable(route = Routes.HISTORY) {
            com.example.vitazen.ui.history.HistoryScreen()
        }

        // Màn hình Cài đặt (tạm thời dùng ProfileScreen)
        composable(route = Routes.SETTINGS) {
            com.example.vitazen.ui.profile.ProfileScreen(
                navController = navController
            )
        }
        
    }
}
