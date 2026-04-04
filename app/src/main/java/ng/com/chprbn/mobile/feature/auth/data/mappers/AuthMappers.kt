package ng.com.chprbn.mobile.feature.auth.data.mappers

import ng.com.chprbn.mobile.core.network.normalizeApiPhotoToDataUri
import ng.com.chprbn.mobile.feature.auth.data.dto.ProfileDataDto
import ng.com.chprbn.mobile.feature.auth.data.local.UserEntity
import ng.com.chprbn.mobile.feature.auth.domain.model.User

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
    accessToken = accessToken,
    permissions = permissions,
    userPhoto = userPhoto,
    role = role,
    staffId = staffId,
    unit = unit,
    organization = organization,
    lastLoginAt = lastLoginAt
)

fun UserEntity.toDomain(): User = User(
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
