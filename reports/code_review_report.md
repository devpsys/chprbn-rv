# Android Project Code Review Report

## 1. Executive Summary
* **High-level overview:** The CHPRBN Mobile project is a modern Android application built using Jetpack Compose, Coroutines/Flow, Hilt, Room, and Retrofit. It adopts a feature-based structure with layers resembling Clean Architecture.
* **Key strengths:** Solid modern tech stack. Proper use of Dependency Injection (Hilt). Excellent adoption of Unidirectional Data Flow (UDF) with Compose `StateFlow`. Good separation of concerns using UseCases and Repositories. Clean UI implementation using Material 3 and custom themes.
* **Key risks:** Critical security vulnerabilities regarding plaintext token storage and unfiltered HTTP logging. Almost total lack of unit and UI testing.

## 2. Detailed Findings

### 2.1 Architecture & Code Organization
* **Findings:** The project is a single-module app structured by feature (e.g., `feature/auth`, `feature/dashboard`). Each feature contains `data`, `domain`, and `presentation` layers.
* **Issues:** While feature packaging is good, keeping everything in a single module limits build parallelization.
* **Severity:** Low

### 2.2 Jetpack Compose Implementation
* **Findings:** Compose is used effectively. ViewModels expose state via `StateFlow` and are collected safely using `collectAsStateWithLifecycle()`. `rememberSaveable` is used properly for surviving configuration changes. `LaunchedEffect` is used correctly for side-effects.
* **Issues:** In `LoginScreen.kt` and `DashboardScreen.kt`, the `ViewModel` is passed directly to the screen composable. Passing only hoisted state and lambda callbacks improves previewability and testability.
* **Severity:** Low

### 2.3 Performance
* **Findings:** The application utilizes Coroutines for asynchronous work. The UI elements do not exhibit obvious extreme recomposition risks.
* **Issues:** `UserDao.kt` functions (`upsertUser`, `getUser`, `clearUser`) are not marked as `suspend`. While currently wrapped in `Dispatchers.IO` at the repository level, it is an idiomatic best practice for Room DAO functions to natively use `suspend` to prevent accidental main thread blocking. Also, there are instances where `Modifier.background()` is chained excessively instead of relying on `Surface` coloring.
* **Severity:** Medium

### 2.4 Code Quality
* **Findings:** The codebase generally follows Kotlin idioms and is highly readable.
* **Issues:** Leftover commented-out code was detected in multiple files (e.g., `LoginScreen.kt`, `DashboardScreen.kt`).
* **Severity:** Low

### 2.5 Data Layer
* **Findings:** Integration of Retrofit and Room is correctly implemented. Good use of `ConnectivityChecker` for offline-first support.
* **Issues:** DAO issues mentioned in the Performance section.
* **Severity:** Low

### 2.6 UI/UX Consistency
* **Findings:** Consistent use of custom themes (`ChprbnTheme`), typography, and standard Material icons. The application has a coherent, modern look and feel.
* **Issues:** None significant observed.
* **Severity:** Low

### 2.7 Testing
* **Findings:** Testing is virtually non-existent.
* **Issues:** Only the default `ExampleUnitTest.kt` and `ExampleInstrumentedTest.kt` exist. There are no unit tests for ViewModels, UseCases, Repositories, or UI components.
* **Severity:** High

### 2.8 Security & Stability
* **Findings:** Dependency injection and Room are used correctly for stable runtime.
* **Issues:** 
  * **Plaintext Token Storage:** `UserEntity.kt` stores the user's `accessToken` in plaintext within the Room database. This is a severe security vulnerability.
  * **HTTP Logging Leak:** `HttpLoggingInterceptor` in `AuthDataModule.kt` is configured to log the full `BODY` of all network requests unconditionally. This will leak sensitive info (passwords, tokens, PII) into logcat, even in release builds.
* **Severity:** High

## 3. Critical Issues
* **Insecure Token Storage:** (`app/src/main/java/ng/com/chprbn/mobile/feature/auth/data/local/UserEntity.kt`) The `accessToken` is stored without encryption.
  * *Fix:* Use `EncryptedSharedPreferences`, AndroidX `DataStore` with cryptography, or integrate SQLCipher for the Room database.
* **Unsafe HTTP Logging:** (`app/src/main/java/ng/com/chprbn/mobile/feature/auth/data/di/AuthDataModule.kt`) Logging interceptor exposes passwords and tokens in production logs.
  * *Fix:* Wrap the interceptor initialization with `if (BuildConfig.DEBUG)` or use a custom logger that redacts sensitive fields.
* **Missing Test Suite:** (`app/src/test` and `app/src/androidTest`) No business logic or UI is covered by tests.
  * *Fix:* Implement Unit Tests for ViewModels and UseCases using tools like JUnit, MockK/Mockito, and Coroutines Test libraries.

## 4. Recommendations
* **Actionable improvements:**
  * Clean up and remove all commented-out dead code from composables.
  * Update `UserDao.kt` to use `suspend` functions and remove the explicit `withContext(Dispatchers.IO)` wrapper in `AuthRepositoryImpl.kt`.
* **Refactoring suggestions:**
  * Refactor screen composables to accept pure state data classes and event lambdas instead of depending directly on the `ViewModel`.
* **Performance optimizations:**
  * Optimize UI modifiers by using `Surface(color = ...)` rather than `Box(modifier = Modifier.background(...))` where applicable to leverage default elevation and drawing behavior better.
* **Architectural improvements:**
  * Consider migrating from a single-module app structured by feature to a multi-module architecture (e.g., `:core`, `:feature:auth`, `:feature:dashboard`) to enforce dependency boundaries and optimize build times as the project scales.

## 5. Suggested Next Steps
1. **Immediate:** Secure the HTTP logging interceptor to only run in `DEBUG` mode to prevent data leaks.
2. **Immediate:** Implement secure storage for the `accessToken` instead of plain text Room storage.
3. **Short-term:** Update the Room DAOs to be `suspend` functions and clean up dead code.
4. **Medium-term:** Set up a robust testing framework and write unit tests for critical paths (e.g., `LoginUseCase`, `AuthRepositoryImpl`, `LoginViewModel`).
5. **Long-term:** Evaluate migrating to a multi-module project structure.
