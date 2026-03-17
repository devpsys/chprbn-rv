package ng.com.chprbn.mobile.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ng.com.chprbn.mobile.feature.auth.presentation.login.LoginScreen
import ng.com.chprbn.mobile.feature.auth.presentation.splash.SplashScreen
import ng.com.chprbn.mobile.feature.dashboard.presentation.DashboardScreen
import ng.com.chprbn.mobile.feature.profile.presentation.ProfileScreen
import ng.com.chprbn.mobile.feature.sync.presentation.SyncScreen
import ng.com.chprbn.mobile.feature.sync.presentation.SyncHistoryScreen
import ng.com.chprbn.mobile.feature.verified.presentation.VerifiedListScreen

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
                onVerifiedList = {
                    if (navController.currentDestination?.route != Routes.Verified) {
                        navController.navigate(Routes.Verified)
                    }
                },
                onSync = {
                    if (navController.currentDestination?.route != Routes.Sync) {
                        navController.navigate(Routes.Sync)
                    }
                },
                onProfile = {
                    if (navController.currentDestination?.route != Routes.Profile) {
                        navController.navigate(Routes.Profile)
                    }
                },
                onHome = { /* already on home */ },
                onSearch = {
                    if (navController.currentDestination?.route != Routes.Verified) {
                        navController.navigate(Routes.Verified)
                    }
                },
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
                onVerified = {
                    if (navController.currentDestination?.route != Routes.Verified) {
                        navController.navigate(Routes.Verified)
                    }
                },
                onScanQr = { /* TODO: navigate to QR scanner */ },
                onSync = {
                    if (navController.currentDestination?.route != Routes.Sync) {
                        navController.navigate(Routes.Sync)
                    }
                },
                onProfile = { /* already on profile */ }
            )
        }
        composable(Routes.Sync) {
            SyncScreen(
                onBack = { navController.popBackStack() },
                onRefresh = { /* TODO: trigger refresh */ },
                onSyncAll = { /* TODO: sync all */ },
                onRetryFailed = { /* TODO: retry failed */ },
                onViewAllHistory = {
                    if (navController.currentDestination?.route != Routes.SyncHistory) {
                        navController.navigate(Routes.SyncHistory)
                    }
                },
                onHome = {
                    if (navController.currentDestination?.route != Routes.Dashboard) {
                        navController.navigate(Routes.Dashboard) {
                            popUpTo(Routes.Dashboard) { inclusive = false }
                        }
                    }
                },
                onVerified = {
                    if (navController.currentDestination?.route != Routes.Verified) {
                        navController.navigate(Routes.Verified)
                    }
                },
                onScanQr = { /* TODO: navigate to QR scanner */ },
                onProfile = {
                    if (navController.currentDestination?.route != Routes.Profile) {
                        navController.navigate(Routes.Profile)
                    }
                }
            )
        }
        composable(Routes.SyncHistory) {
            SyncHistoryScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { /* TODO: navigate to record detail */ },
                onHome = {
                    if (navController.currentDestination?.route != Routes.Dashboard) {
                        navController.navigate(Routes.Dashboard) {
                            popUpTo(Routes.Dashboard) { inclusive = false }
                        }
                    }
                },
                onVerified = {
                    if (navController.currentDestination?.route != Routes.Verified) {
                        navController.navigate(Routes.Verified)
                    }
                },
                onScanQr = { /* TODO: navigate to QR scanner */ },
                onProfile = {
                    if (navController.currentDestination?.route != Routes.Profile) {
                        navController.navigate(Routes.Profile)
                    }
                }
            )
        }
        composable(Routes.Verified) {
            VerifiedListScreen(
                onBack = { navController.popBackStack() },
                onMenu = { /* TODO: overflow menu */ },
                onPractitionerClicked = { /* TODO: navigate to practitioner detail */ },
                onHome = {
                    if (navController.currentDestination?.route != Routes.Dashboard) {
                        navController.navigate(Routes.Dashboard) {
                            popUpTo(Routes.Dashboard) { inclusive = false }
                        }
                    }
                },
                onScanQr = { /* TODO: navigate to QR scanner */ },
                onSync = {
                    if (navController.currentDestination?.route != Routes.Sync) {
                        navController.navigate(Routes.Sync)
                    }
                },
                onProfile = {
                    if (navController.currentDestination?.route != Routes.Profile) {
                        navController.navigate(Routes.Profile)
                    }
                }
            )
        }
    }
}

