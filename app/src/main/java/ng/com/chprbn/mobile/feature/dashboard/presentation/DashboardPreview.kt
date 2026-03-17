package ng.com.chprbn.mobile.feature.dashboard.presentation

import ng.com.chprbn.mobile.feature.dashboard.domain.model.DashboardFeature
import ng.com.chprbn.mobile.feature.dashboard.domain.model.FeatureType

/**
 * Preview-only data for Compose previews (no Hilt).
 */
val previewDashboardFeatures: List<DashboardFeature> = listOf(
    DashboardFeature(
        FeatureType.ScanQr,
        "Scan License QR",
        "Validate practitioner credentials",
        isPrimary = true
    ),
    DashboardFeature(
        FeatureType.VerifiedList,
        "Verified Practitioners",
        "Search secure database",
        isPrimary = false
    ),
    DashboardFeature(FeatureType.Sync, "Sync Records", "Last sync: 2 mins ago", isPrimary = false),
    DashboardFeature(FeatureType.Profile, "Profile", "Settings and identity", isPrimary = false)
)
