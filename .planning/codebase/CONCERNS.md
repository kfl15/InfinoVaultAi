# Codebase Concerns

**Analysis Date:** 2026-03-20

## Tech Debt

**Synchronous DB access from UI via `runBlocking`:**
- Issue: `AppDB` exposes many synchronous methods that wrap Room calls with `runBlocking(Dispatchers.IO)`.
- Files: `app/src/main/java/io/shubham0204/smollmandroid/data/AppDB.kt`
- Why: Simplifies call sites (no `suspend`/Flow plumbing).
- Impact: UI-thread callers still block waiting for the IO work to finish (jank/ANR risk if called frequently or on slow storage).
- Fix approach: Convert read/write APIs to `suspend` (and/or expose `Flow`) and call from `viewModelScope`/`lifecycleScope`; keep a thin sync wrapper only where truly needed.

**Blocking network call from Compose UI via `runBlocking`:**
- Issue: `checkConnectivity()` performs a synchronous `HttpURLConnection` request inside `runBlocking(Dispatchers.IO)` and is called directly from a composable.
- Files: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/HFModelSearchScreen.kt`
- Impact: Composable recomposition can repeatedly block the UI thread; contributes to jank and risks ANRs on slow networks.
- Fix approach: Make connectivity checks async (suspend/Flow/state), cache results, and trigger via `LaunchedEffect`/`viewModelScope`.

**Model reload cycle on message edit:**
- Issue: Editing a message deletes DB rows, unloads the model, reloads it, and re-sends the edited message.
- Files: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatScreenViewModel.kt` (comment near `// TODO: There should be no need to unload/load the model again`)
- Why: No native "edit conversation" support; reload ensures model context matches DB.
- Impact: Expensive and slow on-device (re-tokenization + model warmup), can feel "broken" on low-end devices.
- Fix approach: Maintain an in-memory conversation state that can be re-fed without full unload, or implement "rebuild context" without tearing down the native model object.

**Backup rules are effectively "default allow":**
- Issue: `allowBackup="true"` is enabled but `backup_rules.xml`/`data_extraction_rules.xml` are template placeholders without explicit include/exclude.
- Files: `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/backup_rules.xml`, `app/src/main/res/xml/data_extraction_rules.xml`
- Why: Defaults left in place.
- Impact: Chat history/model metadata may be included in cloud/device backups unexpectedly (privacy + large backup size).
- Fix approach: Explicitly exclude Room DB and any model directories; document what is safe to back up.

## Known Bugs

**Potential ABI list crash for 32-bit check:**
- Symptoms: Crash at startup on devices where `Build.SUPPORTED_32_BIT_ABIS` is empty.
- Trigger: Non-emulated, non-arm64 device path executes `Build.SUPPORTED_32_BIT_ABIS[0]`.
- Files: `smollm/src/main/java/io/shubham0204/smollm/SmolLM.kt`
- Workaround: None in code; would require release fix.
- Root cause: Indexing `[0]` without checking array length.
- Fix approach: Guard with `isNotEmpty()` before indexing; treat "empty" as not supported.

**ASR ZIP extraction can hang on `__MACOSX` entries:**
- Symptoms: App hangs during ASR model extraction for ZIP bundles that contain `__MACOSX/*` entries (common when zipped on macOS).
- Trigger: `unzipModel()` hits an entry whose name starts with `__MACOSX` and executes `continue` without advancing to the next zip entry.
- Files: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/ManageASRViewModel.kt` (`unzipModel`)
- Root cause: Loop control bug (`continue` without `getNextEntry()`).
- Fix approach: Always advance to the next entry; also handle directory entries explicitly.

## Security Considerations

**Exported `ChatActivity` accepts external intents (share + deep-link style extras):**
- Risk: Other apps can send large/untrusted text (`Intent.EXTRA_TEXT`) or arbitrary `task_id` extras; can cause unexpected chat creation, DB mutations, or crashes.
- Files: `app/src/main/AndroidManifest.xml`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatActivity.kt`
- Current mitigation: MIME type check for `ACTION_SEND` (`text/plain`).
- Recommendations: Add strict validation/size limits; handle missing/invalid `task_id` defensively; consider moving "task shortcut" handling behind an internal-only exported component if possible.

**Model & ASR downloads lack integrity verification:**
- Risk: If a downloaded GGUF/ASR bundle is corrupted/tampered, the app may crash, misbehave, or load untrusted content.
- Files: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/PopularModelsList.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/ASRModels.kt`
- Current mitigation: HTTPS only (no explicit checksum/signature validation found).
- Recommendations: Add SHA-256 checksums (or signed metadata) for "known" URLs; verify imported models (size + hash) before indexing/using.

**ASR ZIP extraction is vulnerable to Zip Slip (path traversal):**
- Risk: A malicious ZIP can write files outside the intended destination directory (e.g., via `../` in entry names). Since bundles are downloaded from the network, this is a remote arbitrary file write within the app sandbox.
- Files: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/ManageASRViewModel.kt` (`unzipModel` uses `Paths.get(destDir.absolutePath, zipEntry.name)` without normalization/containment checks)
- Recommendations: Normalize and validate target paths to ensure they stay under `destDir`; reject absolute/parent-traversal paths; handle directories safely.

**Markdown/HTML rendering surface:**
- Risk: LLM output rendered as Markdown with HTML extension can create surprising clickable content or unsafe link handling.
- Files: `app/build.gradle.kts` (Markwon deps), `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/MDRenderer.kt` (`HtmlPlugin.create()`, `LinkifyPlugin.create(Linkify.WEB_URLS)`) 
- Current mitigation: Not apparent in `MDRenderer` (HTML plugin enabled; URL spans created).
- Recommendations: Disable HTML rendering for untrusted content, or sanitize; add link-opening confirmation + safe URL handling.

**Audio recording permission & privacy:**
- Risk: `RECORD_AUDIO` permission increases privacy sensitivity; ensure clear UX, on-device processing claims, and avoid accidental background capture.
- Files: `app/src/main/AndroidManifest.xml`, ASR screens under `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/`
- Recommendations: Ensure microphone usage is explicit (foreground UI), add privacy disclosures and permission rationale; consider runtime checks and telemetry redaction.

## Performance Bottlenecks

**Native LLM initialization / reload cost:**
- Problem: Multiple code paths can unload/reload native libraries/models; model edit flow forces reload.
- Files: `smollm/src/main/java/io/shubham0204/smollm/SmolLM.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatScreenViewModel.kt`
- Measurement: Not captured in repo docs (add tracing).
- Cause: Tearing down native state rather than reusing/rebuilding context.
- Improvement path: Add profiling + structured lifecycle; cache model instance; rebuild conversation context without full reload.

**Blocking calls hidden behind `runBlocking`:**
- Problem: DB operations can block call sites even when dispatching work to `Dispatchers.IO`.
- Files: `app/src/main/java/io/shubham0204/smollmandroid/data/AppDB.kt`
- Measurement: Not captured (add StrictMode + trace markers).
- Improvement path: Make DB calls truly async (`suspend`/Flow), avoid blocking UI.

## Fragile Areas

**CPU-feature-based native library selection:**
- Why fragile: Startup depends on CPU feature parsing from `/proc/cpuinfo` and a complex decision tree for `System.loadLibrary`.
- Common failures: Missing/incorrectly packaged `.so` for a device; edge-case feature strings; ABI list assumptions.
- Safe modification: Add unit/instrumentation tests for selection logic and a robust fallback; log-and-fallback on `UnsatisfiedLinkError`.
- Files: `smollm/src/main/java/io/shubham0204/smollm/SmolLM.kt`
- Test coverage: Minimal; only a single instrumentation test exists (`smollm/src/androidTest/java/io/shubham0204/smollm/SmolLMTest.kt`).

**Untrusted file import + parsing error handling:**
- Why fragile: Model import copies a user-selected URI to internal storage and immediately parses it as GGUF; there is no visible error handling around the parse.
- Files: `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/DownloadModelsViewModel.kt` (`copyModelFile`)
- Common failures: Null `openInputStream`/partial copy; invalid/non-GGUF file; truncated files leading to parse exceptions; leftover partial files.
- Safe modification: Validate file name/size/extension, handle null streams, wrap GGUF parsing in `try/catch`, and write atomically (temp file -> rename).

**Native build toolchain (NDK/CMake) coupling:**
- Why fragile: Multiple modules rely on NDK 27 + CMake 3.22.1; build flags differ across modules.
- Common failures: CI/local NDK mismatch, ABI packaging issues, increased APK size, hard-to-debug JNI crashes.
- Files: `smollm/build.gradle.kts`, `smolvectordb/build.gradle.kts`, `smollm/src/main/cpp/CMakeLists.txt`, `smolvectordb/src/main/cpp/CMakeLists.txt`
- Safe modification: Pin toolchain in CI; document required NDK/CMake; consider ABI splits and symbol stripping policies.
- Test coverage: Very limited for JNI failure modes.

## Scaling Limits

**On-device storage & RAM pressure from models:**
- Current capacity: Depends on user-downloaded GGUF/ASR bundles (often hundreds of MB to multiple GB).
- Limit: Low-end devices may run out of storage/RAM; background eviction can break sessions.
- Symptoms at limit: OOM crashes, slow token generation, failed downloads/import, unusable backups.
- Files: Model URL lists in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/PopularModelsList.kt`, `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/manage_asr/ASRModels.kt`
- Scaling path: Enforce per-device recommendations, storage quota UI, model pruning, and memory-safe defaults.

## Dependencies at Risk

**Android/NDK fast-moving targets:**
- Risk: `compileSdk`/`targetSdk` and NDK are pinned; library/tooling changes can break builds or native ABI behavior.
- Files: `app/build.gradle.kts`, `smollm/build.gradle.kts`, `smolvectordb/build.gradle.kts`
- Impact: Build failures in CI, runtime crashes on newer Android versions if not continuously validated.
- Migration plan: Add CI matrix (AGP/Gradle/NDK), document upgrade playbook, and run instrumentation smoke tests on API 26/31/35+.

## Missing Critical Features

**Download integrity + resume robustness:**
- Problem: No checksums/signatures and unclear resume/partial download handling for large model files.
- Current workaround: Retry manually; rely on HTTPS.
- Blocks: Safe "one-click" downloads for large models and reliable field usage.
- Files: Model download screens under `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/`
- Implementation complexity: Medium (hash pipeline + UI + persisted metadata).

## Test Coverage Gaps

**Chat + model lifecycle edge cases:**
- What's not tested: Intent entry points (share/task), model load/unload/reload behaviors, editing message flow, failure recovery.
- Risk: Regressions lead to startup crashes or broken chat sessions.
- Priority: High
- Files: Existing tests are few (`app/src/androidTest/java/io/shubham0204/smollmandroid/ChatActivityTests.kt`, `app/src/androidTest/java/io/shubham0204/smollmandroid/TaskActivityTests.kt`, `smollm/src/androidTest/java/io/shubham0204/smollm/SmolLMTest.kt`)
- Difficulty to test: Requires device/emulator coverage across ABIs and mocked model artifacts.

---

*Concerns audit: 2026-03-20*
*Update as issues are fixed or new ones discovered*
