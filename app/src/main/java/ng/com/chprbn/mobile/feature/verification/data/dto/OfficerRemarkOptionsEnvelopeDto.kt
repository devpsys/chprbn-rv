package ng.com.chprbn.mobile.feature.verification.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Mobile API v1 envelope for `GET /practitioners/officer-remark-options`.
 *
 * Adopts the standardised envelope (`success` + `data`) per
 * `docs/api/full-api-documentation.md` §3.1 rather than the legacy
 * `status` shape — this is a NEW endpoint so it ships on the new
 * convention from day one.
 *
 * `options` is intentionally nullable on the wire (and the mapper
 * `.orEmpty()`s it) to dodge the Gson + Kotlin-defaults gotcha pinned
 * in `VerificationApiServiceTest`.
 */
data class OfficerRemarkOptionsEnvelopeDto(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: OfficerRemarkOptionsDataDto? = null,
)

data class OfficerRemarkOptionsDataDto(
    @SerializedName("options") val options: List<String>? = null,
)
