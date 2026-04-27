package ng.com.chprbn.mobile.feature.verification.presentation

import ng.com.chprbn.mobile.feature.verification.domain.model.VerificationFeature
import ng.com.chprbn.mobile.feature.verification.domain.model.FeatureType

/**
 * Preview-only data for Compose previews (no Hilt).
 */
val previewVerificationFeatures: List<VerificationFeature> = listOf(
    VerificationFeature(
        FeatureType.ScanQr,
        "Scan License QR",
        "Validate practitioner credentials",
        isPrimary = true
    ),
    VerificationFeature(
        FeatureType.VerifiedList,
        "Verified Practitioners",
        "Search secure database",
        isPrimary = false
    ),
    VerificationFeature(FeatureType.Sync, "Sync Records", "Last sync: 2 mins ago", isPrimary = false),
    VerificationFeature(FeatureType.Profile, "Profile", "Settings and identity", isPrimary = false)
)
