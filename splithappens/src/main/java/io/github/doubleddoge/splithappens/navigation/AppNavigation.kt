package io.github.doubleddoge.splithappens.navigation

import android.net.http.SslCertificate.restoreState
import android.net.http.SslCertificate.saveState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.savedState
import io.github.doubleddoge.splithappens.HomeScreen
import io.github.doubleddoge.splithappens.HomeViewModel
import io.github.doubleddoge.splithappens.HomeViewModelFactory
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
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }


        composable(Screen.Home.route) {
            val context = LocalContext.current
           val db = remember(context){ buildSplitHappensDatabase(context) }
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
                }
            )
        }
        composable(Screen.Profile.route) { Text("Profile Screen Placeholder") }

    }
}