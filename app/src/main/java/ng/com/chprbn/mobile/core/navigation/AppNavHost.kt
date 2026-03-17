package ng.com.chprbn.mobile.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ng.com.chprbn.mobile.feature.auth.presentation.login.LoginScreen
import ng.com.chprbn.mobile.feature.auth.presentation.splash.SplashScreen
import ng.com.chprbn.mobile.feature.dashboard.presentation.DashboardScreen
import ng.com.chprbn.mobile.feature.profile.presentation.ProfileScreen

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
                onSignIn = {
                    navController.navigate(Routes.Dashboard) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
                onRecovery = { /* TODO: recovery flow */ },
                onRequestAccess = { /* TODO: request access */ }
            )
        }
        composable(Routes.Dashboard) {
            DashboardScreen(
                onScanQr = { /* TODO: navigate to QR scanner */ },
                onVerifiedList = { /* TODO: navigate to verified list */ },
                onSync = { /* TODO: sync */ },
                onProfile = {
                    if (navController.currentDestination?.route != Routes.Profile) {
                        navController.navigate(Routes.Profile)
                    }
                },
                onHome = { /* already on home */ },
                onSearch = { /* TODO: navigate to verified list from bottom bar */ },
                onSettings = { /* TODO: settings/profile options */ }
            )
        }
        composable(Routes.Profile) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onMenu = { /* TODO: overflow menu */ },
                onEditProfile = { /* TODO: edit profile */ },
                onChangePassword = { /* TODO */ },
                onLogout = { /* TODO */ },
                onHome = {
                    if (navController.currentDestination?.route != Routes.Dashboard) {
                        navController.navigate(Routes.Dashboard) {
                            popUpTo(Routes.Dashboard) { inclusive = false }
                        }
                    }
                },
                onVerified = { /* TODO: navigate to verified list */ },
                onScanQr = { /* TODO: navigate to QR scanner */ },
                onSync = { /* TODO: navigate to sync */ },
                onProfile = { /* already on profile */ }
            )
        }
    }
}

