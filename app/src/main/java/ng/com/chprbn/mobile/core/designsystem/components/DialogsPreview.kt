package ng.com.chprbn.mobile.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme

@Preview(showBackground = true)
@Composable
private fun InfoDialogPreview() {
    ChprbnTheme {
        InfoDialog(
            title = "System Update",
            message = "We've enhanced the verification process for multi-state practitioners. You can now track licensing status across multiple jurisdictions from a single dashboard view.",
            primaryButtonText = "Got it",
            onPrimary = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessDialogPreview() {
    ChprbnTheme {
        SuccessDialog(
            title = "Verification Successful",
            message = "The practitioner has been successfully verified and synced.",
            primaryButtonText = "Continue",
            onPrimary = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorDialogPreview() {
    ChprbnTheme {
        ErrorDialog(
            title = "Verification Failed",
            message = "Invalid QR Code. Please ensure the license is valid and try again.",
            primaryButtonText = "Try Again",
            secondaryButtonText = "Manual Entry",
            onPrimary = {},
            onSecondary = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmationDialogPreview() {
    ChprbnTheme {
        ConfirmationDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out? You will need to re-authenticate to access your practitioner verification dashboard.",
            primaryButtonText = "Sign Out",
            secondaryButtonText = "Cancel",
            onConfirm = {},
            onCancel = {},
            onDismiss = {}
        )
    }
}

