package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.components.ConfirmationDialog
import ng.com.chprbn.mobile.core.designsystem.components.ErrorDialog
import ng.com.chprbn.mobile.core.designsystem.components.InfoDialog
import ng.com.chprbn.mobile.core.network.stripHostsAndUrls

/**
 * Confirm discard of an unsaved local assessment / section draft.
 */
@Composable
fun DiscardDraftDialog(
    onDiscard: () -> Unit,
    onKeepEditing: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(R.string.accreditation_dialog_discard_title),
        message = stringResource(R.string.accreditation_dialog_discard_message),
        primaryButtonText = stringResource(R.string.accreditation_dialog_discard_confirm),
        secondaryButtonText = stringResource(R.string.accreditation_dialog_discard_keep_editing),
        onConfirm = onDiscard,
        onCancel = onKeepEditing,
        onDismiss = onDismiss,
    )
}

/**
 * Confirm final transmit for server-side grading. Grades / tenure are never computed on-device.
 */
@Composable
fun SubmitAssessmentDialog(
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(R.string.accreditation_dialog_submit_title),
        message = stringResource(R.string.accreditation_dialog_submit_message),
        primaryButtonText = stringResource(R.string.accreditation_dialog_submit_confirm),
        secondaryButtonText = stringResource(R.string.accreditation_dialog_submit_cancel),
        onConfirm = onSubmit,
        onCancel = onCancel,
        onDismiss = onDismiss,
    )
}

/**
 * Sync failure dialog. [message] is sanitised so hostnames / URLs never appear.
 */
@Composable
fun SyncFailedDialog(
    message: String,
    onRetry: () -> Unit,
    onClose: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sanitised = remember(message) { message.stripHostsAndUrls() }
    ErrorDialog(
        title = stringResource(R.string.accreditation_dialog_sync_failed_title),
        message = sanitised,
        primaryButtonText = stringResource(R.string.accreditation_dialog_sync_failed_retry),
        secondaryButtonText = stringResource(R.string.accreditation_dialog_sync_failed_close),
        onPrimary = onRetry,
        onSecondary = onClose,
        onDismiss = onDismiss,
    )
}

/**
 * Explains that grades and accreditation periods are determined by the CHPRBN server.
 */
@Composable
fun GradingInfoDialog(
    onDismiss: () -> Unit,
    onGotIt: (() -> Unit)? = null,
) {
    InfoDialog(
        title = stringResource(R.string.accreditation_dialog_grading_title),
        message = stringResource(R.string.accreditation_dialog_grading_message),
        primaryButtonText = stringResource(R.string.accreditation_dialog_grading_got_it),
        onPrimary = onGotIt ?: onDismiss,
        onDismiss = onDismiss,
    )
}

@Preview(showBackground = true)
@Composable
private fun DiscardDraftDialogPreview() {
    ChprbnTheme {
        DiscardDraftDialog(onDiscard = {}, onKeepEditing = {}, onDismiss = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmitAssessmentDialogPreview() {
    ChprbnTheme {
        SubmitAssessmentDialog(onSubmit = {}, onCancel = {}, onDismiss = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncFailedDialogPreview() {
    ChprbnTheme {
        SyncFailedDialog(
            message = "Unable to reach https://api.chprbn.gov.ng/sync",
            onRetry = {},
            onClose = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GradingInfoDialogPreview() {
    ChprbnTheme {
        GradingInfoDialog(onDismiss = {})
    }
}
