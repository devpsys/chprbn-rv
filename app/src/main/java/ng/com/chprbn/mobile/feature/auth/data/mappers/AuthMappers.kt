package ng.com.chprbn.mobile.feature.auth.data.mappers

import ng.com.chprbn.mobile.feature.auth.data.dto.LoginResponseDto
import ng.com.chprbn.mobile.feature.auth.data.dto.UserDto
import ng.com.chprbn.mobile.feature.auth.data.local.UserEntity
import ng.com.chprbn.mobile.feature.auth.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    fullName = fullName,
    accessToken = "", // access token is added by the response mapper
    permissions = permissions,
    userPhoto = userPhoto,
    role = role,
    staffId = staffId,
    unit = unit
)

fun LoginResponseDto.toDomainUser(): User = User(
    id = user.id,
    email = user.email,
    fullName = user.fullName,
    accessToken = accessToken,
    permissions = user.permissions,
    userPhoto = user.userPhoto,
    role = user.role,
    staffId = user.staffId,
    unit = user.unit
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    email = email,
    fullName = fullName,
    accessToken = accessToken,
    permissions = permissions,
    userPhoto = userPhoto
)

fun UserEntity.toDomain(): User = User(
    id = id,
    email = email,
    fullName = fullName,
    accessToken = accessToken,
    permissions = permissions,
    userPhoto = userPhoto,
    role = role,
    staffId = staffId,
    unit = unit
)

