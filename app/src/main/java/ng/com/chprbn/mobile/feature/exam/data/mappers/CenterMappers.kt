package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.feature.exam.data.local.CenterEntity
import ng.com.chprbn.mobile.feature.exam.domain.model.Center

internal fun CenterEntity.toDomain(): Center = Center(
    id = id,
    name = name,
    code = code,
    location = location,
    heroImageUrl = heroImageUrl,
)

internal fun Center.toEntity(): CenterEntity = CenterEntity(
    id = id,
    name = name,
    code = code,
    location = location,
    heroImageUrl = heroImageUrl,
)
