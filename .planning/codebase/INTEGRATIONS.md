# External Integrations

**Analysis Date:** 2026-03-20

## APIs & External Services

**Hugging Face Model Hub (public):**
- Service: `https://huggingface.co` - model search/metadata/file hosting
  - Client module: `:hf-model-hub-api` (Ktor Client + OkHttp engine) - `hf-model-hub-api/build.gradle.kts`, `hf-model-hub-api/src/main/java/io/shubham0204/hf_model_hub_api/HFModels.kt`
  - Base + endpoints:
    - Base URL `https://huggingface.co` - `hf-model-hub-api/src/main/java/io/shubham0204/hf_model_hub_api/HFEndpoints.kt`
    - Models list/search `https://huggingface.co/api/models` - `hf-model-hub-api/src/main/java/io/shubham0204/hf_model_hub_api/HFEndpoints.kt`, `hf-model-hub-api/src/main/java/io/shubham0204/hf_model_hub_api/HFModelSearch.kt`
    - Model specs `https://huggingface.co/api/models/{modelId}` - `hf-model-hub-api/src/main/java/io/shubham0204/hf_model_hub_api/HFEndpoints.kt`, `hf-model-hub-api/src/main/java/io/shubham0204/hf_model_hub_api/HFModelInfo.kt`
    - Model file tree `https://huggingface.co/api/models/{modelId}/tree/main` - `hf-model-hub-api/src/main/java/io/shubham0204/hf_model_hub_api/HFEndpoints.kt`, `hf-model-hub-api/src/main/java/io/shubham0204/hf_model_hub_api/HFModelTree.kt`
  - Auth: no tokens/credentials present in repo; assumes public endpoints
  - App usage:
    - Connectivity check hits base URL - `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt`
    - File downloads use Android `DownloadManager` to public Downloads dir - `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt`
    - Direct GGUF resolve URL pattern `https://huggingface.co/{modelId}/resolve/main/{path}` - `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/ViewHFModelScreen.kt`
    - Curated "popular" GGUF URLs are hardcoded - `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/PopularModelsList.kt`

**Moonshine ASR model bundles (hosted on Hugging Face):**
- Service: `https://huggingface.co/shubhxm0204/moonshine-asr-models`
  - Bundle URLs (zip) - `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/ASRModels.kt`
  - Download client: Ketch (OkHttp-backed) - `app/build.gradle.kts`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/DownloadService.kt`

## SDKs (On-device)

**Moonshine Voice (speech-to-text SDK):**
- Dependency: `ai.moonshine:moonshine-voice:0.0.48` - `gradle/libs.versions.toml`, `app/build.gradle.kts`
- Manifest wiring/permissions:
  - `tools:overrideLibrary="ai.moonshine.voice"` - `app/src/main/AndroidManifest.xml`
  - `android.permission.RECORD_AUDIO` - `app/src/main/AndroidManifest.xml`

**Native inference (llama.cpp via JNI):**
- Source integration: git submodule `llama.cpp` - `.gitmodules`, `llama.cpp/`
- Build integration: CMake adds subdirectory + links `vulkan` - `smollm/src/main/cpp/CMakeLists.txt`

## Data Storage

**Database (local):**
- SQLite via Room - configured in Gradle and used from app data layer - `app/build.gradle.kts`, `app/src/main/java/io/shubham0204/smollmandroid/data/AppDB.kt`

**On-device files (local):**
- Downloaded models:
  - GGUF downloads to public Downloads dir (Android `DownloadManager`) - `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt`
  - Imported GGUF copied into `context.filesDir` - `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt`
- ASR bundles:
  - Downloaded to app-internal storage (`context.filesDir`) by default - `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/DownloadService.kt`

## Android OS Integrations

- Network permission `android.permission.INTERNET` - `app/src/main/AndroidManifest.xml`
- Share intent receive (`ACTION_SEND`, `text/plain`) in chat activity - `app/src/main/AndroidManifest.xml`
- System download UI/notifications via `DownloadManager` - `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt`

## Authentication & Identity

- No user auth provider (OAuth/login) found in Gradle deps or code references.
- Release signing uses environment-provided keystore passwords/alias - `app/build.gradle.kts`, `.github/workflows/build.yml`, `.github/workflows/build_and_release.yml`

## Monitoring & Observability

- No explicit crash reporting / analytics SDKs found (e.g., Firebase/Sentry) in dependencies or source references.

## CI/CD & Deployment

**CI Pipeline: GitHub Actions**
- Build workflow (branches `feat/*`) - `.github/workflows/build.yml`
- Build + GitHub Release workflow (tags `v*`) - `.github/workflows/build_and_release.yml`
- Release signing secrets:
  - `KEYSTORE_BASE_64` decoded to `keystore.jks` - `.github/workflows/build.yml`, `.github/workflows/build_and_release.yml`
  - `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEYSTORE_ALIAS`, `RELEASE_KEY_PASSWORD` - `.github/workflows/build.yml`, `.github/workflows/build_and_release.yml`
- NDK setup: `nttld/setup-ndk@v1` using `r27c` - `.github/workflows/build.yml`, `.github/workflows/build_and_release.yml`

## Build-time Integrations

- Maven repositories: Google + Maven Central + JitPack + Sonatype snapshots - `settings.gradle.kts`

## Webhooks & Callbacks

- None found.

---

*Integration audit: 2026-03-20*  
*Update when adding/removing external services*
