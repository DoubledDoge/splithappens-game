package io.github.doubleddoge.splithappens.navigation

sealed class Screen(val route: String){
    //Where your screens must go
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Profile : Screen("profile")

}