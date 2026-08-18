package ng.com.chprbn.mobile.core.sync.dto

import com.google.gson.annotations.SerializedName

/**
 * The `assessor` block the three push-record endpoints now require:
 * `attendance/push-record`, `project/push-record`, and
 * `practical/push-record`. Missing the block yields:
 *
 * ```
 * { "status": false, "message": "Failed",
 *   "data": "Assessor is required. Include an \"assessor\" object with id and username." }
 * ```
 *
 * so [id] and [username] are the load-bearing fields; everything else is
 * best-effort from what the officer's `adhoc/profile` returned. Built at
 * sync time by
 * [ng.com.chprbn.mobile.core.sync.AssessorProvider] from the cached
 * [ng.com.chprbn.mobile.feature.auth.data.local.UserEntity] — never
 * synthesised, never partially populated. The provider returns `null` if
 * either required field is missing on the cache, and the remote source
 * fails the batch fast so the row stays queued and retries later
 * instead of hitting the server with a request the server will 4xx
 * anyway.
 */
data class AssessorDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("username") val username: String,
    @SerializedName("status") val status: Int?,
    @SerializedName("department") val department: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("roles") val roles: List<String>,
)
