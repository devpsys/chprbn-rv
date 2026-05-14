package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.feature.exam.data.dto.CenterDto
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

/** Returns `null` when the wire payload omits a required identity field. */
internal fun CenterDto.toDomain(): Center? {
    val safeId = id?.takeIf { it.isNotBlank() } ?: return null
    return Center(
        id = safeId,
        name = name.orEmpty(),
        code = code.orEmpty(),
        location = location.orEmpty(),
        heroImageUrl = heroImageUrl,
    )
}
