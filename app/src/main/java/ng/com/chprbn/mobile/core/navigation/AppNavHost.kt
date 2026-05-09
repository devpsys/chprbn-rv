package ng.com.chprbn.mobile.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ng.com.chprbn.mobile.feature.auth.presentation.login.LoginScreen
import ng.com.chprbn.mobile.feature.auth.presentation.splash.SplashScreen
import ng.com.chprbn.mobile.feature.verification.presentation.VerificationScreen
import ng.com.chprbn.mobile.feature.profile.presentation.ProfileScreen
import ng.com.chprbn.mobile.feature.verification.presentation.ManualEntryScreen
import ng.com.chprbn.mobile.feature.scan.presentation.QrScanScreen
import ng.com.chprbn.mobile.feature.verification.presentation.RecordDetailScreen
import ng.com.chprbn.mobile.feature.verification.presentation.SyncScreen
import ng.com.chprbn.mobile.feature.verification.presentation.SyncHistoryScreen
import ng.com.chprbn.mobile.feature.verification.presentation.ReportIrregularityScreen
import ng.com.chprbn.mobile.feature.verification.presentation.VerifiedListScreen
import ng.com.chprbn.mobile.feature.verification.presentation.VerificationFormScreen
import ng.com.chprbn.mobile.feature.verification.domain.extractRegistrationFromQrPayload
import ng.com.chprbn.mobile.feature.dashboard.presentation.UnifiedDashboardScreen
import ng.com.chprbn.mobile.feature.exam.presentation.ExamDashboardScreen
import ng.com.chprbn.mobile.feature.exam.presentation.ExamPapersScreen
import ng.com.chprbn.mobile.feature.exam.presentation.ExamCandidatesScreen
import ng.com.chprbn.mobile.feature.exam.presentation.ExamPaperScreen
import ng.com.chprbn.mobile.feature.exam.presentation.ExamStatisticsScreen
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.feature.assessment.presentation.AssessmentPaperDetailScreen
import ng.com.chprbn.mobile.feature.assessment.presentation.ExaminationSchedulesScreen
import ng.com.chprbn.mobile.feature.exam.presentation.CandidateScanResultScreen

/**
 * Single-activity navigation host. Destinations are declared as @Serializable
 * route types in [Routes]; navigate calls pass the route instance directly,
 * which gives compile-time type safety and removes Gson-encoded nav payloads.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Splash
    ) {
        composable<Routes.Splash> {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.Login) {
                        popUpTo<Routes.Splash> { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Routes.Dashboard) {
                        popUpTo<Routes.Splash> { inclusive = true }
                    }
                }
            )
        }
        composable<Routes.Login> {
            LoginScreen(
                onSignIn = {
                    navController.navigate(Routes.Dashboard) {
                        popUpTo<Routes.Login> { inclusive = true }
                    }
                },
                onRecovery = { /* TODO: recovery flow */ },
                onRequestAccess = { /* TODO: request access */ }
            )
        }
        composable<Routes.Dashboard> {
            UnifiedDashboardScreen(
                onNavigateToVerification = {
                    navController.navigate(Routes.Verification)
                },
                onNavigateToVerifiedList = {
                    navController.navigate(Routes.Verified)
                },
                onNavigateToSync = {
                    navController.navigate(Routes.Sync)
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.Profile)
                },
                onNavigateToExamAttendance = {
                    navController.navigate(Routes.ExamDashboard)
                },
                onNavigateToPracticalAssessment = { /* TODO: Practical Assessment feature */ },
                onNavigateToAccreditation = { /* TODO: Accreditation feature */ },
                onViewRecentLogs = {
                    navController.navigate(Routes.SyncHistory)
                }
            )
        }
        composable<Routes.ExamDashboard> {
            ExamDashboardScreen(
                onNotifications = { /* TODO: exam notifications */ },
                onLogAttendance = {
                    navController.navigate(Routes.ExamPapers)
                },
                onAttendanceMore = { /* TODO */ },
                onGradePractical = {
                    navController.navigate(Routes.ExaminationSchedules)
                },
                onPracticalInfo = { /* TODO */ },
                onDownloadDossier = { /* TODO: export dossier */ },
                onExamDashboardTab = {},
                onStatisticsTab = {
                    navController.navigate(Routes.ExamStatistics)
                }
            )
        }
        composable<Routes.ExamPapers> {
            ExamPapersScreen(
                onBack = { navController.popBackStack() },
                onOpenPaper = {
                    navController.navigate(Routes.ExamPaper)
                },
                onSyncNow = { /* TODO: sync attendance */ }
            )
        }
        composable<Routes.ExamPaper> {
            ExamPaperScreen(
                onBack = { navController.popBackStack() },
                onViewCandidates = {
                    navController.navigate(Routes.ExamCandidates)
                },
                onSyncData = { /* TODO: sync paper data */ },
                onScanQr = {
                    navController.navigate(Routes.ExamScan)
                }
            )
        }
        composable<Routes.ExamCandidates> {
            ExamCandidatesScreen(
                onBack = { navController.popBackStack() },
                onAddRemark = { /* TODO */ },
                onViewProfile = { /* TODO */ }
            )
        }
        composable<Routes.ExamStatistics> {
            ExamStatisticsScreen(
                onBack = { navController.popBackStack() },
                onRefresh = { /* TODO: refresh statistics */ },
                onSyncNow = { /* TODO: sync */ },
                onClearCached = { /* TODO: clear cache */ },
                onExamDashboardTab = {
                    navController.popBackStack<Routes.ExamDashboard>(inclusive = false)
                },
                onStatisticsTab = { /* already on statistics */ }
            )
        }
        composable<Routes.Verification> {
            VerificationScreen(
                onScanQr = { navController.navigate(Routes.Scan) },
                onVerifiedList = { navController.navigate(Routes.Verified) },
                onSync = { navController.navigate(Routes.Sync) },
                onProfile = { navController.navigate(Routes.Profile) },
                onHome = { /* already on home */ },
                onSearch = { navController.navigate(Routes.Verified) },
                onSettings = { /* TODO: settings/profile options */ }
            )
        }
        composable<Routes.Profile> {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onMenu = { /* TODO: overflow menu */ },
                onEditProfile = { /* TODO: edit profile */ },
                onChangePassword = { /* TODO */ },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo<Routes.Splash> { inclusive = true }
                    }
                },
                onHome = {
                    navController.navigate(Routes.Verification) {
                        popUpTo<Routes.Verification> { inclusive = false }
                    }
                },
                onVerified = { navController.navigate(Routes.Verified) },
                onScanQr = { navController.navigate(Routes.Scan) },
                onSync = { navController.navigate(Routes.Sync) },
                onProfile = { /* already on profile */ }
            )
        }
        composable<Routes.Sync> {
            SyncScreen(
                onBack = { navController.popBackStack() },
                onViewAllHistory = { navController.navigate(Routes.SyncHistory) },
                onHome = {
                    navController.navigate(Routes.Verification) {
                        popUpTo<Routes.Verification> { inclusive = false }
                    }
                },
                onVerified = { navController.navigate(Routes.Verified) },
                onScanQr = { navController.navigate(Routes.Scan) },
                onProfile = { navController.navigate(Routes.Profile) }
            )
        }
        composable<Routes.SyncHistory> {
            SyncHistoryScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { /* TODO: navigate to record detail */ },
                onHome = {
                    navController.navigate(Routes.Verification) {
                        popUpTo<Routes.Verification> { inclusive = false }
                    }
                },
                onVerified = { navController.navigate(Routes.Verified) },
                onScanQr = { navController.navigate(Routes.Scan) },
                onProfile = { navController.navigate(Routes.Profile) }
            )
        }
        composable<Routes.Verified> { backStackEntry ->
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
                    // TODO: navigate to verification form with practitioner.license once
                    // the route from this list re-uses the cached record.
                },
                onHome = {
                    navController.navigate(Routes.Verification) {
                        popUpTo<Routes.Verification> { inclusive = false }
                    }
                },
                onScanQr = { navController.navigate(Routes.Scan) },
                onSync = { navController.navigate(Routes.Sync) },
                onProfile = { navController.navigate(Routes.Profile) }
            )
        }
        composable<Routes.VerificationForm> { backStackEntry ->
            val args: Routes.VerificationForm = backStackEntry.toRoute()
            VerificationFormScreen(
                onBack = { navController.popBackStack() },
                onSaveVerification = {
                    runCatching {
                        navController.getBackStackEntry(Routes.Verified)
                            .savedStateHandle["verified_list_refresh"] = true
                    }
                    val poppedBoth = navController.popBackStack<Routes.Scan>(inclusive = true)
                    if (!poppedBoth) {
                        navController.popBackStack()
                    }
                },
                onReportIrregularity = {
                    navController.navigate(Routes.ReportIrregularity(args.registrationNumber))
                }
            )
        }
        composable<Routes.ReportIrregularity> {
            ReportIrregularityScreen(
                onBack = { navController.popBackStack() },
                onSubmitted = { navController.popBackStack() }
            )
        }
        composable<Routes.ManualLicenseEntry> { backStackEntry ->
            val args: Routes.ManualLicenseEntry = backStackEntry.toRoute()
            ManualEntryScreen(
                forExamIndexing = args.forExam,
                onBack = {
                    if (!navController.popBackStack<Routes.ExamScan>(inclusive = true)) {
                        navController.popBackStack<Routes.Scan>(inclusive = true)
                    }
                },
                onVerifyLicense = { enteredLicense ->
                    navController.navigate(Routes.RecordDetail(enteredLicense))
                },
            )
        }
        composable<Routes.Scan> {
            QrScanScreen(
                onManualEntry = {
                    navController.navigate(Routes.ManualLicenseEntry(forExam = false))
                },
                qrValidator = { extractRegistrationFromQrPayload(it) },
                onQrScanned = { registrationNumber ->
                    navController.navigate(Routes.RecordDetail(registrationNumber))
                }
            )
        }
        composable<Routes.ExamScan> {
            QrScanScreen(
                manualEntryButtonLabel = stringResource(R.string.scan_manual_entry_exam_action),
                onManualEntry = {
                    navController.navigate(Routes.ManualLicenseEntry(forExam = true))
                },
                qrValidator = { extractRegistrationFromQrPayload(it) },
                onQrScanned = { registrationNumber ->
                    navController.navigate(Routes.CandidateScanResult(registrationNumber))
                }
            )
        }
        composable<Routes.CandidateScanResult> {
            CandidateScanResultScreen(
                onBack = {
                    navController.popBackStack()
                    navController.popBackStack()
                },
                onMarkAttendance = {
                    navController.popBackStack()
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                    navController.popBackStack()
                }
            )
        }
        composable<Routes.RecordDetail> { backStackEntry ->
            val args: Routes.RecordDetail = backStackEntry.toRoute()
            RecordDetailScreen(
                registrationNumber = args.registrationNumber,
                onBack = {
                    if (!navController.popBackStack<Routes.ExamScan>(inclusive = true)) {
                        navController.popBackStack<Routes.Scan>(inclusive = true)
                    }
                },
                onMenu = { /* TODO: overflow menu */ },
                onProceedToVerification = { record ->
                    navController.navigate(Routes.VerificationForm(record.registrationNumber))
                },
                onReportIrregularity = { record ->
                    navController.navigate(Routes.ReportIrregularity(record.registrationNumber))
                },
                onManualEntry = {
                    navController.navigate(Routes.ManualLicenseEntry(forExam = false))
                }
            )
        }
        composable<Routes.ExaminationSchedules> {
            ExaminationSchedulesScreen(
                onBack = { navController.popBackStack() },
                onScheduleClick = { schedule ->
                    navController.navigate(Routes.AssessmentPaperDetail(schedule.id))
                },
            )
        }
        composable<Routes.AssessmentPaperDetail> {
            AssessmentPaperDetailScreen(
                onBack = { navController.popBackStack() },
                onShare = { /* TODO: assessment share */ },
                onMore = { /* TODO: assessment paper-detail overflow */ },
                onCandidateClick = { /* TODO: candidate detail when that screen lands */ },
                onViewFullDirectory = { /* TODO: full directory screen */ },
                onScanQr = {
                    // Reuse the existing ExamScan flow (lands on
                    // CandidateScanResult) until the assessment feature
                    // grows its own scan destination.
                    navController.navigate(Routes.ExamScan)
                },
            )
        }
    }
}
