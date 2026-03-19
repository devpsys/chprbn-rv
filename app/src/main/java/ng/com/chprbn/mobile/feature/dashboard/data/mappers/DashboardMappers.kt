package ng.com.chprbn.mobile.feature.dashboard.data.mappers

import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.dashboard.data.dto.ProfileResponseDto

/**
 * Maps dashboard API DTOs to domain. Domain User requires accessToken from auth cache.
 * Use when refreshing profile from dashboard API: merge with cached token in repository.
 */
fun ProfileResponseDto.toDomain(accessToken: String): User = User(
    id = id,
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
