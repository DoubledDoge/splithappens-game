package io.github.doubleddoge.splithappens.navigation

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.doubleddoge.splithappens.BorderGold
import io.github.doubleddoge.splithappens.DarkBackground
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

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val settingViewModel: SettingViewModel = viewModel(
        factory = SettingViewModelFactory(application)
    )
    val settingUiState by settingViewModel.uiState.collectAsState()
    val isDarkTheme = settingUiState.darkTheme

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            val app = LocalContext.current.applicationContext as SplitHappensApp
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                awaitReady = { app.seedJob.join() }
            )
        }

        composable(Screen.Home.route) {
            val db = remember(context.applicationContext) { (context.applicationContext as SplitHappensApp).database }
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(
                    userDao = db.userDao(),
                    gameHistoryDao = db.gameHistoryDao()
                )
            )
            HomeScreen(
                viewModel = homeViewModel,
                isDarkTheme = isDarkTheme,
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

        composable(Screen.Profile.route) {
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
                    isDarkTheme = isDarkTheme,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateHome = { navController.navigate(Screen.Home.route) },
                    onNavigateGame = { navController.navigate(Screen.Game.route) },
                    onNavigateMenu = { navController.navigate(Screen.Menu.route) }
                )
            }
        }

        composable(Screen.Menu.route) {
            val db = remember(context.applicationContext) { (context.applicationContext as SplitHappensApp).database }
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(
                    userDao = db.userDao(),
                    gameHistoryDao = db.gameHistoryDao()
                )
            )
            val homeUiState by homeViewModel.uiState.collectAsState()
            val menuViewModel: MenuViewModel = viewModel(
                factory = MenuViewModelFactory(application)
            )
            val menuUiState by menuViewModel.uiState.collectAsState()

            MenuScreen(
                uiState = menuUiState,
                homeUiState = homeUiState,
                isDarkTheme = isDarkTheme,
                onOpenRules = { menuViewModel.openRulesBooklet() },
                onCloseRules = { menuViewModel.closeRulesBooklet() },
                onOpenLogoutConfirm = { menuViewModel.openLogoutConfirmation() },
                onCloseLogoutConfirm = { menuViewModel.closeLogoutConfirmation() },
                onConfirmLogout = {
                    menuViewModel.logout {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                },
                onResumeGame = { navController.navigate(Screen.Game.route) },
                onNavigateHome = { navController.navigate(Screen.Home.route) },
                onNavigateGame = { navController.navigate(Screen.Game.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Settings.route) {
            ScreenSettings(
                uiState = settingUiState,
                onToggleDarkTheme = { settingViewModel.toggleDarkTheme(it) },
                onToggleShowCardTotal = { settingViewModel.toggleShowCardTotal(it) },
                onToggleDealerRuleBanner = { settingViewModel.toggleDealerRuleBanner(it) },
                onToggleSoundEffects = { settingViewModel.toggleSoundEffects(it) },
                onToggleHapticVibration = { settingViewModel.toggleHapticVibration(it) },
                onToggleFastDeal = { settingViewModel.toggleFastDeal(it) },
                onToggleAutoStandOn21 = { settingViewModel.toggleAutoStandOn21(it) },
                onNavigateBack = { navController.popBackStack() },
                onNavigateHome = { navController.navigate(Screen.Home.route) },
                onNavigateGame = { navController.navigate(Screen.Game.route) },
                onNavigateMenu = { navController.navigate(Screen.Menu.route) }
            )
        }

        composable(Screen.Game.route) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDarkTheme) DarkBackground else Color(0xFF144D38)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "♠ BLACKJACK TABLE ♠",
                        color = BorderGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Table game coming soon!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("BACK TO HOME")
                    }
                }
            }
        }
    }
}
