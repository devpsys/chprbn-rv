package ng.com.chprbn.mobile.feature.auth.data.mappers

import ng.com.chprbn.mobile.feature.auth.data.dto.AdhocProfileDataDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Locks the AdhocProfile → User field propagation. New fields
 * (phone, status, assessorId) are needed for the sync-layer assessor
 * block; regressions here would fail every push-record with the
 * server-side "Assessor is required" error.
 */
class AuthMappersTest {

    @Test
    fun `AdhocProfileDataDto toDomain preserves the assessor-relevant fields`() {
        val user = AdhocProfileDataDto(
            id = 12.0,
            name = "Jane Field Officer",
            email = "jane.field@example.com",
            phone = "08012345678",
            username = "jane.field",
            status = 1,
            department = "ACC",
            location = "Lagos",
            roles = listOf("Inspector", "Verify Practitioners"),
        ).toDomain(accessToken = "tok")

        // Load-bearing sync fields:
        assertEquals(12L, user.assessorId)
        assertEquals("jane.field", user.username)

        // Best-effort sync fields:
        assertEquals("Jane Field Officer", user.fullName)
        assertEquals("jane.field@example.com", user.email)
        assertEquals("08012345678", user.phone)
        assertEquals(1, user.status)
        assertEquals("ACC", user.unit) // department → unit
        assertEquals("Lagos", user.location)
        assertEquals(listOf("Inspector", "Verify Practitioners"), user.permissions)

        // App-side stringified id still uses the "adhoc_<numeric>" shape.
        assertEquals("adhoc_12", user.id)
    }

    @Test
    fun `AdhocProfileDataDto toDomain yields null assessorId when the wire id is missing`() {
        val user = AdhocProfileDataDto(
            id = null,
            name = "Nameless",
            email = "n@example.com",
            username = "u",
        ).toDomain(accessToken = "tok")

        // Provider will return null → remote sources fail fast rather than
        // silently omitting the assessor block on a request the server 4xx's.
        assertNull(user.assessorId)
        assertEquals("adhoc_unknown", user.id)
    }

    @Test
    fun `User to Entity to User round-trips the assessor fields`() {
        val original = AdhocProfileDataDto(
            id = 12.0,
            name = "Jane",
            email = "j@example.com",
            phone = "0801",
            username = "jane",
            status = 1,
            department = "ACC",
            location = "Lagos",
            roles = listOf("Inspector"),
        ).toDomain(accessToken = "tok")

        val roundTripped = original.toEntity().toDomain(accessToken = "tok")

        assertEquals(original.assessorId, roundTripped.assessorId)
        assertEquals(original.phone, roundTripped.phone)
        assertEquals(original.status, roundTripped.status)
    }
}
