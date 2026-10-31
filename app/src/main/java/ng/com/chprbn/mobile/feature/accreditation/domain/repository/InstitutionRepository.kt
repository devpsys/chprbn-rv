package ng.com.chprbn.mobile.feature.accreditation.domain.repository

import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetInstitutionProfileResult

interface InstitutionRepository {
    suspend fun getProfile(institutionId: String): GetInstitutionProfileResult
}
