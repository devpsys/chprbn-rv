package ng.com.chprbn.mobile.feature.verification.presentation

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ManualEntryViewModelTest {

    @Test
    fun `initial state has empty license number`() = runTest {
        val viewModel = ManualEntryViewModel()

        assertEquals("", viewModel.uiState.value.licenseNumber)
    }

    @Test
    fun `onLicenseNumberChange updates state`() = runTest {
        val viewModel = ManualEntryViewModel()

        viewModel.uiState.test {
            assertEquals("", awaitItem().licenseNumber)
            viewModel.onLicenseNumberChange("MED-12345")
            assertEquals("MED-12345", awaitItem().licenseNumber)
            viewModel.onLicenseNumberChange("MED-67890")
            assertEquals("MED-67890", awaitItem().licenseNumber)
        }
    }
}
