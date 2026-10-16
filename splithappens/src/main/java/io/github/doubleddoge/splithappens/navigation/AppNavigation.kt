package io.github.doubleddoge.splithappens.navigation

import android.app.Application
import androidx.compose.material3.Text
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
import io.github.doubleddoge.splithappens.*
import kotlinx.coroutines.flow.first

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val app = context.applicationContext as SplitHappensApp
    val db = app.database // single instance for the whole process

    val settingsViewModel: SettingViewModel = viewModel(
        factory = SettingViewModelFactory(context.applicationContext as Application)
    )
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    fun goHome() {
        if (!navController.popBackStack(Screen.Home.route, false)) {
            navController.navigate(Screen.Home.route) { launchSingleTop = true }
        }
    }
    fun goMenu() {
        if (!navController.popBackStack(Screen.Menu.route, false)) {
            navController.navigate(Screen.Menu.route) { launchSingleTop = true }
        }
    }
    fun goGame() = navController.navigate(Screen.Game.route) { launchSingleTop = true }
    fun goProfile() = navController.navigate(Screen.Profile.route) { launchSingleTop = true }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                awaitReady = { app.seedJob.join() },
                onTimeout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(db.userDao(), db.gameHistoryDao())
            )
            HomeScreen(
                viewModel = homeViewModel,
                isDarkTheme = settingsUiState.darkTheme,
                onPlayClick = { goGame() },
                onNavigateGame = { goGame() },
                onNavigateMenu = {
                    navController.navigate(Screen.Menu.route) { launchSingleTop = true }
                },
                onNavigateProfile = { goProfile() }
            )
        }

        composable(Screen.Menu.route) {
            val menuViewModel: MenuViewModel = viewModel(
                factory = MenuViewModelFactory(context.applicationContext as Application)
            )
            val menuUiState by menuViewModel.uiState.collectAsState()
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(db.userDao(), db.gameHistoryDao())
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
                        navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
                    })
                },
                onResumeGame = { goGame() },
                onNavigateHome = { goHome() },
                onNavigateGame = { goGame() },
                onNavigateToProfile = { goProfile() },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                }
            )
        }

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
                onNavigateBack = { navController.popBackStack() },
