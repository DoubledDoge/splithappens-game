package io.github.doubleddoge.splithappens.navigation

sealed class Screen(val route: String) {
  object Splash : Screen("splash")

  data object Login : Screen("login")

  object Home : Screen("home")

  object Profile : Screen("profile")

  object Game : Screen("game/{mode}") {
    fun createRoute(mode: String) = "game/$mode"
  }

  object Settings : Screen("settings")

  object Menu : Screen("menu")
}
