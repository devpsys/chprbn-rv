package ng.com.chprbn.mobile.feature.auth.data.mappers

import ng.com.chprbn.mobile.core.network.normalizeApiPhotoToDataUri
import ng.com.chprbn.mobile.feature.auth.data.dto.AdhocProfileDataDto
import ng.com.chprbn.mobile.feature.auth.data.dto.ProfileDataDto
import ng.com.chprbn.mobile.feature.auth.data.local.UserEntity
import ng.com.chprbn.mobile.feature.auth.domain.model.User

fun AdhocProfileDataDto.toDomain(accessToken: String): User {
    val publicId = id?.let { num ->
        if (num % 1.0 == 0.0) "adhoc_${num.toLong()}" else "adhoc_${num}"
    } ?: "adhoc_unknown"
    return User(
        id = publicId,
        username = username,
        email = email,
        fullName = name,
        accessToken = accessToken,
        permissions = emptyList(),
        userPhoto = null,
        role = role,
        staffId = null,
        unit = department,
        organization = null,
        lastLoginAt = null
    )
}

fun ProfileDataDto.toDomain(accessToken: String): User = User(
    id = id,
    username = username,
    email = email,
    fullName = name,
    accessToken = accessToken,
    permissions = permissions,
    userPhoto = photo.normalizeApiPhotoToDataUri(),
    role = role,
    staffId = null,
    unit = unit,
    organization = null,
    lastLoginAt = lastLoginAt
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
    lastLoginAt = lastLoginAt
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
    lastLoginAt = lastLoginAt
)
