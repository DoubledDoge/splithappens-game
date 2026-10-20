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
import io.github.doubleddoge.splithappens.ui.theme.SplitHappensTheme
import kotlinx.coroutines.flow.first

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val settingViewModel: SettingViewModel = viewModel(
        factory = SettingViewModelFactory(context.applicationContext as Application)
    )
    val settingUiState by settingViewModel.uiState.collectAsState()

    SplitHappensTheme(darkTheme = settingUiState.darkTheme) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route
        ) {
            composable(Screen.Splash.route) {
                val app = LocalContext.current.applicationContext as SplitHappensApp
                SplashScreen(
                    onTimeout = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                    awaitReady = { app.seedJob.join() }
                )
            }

            composable(Screen.Home.route) {
                val context = LocalContext.current
                val db = remember(context.applicationContext) { (context.applicationContext as SplitHappensApp).database }
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(
                        userDao = db.userDao(),
                        gameHistoryDao = db.gameHistoryDao()
                    )
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    isDarkTheme = settingUiState.darkTheme,
                    onPlayClick = {
                        navController.navigate(Screen.Game.route)
                    },
                    onNavigateSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateMenu = {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateGame = {
                        navController.navigate(Screen.Game.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateProfile = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }


            // --- MENU SCREEN DESTINATION ---
            composable(Screen.Menu.route) {
                val context = LocalContext.current
                val menuViewModel: MenuViewModel = viewModel(
                    factory = MenuViewModelFactory(context.applicationContext as Application)
                )
                val menuUiState by menuViewModel.uiState.collectAsState()

                val db = remember(context.applicationContext) { (context.applicationContext as SplitHappensApp).database }
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
                    isDarkTheme = settingUiState.darkTheme,
                    onOpenRules = { menuViewModel.openRulesBooklet() },
                    onCloseRules = { menuViewModel.closeRulesBooklet() },
                    onOpenLogoutConfirm = { menuViewModel.openLogoutConfirmation() },
                    onCloseLogoutConfirm = { menuViewModel.closeLogoutConfirmation() },
                    onConfirmLogout = {
                        menuViewModel.logout {
                            navController.navigate(Screen.Splash.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onNavigateHome = { navController.navigate(Screen.Home.route) },
                    onNavigateGame = { navController.navigate(Screen.Game.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            // --- SETTINGS SCREEN DESTINATION ---
            composable(Screen.Settings.route) {
                val context = LocalContext.current
                val settingViewModelModal: SettingViewModel = viewModel(
                    factory = SettingViewModelFactory(context.applicationContext as Application)
                )
                val settingUiStateModal by settingViewModelModal.uiState.collectAsState()
                ScreenSettings(
                    uiState = settingUiStateModal,
                    onToggleDarkTheme = { settingViewModelModal.toggleDarkTheme(it) },
                    onToggleShowCardTotal = { settingViewModelModal.toggleShowCardTotal(it) },
                    onToggleDealerRuleBanner = { settingViewModelModal.toggleDealerRuleBanner(it) },
                    onToggleSoundEffects = { settingViewModelModal.toggleSoundEffects(it) },
                    onToggleHapticVibration = { settingViewModelModal.toggleHapticVibration(it) },
                    onToggleFastDeal = { settingViewModelModal.toggleFastDeal(it) },
                    onToggleAutoStandOn21 = { settingViewModelModal.toggleAutoStandOn21(it) },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateHome = { navController.navigate(Screen.Home.route) },
                    onNavigateGame = { navController.navigate(Screen.Game.route) },
                    onNavigateMenu = { navController.navigate(Screen.Menu.route) }
                )
            }
            composable(Screen.Game.route) {
                GameScreen(
                    isDarkTheme = settingUiState.darkTheme,
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateMenu = {
                        navController.navigate(Screen.Menu.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Profile.route) {
                val context = LocalContext.current
                val db = remember(context.applicationContext) { (context.applicationContext as SplitHappensApp).database }

                val userId by produceState("") {
                    value = db.userDao().observeAll().first().firstOrNull()?.userId ?: ""
                }

                if (userId.isNotBlank()) {
                    val vm: ProfileViewModel = viewModel(
                        factory = ProfileViewModelFactory(db.userDao(), db.gameHistoryDao(), userId)
                    )
                    ProfileScreen(
                        viewModel = vm,
                        isDarkTheme = settingUiState.darkTheme,
                        onNavigateHome = { navController.navigate(Screen.Home.route) },
                        onNavigateGame = { navController.navigate(Screen.Game.route) },
                        onNavigateMenu = { navController.navigate(Screen.Menu.route) }
                    )
                }
            }
        }
    }
}
