package ng.com.chprbn.mobile.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ng.com.chprbn.mobile.feature.auth.presentation.login.LoginScreen
import ng.com.chprbn.mobile.feature.auth.presentation.splash.SplashScreen

/**
 * Single-activity navigation host.
 * Each feature will contribute its start destination and routes here.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Splash
    ) {
        composable(Routes.Splash) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Login) {
            LoginScreen(
                onSignIn = { /* TODO: navigate to dashboard */ },
                onRecovery = { /* TODO: recovery flow */ },
                onRequestAccess = { /* TODO: request access */ }
            )
        }
    }
}

