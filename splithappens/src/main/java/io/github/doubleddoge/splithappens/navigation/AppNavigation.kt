package io.github.doubleddoge.splithappens.navigation

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.doubleddoge.splithappens.HomeScreen
import io.github.doubleddoge.splithappens.HomeViewModel
import io.github.doubleddoge.splithappens.HomeViewModelFactory
import io.github.doubleddoge.splithappens.MenuScreen
import io.github.doubleddoge.splithappens.MenuViewModel
import io.github.doubleddoge.splithappens.MenuViewModelFactory
import io.github.doubleddoge.splithappens.ProfileScreen
import io.github.doubleddoge.splithappens.ProfileViewModel
import io.github.doubleddoge.splithappens.ProfileViewModelFactory
import io.github.doubleddoge.splithappens.ScreenSettings
import io.github.doubleddoge.splithappens.SettingViewModel
import io.github.doubleddoge.splithappens.SettingViewModelFactory
import io.github.doubleddoge.splithappens.SplashScreen
import io.github.doubleddoge.splithappens.SplitHappensApp
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.getValue
import io.github.doubleddoge.splithappens.ProfileViewModelFactory
import io.github.doubleddoge.splithappens.ProfileViewModel
import androidx.compose.runtime.produceState
import io.github.doubleddoge.splithappens.ProfileScreen


@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    // Hoisted Settings ViewModel so settings state propagates across screens
    val settingsViewModel: SettingViewModel = viewModel(
        factory = SettingViewModelFactory(context.applicationContext as Application)
    )
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            val app = LocalContext.current.applicationContext as SplitHappensApp
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) {
                            inclusive = true
                        }
                    }
                },
                awaitReady = { app.seedJob.join() }
            )
        }

        // 2. Home Screen
        composable(Screen.Home.route) {
            val context = LocalContext.current
            val db = remember(context){ (context.applicationContext as SplitHappensApp).database }
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(
                    userDao = db.userDao(),
                    gameHistoryDao = db.gameHistoryDao()
                )
            )

            HomeScreen(
                viewModel = homeViewModel,
                isDarkTheme = settingsUiState.darkTheme,
                onPlayClick = {
                    navController.navigate(Screen.Game.route)
                },
                onNavigateMenu = {
                    navController.navigate(Screen.Menu.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateGame = {
                    navController.navigate(Screen.Game.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // 3. Settings Screen
        composable(Screen.Settings.route) {
            ScreenSettings(
                uiState = settingsUiState,
                onToggleDarkTheme = { settingsViewModel.toggleDarkTheme(it) },
                onToggleShowCardTotal = { settingsViewModel.toggleShowCardTotal(it) },
                onToggleDealerRuleBanner = { settingsViewModel.toggleDealerRuleBanner(it) },
                onToggleSoundEffects = { settingsViewModel.toggleSoundEffects(it) },
                onToggleHapticVibration = { settingsViewModel.toggleHapticVibration(it) },
                onToggleFastDeal = { settingsViewModel.toggleFastDeal(it) },
                onToggleAutoStandOn21 = { settingsViewModel.toggleAutoStandOn21(it) },
                onNavigateBack = {
                    val popped = navController.popBackStack(Screen.Menu.route, false)
                    if (!popped) {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Settings.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onNavigateHome = {
                    val popped = navController.popBackStack(Screen.Home.route, false)
                    if (!popped) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                onNavigateGame = {
                    navController.navigate(Screen.Game.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateMenu = {
                    val popped = navController.popBackStack(Screen.Menu.route, false)
                    if (!popped) {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Settings.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // 4. Menu Screen
        composable(Screen.Menu.route) {
            val menuViewModel: MenuViewModel = viewModel(
                factory = MenuViewModelFactory(context.applicationContext as Application)
            )
            val menuUiState by menuViewModel.uiState.collectAsState()

            val db = remember(context) { buildSplitHappensDatabase(context) }
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(
                    userDao = db.userDao(),
                    gameHistoryDao = db.gameHistoryDao()
                )
            )
            val homeUiState by homeViewModel.uiState.collectAsState()

            MenuScreen(
                uiState = menuUiState,
                homeUiState = homeUiState,
                isDarkTheme = settingsUiState.darkTheme,
                onOpenRules = { menuViewModel.openRulesBooklet() },
                onCloseRules = { menuViewModel.closeRulesBooklet() },
                onOpenLogoutConfirm = { menuViewModel.openLogoutConfirmation() },
                onCloseLogoutConfirm = { menuViewModel.closeLogoutConfirmation() },
                onConfirmLogout = {
                    menuViewModel.logout(onLoggedOut = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    })
                },
                onResumeGame = {
                    navController.navigate(Screen.Game.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateHome = {
                    val popped = navController.popBackStack(Screen.Home.route, false)
                    if (!popped) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                onNavigateGame = {
                    navController.navigate(Screen.Game.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateProfile = {
                    navController.navigate(Screen.Profile.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.Profile.route) {
            val context = LocalContext.current
            val db = remember(context) { (context.applicationContext as SplitHappensApp).database }

            val userId by produceState("") {
                value = db.userDao().observeAll().first().firstOrNull()?.userId ?: ""
            }

            if (userId.isNotBlank()) {
                val vm: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(db.userDao(), db.gameHistoryDao(), userId)
                )
                ProfileScreen(
                    viewModel = vm,
                    onNavigateHome = { navController.navigate(Screen.Home.route) },
                    onNavigateGame = { navController.navigate(Screen.Game.route) },
                    onNavigateSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
        }

        // 5. Game Screen Stub
        composable(Screen.Game.route) {
            Text("Game Screen Placeholder")
        }

        // 6. Profile Screen
        composable(Screen.Profile.route) {
            val db = remember(context) { buildSplitHappensDatabase(context) }
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(
                    userDao = db.userDao(),
                    gameHistoryDao = db.gameHistoryDao()
                )
            )
            val homeUiState by homeViewModel.uiState.collectAsState()

            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(
                    userDao = db.userDao(),
                    gameHistoryDao = db.gameHistoryDao(),
                    userId = homeUiState.userId
                )
            )

            ProfileScreen(
                viewModel = profileViewModel,
                isDarkTheme = settingsUiState.darkTheme, // <-- Connects to global dark/light state
                onNavigateBack = {
                    val popped = navController.popBackStack(Screen.Menu.route, false)
                    if (!popped) {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Profile.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onNavigateHome = {
                    val popped = navController.popBackStack(Screen.Home.route, false)
                    if (!popped) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                onNavigateGame = {
                    navController.navigate(Screen.Game.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateMenu = {
                    val popped = navController.popBackStack(Screen.Menu.route, false)
                    if (!popped) {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Profile.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}