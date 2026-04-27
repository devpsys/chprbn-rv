package ng.com.chprbn.mobile.feature.verification.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.verification.data.api.IrregularityReportApiService
import ng.com.chprbn.mobile.feature.verification.domain.model.ReportLicenseIrregularityPayload
import ng.com.chprbn.mobile.feature.verification.domain.model.SubmitIrregularityReportResult
import ng.com.chprbn.mobile.feature.verification.domain.repository.IrregularityReportRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import androidx.core.net.toUri

class IrregularityReportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: IrregularityReportApiService
) : IrregularityReportRepository {

    override suspend fun submitReport(payload: ReportLicenseIrregularityPayload): SubmitIrregularityReportResult =
        withContext(Dispatchers.IO) {
            if (payload.snapshotContentUri.isBlank()) {
                return@withContext SubmitIrregularityReportResult.Error("A license snapshot is required.")
            }
            val uri = runCatching { payload.snapshotContentUri.toUri() }.getOrElse {
                return@withContext SubmitIrregularityReportResult.Error("Invalid snapshot.")
            }

            var uploadFile: File? = null
            try {
                uploadFile = materializeSnapshotForUpload(context, uri)
                    ?: return@withContext SubmitIrregularityReportResult.Error("Unable to read the snapshot image.")

                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                val snapshotPart = MultipartBody.Part.createFormData(
                    "snapshot",
                    uploadFile.name,
                    uploadFile.asRequestBody(mime.toMediaTypeOrNull())
                )

                val response = api.submitIrregularityReport(
                    nameOnCard = payload.nameOnCard.toPlainTextBody(),
                    licenseNumber = payload.licenseNumber.toPlainTextBody(),
                    cadre = payload.cadre.toPlainTextBody(),
                    gender = payload.gender.toPlainTextBody(),
                    remark = payload.remark.apiValue.toPlainTextBody(),
                    reportedAt = payload.reportedAtEpochMillis.toString().toPlainTextBody(),
                    snapshot = snapshotPart
                )

                if (!response.isSuccessful) {
                    val err = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                        ?: "Request failed (${response.code()})"
                    return@withContext SubmitIrregularityReportResult.Error(err)
                }

                val body = response.body()
                if (body == null) {
                    return@withContext SubmitIrregularityReportResult.Error("Empty response from server.")
                }
                if (!body.status) {
                    return@withContext SubmitIrregularityReportResult.Error(
                        body.message?.takeIf { it.isNotBlank() } ?: "Report was not accepted."
                    )
                }
                SubmitIrregularityReportResult.Success
            } catch (t: Throwable) {
                SubmitIrregularityReportResult.Error(
                    t.message?.takeIf { it.isNotBlank() } ?: "Unable to submit report."
                )
            } finally {
                uploadFile?.delete()
            }
        }

    private fun String.toPlainTextBody() =
        toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())

    private fun materializeSnapshotForUpload(context: Context, uri: Uri): File? {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: "image/jpeg"
        val ext = when {
            mime.contains("png", ignoreCase = true) -> "png"
            mime.contains("webp", ignoreCase = true) -> "webp"
            else -> "jpg"
        }
        val out = File(context.cacheDir, "irregularity_upload_${System.currentTimeMillis()}.$ext")
        return try {
            resolver.openInputStream(uri)?.use { input ->
                out.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            if (out.length() == 0L) null else out
        } catch (_: Throwable) {
            if (out.exists()) out.delete()
            null
        }
    }
}
