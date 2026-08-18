package ng.com.chprbn.mobile.feature.auth.data.mappers

import ng.com.chprbn.mobile.core.network.normalizeApiPhotoToDataUri
import ng.com.chprbn.mobile.feature.auth.data.dto.AdhocProfileDataDto
import ng.com.chprbn.mobile.feature.auth.data.dto.ProfileDataDto
import ng.com.chprbn.mobile.feature.auth.data.local.UserEntity
import ng.com.chprbn.mobile.feature.auth.domain.model.User

fun AdhocProfileDataDto.toDomain(accessToken: String): User {
    val rawNumericId = id?.takeIf { it % 1.0 == 0.0 }?.toLong()
    val publicId = when {
        rawNumericId != null -> "adhoc_$rawNumericId"
        id != null -> "adhoc_$id"
        else -> "adhoc_unknown"
    }
    val roleNames = roles.orEmpty()
    return User(
        id = publicId,
        username = username,
        email = email,
        fullName = name,
        accessToken = accessToken,
        permissions = roleNames,
        userPhoto = null,
        role = roleNames.firstOrNull(),
        staffId = null,
        unit = department,
        organization = null,
        lastLoginAt = null,
        location = location,
        // Fields new to schema v10 — needed by the sync layer to build
        // the `assessor` block on push-record requests.
        phone = phone,
        status = status,
        assessorId = rawNumericId,
    )
}

fun ProfileDataDto.toDomain(accessToken: String): User = User(
    id = id,
    username = username,
    email = email,
    fullName = name,
    accessToken = accessToken,
    permissions = permissions.orEmpty(),
    userPhoto = photo.normalizeApiPhotoToDataUri(),
    role = role,
    staffId = null,
    unit = unit,
    organization = null,
    lastLoginAt = lastLoginAt,
    // Tutor profile envelope has no location field yet — leave null.
    location = null,
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    username = username,
    email = email,
    fullName = fullName,
    permissions = permissions,
    userPhoto = userPhoto,
    role = role,
    staffId = staffId,
    unit = unit,
    organization = organization,
    lastLoginAt = lastLoginAt,
    location = location,
    phone = phone,
    status = status,
    assessorId = assessorId,
)

fun UserEntity.toDomain(accessToken: String): User = User(
    id = id,
    username = username,
    email = email,
    fullName = fullName,
    accessToken = accessToken,
    permissions = permissions,
    userPhoto = userPhoto,
    role = role,
    staffId = staffId,
    unit = unit,
    organization = organization,
    lastLoginAt = lastLoginAt,
    location = location,
    phone = phone,
    status = status,
    assessorId = assessorId,
)
