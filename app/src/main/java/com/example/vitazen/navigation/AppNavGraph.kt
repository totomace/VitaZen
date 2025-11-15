
package com.example.vitazen.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.example.vitazen.ui.nameinput.NameInputModalScreen
import com.example.vitazen.viewmodel.NameInputViewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import com.example.vitazen.viewmodel.LoginViewModel
import com.example.vitazen.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
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

private const val ANIMATION_DURATION = 400

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userRepository = remember { UserRepository(VitaZenDatabase.getInstance(context).userDao()) }

    fun <T : ViewModel> viewModelFactory(create: () -> T) = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T2 : ViewModel> create(modelClass: Class<T2>): T2 = create() as T2
    }

    val loginViewModelFactory = remember { viewModelFactory { LoginViewModel(FirebaseAuth.getInstance(), userRepository) } }
    val registerViewModelFactory = remember { viewModelFactory { RegisterViewModel(FirebaseAuth.getInstance(), userRepository) } }
    val homeViewModelFactory = remember { viewModelFactory { com.example.vitazen.viewmodel.HomeViewModel(userRepository) } }
    val nameInputViewModelFactory = remember { viewModelFactory { NameInputViewModel(userRepository) } }

    val welcomeViewModelFactory = remember { viewModelFactory { com.example.vitazen.viewmodel.WelcomeViewModel() } }

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        composable(route = Routes.WELCOME) {
            val welcomeViewModel: com.example.vitazen.viewmodel.WelcomeViewModel = viewModel(factory = welcomeViewModelFactory)
            WelcomeScreen(
                viewModel = welcomeViewModel,
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }
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
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }
        composable(route = Routes.NAME_INPUT) {
            val nameInputViewModel: NameInputViewModel = viewModel(factory = nameInputViewModelFactory)
            NameInputModalScreen(
                viewModel = nameInputViewModel,
                onSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.NAME_INPUT) { inclusive = true }
                    }
                }
            )
        }
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
        composable(
            route = Routes.HOME,
            enterTransition = {
                fadeIn(animationSpec = tween(ANIMATION_DURATION)) +
                scaleIn(initialScale = 0.96f, animationSpec = tween(ANIMATION_DURATION))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(ANIMATION_DURATION)) +
                scaleOut(targetScale = 1.04f, animationSpec = tween(ANIMATION_DURATION))
            }
        ) {
            val homeViewModel: com.example.vitazen.viewmodel.HomeViewModel = viewModel(factory = homeViewModelFactory)
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToHome = {},
                onNavigateToReminder = { navController.navigate(Routes.REMINDER) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                selectedTab = 0,
                onTabSelected = { index ->
                    when (index) {
                        0 -> {}
                        1 -> navController.navigate(Routes.REMINDER)
                        2 -> navController.navigate(Routes.HISTORY)
                        3 -> navController.navigate(Routes.SETTINGS)
                    }
                }
            )
        }
        composable(
            route = Routes.REMINDER,
            enterTransition = {
                fadeIn(animationSpec = tween(ANIMATION_DURATION)) +
                scaleIn(initialScale = 0.96f, animationSpec = tween(ANIMATION_DURATION))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(ANIMATION_DURATION)) +
                scaleOut(targetScale = 1.04f, animationSpec = tween(ANIMATION_DURATION))
            }
        ) {
            com.example.vitazen.ui.reminder.ReminderScreen()
        }
        composable(
            route = Routes.HISTORY,
            enterTransition = {
                fadeIn(animationSpec = tween(ANIMATION_DURATION)) +
                scaleIn(initialScale = 0.96f, animationSpec = tween(ANIMATION_DURATION))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(ANIMATION_DURATION)) +
                scaleOut(targetScale = 1.04f, animationSpec = tween(ANIMATION_DURATION))
            }
        ) {
            com.example.vitazen.ui.history.HistoryScreen()
        }
        composable(
            route = Routes.SETTINGS,
            enterTransition = {
                fadeIn(animationSpec = tween(ANIMATION_DURATION)) +
                scaleIn(initialScale = 0.96f, animationSpec = tween(ANIMATION_DURATION))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(ANIMATION_DURATION)) +
                scaleOut(targetScale = 1.04f, animationSpec = tween(ANIMATION_DURATION))
            }
        ) {
            com.example.vitazen.ui.profile.ProfileScreen(
                navController = navController
            )
        }
    }
}
