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
import io.github.doubleddoge.splithappens.ScreenSettings
import io.github.doubleddoge.splithappens.SettingViewModel
import io.github.doubleddoge.splithappens.SettingViewModelFactory
import io.github.doubleddoge.splithappens.SplashScreen
import io.github.doubleddoge.splithappens.data.buildSplitHappensDatabase

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Home Screen
        composable(Screen.Home.route) {
            val context = LocalContext.current
            val db = remember(context) { buildSplitHappensDatabase(context) }
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(
                    userDao = db.userDao(),
                    gameHistoryDao = db.gameHistoryDao()
                )
            )

            HomeScreen(
                viewModel = homeViewModel,
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
            val context = LocalContext.current
            val settingsViewModel: SettingViewModel = viewModel(
                factory = SettingViewModelFactory(context.applicationContext as Application)
            )
            val uiState by settingsViewModel.uiState.collectAsState()

            ScreenSettings(
                uiState = uiState,
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
            val context = LocalContext.current
            val menuViewModel: MenuViewModel = viewModel(
                factory = MenuViewModelFactory(context.applicationContext as Application)
            )
            val uiState by menuViewModel.uiState.collectAsState()

            MenuScreen(
                uiState = uiState,
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
                }
            )
        }

        // 5. Game Screen Stub
        composable(Screen.Game.route) {
            Text("Game Screen Placeholder")
        }

        // 6. Profile Screen Stub
        composable(Screen.Profile.route) {
            Text("Profile Screen Placeholder")
        }
    }
}