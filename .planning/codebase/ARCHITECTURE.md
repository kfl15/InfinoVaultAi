# Architecture

SmolChat-Android is a multi-module Android app that runs GGUF LLMs fully on-device. The `:app` module provides the Compose UI and persistence, while the `:smollm` module provides a JNI-backed inference engine built on `llama.cpp`.

## High-level module boundaries

- `:app` (Android application): UI (Compose), Room database, Koin DI, model download/import flows, and orchestration of inference.
- `:smollm` (Android library): Kotlin API (`SmolLM`, `GGUFReader`) + JNI/C++ implementation (`LLMInference`) + CMake build that compiles `llama.cpp`.
- `:hf-model-hub-api` (JVM library): Hugging Face model hub HTTP client used by `:app` to search/browse models.
- `:smolvectordb` (Android library): separate JNI/C++ module (not currently wired into `:app`).

Gradle module inclusion is defined in `settings.gradle.kts`.

## App entry points (runtime)

- Application / DI boot: `app/src/main/java/io/shubham0204/smollmandroid/SmolChatApplication.kt` starts Koin with generated KSP module wiring from `app/src/main/java/io/shubham0204/smollmandroid/KoinAppModule.kt`.
- Launcher/router: `app/src/main/java/io/shubham0204/smollmandroid/MainActivity.kt` routes the user to model download vs chat based on whether `ModelsRepository.getAvailableModelsList()` returns any models.
- Feature Activities (declared in `app/src/main/AndroidManifest.xml`):
  - Chat: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatActivity.kt`
  - Model download/import: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelActivity.kt`
  - Tasks: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_tasks/ManageTasksActivity.kt`
  - ASR: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/ManageASRActivity.kt`

## Primary data flows

### 1) Model acquisition and registration

The app supports (a) downloading GGUF files from a URL (including a curated list), (b) browsing/searching Hugging Face models, and (c) importing a local file.

- Download to public downloads (system DownloadManager): `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt` uses `DownloadManager` with destination `Environment.DIRECTORY_DOWNLOADS`.
- Import/copy into app storage: `DownloadModelsViewModel.copyModelFile(...)` copies the selected URI into `context.filesDir`.
- Metadata extraction: after copying, the app reads model metadata from the GGUF via `io.shubham0204.smollm.GGUFReader` (`smollm/src/main/java/io/shubham0204/smollm/GGUFReader.kt`) to populate:
  - context length (GGUF key derived from `general.architecture` and `<arch>.context_length`)
  - chat template (`tokenizer.chat_template`)
  Native implementation: `smollm/src/main/cpp/GGUFReader.cpp`.
- Persistence: the imported model is stored as `LLMModel` in Room (`app/src/main/java/io/shubham0204/smollmandroid/data/ModelsDB.kt`) via the `AppDB` facade (`app/src/main/java/io/shubham0204/smollmandroid/data/AppDB.kt`). The stored record includes `path`, `contextSize`, and `chatTemplate`.

Hugging Face browsing/search is wrapped by `app/src/main/java/io/shubham0204/smollmandroid/data/HFModelsAPI.kt`, which calls into `:hf-model-hub-api` types such as `io.shubham0204.hf_model_hub_api.HFModels`.

### 2) Chat + inference loop

Core path for a single user message:

1. UI host: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatActivity.kt` renders Compose content and wires callbacks.
2. State + events: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatScreenViewModel.kt` maintains UI state and delegates model work.
3. Orchestration: `app/src/main/java/io/shubham0204/smollmandroid/llm/SmolLMManager.kt`:
   - loads/unloads the model via `io.shubham0204.smollm.SmolLM` (`smollm/src/main/java/io/shubham0204/smollm/SmolLM.kt`)
   - replays prior chat history from Room into the native context (unless `chat.isTask`)
   - streams tokens via `SmolLM.getResponseAsFlow(...)` and surfaces partial responses back to the UI on `Dispatchers.Main`
   - persists assistant messages back into Room (`AppDB.addAssistantMessage(...)`)

On the native side, the Kotlin `SmolLM` external methods map to JNI functions implemented in `smollm/src/main/cpp/smollm.cpp`, which delegates to `LLMInference` (`smollm/src/main/cpp/LLMInference.cpp`). `LLMInference` uses `llama.cpp` APIs (model load, decode, sampling) and `common_chat_templates_*` helpers for applying chat templates.

### 3) ASR (speech-to-text) management

The ASR settings/download UI is hosted at `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/ManageASRActivity.kt`.

- Downloads: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/DownloadService.kt` uses Ketch (OkHttp-backed) to download ASR models into a destination directory (default is `context.filesDir`).
- Transcription: services live in `app/src/main/java/io/shubham0204/smollmandroid/llm/speech2text/AudioTranscriptionService.kt` and `app/src/main/java/io/shubham0204/smollmandroid/llm/speech2text/MyTranscriber.java`.

## Native wiring (`llama.cpp` and shared libraries)

### Build-time composition (CMake)

- `:smollm` compiles native code using `externalNativeBuild` configured in `smollm/build.gradle.kts`.
- The `llama.cpp` repository is included directly into the CMake build via:
  - `smollm/src/main/cpp/CMakeLists.txt` -> `add_subdirectory(../../../../llama.cpp llama.cpp)`
- The `:smollm` CMake builds multiple shared libraries from the same sources (`LLMInference.cpp`, `smollm.cpp`) with different CPU flags (notably for `arm64-v8a`), plus a standalone GGUF reader library:
  - `smollm` (universal)
  - `smollm_v7a` (armeabi-v7a)
  - `smollm_v8`, `smollm_v8_2_fp16`, `smollm_v8_2_fp16_dotprod`
  - `smollm_v8_4_fp16_dotprod`, `smollm_v8_4_fp16_dotprod_sve`, `smollm_v8_4_fp16_dotprod_i8mm`, `smollm_v8_4_fp16_dotprod_i8mm_sve`
  - `ggufreader`

The build links against `llama.cpp` targets (e.g., `llama`, `common`, and `ggml`) and also links `vulkan` for the `smollm_*` targets (`smollm/src/main/cpp/CMakeLists.txt`).

### Runtime loading (Kotlin)

- `SmolLM` selects and loads the best-matching `libsmollm_*.so` at class-load time by inspecting CPU features and emulator heuristics:
  - `smollm/src/main/java/io/shubham0204/smollm/SmolLM.kt`
- `GGUFReader` always loads `libggufreader.so`:
  - `smollm/src/main/java/io/shubham0204/smollm/GGUFReader.kt`

## Persistence + threading model

- Persistence: Room entities and DAOs under `app/src/main/java/io/shubham0204/smollmandroid/data/` (e.g., `ModelsDB.kt`, `MessagesDB.kt`). `AppDB` provides a facade used throughout the app.
- Inference threading: `SmolLMManager` runs model load/inference work in `CoroutineScope(Dispatchers.Default)` and marshals partial/final UI updates to `Dispatchers.Main`.
- Concurrency control: `SmolLMManager` guards model state with a `ReentrantLock` and tracks load/inference jobs (`modelInitJob`, `responseGenerationJob`).

## Navigation patterns

Navigation is primarily per-Activity, with Compose Navigation used inside specific Activities:

- Chat: routes/types and NavType adapters under `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/CustomNavTypes.kt`.
- Model download flow: typed navigation adapters under `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/CustomNavTypes.kt`.

Markdown rendering used in chat lives in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/MDRenderer.kt` with Prism4j/Markwon integration via `app/src/main/java/io/shubham0204/smollmandroid/prism4j/MarkwonProvider.kt`.
