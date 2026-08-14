package ng.com.chprbn.mobile.feature.exam.data.di

import javax.inject.Qualifier

/**
 * Hilt qualifier for the [retrofit2.Retrofit] instance bound to the exam
 * backend (jarabawa.chprbn.gov.ng). Distinct from the app-wide [retrofit2.Retrofit]
 * in `AuthDataModule` — this backend takes no bearer token; every request is
 * identified by `X-Location` instead.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class JarabawaRetrofit
