package io.github.doubleddoge.splithappens.navigation

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

private val NavDarkBackground = Color(0xFF071B12)
private val NavLightBackground = Color(0xFF144D38)

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val app = context.applicationContext as SplitHappensApp
    val db = remember(app) { app.database }

    val authPrefs = remember(context) {
        context.getSharedPreferences("split_happens_auth", Context.MODE_PRIVATE)
    }

    // Reactive state for the active user ID so navigation updates when profiles switch
    var activeUserId by remember {
        mutableStateOf(authPrefs.getString("saved_user_id", "") ?: "")
    }

    DisposableEffect(authPrefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "saved_user_id") {
                activeUserId = authPrefs.getString("saved_user_id", "") ?: ""
            }
        }
        authPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            authPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val settingsViewModel: SettingViewModel =
        viewModel(factory = SettingViewModelFactory(context.applicationContext as Application))
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    val navBgColor = if (settingsUiState.darkTheme) NavDarkBackground else NavLightBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(navBgColor)
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .background(navBgColor)
        ) {
            // 1. Splash Screen
            composable(Screen.Splash.route) {
                val loginVm: LoginViewModel =
                    viewModel(factory = LoginViewModelFactory(db.userDao(), context))
                SplashScreen(
                    onTimeout = {
                        loginVm.checkAutoLogin {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                        if (navController.currentDestination?.route == Screen.Splash.route) {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    },
                    awaitReady = { app.seedJob.join() },
                )
            }

            // 2. Login Screen
            composable(Screen.Login.route) {
                val loginVm: LoginViewModel =
                    viewModel(factory = LoginViewModelFactory(db.userDao(), context))
                LoginScreen(
                    viewModel = loginVm,
                    isDarkTheme = settingsUiState.darkTheme,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                )
            }

            // 3. Home Screen
            composable(Screen.Home.route) {
                val homeVm: HomeViewModel =
                    viewModel(
                        factory = HomeViewModelFactory(db.userDao(), db.gameHistoryDao(), activeUserId),
                        key = "home_$activeUserId"
                    )
                HomeScreen(
                    viewModel = homeVm,
                    isDarkTheme = settingsUiState.darkTheme,
                    onPlayClick = { navController.navigate(Screen.Game.route) },
                    onNavigateSettings = {
                        navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                    },
                    onNavigateMenu = { navController.navigate(Screen.Menu.route) { launchSingleTop = true } },
                    onNavigateGame = { navController.navigate(Screen.Game.route) { launchSingleTop = true } },
                    onNavigateProfile = {
                        navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                    },
                )
            }

            // 4. Game Screen
            composable(Screen.Game.route) {
                val homeVm: HomeViewModel =
                    viewModel(
                        factory = HomeViewModelFactory(db.userDao(), db.gameHistoryDao(), activeUserId),
                        key = "game_header_$activeUserId"
                    )
                val homeUiState by homeVm.uiState.collectAsState()
                GameScreen(
                    userId = activeUserId,
                    displayName = homeUiState.displayName,
                    chipsOwned = homeUiState.chipsOwned,
                    isDarkTheme = settingsUiState.darkTheme,
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateMenu = {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            // 5. Menu Screen
            composable(Screen.Menu.route) {
                val menuVm: MenuViewModel =
                    viewModel(factory = MenuViewModelFactory(context.applicationContext as Application))
                val menuUiState by menuVm.uiState.collectAsState()
                val homeVm: HomeViewModel =
                    viewModel(
                        factory = HomeViewModelFactory(db.userDao(), db.gameHistoryDao(), activeUserId),
                        key = "menu_$activeUserId"
                    )
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
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                    },
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
                    onNavigateMenu = { navController.navigate(Screen.Menu.route) { launchSingleTop = true } },
                )
            }

            // 7. Profile Screen
            composable(Screen.Profile.route) {
                val resolvedUserId by
                produceState(initialValue = activeUserId, key1 = activeUserId) {
                    if (activeUserId.isNotBlank()) {
                        value = activeUserId
                    } else {
                        value = db.userDao().observeAll().first().firstOrNull()?.userId ?: "default_player"
                    }
                }
                if (resolvedUserId.isNotBlank()) {
                    val profileVm: ProfileViewModel =
                        viewModel(
                            factory = ProfileViewModelFactory(db.userDao(), db.gameHistoryDao(), resolvedUserId),
                            key = "profile_$resolvedUserId"
                        )
                    ProfileScreen(
                        viewModel = profileVm,
                        isDarkTheme = settingsUiState.darkTheme,
                        onNavigateHome = { navController.navigate(Screen.Home.route) },
                        onNavigateGame = { navController.navigate(Screen.Game.route) },
                        onNavigateMenu = { navController.navigate(Screen.Menu.route) },
                    )
                }
            }
        }
    }
}