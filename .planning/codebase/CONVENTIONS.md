# Coding Conventions

**Analysis Date:** 2026-03-20

## Project Layout / Modules

- Modules are declared in `settings.gradle.kts`: `:app`, `:smollm`, `:hf-model-hub-api`, `:smolvectordb`.
- Kotlin sources generally live under `*/src/main/java/...` (even though they are Kotlin) except the HF module which uses `hf-model-hub-api/src/main/kotlin/...`.

## Naming Patterns

**Packages:**
- Reverse-domain style under `io.shubham0204.*` (e.g., `app/src/main/java/io/shubham0204/smollmandroid/...`).
- Modules have their own top-level packages, e.g.:
  - App: `app/src/main/java/io/shubham0204/smollmandroid/...`
  - LLM library: `smollm/src/main/java/io/shubham0204/smollm/...`
  - HF client: `hf-model-hub-api/src/main/kotlin/io/shubham0204/hf_model_hub_api/...`
  - Vector DB: `smolvectordb/src/main/java/io/shubham0204/smolvectordb/...`

**Files:**
- Kotlin/Java file names usually match their primary type and use PascalCase (e.g., `app/src/main/java/io/shubham0204/smollmandroid/MainActivity.kt`).
- Compose UI is grouped under `ui/` with components and screens split (e.g., `app/src/main/java/io/shubham0204/smollmandroid/ui/components/AppAlertDialog.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatActivity.kt`).
- Tests mirror the feature name and use PascalCase (e.g., `app/src/androidTest/java/io/shubham0204/smollmandroid/TaskActivityTests.kt`, `hf-model-hub-api/src/test/java/HFModelTests.kt`).

**Functions:**
- Production code uses `camelCase` (e.g., `load`, `unload`, `getResponse` in `app/src/main/java/io/shubham0204/smollmandroid/llm/SmolLMManager.kt`).
- Tests often use descriptive names with underscores to communicate “action_expectedResult” (e.g., `addition_isCorrect` in `app/src/test/java/io/shubham0204/smollmandroid/ExampleUnitTest.kt`, `clickAddTask_showsNewTask` in `app/src/androidTest/java/io/shubham0204/smollmandroid/TaskActivityTests.kt`).

**Variables & Constants:**
- `camelCase` for locals and properties.
- `private const val` for compile-time constants; constants frequently use UPPER_SNAKE_CASE or tagged names (e.g., `LOGTAG` in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatScreenViewModel.kt`).
- A common pattern is a tag constant + a small logging lambda (e.g., `private const val LOGTAG = "[SmolLMAndroid-Kt]"` and `private val LOGD: (String) -> Unit = { Log.d(LOGTAG, it) }` in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatScreenViewModel.kt`).

**Types:**
- Data models are often `data class`es with Room and/or serialization annotations (e.g., `Chat` in `app/src/main/java/io/shubham0204/smollmandroid/data/ChatsDB.kt`).
- Navigation routes are often `@Serializable` objects/data classes inside Activities (e.g., `DownloadModelActivity.ViewModelRoute` in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelActivity.kt`).

## Code Style

**Formatting:**
- Kotlin official code style is configured in `gradle.properties` via `kotlin.code.style=official`.
- 4-space indentation (typical Kotlin/Android defaults).
- Frequent use of trailing commas in multi-line argument lists (e.g., `SmolLM.InferenceParams(...)` in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatScreenViewModel.kt`).

**Tooling / Lint / Formatting:**
- No repo-level Kotlin formatter/linter config is present (no `.editorconfig`; no ktlint/detekt/spotless config found).
- Java/Kotlin targets Java 17 across modules (e.g., `app/build.gradle.kts`, `smollm/build.gradle.kts`, `hf-model-hub-api/build.gradle.kts`).
- KSP is used for code generation (root `build.gradle.kts` + module usage in `app/build.gradle.kts`), notably:
  - Koin annotations + config check (`ksp { arg("KOIN_CONFIG_CHECK", "true") }` in `app/build.gradle.kts`)
  - Room compiler (`ksp("androidx.room:room-compiler:...")` in `app/build.gradle.kts`)
- A `.clang-format` exists at the repo root for native code formatting (`.clang-format`, `llama.cpp/`, `smollm/src/main/cpp/...`, `smolvectordb/src/main/cpp/...`).

## Import Organization

**Observed Pattern:**
- Imports are generally grouped by “platform/framework” then “project” then “third party” (e.g., `android.*` / `androidx.*` before project imports in `app/src/main/java/io/shubham0204/smollmandroid/MainActivity.kt`).
- Blank lines are used occasionally but not strictly enforced between every import group.

## Error Handling

**Patterns:**
- Boundary-style APIs often accept callbacks and marshal results back to the main thread (e.g., `onSuccess`, `onError` in `app/src/main/java/io/shubham0204/smollmandroid/llm/SmolLMManager.kt`).
- `CancellationException` is treated specially in coroutine code (handled separately from generic `Exception`).
- UI error surfacing is typically via dialogs/toasts (e.g., `createAlertDialog(...)` in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatScreenViewModel.kt`, `Toast.makeText(...)` in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt`).

## Logging

**Framework:**
- Uses `android.util.Log` directly (e.g., `Log.d`, `Log.e` in `app/src/main/java/io/shubham0204/smollmandroid/llm/speech2text/AudioTranscriptionService.kt`).

**Conventions:**
- Log tags are typically bracketed and include a “-Kt” suffix (e.g., `private const val LOGTAG = "[SmolLMAndroid-Kt]"` in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatScreenViewModel.kt`).
- A local `LOGD` lambda wrapper is used in several files to keep calls concise and consistent.

## Threading & Coroutines

**Patterns:**
- Background work commonly uses `Dispatchers.IO`/`Dispatchers.Default` with results marshaled back to `Dispatchers.Main` (e.g., callback APIs in `app/src/main/java/io/shubham0204/smollmandroid/llm/SmolLMManager.kt`; `withContext(Dispatchers.Main)` UI updates in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt`).
- UI state is commonly modeled as `StateFlow` in `ViewModel`s (e.g., `MutableStateFlow` + `.update { ... }` in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatScreenViewModel.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/ManageASRViewModel.kt`).
- Database access uses `Flow` patterns via Room (e.g., `collectAsState(emptyList())` of Room flows in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_tasks/ManageTasksActivity.kt`).

## Jetpack Compose Conventions

**Structure:**
- Compose UI code lives under `app/src/main/java/io/shubham0204/smollmandroid/ui/`.
- Reusable widgets are in `ui/components/`; screens/flows in `ui/screens/...` (e.g., `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatActivity.kt`).

**State + Side Effects:**
- `rememberSaveable { mutableStateOf(...) }` is used for transient UI state (e.g., `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatActivity.kt`).
- `collectAsStateWithLifecycle()` is used to bind `Flow`/`StateFlow` to Compose state (e.g., `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatActivity.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/ManageASRActivity.kt`).
- `LaunchedEffect(...)` is used for one-off effects tied to state changes (e.g., list scrolling behavior in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatActivity.kt`).

**Navigation:**
- Compose Navigation is used with typed destinations via `@Serializable` route objects/data classes and `composable<RouteType>(...)` (e.g., `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelActivity.kt`).
- Complex nav arguments are passed using custom `NavType` wrappers backed by `kotlinx.serialization` (e.g., `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/CustomNavTypes.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/CustomNavTypes.kt`).

## Dependency Injection

**Koin (+ KSP):**
- Bootstrapped in the Application with generated Koin modules (see `app/src/main/java/io/shubham0204/smollmandroid/SmolChatApplication.kt` and `app/src/main/java/io/shubham0204/smollmandroid/KoinAppModule.kt`).
- Injection via delegated properties in Activities (e.g., `private val modelsRepository by inject<ModelsRepository>()` in `app/src/main/java/io/shubham0204/smollmandroid/MainActivity.kt`).
- ViewModels commonly use Koin annotations: `@KoinViewModel` (e.g., `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatScreenViewModel.kt`) and `@Single` for other injectable classes (e.g., `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt`).
- Compose occasionally uses `koinViewModel()` (e.g., `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/ManageASRActivity.kt`).

## Comments & Documentation

**Headers:**
- Many source files begin with an Apache 2.0 license header comment.

**KDoc / Inline Comments:**
- KDoc is used where field meaning/behavior matters (e.g., data layer models in `app/src/main/java/io/shubham0204/smollmandroid/data/ChatsDB.kt`).
- Inline comments are often used to capture intent/UX reasoning (e.g., routing logic in `app/src/main/java/io/shubham0204/smollmandroid/MainActivity.kt`).

## Generated / Vendored Code

**Generated-looking files:**
- Prism lexers and similar code exist under `app/src/main/java/io/shubham0204/smollmandroid/prism4j/` (e.g., `app/src/main/java/io/shubham0204/smollmandroid/prism4j/Prism_kotlin.java`). Treat these as generated/vendored and avoid manual reformatting unless required.

---

*Convention analysis: 2026-03-20*
*Update when patterns change*