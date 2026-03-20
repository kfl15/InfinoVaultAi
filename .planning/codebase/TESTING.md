# Testing Patterns

**Analysis Date:** 2026-03-20

## Test Frameworks / Runners

**App module (`:app`):**
- **Host unit tests:** JUnit 4 (`testImplementation(libs.junit)` in `app/build.gradle.kts`).
- **Instrumented tests:** `androidx.test.runner.AndroidJUnitRunner` (`defaultConfig.testInstrumentationRunner` in `app/build.gradle.kts`).
- **UI testing:** Compose UI test APIs (`androidTestImplementation(libs.androidx.ui.test.junit4)` in `app/build.gradle.kts`). Espresso is included as a dependency (`androidTestImplementation(libs.androidx.espresso.core)`), though current tests are Compose-rule based.

**HF API module (`:hf-model-hub-api`):**
- **Host unit tests:** runs on JUnit Platform (`tasks.test { useJUnitPlatform() }` in `hf-model-hub-api/build.gradle.kts`).
- Tests use `kotlin.test.Test` and coroutines `runTest` (see `hf-model-hub-api/src/test/java/HFModelTests.kt`).

**SmolLM library (`:smollm`):**
- **Instrumented tests:** AndroidJUnit4 runner (`smollm/src/androidTest/java/io/shubham0204/smollm/SmolLMTest.kt`) with coroutine testing via `kotlinx-coroutines-test` (declared in `smollm/build.gradle.kts`).

**SmolVectorDB library (`:smolvectordb`):**
- **Instrumented tests:** AndroidJUnit4 runner (`smolvectordb/src/androidTest/java/io/shubham0204/SmolVectorDBTests.kt`).

## Test File Organization

**Locations:**
- App unit tests: `app/src/test/java/...` (e.g., `app/src/test/java/io/shubham0204/smollmandroid/ExampleUnitTest.kt`).
- App instrumentation/UI tests: `app/src/androidTest/java/...` (e.g., `app/src/androidTest/java/io/shubham0204/smollmandroid/TaskActivityTests.kt`, `app/src/androidTest/java/io/shubham0204/smollmandroid/ChatActivityTests.kt`).
- HF API unit tests: `hf-model-hub-api/src/test/java/HFModelTests.kt`.
- SmolLM instrumentation tests: `smollm/src/androidTest/java/io/shubham0204/smollm/SmolLMTest.kt`.
- SmolVectorDB instrumentation tests: `smolvectordb/src/androidTest/java/io/shubham0204/SmolVectorDBTests.kt`.

## Test Structure / Patterns

- Compose UI tests use `createComposeRule()` and drive semantics via matchers + actions (see `app/src/androidTest/java/io/shubham0204/smollmandroid/TaskActivityTests.kt`).
- Some tests are currently placeholders with empty bodies (see `app/src/androidTest/java/io/shubham0204/smollmandroid/ChatActivityTests.kt`).
- HF API tests are integration-like and perform real network calls to Hugging Face endpoints (see `hf-model-hub-api/src/test/java/HFModelTests.kt`).
- SmolLM tests are device/emulator tests and rely on a GGUF model file on-device at a hard-coded path (see `modelPath` in `smollm/src/androidTest/java/io/shubham0204/smollm/SmolLMTest.kt`).

## Run Commands

```bash
./gradlew test

./gradlew :app:testDebugUnitTest
./gradlew :hf-model-hub-api:test

./gradlew connectedAndroidTest
./gradlew :app:connectedAndroidTest
./gradlew :smollm:connectedAndroidTest
./gradlew :smolvectordb:connectedAndroidTest
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## CI / Automation Hooks

- GitHub Actions workflows build release APKs but do not run tests by default:
  - `./gradlew assembleRelease` in `.github/workflows/build.yml`
  - `./gradlew assembleRelease` in `.github/workflows/build_and_release.yml`

## Coverage / Quality Gates

- No explicit coverage configuration or enforcement is present in the Gradle build scripts.

---

*Testing analysis: 2026-03-20*
*Update when test patterns change*