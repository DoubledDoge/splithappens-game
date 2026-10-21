package io.github.doubleddoge.splithappens.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.doubleddoge.splithappens.GameScreen
import io.github.doubleddoge.splithappens.HomeScreen
import io.github.doubleddoge.splithappens.HomeViewModel
import io.github.doubleddoge.splithappens.HomeViewModelFactory
import io.github.doubleddoge.splithappens.LoginScreen
import io.github.doubleddoge.splithappens.LoginViewModel
import io.github.doubleddoge.splithappens.LoginViewModelFactory
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

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as SplitHappensApp
    val db = remember(app) { app.database }

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
            val loginVm: LoginViewModel = viewModel(factory = LoginViewModelFactory(db.userDao(), context))
            SplashScreen(
                onTimeout = {
                    loginVm.checkAutoLogin {
                        navController.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                    }
                    if (navController.currentDestination?.route == Screen.Splash.route) {
                        navController.navigate(Screen.Login.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                    }
                },
                awaitReady = { app.seedJob.join() }
            )
        }

        // 2. Login Screen
        composable(Screen.Login.route) {
            val loginVm: LoginViewModel = viewModel(factory = LoginViewModelFactory(db.userDao(), context))
            LoginScreen(
                viewModel = loginVm,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Home Screen
        composable(Screen.Home.route) {
            val homeVm: HomeViewModel = viewModel(factory = HomeViewModelFactory(db.userDao(), db.gameHistoryDao()))
            HomeScreen(
                viewModel = homeVm,
                isDarkTheme = settingsUiState.darkTheme,
                onPlayClick = { navController.navigate(Screen.Game.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } },
                onNavigateMenu = { navController.navigate(Screen.Menu.route) { launchSingleTop = true } },
                onNavigateGame = { navController.navigate(Screen.Game.route) { launchSingleTop = true } },
                onNavigateProfile = { navController.navigate(Screen.Profile.route) { launchSingleTop = true } }
            )
        }

        // 4. Game Screen
        composable(Screen.Game.route) {
            val homeVm: HomeViewModel = viewModel(factory = HomeViewModelFactory(db.userDao(), db.gameHistoryDao()))
            val homeUiState by homeVm.uiState.collectAsState()
            GameScreen(
                displayName = homeUiState.displayName,
                chipsOwned = homeUiState.chipsOwned,
                isDarkTheme = settingsUiState.darkTheme,
                onNavigateHome = { navController.navigate(Screen.Home.route) { popUpTo(0) } },
                onNavigateMenu = { navController.navigate(Screen.Menu.route) { launchSingleTop = true } }
            )
        }

        // 5. Menu Screen
        composable(Screen.Menu.route) {
            val menuVm: MenuViewModel = viewModel(factory = MenuViewModelFactory(context.applicationContext as Application))
            val menuUiState by menuVm.uiState.collectAsState()
            val homeVm: HomeViewModel = viewModel(factory = HomeViewModelFactory(db.userDao(), db.gameHistoryDao()))
            val homeUiState by homeVm.uiState.collectAsState()

            MenuScreen(
                uiState = menuUiState,
                homeUiState = homeUiState,
                isDarkTheme = settingsUiState.darkTheme,
                onOpenRules = menuVm::openRulesBooklet,
                onCloseRules = menuVm::closeRulesBooklet,
                onOpenLogoutConfirm = menuVm::openLogoutConfirmation,
                onCloseLogoutConfirm = menuVm::closeLogoutConfirmation,
                onConfirmLogout = {
                    menuVm.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateHome = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
                onNavigateGame = { navController.navigate(Screen.Game.route) { launchSingleTop = true } },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) { launchSingleTop = true } },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } }
            )
        }

        // 6. Settings Screen
        composable(Screen.Settings.route) {
            ScreenSettings(
                uiState = settingsUiState,
                onToggleDarkTheme = settingsViewModel::toggleDarkTheme,
                onToggleShowCardTotal = settingsViewModel::toggleShowCardTotal,
                onToggleDealerRuleBanner = settingsViewModel::toggleDealerRuleBanner,
                onToggleSoundEffects = settingsViewModel::toggleSoundEffects,
                onToggleHapticVibration = settingsViewModel::toggleHapticVibration,
                onToggleFastDeal = settingsViewModel::toggleFastDeal,
                onToggleAutoStandOn21 = settingsViewModel::toggleAutoStandOn21,
                onNavigateHome = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
                onNavigateGame = { navController.navigate(Screen.Game.route) { launchSingleTop = true } },
                onNavigateMenu = { navController.navigate(Screen.Menu.route) { launchSingleTop = true } }
            )
        }

        // 7. Profile Screen
        composable(Screen.Profile.route) {
            val userId by produceState("") {
                value = db.userDao().observeAll().first().firstOrNull()?.userId ?: "default_player"
            }
            if (userId.isNotBlank()) {
                val profileVm: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(db.userDao(), db.gameHistoryDao(), userId)
                )
                ProfileScreen(
                    viewModel = profileVm,
                    isDarkTheme = settingsUiState.darkTheme,
                    onNavigateHome = { navController.navigate(Screen.Home.route) },
                    onNavigateGame = { navController.navigate(Screen.Game.route) },
                    onNavigateMenu = { navController.navigate(Screen.Menu.route) }
                )
            }
        }
    }
}
