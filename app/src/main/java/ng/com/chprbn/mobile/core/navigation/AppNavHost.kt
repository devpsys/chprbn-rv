package ng.com.chprbn.mobile.core.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ng.com.chprbn.mobile.feature.auth.presentation.login.LoginScreen
import ng.com.chprbn.mobile.feature.auth.presentation.splash.SplashScreen
import ng.com.chprbn.mobile.feature.dashboard.presentation.DashboardScreen
import ng.com.chprbn.mobile.feature.profile.presentation.ProfileScreen
import ng.com.chprbn.mobile.feature.scan.presentation.QrScanScreen
import ng.com.chprbn.mobile.feature.scan.presentation.RecordDetailScreen
import ng.com.chprbn.mobile.feature.sync.presentation.SyncScreen
import ng.com.chprbn.mobile.feature.sync.presentation.SyncHistoryScreen
import ng.com.chprbn.mobile.feature.verified.presentation.VerifiedListScreen
import ng.com.chprbn.mobile.feature.verified.presentation.VerificationFormScreen

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
                onScanQr = {
                    if (navController.currentDestination?.route != Routes.Scan) {
                        navController.navigate(Routes.Scan)
                    }
                },
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
                onScanQr = {
                    if (navController.currentDestination?.route != Routes.Scan) {
                        navController.navigate(Routes.Scan)
                    }
                },
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
                onScanQr = {
                    if (navController.currentDestination?.route != Routes.Scan) {
                        navController.navigate(Routes.Scan)
                    }
                },
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
                onScanQr = {
                    if (navController.currentDestination?.route != Routes.Scan) {
                        navController.navigate(Routes.Scan)
                    }
                },
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
                onPractitionerClicked = { practitioner ->
                    navController.navigate(Routes.verificationFormRoute(practitioner.name, practitioner.license))
                },
                onHome = {
                    if (navController.currentDestination?.route != Routes.Dashboard) {
                        navController.navigate(Routes.Dashboard) {
                            popUpTo(Routes.Dashboard) { inclusive = false }
                        }
                    }
                },
                onScanQr = {
                    if (navController.currentDestination?.route != Routes.Scan) {
                        navController.navigate(Routes.Scan)
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
                }
            )
        }
        composable(
            route = Routes.VerificationForm,
            arguments = listOf(
                navArgument("practitionerName") { type = NavType.StringType; defaultValue = "" },
                navArgument("licenseNumber") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val practitionerName = Uri.decode(backStackEntry.arguments?.getString("practitionerName").orEmpty())
            val licenseNumber = Uri.decode(backStackEntry.arguments?.getString("licenseNumber").orEmpty())
            VerificationFormScreen(
                practitionerName = practitionerName.ifEmpty { "Dr. Sarah Elizabeth Jenkins" },
                licenseNumber = licenseNumber.ifEmpty { "MED-99284-TX" },
                onBack = { navController.popBackStack() },
                onSaveVerification = { navController.popBackStack() }
            )
        }
        composable(Routes.Scan) {
            QrScanScreen(
                onManualEntry = { /* TODO: navigate to manual entry flow */ },
                onQrScanned = { registrationNumber ->
                    navController.navigate(Routes.recordDetailRoute(registrationNumber))
                }
            )
        }
        composable(
            route = Routes.RecordDetail,
            arguments = listOf(
                navArgument("registrationNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val registrationNumber = Uri.decode(
                backStackEntry.arguments?.getString("registrationNumber").orEmpty()
            )
            RecordDetailScreen(
                registrationNumber = registrationNumber,
                onBack = {
                    navController.popBackStack(Routes.Scan, inclusive = true)
                },
                onMenu = { /* TODO: overflow menu */ },
                onProceedToVerification = {
                    navController.navigate(Routes.verificationFormRoute("Dr. Jane Doe", registrationNumber))
                }
            )
        }
    }
}

