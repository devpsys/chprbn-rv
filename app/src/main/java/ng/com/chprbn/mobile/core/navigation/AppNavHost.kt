package ng.com.chprbn.mobile.core.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import ng.com.chprbn.mobile.feature.auth.presentation.login.LoginScreen
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.auth.presentation.splash.SplashScreen
import ng.com.chprbn.mobile.feature.verification.presentation.VerificationScreen
import ng.com.chprbn.mobile.feature.profile.presentation.ProfileScreen
import ng.com.chprbn.mobile.feature.verification.presentation.ManualEntryScreen
import ng.com.chprbn.mobile.feature.scan.presentation.QrScanScreen
import ng.com.chprbn.mobile.feature.verification.presentation.RecordDetailScreen
import ng.com.chprbn.mobile.feature.verification.presentation.SyncScreen
import ng.com.chprbn.mobile.feature.verification.presentation.SyncHistoryScreen
import ng.com.chprbn.mobile.feature.verification.domain.model.IrregularityReportPrefill
import ng.com.chprbn.mobile.feature.verification.presentation.ReportIrregularityScreen
import ng.com.chprbn.mobile.feature.verification.presentation.VerifiedListScreen
import ng.com.chprbn.mobile.feature.verification.presentation.VerificationFormScreen
import ng.com.chprbn.mobile.feature.verification.domain.extractRegistrationFromQrPayload
import ng.com.chprbn.mobile.feature.dashboard.presentation.UnifiedDashboardScreen
import ng.com.chprbn.mobile.feature.exam.presentation.ExamDashboardScreen
import ng.com.chprbn.mobile.feature.exam.presentation.ExamStatisticsScreen

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
                },
                onNavigateToDashboard = {
                    navController.navigate(Routes.Dashboard) {
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
            UnifiedDashboardScreen(
                onNavigateToVerification = {
                    if (navController.currentDestination?.route != Routes.Verification) {
                        navController.navigate(Routes.Verification)
                    }
                },
                onNavigateToVerifiedList = {
                    if (navController.currentDestination?.route != Routes.Verified) {
                        navController.navigate(Routes.Verified)
                    }
                },
                onNavigateToSync = {
                    if (navController.currentDestination?.route != Routes.Sync) {
                        navController.navigate(Routes.Sync)
                    }
                },
                onNavigateToProfile = {
                    if (navController.currentDestination?.route != Routes.Profile) {
                        navController.navigate(Routes.Profile)
                    }
                },
                onNavigateToExamAttendance = {
                    if (navController.currentDestination?.route != Routes.ExamDashboard) {
                        navController.navigate(Routes.ExamDashboard)
                    }
                },
                onNavigateToPracticalAssessment = { /* TODO: Practical Assessment feature */ },
                onNavigateToAccreditation = { /* TODO: Accreditation feature */ },
                onViewRecentLogs = {
                    if (navController.currentDestination?.route != Routes.SyncHistory) {
                        navController.navigate(Routes.SyncHistory)
                    }
                }
            )
        }
        composable(Routes.ExamDashboard) {
            ExamDashboardScreen(
                onNotifications = { /* TODO: exam notifications */ },
                onLogAttendance = { /* TODO: attendance */ },
                onAttendanceMore = { /* TODO */ },
                onGradePractical = { /* TODO: practical */ },
                onPracticalInfo = { /* TODO */ },
                onDownloadDossier = { /* TODO: export dossier */ },
                onExamDashboardTab = {},
                onStatisticsTab = {
                    if (navController.currentDestination?.route != Routes.ExamStatistics) {
                        navController.navigate(Routes.ExamStatistics)
                    }
                }
            )
        }
        composable(Routes.ExamStatistics) {
            ExamStatisticsScreen(
                onBack = { navController.popBackStack() },
                onRefresh = { /* TODO: refresh statistics */ },
                onSyncNow = { /* TODO: sync */ },
                onClearCached = { /* TODO: clear cache */ },
                onExamDashboardTab = {
                    navController.popBackStack(Routes.ExamDashboard, inclusive = false)
                },
                onStatisticsTab = { /* already on statistics */ }
            )
        }
        composable(Routes.Verification) {
            VerificationScreen(
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
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onHome = {
                    if (navController.currentDestination?.route != Routes.Verification) {
                        navController.navigate(Routes.Verification) {
                            popUpTo(Routes.Verification) { inclusive = false }
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
                onViewAllHistory = {
                    if (navController.currentDestination?.route != Routes.SyncHistory) {
                        navController.navigate(Routes.SyncHistory)
                    }
                },
                onHome = {
                    if (navController.currentDestination?.route != Routes.Verification) {
                        navController.navigate(Routes.Verification) {
                            popUpTo(Routes.Verification) { inclusive = false }
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
                    if (navController.currentDestination?.route != Routes.Verification) {
                        navController.navigate(Routes.Verification) {
                            popUpTo(Routes.Verification) { inclusive = false }
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
        composable(Routes.Verified) { backStackEntry ->
            val refreshRequested =
                backStackEntry.savedStateHandle.get<Boolean>("verified_list_refresh") == true
            VerifiedListScreen(
                refreshRequested = refreshRequested,
                onRefreshConsumed = {
                    backStackEntry.savedStateHandle["verified_list_refresh"] = false
                },
                onBack = { navController.popBackStack() },
                onMenu = { /* TODO: overflow menu */ },
                onPractitionerClicked = { practitioner ->
//                    navController.navigate(
//                        Routes.verificationFormRoute(
//                            practitioner.name,
//                            practitioner.license
//                        )
//                    )
                },
                onHome = {
                    if (navController.currentDestination?.route != Routes.Verification) {
                        navController.navigate(Routes.Verification) {
                            popUpTo(Routes.Verification) { inclusive = false }
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
                navArgument("licenseRecordJson") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val encodedJson = backStackEntry.arguments?.getString("licenseRecordJson").orEmpty()
            val licenseRecord = runCatching {
                val json = Uri.decode(encodedJson)
                if (json.isBlank()) null else Gson().fromJson(json, LicenseRecord::class.java)
            }.getOrNull()
            VerificationFormScreen(
                licenseRecord = licenseRecord,
                onBack = { navController.popBackStack() },
                onSaveVerification = {
                    runCatching {
                        navController.getBackStackEntry(Routes.Verified)
                            .savedStateHandle["verified_list_refresh"] = true
                    }
                    val poppedBoth = navController.popBackStack(Routes.Scan, inclusive = true)
                    if (!poppedBoth) {
                        navController.popBackStack()
                    }
                },
                onReportIrregularity = {
                    val rec = licenseRecord
                    val prefill = IrregularityReportPrefill(
                        nameOnCard = rec?.fullName.orEmpty(),
                        licenseNumber = rec?.registrationNumber.orEmpty(),
                        cadre = rec?.profession.orEmpty(),
                        gender = rec?.gender.orEmpty()
                    )
                    val encoded = Uri.encode(Gson().toJson(prefill))
                    navController.navigate(Routes.reportIrregularityRoute(encoded))
                }
            )
        }
        composable(
            route = Routes.ReportIrregularity,
            arguments = listOf(
                navArgument("prefillJson") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            ReportIrregularityScreen(
                onBack = { navController.popBackStack() },
                onSubmitted = { navController.popBackStack() }
            )
        }
        composable(Routes.ManualLicenseEntry) {
            ManualEntryScreen(
                onBack = {
                    // Pop QrScanScreen as well so we return to the screen before scan
                    navController.popBackStack(Routes.Scan, inclusive = true)
                },
                onVerifyLicense = { enteredLicense ->
                    navController.navigate(Routes.recordDetailRoute(enteredLicense))
                }
            )
        }
        composable(Routes.Scan) {
            QrScanScreen(
                onManualEntry = {
                    if (navController.currentDestination?.route != Routes.ManualLicenseEntry) {
                        navController.navigate(Routes.ManualLicenseEntry)
                    }
                },
                qrValidator = { extractRegistrationFromQrPayload(it) },
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
                onProceedToVerification = { record ->
                    val json = Gson().toJson(record)
                    val encoded = Uri.encode(json)
                    navController.navigate(Routes.verificationFormRoute(encoded))
                },
                onReportIrregularity = { record ->
                    val prefill = IrregularityReportPrefill(
                        nameOnCard = record.fullName,
                        licenseNumber = record.registrationNumber,
                        cadre = record.profession,
                        gender = record.gender
                    )
                    val encoded = Uri.encode(Gson().toJson(prefill))
                    navController.navigate(Routes.reportIrregularityRoute(encoded))
                },
                onManualEntry = {
                    if (navController.currentDestination?.route != Routes.ManualLicenseEntry) {
                        navController.navigate(Routes.ManualLicenseEntry)
                    }
                }
            )
        }
    }
}

