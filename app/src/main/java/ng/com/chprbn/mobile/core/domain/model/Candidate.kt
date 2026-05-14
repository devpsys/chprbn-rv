package ng.com.chprbn.mobile.core.domain.model

/**
 * Cross-feature candidate identity. Both the exam feature (attendance) and the
 * assessment feature (scoring) join against the same candidate roster.
 *
 * `examNumber` is the user-facing identifier (called "indexing number" in the
 * assessment UI). It is stable across features and is what QR scans resolve to.
 *
 * `photoUrl` is the canonical wire-friendly URL after passing through
 * `core.network.ImageUrlNormalization`. `photoDataUri` is an optional Base64
 * fallback for the legacy inline-photo case.
 */
data class Candidate(
    val id: String,
    val examNumber: String,
    val fullName: String,
    val photoUrl: String? = null,
    val photoDataUri: String? = null,
)
