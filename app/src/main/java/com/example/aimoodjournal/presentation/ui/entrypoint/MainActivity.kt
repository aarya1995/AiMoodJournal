package com.example.aimoodjournal.presentation.ui.entrypoint

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aimoodjournal.presentation.navigation.NavDestinations
import com.example.aimoodjournal.presentation.ui.theme.AiMoodJournalTheme
import com.example.aimoodjournal.presentation.ui.journal_home.JournalHomeScreen
import com.example.aimoodjournal.presentation.ui.journal_history.JournalHistoryScreen
import com.example.aimoodjournal.presentation.ui.nux.disclaimer.DisclaimerScreen
import com.example.aimoodjournal.presentation.ui.nux.biometrics.UserBiometricsScreen
import com.example.aimoodjournal.presentation.ui.nux.setup.SetupScreen
import com.example.aimoodjournal.presentation.ui.nux.user_details.UserDetailsScreen
import com.example.aimoodjournal.presentation.ui.nux.welcome.WelcomeScreen
import com.example.aimoodjournal.presentation.ui.shared.LoadingDots
import com.example.aimoodjournal.presentation.ui.shared.nuxEnterTransition
import com.example.aimoodjournal.presentation.ui.shared.nuxExitTransition
import com.example.aimoodjournal.presentation.ui.shared.nuxPopEnterTransition
import com.example.aimoodjournal.presentation.ui.shared.nuxPopExitTransition
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiMoodJournalTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = hiltViewModel()
) {
    var isInitialLoad by remember { mutableStateOf(true) }
    val state by viewModel.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NavDestinations.startDestination,
        modifier = modifier
    ) {
        // Main Flow
        composable(NavDestinations.JournalHome.route) {
            LaunchedEffect(state.hasCompletedNux) {
                // Only handle navigation on initial load and when we have a definitive NUX state
                if (isInitialLoad && state.hasCompletedNux != null) {
                    isInitialLoad = false
                    if (!state.hasCompletedNux!!) {
                        navController.navigate(NavDestinations.Welcome.route) {
                            popUpTo(NavDestinations.JournalHome.route) { inclusive = true }
                        }
                    }
                }
            }

            // Show loading or home screen based on state
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LoadingDots(modifier = Modifier.align(Alignment.Center))
                }
            } else if (state.hasCompletedNux == true) {
                JournalHomeScreen(
                    onNavigateToHistory = {
                        navController.navigate(NavDestinations.JournalHistory.route)
                    }
                )
            }
        }

        composable(NavDestinations.JournalHistory.route) {
            JournalHistoryScreen()
        }

        // NUX Flow
        composable(
            route = NavDestinations.Welcome.route,
            enterTransition = { nuxEnterTransition },
            exitTransition = { nuxExitTransition },
            popEnterTransition = { nuxPopEnterTransition },
            popExitTransition = { nuxPopExitTransition }
        ) {
            WelcomeScreen(
                onNext = {
                    navController.navigate(NavDestinations.UserDetails.route)
                }
            )
        }

        composable(
            route = NavDestinations.UserDetails.route,
            enterTransition = { nuxEnterTransition },
            exitTransition = { nuxExitTransition },
            popEnterTransition = { nuxPopEnterTransition },
            popExitTransition = { nuxPopExitTransition }
        ) {
            UserDetailsScreen(
                onNext = {
                    navController.navigate(NavDestinations.UserBiometrics.route)
                }
            )
        }

        composable(
            route = NavDestinations.UserBiometrics.route,
            enterTransition = { nuxEnterTransition },
            exitTransition = { nuxExitTransition },
            popEnterTransition = { nuxPopEnterTransition },
            popExitTransition = { nuxPopExitTransition }
        ) {
            UserBiometricsScreen(
                onNext = {
                    navController.navigate(NavDestinations.Disclaimer.route)
                }
            )
        }

        composable(
            route = NavDestinations.Disclaimer.route,
            enterTransition = { nuxEnterTransition },
            exitTransition = { nuxExitTransition },
            popEnterTransition = { nuxPopEnterTransition },
            popExitTransition = { nuxPopExitTransition }
        ) {
            DisclaimerScreen(
                onNext = {
                    navController.navigate(NavDestinations.Setup.route)
                }
            )
        }

        composable(
            route = NavDestinations.Setup.route,
            enterTransition = { nuxEnterTransition },
            exitTransition = { nuxExitTransition },
            popEnterTransition = { nuxPopEnterTransition },
            popExitTransition = { nuxPopExitTransition }
        ) {
            SetupScreen(
                onNext = {
                    navController.navigate(NavDestinations.JournalHome.route) {
                        popUpTo(NavDestinations.Welcome.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}