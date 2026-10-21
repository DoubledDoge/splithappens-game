package io.github.doubleddoge.splithappens.navigation

sealed class Screen(val route: String) {
  object Splash : Screen("splash")

  data object Login : Screen("login")

  object Home : Screen("home")

  object Profile : Screen("profile")

  object Game : Screen("game")

  object Settings : Screen("settings")

  object Menu : Screen("menu")
}
