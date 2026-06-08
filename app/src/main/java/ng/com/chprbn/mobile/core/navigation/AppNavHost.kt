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
import ng.com.chprbn.mobile.feature.assessment.presentation.AssessmentCandidatesScreen
import ng.com.chprbn.mobile.feature.assessment.presentation.AssessmentPaperDetailScreen
import ng.com.chprbn.mobile.feature.assessment.presentation.AssessmentPracticalScoringScreen
import ng.com.chprbn.mobile.feature.assessment.presentation.AssessmentPracticalSectionsScreen
import ng.com.chprbn.mobile.feature.assessment.presentation.AssessmentProjectAssessmentScreen
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
                onExamDashboardTab = {},
                onStatisticsTab = {
                    navController.navigate(Routes.ExamStatistics)
                }
            )
        }
        composable<Routes.ExamPapers> {
            ExamPapersScreen(
                onBack = { navController.popBackStack() },
                onOpenPaper = { paperId ->
                    navController.navigate(Routes.ExamPaper(paperId))
                },
            )
        }
        composable<Routes.ExamPaper> {
            ExamPaperScreen(
                onBack = { navController.popBackStack() },
                onViewCandidates = {
                    navController.navigate(Routes.ExamCandidates)
                },
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
                onExamDashboardTab = {
                    navController.popBackStack<Routes.ExamDashboard>(inclusive = false)
                },
                onStatisticsTab = { /* already on statistics */ }
            )
        }
        composable<Routes.Verification> {
            VerificationScreen(
                onBack = { navController.popBackStack() },
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
                // Anything other than Verification uses the exam/indexing
                // copy variant; the strings are shared between exam
                // attendance and assessment indexing.
                forExamIndexing = args.source != ScanSource.Verification,
                onBack = {
                    // Pop the originating scan screen (if it's on the
                    // back stack) so back doesn't bounce through the
                    // camera screen. Falls back to a plain pop when
                    // manual entry was opened directly (e.g. from
                    // RecordDetail to re-enter a license).
                    val popped = when (args.source) {
                        ScanSource.Verification ->
                            navController.popBackStack<Routes.Scan>(inclusive = true)
                        ScanSource.ExamAttendance ->
                            navController.popBackStack<Routes.ExamScan>(inclusive = true)
                        ScanSource.AssessmentScoring ->
                            navController.popBackStack<Routes.AssessmentScan>(inclusive = true)
                    }
                    if (!popped) navController.popBackStack()
                },
                onVerifyLicense = { enteredLicense ->
                    // Mirror the QR scan flow: the destination is chosen
                    // by source so manual entry behaves the same as a
                    // successful camera scan.
                    when (args.source) {
                        ScanSource.Verification ->
                            navController.navigate(Routes.RecordDetail(enteredLicense))
                        ScanSource.ExamAttendance ->
                            navController.navigate(Routes.CandidateScanResult(enteredLicense))
                        ScanSource.AssessmentScoring ->
                            navController.navigate(
                                Routes.AssessmentPracticalSections(
                                    scheduleId = args.scheduleId.orEmpty(),
                                    candidateId = enteredLicense,
                                ),
                            ) {
                                popUpTo<Routes.ManualLicenseEntry> { inclusive = true }
                            }
                    }
                },
            )
        }
        composable<Routes.Scan> {
            QrScanScreen(
                onManualEntry = {
                    navController.navigate(
                        Routes.ManualLicenseEntry(source = ScanSource.Verification),
                    )
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
                    navController.navigate(
                        Routes.ManualLicenseEntry(source = ScanSource.ExamAttendance),
                    )
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
                    navController.navigate(
                        Routes.ManualLicenseEntry(source = ScanSource.Verification),
                    )
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
        composable<Routes.AssessmentPaperDetail> { backStackEntry ->
            val args: Routes.AssessmentPaperDetail = backStackEntry.toRoute()
            AssessmentPaperDetailScreen(
                onBack = { navController.popBackStack() },
                onShare = { /* TODO: assessment share */ },
                onCandidateClick = { /* TODO: candidate detail when that screen lands */ },
                onViewFullDirectory = {
                    navController.navigate(Routes.AssessmentCandidates(args.scheduleId))
                },
                onScanQr = {
                    // Assessment-side scan flow lands on the Practical
                    // Sections hub for the scanned candidate.
                    navController.navigate(Routes.AssessmentScan(args.scheduleId))
                },
            )
        }
        composable<Routes.AssessmentCandidates> {
            AssessmentCandidatesScreen(
                onBack = { navController.popBackStack() },
                onCandidateClick = { /* TODO: candidate detail when that screen lands */ },
                onAddRemark = { /* TODO: remark sheet/screen */ },
            )
        }
        composable<Routes.AssessmentScan> { backStackEntry ->
            val args: Routes.AssessmentScan = backStackEntry.toRoute()
            QrScanScreen(
                manualEntryButtonLabel = stringResource(R.string.scan_manual_entry_exam_action),
                onManualEntry = {
                    navController.navigate(
                        Routes.ManualLicenseEntry(
                            source = ScanSource.AssessmentScoring,
                            scheduleId = args.scheduleId,
                        ),
                    )
                },
                qrValidator = { extractRegistrationFromQrPayload(it) },
                onQrScanned = { candidateId ->
                    // Replace the scan destination so back doesn't bounce
                    // through the camera screen.
                    navController.navigate(
                        Routes.AssessmentPracticalSections(
                            scheduleId = args.scheduleId,
                            candidateId = candidateId,
                        ),
                    ) {
                        popUpTo<Routes.AssessmentScan> { inclusive = true }
                    }
                },
            )
        }
        composable<Routes.AssessmentPracticalSections> { backStackEntry ->
            val args: Routes.AssessmentPracticalSections = backStackEntry.toRoute()
            AssessmentPracticalSectionsScreen(
                onBack = { navController.popBackStack() },
                onSectionClick = { section ->
                    navController.navigate(
                        Routes.AssessmentPracticalScoring(
                            scheduleId = args.scheduleId,
                            candidateId = args.candidateId,
                            sectionId = section.id,
                        ),
                    )
                },
                onAssessProject = {
                    navController.navigate(
                        Routes.AssessmentProjectAssessment(
                            scheduleId = args.scheduleId,
                            candidateId = args.candidateId,
                        ),
                    )
                },
            )
        }
        composable<Routes.AssessmentPracticalScoring> {
            AssessmentPracticalScoringScreen(
                onBack = { navController.popBackStack() },
                onInfoClick = { /* TODO: section info sheet */ },
                onSaveScores = { /* TODO: persist + return to hub */ },
            )
        }
        composable<Routes.AssessmentProjectAssessment> {
            AssessmentProjectAssessmentScreen(
                onBack = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
                onSaveScore = { /* TODO: persist + return to hub */ },
            )
        }
    }
}
