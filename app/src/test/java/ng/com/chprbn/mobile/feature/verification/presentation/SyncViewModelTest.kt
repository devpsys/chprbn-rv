package ng.com.chprbn.mobile.feature.verification.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.verification.domain.model.VerifiedLicense
import ng.com.chprbn.mobile.feature.verification.domain.usecase.GetSyncRecordsUseCase
import ng.com.chprbn.mobile.feature.verification.domain.usecase.RetryFailedSyncUseCase
import ng.com.chprbn.mobile.feature.verification.domain.usecase.SyncAllRecordsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getSyncRecordsUseCase: GetSyncRecordsUseCase
    private lateinit var syncAllRecordsUseCase: SyncAllRecordsUseCase
    private lateinit var retryFailedSyncUseCase: RetryFailedSyncUseCase

    @Before
    fun setUp() {
        getSyncRecordsUseCase = mockk()
        syncAllRecordsUseCase = mockk()
        retryFailedSyncUseCase = mockk()
        // Default: empty list so init/refresh always succeeds. Tests that need a
        // different behaviour override this stub before constructing the ViewModel.
        coEvery { getSyncRecordsUseCase() } returns emptyList()
    }

    @Test
    fun `init loads records and clears loading flag`() = runTest {
        coEvery { getSyncRecordsUseCase() } returns listOf(
            verified("REG-1", SyncStatus.Synced),
            verified("REG-2", SyncStatus.Pending)
        )

        val viewModel = makeViewModel()

        with(viewModel.uiState.value) {
            assertEquals(2, records.size)
            assertEquals(false, isLoading)
            assertNull(error)
        }
    }

    @Test
    fun `init surfaces error message when load throws`() = runTest {
        coEvery { getSyncRecordsUseCase() } throws RuntimeException("DB locked")

        val viewModel = makeViewModel()

        with(viewModel.uiState.value) {
            assertEquals(false, isLoading)
            assertEquals("DB locked", error)
        }
    }

    @Test
    fun `syncAll formats summary on success and reloads records`() = runTest {
        coEvery { syncAllRecordsUseCase() } returns SyncBatchResult(
            attempted = 3, succeeded = 2, failed = 1
        )
        val viewModel = makeViewModel()

        // After syncAll, getSyncRecordsUseCase is called again — bump the stub.
        coEvery { getSyncRecordsUseCase() } returns listOf(
            verified("REG-1", SyncStatus.Synced),
            verified("REG-2", SyncStatus.Synced),
            verified("REG-3", SyncStatus.Failed)
        )

        viewModel.syncAll()

        with(viewModel.uiState.value) {
            assertEquals(false, isSyncing)
            assertEquals("Sync all: 2 ok, 1 failed (3 attempted)", lastBatchSummary)
            assertEquals(3, records.size)
            assertNull(error)
        }
    }

    @Test
    fun `syncAll reports nothing-to-upload when attempted is zero`() = runTest {
        coEvery { syncAllRecordsUseCase() } returns SyncBatchResult(0, 0, 0)
        val viewModel = makeViewModel()

        viewModel.syncAll()

        assertEquals("Sync all: nothing to upload.", viewModel.uiState.value.lastBatchSummary)
    }

    @Test
    fun `syncAll on failure surfaces the error and clears isSyncing`() = runTest {
        coEvery { syncAllRecordsUseCase() } throws RuntimeException("Server 502")
        val viewModel = makeViewModel()

        viewModel.syncAll()

        with(viewModel.uiState.value) {
            assertEquals(false, isSyncing)
            assertNull(lastBatchSummary)
            assertEquals("Server 502", error)
        }
    }

    @Test
    fun `retryFailed formats its summary with the Retry failed prefix`() = runTest {
        coEvery { retryFailedSyncUseCase() } returns SyncBatchResult(
            attempted = 2, succeeded = 1, failed = 1
        )
        val viewModel = makeViewModel()

        viewModel.retryFailed()

        assertEquals(
            "Retry failed: 1 ok, 1 failed (2 attempted)",
            viewModel.uiState.value.lastBatchSummary
        )
    }

    @Test
    fun `retryFailed on failure surfaces the error and clears isSyncing`() = runTest {
        coEvery { retryFailedSyncUseCase() } throws RuntimeException("No connection")
        val viewModel = makeViewModel()

        viewModel.retryFailed()

        with(viewModel.uiState.value) {
            assertEquals(false, isSyncing)
            assertNull(lastBatchSummary)
            assertEquals("No connection", error)
        }
    }

    @Test
    fun `sync error survives the implicit reloadFromDb that follows a failure`() = runTest {
        // Regression test for the bug fixed in SyncViewModel#reloadFromDb:
        // previously, onSuccess of the post-failure reload cleared `error`,
        // so the user never saw the failure message. This test pins the fix.
        coEvery { syncAllRecordsUseCase() } throws RuntimeException("Server 502")
        val viewModel = makeViewModel()

        viewModel.syncAll()

        assertEquals(
            "Failure message must survive the post-sync reload",
            "Server 502",
            viewModel.uiState.value.error
        )
    }

    @Test
    fun `consumeError clears the error field`() = runTest {
        coEvery { getSyncRecordsUseCase() } throws RuntimeException("DB locked")
        val viewModel = makeViewModel()
        // sanity
        assertEquals("DB locked", viewModel.uiState.value.error)

        viewModel.consumeError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `derived counts reflect the loaded records`() = runTest {
        coEvery { getSyncRecordsUseCase() } returns listOf(
            verified("REG-1", SyncStatus.Synced),
            verified("REG-2", SyncStatus.Synced),
            verified("REG-3", SyncStatus.Pending),
            verified("REG-4", SyncStatus.Failed)
        )

        val viewModel = makeViewModel()

        with(viewModel.uiState.value) {
            assertEquals(4, total)
            assertEquals(2, syncedCount)
            assertEquals(1, pendingCount)
            assertEquals(1, failedCount)
            assertEquals(0.5f, syncProgress, 0.0001f)
        }
    }

    @Test
    fun `lastSuccessfulSyncMillis returns the max of synced lastSyncAttempt`() = runTest {
        coEvery { getSyncRecordsUseCase() } returns listOf(
            verified("REG-1", SyncStatus.Synced, lastSyncAttempt = 1_700_000_000_000L),
            verified("REG-2", SyncStatus.Synced, lastSyncAttempt = 1_800_000_000_000L),
            verified("REG-3", SyncStatus.Failed, lastSyncAttempt = 9_000_000_000_000L)
        )

        val viewModel = makeViewModel()

        // Failed records are ignored even with a more recent attempt timestamp.
        assertEquals(1_800_000_000_000L, viewModel.uiState.value.lastSuccessfulSyncMillis)
    }

    @Test
    fun `formatRelativeLastSync handles null sentinel`() = runTest {
        val viewModel = makeViewModel()

        assertEquals("No successful sync yet", viewModel.formatRelativeLastSync(null))
    }

    @Test
    fun `refresh re-invokes the load use case`() = runTest {
        val viewModel = makeViewModel() // first invocation in init

        viewModel.refresh()

        coVerify(atLeast = 2) { getSyncRecordsUseCase() }
    }

    private fun makeViewModel() = SyncViewModel(
        getSyncRecordsUseCase,
        syncAllRecordsUseCase,
        retryFailedSyncUseCase
    )

    private fun verified(
        registrationNumber: String,
        syncStatus: SyncStatus,
        lastSyncAttempt: Long? = null
    ) = VerifiedLicense(
        registrationNumber = registrationNumber,
        fullName = "Jane Doe",
        photoUrl = null,
        profession = "Pharmacist",
        licenseStatus = "Active",
        expiryDate = "2027-01-01",
        subtitle = null,
        verifiedAt = 1_700_000_000_000L,
        verificationLocation = "",
        practitionerPresent = true,
        remark = "Identity confirmed",
        syncStatus = syncStatus,
        lastSyncAttempt = lastSyncAttempt
    )
}
