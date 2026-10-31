package ng.com.chprbn.mobile.feature.accreditation.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.accreditation.data.local.AccreditationAttachmentDao
import ng.com.chprbn.mobile.feature.accreditation.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.accreditation.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AccreditationAttachmentRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AttachmentUploadState
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftOpResult
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AccreditationAttachmentRepository
import java.io.File
import java.util.UUID
import javax.inject.Inject

class AccreditationAttachmentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val attachmentDao: AccreditationAttachmentDao,
) : AccreditationAttachmentRepository {

    override suspend fun add(
        assessmentId: String,
        parentKey: String,
        fromUri: Uri,
        isPhoto: Boolean,
        title: String?,
        locationLabel: String?,
    ): AccreditationAttachmentRecord? = withContext(Dispatchers.IO) {
        try {
            val id = "att-${UUID.randomUUID()}"
            val ext = extensionFor(fromUri, isPhoto)
            val dir = File(context.filesDir, ATTACHMENTS_DIR).apply { mkdirs() }
            val dest = File(dir, "$id.$ext")
            context.contentResolver.openInputStream(fromUri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
            if (!dest.exists() || dest.length() == 0L) {
                dest.delete()
                return@withContext null
            }
            val contentUri = runCatching {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    dest,
                ).toString()
            }.getOrElse { dest.toURI().toString() }
            val displayTitle = title?.takeIf { it.isNotBlank() }
                ?: fromUri.lastPathSegment?.substringAfterLast('/')
                ?: "attachment.$ext"
            val sizeLabel = formatSize(dest.length())
            val record = AccreditationAttachmentRecord(
                id = id,
                assessmentId = assessmentId,
                parentKey = parentKey,
                title = displayTitle,
                sizeLabel = sizeLabel,
                isPhoto = isPhoto,
                localPath = dest.absolutePath,
                contentUri = contentUri,
                locationLabel = locationLabel,
                uploadState = AttachmentUploadState.LocalOnly,
            )
            attachmentDao.upsert(record.toEntity())
            record
        } catch (_: Throwable) {
            null
        }
    }

    override suspend fun remove(attachmentId: String): DraftOpResult =
        withContext(Dispatchers.IO) {
            try {
                val existing = attachmentDao.getById(attachmentId)
                if (existing != null) {
                    runCatching { File(existing.localPath).delete() }
                    attachmentDao.deleteById(attachmentId)
                }
                DraftOpResult.Success
            } catch (t: Throwable) {
                DraftOpResult.Error(t.message ?: "Unable to remove attachment.")
            }
        }

    override suspend fun listForParent(
        assessmentId: String,
        parentKey: String,
    ): List<AccreditationAttachmentRecord> = withContext(Dispatchers.IO) {
        attachmentDao.listForParent(assessmentId, parentKey).map { it.toDomain() }
    }

    private fun extensionFor(uri: Uri, isPhoto: Boolean): String {
        val mime = context.contentResolver.getType(uri).orEmpty()
        return when {
            mime.contains("png", ignoreCase = true) -> "png"
            mime.contains("webp", ignoreCase = true) -> "webp"
            mime.contains("pdf", ignoreCase = true) -> "pdf"
            mime.contains("jpeg", ignoreCase = true) || mime.contains("jpg", ignoreCase = true) -> "jpg"
            else -> {
                val fromPath = uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()
                when {
                    !fromPath.isNullOrBlank() && fromPath.length <= 5 -> fromPath
                    isPhoto -> "jpg"
                    else -> "bin"
                }
            }
        }
    }

    private fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    }

    companion object {
        const val ATTACHMENTS_DIR = "accreditation_attachments"
    }
}
