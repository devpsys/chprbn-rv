package ng.com.chprbn.mobile.core.persistence.encryption

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DatabaseKeyProviderTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var provider: DatabaseKeyProvider

    @Before
    fun setUp() {
        prefs = mockk()
        editor = mockk(relaxed = true)
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.commit() } returns true
        provider = DatabaseKeyProvider(prefs)
    }

    @Test
    fun `first call generates 32-byte key when no key persisted`() {
        every { prefs.getString("db_passphrase_v1", null) } returns null
        val captured = slot<String>()
        every { editor.putString("db_passphrase_v1", capture(captured)) } returns editor

        val key = provider.getOrCreatePassphrase()

        assertEquals(32, key.size)
        // Hex-encoded 32 bytes = 64 hex chars
        assertEquals(64, captured.captured.length)
        assertTrue(
            "persisted key should be lowercase hex",
            captured.captured.all { it in '0'..'9' || it in 'a'..'f' }
        )
        verify(exactly = 1) { editor.commit() }
    }

    @Test
    fun `subsequent call returns persisted key when present`() {
        // 32 bytes of 0xAB, hex-encoded.
        val persistedHex = "ab".repeat(32)
        every { prefs.getString("db_passphrase_v1", null) } returns persistedHex

        val key = provider.getOrCreatePassphrase()

        assertEquals(32, key.size)
        assertArrayEquals(ByteArray(32) { 0xab.toByte() }, key)
        // Persistence path should not run.
        verify(exactly = 0) { editor.putString(any(), any()) }
        verify(exactly = 0) { editor.commit() }
    }

    @Test
    fun `key is round-trippable through hex encoding`() {
        // First call: nothing persisted → generates and captures the hex written.
        val captured = slot<String>()
        every { prefs.getString("db_passphrase_v1", null) } returns null
        every { editor.putString("db_passphrase_v1", capture(captured)) } returns editor

        val firstKey = provider.getOrCreatePassphrase()

        // Second call: now return the captured hex → must decode back to firstKey.
        every { prefs.getString("db_passphrase_v1", null) } returns captured.captured

        val secondKey = provider.getOrCreatePassphrase()

        assertArrayEquals(firstKey, secondKey)
    }

    @Test
    fun `independent generations produce different keys`() {
        // Two independent providers (e.g. fresh installs) must each get a unique key.
        every { prefs.getString("db_passphrase_v1", null) } returns null
        every { editor.putString("db_passphrase_v1", any()) } returns editor

        val keyA = provider.getOrCreatePassphrase()
        val keyB = DatabaseKeyProvider(prefs).getOrCreatePassphrase()

        // SecureRandom theoretically could collide but the probability is 2^-256.
        assertNotEquals(
            "two SecureRandom-derived 32-byte keys should not be identical",
            keyA.toList(),
            keyB.toList()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `corrupted hex of wrong length is rejected`() {
        every { prefs.getString("db_passphrase_v1", null) } returns "abcd"

        provider.getOrCreatePassphrase()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `corrupted hex with non-hex characters is rejected`() {
        // Right length (64 chars), but contains 'z' which isn't hex.
        every { prefs.getString("db_passphrase_v1", null) } returns "z".repeat(64)

        provider.getOrCreatePassphrase()
    }
}
