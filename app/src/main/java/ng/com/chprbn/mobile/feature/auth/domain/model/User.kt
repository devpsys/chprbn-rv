package ng.com.chprbn.mobile.feature.auth.domain.model

/**
 * Domain model representing the authenticated practitioner (tutor) session.
 *
 * [username] is the license number used to sign in (mobile API `username` field).
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String?,
    val accessToken: String,
    val permissions: List<String>,
    val userPhoto: String?,
    val role: String? = null,
    val staffId: String? = null,
    val unit: String? = null,
    val organization: String? = null,
    val lastLoginAt: String? = null,
    val location: String? = null,
    /**
     * `AdhocProfileDataDto.phone`, carried through so the sync layer
     * can put it on the `assessor` block that
     * `attendance/push-record`, `project/push-record`, and
     * `practical/push-record` now require.
     */
    val phone: String? = null,
    /**
     * `AdhocProfileDataDto.status` — same reason as [phone]. Kept as an
     * `Int` (not a boolean) because the wire semantics beyond `1 = active`
     * aren't confirmed.
     */
    val status: Int? = null,
    /**
     * Raw numeric id from `AdhocProfileDataDto.id`, preserved verbatim so
     * the sync assessor block matches the wire's numeric shape. [id] is
     * the app-side stringified id (`"adhoc_12"`); this is the `12` on the
     * wire. Nullable because rows carried over from an earlier schema
     * predate this field.
     */
    val assessorId: Long? = null,
)
