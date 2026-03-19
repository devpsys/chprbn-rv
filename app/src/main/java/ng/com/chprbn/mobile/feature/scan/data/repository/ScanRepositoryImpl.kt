package ng.com.chprbn.mobile.feature.scan.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.scan.data.api.ScanApiService
import ng.com.chprbn.mobile.feature.scan.data.local.LicenseRecordDao
import ng.com.chprbn.mobile.feature.scan.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.scan.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecordResult
import ng.com.chprbn.mobile.feature.scan.domain.repository.ScanRepository
import java.io.IOException
import javax.inject.Inject

/**
 * Data layer: local-first, then remote; cache remote result.
 * Returns cached record if present; else fetches from API, caches, and returns.
 * [refreshLicenseRecord] performs silent refresh from API and updates cache.
 */
class ScanRepositoryImpl @Inject constructor(
    private val licenseRecordDao: LicenseRecordDao,
    private val scanApiService: ScanApiService
) : ScanRepository {

    override suspend fun getLicenseRecord(registrationNumber: String): LicenseRecordResult =
        withContext(Dispatchers.IO) {
            val cached = licenseRecordDao.getByRegistrationNumber(registrationNumber)
            if (cached != null) return@withContext LicenseRecordResult.Success(cached.toDomain())

            val response = runCatching {
                scanApiService.getLicenseRecord(registrationNumber)
            }.getOrElse { t ->
                return@withContext LicenseRecordResult.Error(
                    if (t is IOException) "Network error. Please check your connection."
                    else (t.message ?: "Unable to fetch license record.")
                )
            }

            when {
                response.code() == 404 -> LicenseRecordResult.NotFound
                response.isSuccessful -> {
                    val body = response.body()
                    if (body == null) LicenseRecordResult.Error("Empty response from server.")
                    else {
                        val record = body.toDomain()
                        licenseRecordDao.insertOrUpdate(record.toEntity())
                        LicenseRecordResult.Success(record)
                    }
                }
                else -> LicenseRecordResult.Error(
                    response.message().ifEmpty { "License record not found." }
                )
            }
        }

    override suspend fun refreshLicenseRecord(registrationNumber: String): LicenseRecord? =
        withContext(Dispatchers.IO) {
            val trimmed = registrationNumber.trim()
            if (trimmed.isEmpty()) return@withContext null
            val response = runCatching {
                scanApiService.getLicenseRecord(trimmed)
            }.getOrElse { _ -> return@withContext null }
            if (!response.isSuccessful || response.code() == 404) return@withContext null
            val body = response.body() ?: return@withContext null
            val record = body.toDomain()
            licenseRecordDao.insertOrUpdate(record.toEntity())
            record
        }
}
