package ng.com.chprbn.mobile.feature.verification.presentation

import ng.com.chprbn.mobile.feature.verification.domain.model.VerificationFeature
import ng.com.chprbn.mobile.feature.verification.domain.model.FeatureType

/**
 * Preview-only data for Compose previews (no Hilt).
 */
val previewVerificationFeatures: List<VerificationFeature> = listOf(
    VerificationFeature(id = FeatureType.ScanQr, isPrimary = true),
    VerificationFeature(id = FeatureType.VerifiedList, isPrimary = false),
    VerificationFeature(id = FeatureType.Sync, isPrimary = false),
    VerificationFeature(id = FeatureType.Profile, isPrimary = false)
)
