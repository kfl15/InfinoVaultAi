# Repository Structure

SmolChat-Android is a multi-module Gradle (Kotlin DSL) Android project. The app module (`:app`) hosts a Compose UI + Room persistence layer and uses a native/JNI inference library (`:smollm`) that vendors `llama.cpp` via CMake.

## Top-level directories (repo root)

- `app/` — Android application module (Compose UI, Room DB, Koin DI, Activities).
- `smollm/` — Android library module (Kotlin API + JNI + C++ inference, `externalNativeBuild`).
- `hf-model-hub-api/` — Kotlin/JVM library module for Hugging Face model hub APIs (Ktor client).
- `smolvectordb/` — Android library module with its own JNI/C++ (`externalNativeBuild`); currently not depended on by `:app`.
- `llama.cpp/` — vendored native dependency (Git submodule per `.gitmodules`), built by `:smollm` CMake.
- `gradle/` — version catalog + wrapper (`gradle/libs.versions.toml`, `gradle/wrapper/*`).
- `docs/`, `resources/`, `metadata/` — documentation and store/release assets.
- `.planning/` — generated planning/docs (including this codebase map).
- `.codex/` — Codex/GSD workflow assets.
- Build entry points: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradlew`, `gradlew.bat`.

## Gradle modules (from `settings.gradle.kts`)

- `:app` — application (depends on `:smollm` and `:hf-model-hub-api`).
- `:smollm` — inference library (CMake + `llama.cpp`).
- `:hf-model-hub-api` — HF model hub client library (Ktor/OkHttp engine).
- `:smolvectordb` — optional native library module.

## `:app` (Android application)

Build + manifest:
- `app/build.gradle.kts` — Android app config and dependencies (Compose, Room, Koin, Paging, Markwon/Prism4j, OkHttp/Ketch, serialization).
- `app/src/main/AndroidManifest.xml` — declares Activities and permissions (`INTERNET`, `RECORD_AUDIO`).

Entry points / DI:
- `app/src/main/java/io/shubham0204/smollmandroid/SmolChatApplication.kt` — boots Koin.
- `app/src/main/java/io/shubham0204/smollmandroid/KoinAppModule.kt` — Koin annotations component scan root.
- `app/src/main/java/io/shubham0204/smollmandroid/MainActivity.kt` — launch router (sends user to model download vs chat).

Data layer (Room + prefs):
- `app/src/main/java/io/shubham0204/smollmandroid/data/AppDB.kt` — Room database + `AppDB` facade.
- Entities/DAOs: `app/src/main/java/io/shubham0204/smollmandroid/data/ChatsDB.kt`, `app/src/main/java/io/shubham0204/smollmandroid/data/MessagesDB.kt`, `app/src/main/java/io/shubham0204/smollmandroid/data/ModelsDB.kt`, `app/src/main/java/io/shubham0204/smollmandroid/data/TasksDB.kt`, `app/src/main/java/io/shubham0204/smollmandroid/data/FoldersDB.kt`.
- Type adapters: `app/src/main/java/io/shubham0204/smollmandroid/data/Converters.kt`.
- Prefs abstraction: `app/src/main/java/io/shubham0204/smollmandroid/data/SharedPrefStore.kt`.
- HF wrapper used by UI: `app/src/main/java/io/shubham0204/smollmandroid/data/HFModelsAPI.kt`.

LLM orchestration (app-side):
- `app/src/main/java/io/shubham0204/smollmandroid/llm/SmolLMManager.kt` — wraps `io.shubham0204.smollm.SmolLM`, manages load/unload + streaming inference and persists messages.
- `app/src/main/java/io/shubham0204/smollmandroid/llm/ModelsRepository.kt` — reads/writes `LLMModel` records and model files.
- Speech-to-text services: `app/src/main/java/io/shubham0204/smollmandroid/llm/speech2text/AudioTranscriptionService.kt`, `app/src/main/java/io/shubham0204/smollmandroid/llm/speech2text/MyTranscriber.java`.

UI (Activities hosting Compose):
- Chat feature: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatActivity.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatScreenViewModel.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/MDRenderer.kt`.
- Model download/import/HF browse: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelActivity.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt`.
- Tasks CRUD: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_tasks/ManageTasksActivity.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_tasks/TasksViewModel.kt`.
- ASR management + downloads: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/ManageASRActivity.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/ManageASRViewModel.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/DownloadService.kt`.
- Shared UI: `app/src/main/java/io/shubham0204/smollmandroid/ui/components/`, theme in `app/src/main/java/io/shubham0204/smollmandroid/ui/theme/`.
- Markdown/code highlighting glue: `app/src/main/java/io/shubham0204/smollmandroid/prism4j/MarkwonProvider.kt` and bundled grammars under `app/src/main/java/io/shubham0204/smollmandroid/prism4j/`.

## `:smollm` (native inference library)

Kotlin API surface:
- `smollm/src/main/java/io/shubham0204/smollm/SmolLM.kt` — main Kotlin wrapper; selects which shared library to load based on CPU features and exposes streaming inference (`getResponseAsFlow`).
- `smollm/src/main/java/io/shubham0204/smollm/GGUFReader.kt` — reads GGUF metadata via JNI and loads `libggufreader.so`.

Native sources + build:
- `smollm/build.gradle.kts` — configures `externalNativeBuild { cmake { ... } }` and passes CMake args (e.g., `-DBUILD_SHARED_LIBS=ON`).
- `smollm/src/main/cpp/CMakeLists.txt` — pulls in `llama.cpp` via `add_subdirectory(../../../../llama.cpp llama.cpp)` and builds multiple `smollm_*` shared libraries per ABI/CPU flags, plus `ggufreader`.
- JNI bridges: `smollm/src/main/cpp/smollm.cpp` (JNI for `SmolLM`), `smollm/src/main/cpp/GGUFReader.cpp` (JNI for `GGUFReader`).
- Inference implementation: `smollm/src/main/cpp/LLMInference.cpp`, `smollm/src/main/cpp/LLMInference.h`.

## `:hf-model-hub-api` (Hugging Face client lib)

- `hf-model-hub-api/build.gradle.kts` — JVM library (Ktor client core + OkHttp engine + Kotlin serialization).
- Sources under `hf-model-hub-api/src/main/java/io/shubham0204/hf_model_hub_api/` — `HFModels`, `HFModelInfo`, `HFModelTree`, `HFModelSearch`, `HFEndpoints`, etc. (consumed by `app/src/main/java/io/shubham0204/smollmandroid/data/HFModelsAPI.kt`).

## `:smolvectordb` (native vector DB; currently unused by `:app`)

- `smolvectordb/src/main/cpp/CMakeLists.txt` — builds `libsmolvectordb.so` from `smolvectordb/src/main/cpp/VectorDB.cpp` and `smolvectordb/src/main/cpp/smolvectordb.cpp`.

## Quick "where is X?"

- App entry point / router: `app/src/main/java/io/shubham0204/smollmandroid/MainActivity.kt`
- Koin bootstrapping: `app/src/main/java/io/shubham0204/smollmandroid/SmolChatApplication.kt`
- Chat UI host: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatActivity.kt`
- Chat state + orchestration: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatScreenViewModel.kt`
- LLM manager (app-side): `app/src/main/java/io/shubham0204/smollmandroid/llm/SmolLMManager.kt`
- Native inference wrapper: `smollm/src/main/java/io/shubham0204/smollm/SmolLM.kt`
- Native build wiring to `llama.cpp`: `smollm/src/main/cpp/CMakeLists.txt`
- GGUF metadata reader (Kotlin + JNI): `smollm/src/main/java/io/shubham0204/smollm/GGUFReader.kt`
- Model download/import flow: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelActivity.kt`
