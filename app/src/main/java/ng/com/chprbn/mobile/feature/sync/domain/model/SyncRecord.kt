package ng.com.chprbn.mobile.feature.sync.domain.model

import ng.com.chprbn.mobile.feature.verified.domain.model.VerifiedLicense

/**
 * A verified license row including sync metadata, used by the sync feature.
 * Alias keeps the sync domain aligned with the verified persistence model without duplication.
 */
typealias SyncRecord = VerifiedLicense
