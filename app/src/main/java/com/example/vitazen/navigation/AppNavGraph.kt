package com.example.vitazen.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
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

/**
 * Hằng số cho animation duration để tái sử dụng
 */
private const val ANIMATION_DURATION = 400
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

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
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) { 
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
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
            val registerViewModel: RegisterViewModel = viewModel()
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

        // Màn hình Home - Fade in mượt mà
        composable(
            route = Routes.HOME,
            enterTransition = {
                fadeIn(animationSpec = tween(ANIMATION_DURATION))
            }
        ) {
            HomeScreen()
        }
    }
}
