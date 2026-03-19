package ng.com.chprbn.mobile.feature.scan.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.scan.data.local.LicenseRecordDao
import ng.com.chprbn.mobile.feature.scan.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.scan.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.scan.data.source.LicenseRecordRemoteSource
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecordResult
import ng.com.chprbn.mobile.feature.scan.domain.repository.ScanRepository
import java.io.IOException
import javax.inject.Inject

/**
 * Data layer: local-first, then remote; cache remote result.
 * Returns cached record if present; else fetches from [LicenseRecordRemoteSource] (API + fake
 * fallback), caches, and returns. [refreshLicenseRecord] performs silent refresh and updates cache.
 */
class ScanRepositoryImpl @Inject constructor(
    private val licenseRecordDao: LicenseRecordDao,
    private val remoteSource: LicenseRecordRemoteSource
) : ScanRepository {

    override suspend fun getLicenseRecord(registrationNumber: String): LicenseRecordResult =
        withContext(Dispatchers.IO) {
            val trimmed = registrationNumber.trim()
            if (trimmed.isEmpty()) return@withContext LicenseRecordResult.NotFound

            val cached = licenseRecordDao.getByRegistrationNumber(trimmed)
            if (cached != null) return@withContext LicenseRecordResult.Success(cached.toDomain())

            val record = runCatching {
                remoteSource.getLicenseRecord(trimmed)
            }.getOrElse { t ->
                return@withContext LicenseRecordResult.Error(
                    if (t is IOException) "Network error. Please check your connection."
                    else (t.message ?: "Unable to fetch license record.")
                )
            }

            if (record == null) LicenseRecordResult.NotFound
            else {
                licenseRecordDao.insertOrUpdate(record.toEntity())
                LicenseRecordResult.Success(record)
            }
        }

    override suspend fun refreshLicenseRecord(registrationNumber: String): LicenseRecord? =
        withContext(Dispatchers.IO) {
            val trimmed = registrationNumber.trim()
            if (trimmed.isEmpty()) return@withContext null
            val record = remoteSource.getLicenseRecord(trimmed) ?: return@withContext null
            licenseRecordDao.insertOrUpdate(record.toEntity())
            record
        }
}
