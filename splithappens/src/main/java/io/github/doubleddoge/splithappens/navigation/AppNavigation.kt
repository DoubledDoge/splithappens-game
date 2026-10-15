package io.github.doubleddoge.splithappens.navigation

import androidx.compose.runtime.Composable
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
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
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
                onPlayClick = {
                    navController.navigate(Screen.Game.route)
                },
                onNavigateSettings = {
                    navController.navigate(Screen.Settings.route){
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


    }
}