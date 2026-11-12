package io.github.doubleddoge.splithappens.ui.navigation

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.doubleddoge.splithappens.SplitHappensApp
import io.github.doubleddoge.splithappens.data.AppDatabase
import io.github.doubleddoge.splithappens.data.pref.PrefFiles
import io.github.doubleddoge.splithappens.data.pref.PrefKeys
import io.github.doubleddoge.splithappens.game.model.GameMode
import io.github.doubleddoge.splithappens.screen.GameScreen
import io.github.doubleddoge.splithappens.screen.HomeScreen
import io.github.doubleddoge.splithappens.screen.LoginScreen
import io.github.doubleddoge.splithappens.screen.MenuScreen
import io.github.doubleddoge.splithappens.screen.ProfileScreen
import io.github.doubleddoge.splithappens.screen.SettingsScreen
import io.github.doubleddoge.splithappens.screen.SplashScreen
import io.github.doubleddoge.splithappens.ui.theme.DarkBackground
import io.github.doubleddoge.splithappens.ui.theme.LightBackground
import io.github.doubleddoge.splithappens.util.PlayerName
import io.github.doubleddoge.splithappens.util.viewModelFactoryOf
import io.github.doubleddoge.splithappens.viewmodel.HomeViewModel
import io.github.doubleddoge.splithappens.viewmodel.LoginViewModel
import io.github.doubleddoge.splithappens.viewmodel.MenuViewModel
import io.github.doubleddoge.splithappens.viewmodel.ProfileViewModel
import io.github.doubleddoge.splithappens.viewmodel.SettingViewModel
import kotlinx.coroutines.flow.first

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
	val context = LocalContext.current
	val app = context.applicationContext as SplitHappensApp
	val db = remember(app) { app.database }

	val authPrefs =
		remember(context) {
			context.getSharedPreferences(PrefFiles.AUTH, Context.MODE_PRIVATE)
		}

	// Reactive state for the active user ID so navigation updates when profiles switch
	var activeUserId by remember {
		mutableStateOf(authPrefs.getString(PrefKeys.SAVED_USER_ID, "") ?: "")
	}

	DisposableEffect(authPrefs) {
		val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
			if (key == PrefKeys.SAVED_USER_ID) {
				activeUserId = authPrefs.getString(PrefKeys.SAVED_USER_ID, "") ?: ""
			}
		}
		authPrefs.registerOnSharedPreferenceChangeListener(listener)
		onDispose {
			authPrefs.unregisterOnSharedPreferenceChangeListener(listener)
		}
	}

	val settingsViewModel: SettingViewModel =
		viewModel(factory = viewModelFactoryOf { SettingViewModel(context.applicationContext as Application) })
	val settingsUiState by settingsViewModel.uiState.collectAsState()

	val navBgColor = if (settingsUiState.darkTheme) DarkBackground else LightBackground

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
                .background(navBgColor),
		) {
			// 1. Splash Screen
			composable(Screen.Splash.route) {
				val loginVm: LoginViewModel =
					viewModel(factory = viewModelFactoryOf { LoginViewModel(db.userDao(), context.applicationContext as Application) })
				SplashScreen(
					awaitReady = {
						app.seedJob.join()
						loginVm.tryAutoLogin()
					},
					onNavigateHome = {
						navController.navigate(Screen.Home.route) {
							popUpTo(Screen.Splash.route) { inclusive = true }
						}
					},
					onNavigateLogin = {
						navController.navigate(Screen.Login.route) {
							popUpTo(Screen.Splash.route) { inclusive = true }
						}
					},
				)
			}

			// 2. Login Screen
			composable(Screen.Login.route) {
				val loginVm: LoginViewModel =
					viewModel(factory = viewModelFactoryOf { LoginViewModel(db.userDao(), context.applicationContext as Application) })
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
				val homeVm: HomeViewModel = rememberHomeViewModel(db, activeUserId, "home")

				HomeScreen(
					viewModel = homeVm,
					isDarkTheme = settingsUiState.darkTheme,
					onPlayClick = { mode -> navController.navigateToGame(mode) },
					onNavigateSettings = { navController.navigateSingleTop(Screen.Settings.route) },
					onNavigateMenu = { navController.navigateSingleTop(Screen.Menu.route) },
					onNavigateGame = { navController.navigateToGame(GameMode.WILDCARD) },
					onNavigateProfile = { navController.navigateSingleTop(Screen.Profile.route) },
				)
			}

			// 4. Game Screen
			composable(Screen.Game.route) { backStackEntry ->
				val modeString =
					backStackEntry.arguments?.getString("mode") ?: GameMode.TRADITIONAL.name
				val mode = GameMode.valueOf(modeString)
				val homeVm: HomeViewModel = rememberHomeViewModel(db, activeUserId, "game_header")

				val homeUiState by homeVm.uiState.collectAsState()

				LaunchedEffect(activeUserId) {
					homeVm.refreshProfilePicture()
				}

				GameScreen(
					userId = activeUserId,
					mode = mode,
					displayName = homeUiState.displayName,
					chipsOwned = homeUiState.chipsOwned,
					profilePictureBytes = homeUiState.profilePictureBytes,
					isDarkTheme = settingsUiState.darkTheme,
					settingsUiState = settingsUiState,
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
					onNavigateProfile = { navController.navigateSingleTop(Screen.Profile.route) },
				)
			}

			// 5. Menu Screen
			composable(Screen.Menu.route) {
				val menuVm: MenuViewModel =
					viewModel(factory = viewModelFactoryOf { MenuViewModel(context.applicationContext as Application) })
				val menuUiState by menuVm.uiState.collectAsState()
				val homeVm: HomeViewModel = rememberHomeViewModel(db, activeUserId, "menu")
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
					onNavigateHome = { navController.navigateSingleTop(Screen.Home.route) },
					onNavigateGame = { navController.navigateToGame(GameMode.WILDCARD) },
					onNavigateToProfile = { navController.navigateSingleTop(Screen.Profile.route) },
					onNavigateToSettings = { navController.navigateSingleTop(Screen.Settings.route) },
				)
			}

			// 6. Settings Screen
			composable(Screen.Settings.route) {
				SettingsScreen(
					uiState = settingsUiState,
					onToggleDarkTheme = settingsViewModel::toggleDarkTheme,
					onToggleShowCardTotal = settingsViewModel::toggleShowCardTotal,
					onToggleDealerRuleBanner = settingsViewModel::toggleDealerRuleBanner,
					onToggleSoundEffects = settingsViewModel::toggleSoundEffects,
					onToggleHapticVibration = settingsViewModel::toggleHapticVibration,
					onToggleFastDeal = settingsViewModel::toggleFastDeal,
					onToggleAutoStandOn21 = settingsViewModel::toggleAutoStandOn21,
					onNavigateHome = { navController.navigateSingleTop(Screen.Home.route) },
					onNavigateGame = { navController.navigateToGame(GameMode.WILDCARD) },
					onNavigateMenu = { navController.navigateSingleTop(Screen.Menu.route) },
				)
			}

			// 7. Profile Screen
			composable(Screen.Profile.route) {
				val resolvedUserId by
				produceState(initialValue = activeUserId, key1 = activeUserId) {
					value = activeUserId.takeIf { it.isNotBlank() }
						?: db.userDao().observeAll().first().firstOrNull()?.userId
						?: PlayerName.LEGACY_DEFAULT_USER_ID
				}
				if (resolvedUserId.isNotBlank()) {
					val profileVm: ProfileViewModel =
						viewModel(
							factory =
								viewModelFactoryOf { ProfileViewModel(
									db.userDao(),
									db.gameHistoryDao(),
									resolvedUserId
								) },
							key = "profile_$resolvedUserId",
						)
					ProfileScreen(
						viewModel = profileVm,
						isDarkTheme = settingsUiState.darkTheme,
						onNavigateHome = { navController.navigateSingleTop(Screen.Home.route) },
						onNavigateGame = { navController.navigateToGame(GameMode.WILDCARD) },
						onNavigateMenu = { navController.navigateSingleTop(Screen.Menu.route) },
					)
				}
			}
		}
	}
}

private fun NavHostController.navigateSingleTop(route: String) =
	navigate(route) { launchSingleTop = true }

private fun NavHostController.navigateToGame(mode: GameMode) =
	navigateSingleTop(Screen.Game.createRoute(mode.name))

@Composable
private fun rememberHomeViewModel(db: AppDatabase, userId: String, keyPrefix: String): HomeViewModel =
	viewModel(
		factory = viewModelFactoryOf { HomeViewModel(db.userDao(), db.gameHistoryDao(), userId) },
		key = "${keyPrefix}_$userId",
	)
