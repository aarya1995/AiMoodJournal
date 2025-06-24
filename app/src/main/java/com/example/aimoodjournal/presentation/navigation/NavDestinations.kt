package com.example.aimoodjournal.presentation.navigation

sealed class NavDestinations(val route: String) {
    // Main Flow
    data object JournalHome : NavDestinations("journal_home")

    // NUX Flow
    data object Welcome : NavDestinations("welcome")
    data object UserDetails : NavDestinations("user_details")
    data object UserBiometrics : NavDestinations("user_biometrics")
    data object Disclaimer : NavDestinations("disclaimer")
    data object Setup : NavDestinations("setup")
    
    companion object {
        val startDestination = JournalHome.route
    }
} 