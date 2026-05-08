package ng.com.chprbn.mobile.core.persistence.encryption

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DatabaseMigrationGuardTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        prefs = mockk()
        editor = mockk(relaxed = true)
        every { prefs.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.commit() } returns true
    }

    @Test
    fun `first run deletes legacy databases and sets marker`() {
        every { prefs.getBoolean("sqlcipher_migration_v1_done", false) } returns false
        every { context.deleteDatabase(any()) } returns true

        DatabaseMigrationGuard(context, prefs).migrateIfNeeded()

        verify(exactly = 1) { context.deleteDatabase("auth.db") }
        verify(exactly = 1) { context.deleteDatabase("scan.db") }
        verify(exactly = 1) { editor.putBoolean("sqlcipher_migration_v1_done", true) }
        verify(exactly = 1) { editor.commit() }
    }

    @Test
    fun `subsequent runs are no-op when marker is set`() {
        every { prefs.getBoolean("sqlcipher_migration_v1_done", false) } returns true

        DatabaseMigrationGuard(context, prefs).migrateIfNeeded()

        verify(exactly = 0) { context.deleteDatabase(any()) }
        verify(exactly = 0) { editor.putBoolean(any(), any()) }
        verify(exactly = 0) { editor.commit() }
    }

    @Test
    fun `marker is set even when deleteDatabase returns false`() {
        // deleteDatabase returns false when the file didn't exist — that's fine,
        // we still want to mark migration as done so we don't retry forever.
        every { prefs.getBoolean("sqlcipher_migration_v1_done", false) } returns false
        every { context.deleteDatabase(any()) } returns false

        DatabaseMigrationGuard(context, prefs).migrateIfNeeded()

        verify(exactly = 1) { editor.putBoolean("sqlcipher_migration_v1_done", true) }
        verify(exactly = 1) { editor.commit() }
    }

    @Test
    fun `custom legacy database list is honored`() {
        every { prefs.getBoolean("sqlcipher_migration_v1_done", false) } returns false
        every { context.deleteDatabase(any()) } returns true

        DatabaseMigrationGuard(
            context,
            prefs,
            legacyDbNames = listOf("auth.db", "scan.db", "extra.db")
        ).migrateIfNeeded()

        verify(exactly = 1) { context.deleteDatabase("auth.db") }
        verify(exactly = 1) { context.deleteDatabase("scan.db") }
        verify(exactly = 1) { context.deleteDatabase("extra.db") }
    }

    @Test
    fun `marker key value matches public constant for SharedPreferences interop`() {
        // Pin the key so a refactor doesn't accidentally retrigger the migration
        // on existing installs (which would wipe their data a second time).
        assertTrue(DatabaseMigrationGuard.GUARD_MARKER_KEY == "sqlcipher_migration_v1_done")
        assertTrue(DatabaseMigrationGuard.PREFS_FILE == "db_migration_guard")
    }
}
