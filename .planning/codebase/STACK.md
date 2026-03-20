# Technology Stack

**Analysis Date:** 2026-03-20

## Languages

**Primary:**
- Kotlin (JVM) — Android app + library modules — configured via Gradle plugins in `gradle/libs.versions.toml` and applied in `app/build.gradle.kts`, `smollm/build.gradle.kts`, `smolvectordb/build.gradle.kts`

**Secondary:**
- C/C++ (NDK) — native inference + vector DB code — `smollm/src/main/cpp/*`, `smolvectordb/src/main/cpp/*`
- Java — some sources in app + `:hf-model-hub-api` (plus generated/embedded Prism4j files) — `app/src/main/java/...`, `hf-model-hub-api/src/main/java/...`, `app/src/main/java/io/shubham0204/smollmandroid/prism4j/*`
- Gradle Kotlin DSL — build scripts — `build.gradle.kts`, `settings.gradle.kts`, `app/build.gradle.kts`

## Runtime

**Android targets:**
- `:app` — minSdk 26, targetSdk 35, compileSdk 35, ndkVersion `27.2.12479018` — `app/build.gradle.kts`
- `:smollm` — minSdk 26, compileSdk 35, ndkVersion `27.2.12479018` — `smollm/build.gradle.kts`
- `:smolvectordb` — minSdk 24, compileSdk 36, ndkVersion `27.2.12479018` — `smolvectordb/build.gradle.kts`

**JVM/toolchain:**
- Java 17 source/target + Kotlin `jvmTarget=17` — `app/build.gradle.kts`, `smollm/build.gradle.kts`, `smolvectordb/build.gradle.kts`
- Java 17 toolchain for `:hf-model-hub-api` — `hf-model-hub-api/build.gradle.kts`

## Build System

**Gradle / plugins:**
- Gradle Wrapper 8.13 — `gradle/wrapper/gradle-wrapper.properties`
- Android Gradle Plugin (AGP) 8.13.0 — `gradle/libs.versions.toml`
- Kotlin Android + Kotlin Compose plugins 2.0.0 — `gradle/libs.versions.toml`
- KSP 2.0.0-1.0.24 — `build.gradle.kts`, `app/build.gradle.kts`
- Kotlin Serialization plugin 2.1.0 (applied in `:app` + `:hf-model-hub-api`) — `build.gradle.kts`, `app/build.gradle.kts`, `hf-model-hub-api/build.gradle.kts`
- Foojay toolchain resolver convention 1.0.0 — `settings.gradle.kts`

**Native tooling:**
- Android NDK 27.2.12479018 — `app/build.gradle.kts`, `smollm/build.gradle.kts`, `smolvectordb/build.gradle.kts`
- CMake 3.22.1 — configured via `externalNativeBuild.cmake.version` — `smollm/build.gradle.kts`, `smolvectordb/build.gradle.kts`

## Frameworks & Libraries

**UI (AndroidX/Compose):**
- Jetpack Compose (BOM `2024.10.01`) — `gradle/libs.versions.toml`, `app/build.gradle.kts`
- Navigation-Compose 2.8.3 — `gradle/libs.versions.toml`, `app/build.gradle.kts`
- Material3 (via Compose BOM) + Material Icons Extended — `gradle/libs.versions.toml`, `app/build.gradle.kts`
- Google Fonts for Compose 1.7.7 — `gradle/libs.versions.toml`, `app/build.gradle.kts`

**DI:**
- Koin 3.5.6 + Koin Annotations/KSP 1.3.1 (with config check enabled) — `gradle/libs.versions.toml`, `app/build.gradle.kts`

**Persistence:**
- Room (SQLite) 2.6.1 — `app/build.gradle.kts`

**Paging:**
- Paging 3.3.5 — `app/build.gradle.kts`

**Networking / downloads:**
- OkHttp 4.12.0 — `app/build.gradle.kts`
- Ktor Client 3.0.2 (OkHttp engine, kotlinx-json) — `hf-model-hub-api/build.gradle.kts`, `hf-model-hub-api/src/main/java/io/shubham0204/hf_model_hub_api/HFModels.kt`
- Android `DownloadManager` (GGUF downloads) — `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt`
- Ketch 2.0.5 (JitPack) — ASR bundle downloads — `settings.gradle.kts`, `app/build.gradle.kts`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/DownloadService.kt`

**Markdown / code highlighting (UI):**
- Markwon 4.6.2 + Prism4j 2.0.0 — `app/build.gradle.kts`, `app/src/main/java/io/shubham0204/smollmandroid/prism4j/*`

**Speech recognition (on-device):**
- Moonshine Voice 0.0.48 — `gradle/libs.versions.toml`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`

**Native LLM runtime:**
- `llama.cpp` as a git submodule — `.gitmodules`, `llama.cpp/`
- JNI shared libs built via CMake, linking `android`, `log`, `vulkan` — `smollm/src/main/cpp/CMakeLists.txt`

## Modules

- `:app` — Android application — `app/build.gradle.kts`
- `:smollm` — Android library (JNI + llama.cpp) — `smollm/build.gradle.kts`, `smollm/src/main/cpp/CMakeLists.txt`
- `:hf-model-hub-api` — JVM `java-library` (Ktor client wrapper for Hugging Face Model Hub) — `hf-model-hub-api/build.gradle.kts`, `hf-model-hub-api/src/main/java/io/shubham0204/hf_model_hub_api/*`
- `:smolvectordb` — Android library (native VectorDB) — `smolvectordb/build.gradle.kts`, `smolvectordb/src/main/cpp/CMakeLists.txt`
  - Included in the Gradle build (`settings.gradle.kts`) but not currently depended on by `:app` (`app/build.gradle.kts` has no `implementation(project(":smolvectordb"))`).

## Configuration

**Dependency management:**
- Version catalog: `gradle/libs.versions.toml`
- Repositories: Google + Maven Central + JitPack + Sonatype snapshots — `settings.gradle.kts`

**Release signing (env vars):**
- Keystore: `keystore.jks` (referenced as `../keystore.jks`) — `app/build.gradle.kts`
- Env vars: `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEYSTORE_ALIAS`, `RELEASE_KEY_PASSWORD` — `app/build.gradle.kts`, `.github/workflows/build.yml`, `.github/workflows/build_and_release.yml`

---

*Stack analysis: 2026-03-20*  
*Update after major dependency or toolchain changes*
