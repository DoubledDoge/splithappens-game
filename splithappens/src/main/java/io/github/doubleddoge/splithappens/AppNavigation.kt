import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.doubleddoge.splithappens.HomeScreen
import io.github.doubleddoge.splithappens.HomeViewModel

@Composable
fun AppNavigation(
    homeViewModel: HomeViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            // SplashScreen implementation or placeholder
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,

                //onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Profile.route) {
            // Once feature/profile-screen merges, ProfileScreen(viewModel = viewModel()) will plug in here cleanly
        }

        composable(Screen.Settings.route) {
            // Once feature/settings-screen merges, SettingsScreen() plugs in here
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
}