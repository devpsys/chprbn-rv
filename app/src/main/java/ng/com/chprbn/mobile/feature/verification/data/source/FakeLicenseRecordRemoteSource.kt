package ng.com.chprbn.mobile.feature.verification.data.source

import kotlinx.coroutines.delay
import ng.com.chprbn.mobile.feature.verification.domain.model.InstitutionAttended
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import javax.inject.Inject

/**
 * Simulated remote source for testing and development.
 * Returns sample practitioner records; optional delay simulates network latency.
 * Easily replaceable with real API by swapping the binding in DI.
 */
class FakeLicenseRecordRemoteSource @Inject constructor() : LicenseRecordRemoteSource {

    override suspend fun getLicenseRecord(registrationNumber: String): LicenseRecord? {
        delay(SIMULATED_DELAY_MS)
        val query = registrationNumber.trim().lowercase()
        if (query.isEmpty()) return null
        return SAMPLE_RECORDS
            .firstOrNull { record ->
                record.registrationNumber.equals(query, ignoreCase = true) ||
                        record.registrationNumber.lowercase().contains(query)
            }
    }

    companion object {
        /** Optional delay in ms to simulate network; set to null to disable. */
        private const val SIMULATED_DELAY_MS: Long = 250L

        private val SAMPLE_RECORDS = listOf(
            LicenseRecord(
                registrationNumber = "MED-12345",
                fullName = "Dr. Jane Doe",
                photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDGwNrw6oZQaZ3azGcP4PrP56q5MV3bz6F2bl-IuygmFXDriKJgdpSy_ndOO9YBkKsOQRk5TrJCLaOwOJNwGnnK5PqCLWyvbNrkAhkwQJD1mE2BcM0rn2nG23k3fmuanXead12LR4L8GCyJieC4dL2mbIFUzchSlO0WQIbmqu6v6paUkf3jYicZtQBrEIOXDf7WFpm6jPbtSsfYHwaEHzuwCfqDNp81YmnQS40CoNyYqqVIxgzym_iSQA5hGYhPX6ekEVIMS63rA6t9",
                profession = "General Practitioner",
                certificateNo = "CERT-MED-001",
                email = "jane.doe@hospital.ng",
                phone = "+2348012345678",
                licenseStatus = "Active",
                expiryDate = "Dec 2026",
                subtitle = "Medical Professional ID",
                issueDate = "Jan 2020",
                gender = "Female",
                graduationDate = "2019",
                institutionAttended = InstitutionAttended(name = "University of Lagos")
            ),
            LicenseRecord(
                registrationNumber = "MED-99284-TX",
                fullName = "Dr. Sarah Elizabeth Jenkins",
                photoUrl = null,
                profession = "Physician",
                certificateNo = "CERT-MED-002",
                email = "sarah.j@clinic.ng",
                phone = "+2348098765432",
                licenseStatus = "Active",
                expiryDate = "Mar 2027",
                subtitle = "Registered Practitioner",
                issueDate = "Mar 2021",
                gender = "Female",
                graduationDate = "2020",
                institutionAttended = InstitutionAttended(name = "College of Medicine, UNN")
            ),
            LicenseRecord(
                registrationNumber = "NUR-44102",
                fullName = "Grace Okon",
                photoUrl = null,
                profession = "Registered Nurse",
                certificateNo = "CERT-NUR-44102",
                email = "",
                phone = "+2347001112233",
                licenseStatus = "Expired",
                expiryDate = "Jan 2024",
                subtitle = "Nursing Professional ID",
                issueDate = "",
                gender = "Female",
                graduationDate = "2015",
                institutionAttended = InstitutionAttended(name = "School of Nursing, Calabar")
            ),
            LicenseRecord(
                registrationNumber = "PHARM-7781",
                fullName = "Dr. Ibrahim Musa",
                photoUrl = null,
                profession = "Pharmacist",
                certificateNo = "CERT-PH-7781",
                email = "ibrahim.m@pharm.ng",
                phone = "",
                licenseStatus = "Expired",
                expiryDate = "Sep 2023",
                subtitle = "Pharmacy Practitioner",
                issueDate = "Jun 2012",
                gender = "Male",
                graduationDate = "2011",
                institutionAttended = InstitutionAttended(name = "University of Jos")
            ),
            LicenseRecord(
                registrationNumber = "DO-88229-P",
                fullName = "Dr. Adewale Bello",
                photoUrl = null,
                profession = "Dental Surgeon",
                certificateNo = "CERT-DO-88229",
                email = "adewale.bello@dental.ng",
                phone = "+2348033334444",
                licenseStatus = "Active",
                expiryDate = "Jun 2025",
                subtitle = "Dental Professional ID",
                issueDate = "Aug 2018",
                gender = "Male",
                graduationDate = "2017",
                institutionAttended = InstitutionAttended(name = "Lagos University Teaching Hospital")
            )
        )
    }
}
