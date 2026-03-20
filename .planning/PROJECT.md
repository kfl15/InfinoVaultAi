# InfinoVault

## What This Is

An on-device, privacy-first chat app that can answer questions from a user’s uploaded PDFs. Users can upload many PDFs, ask questions in a continuous chat, and get grounded answers with citations (page number + highlighted snippet).

This builds on the existing SmolChat Android app (local SLM via `:smollm` / `llama.cpp`) and adds a fully local RAG pipeline (PDF → text/OCR → chunks → embeddings → retrieval → cited answer).

## Core Value

Users can ask questions about their private PDFs and get accurate, cited answers without the data leaving the device.

## Requirements

### Validated

- ✓ On-device chat with a local SLM (via `smollm/src/main/java/io/shubham0204/smollm/*` + `llama.cpp`)
- ✓ Local model download/import flows (see `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/*`)

### Active

- [ ] Privacy-first on-device PDF RAG across all uploaded PDFs, with citations (page + snippet highlight)
- [ ] Robust continuous chat even when context window/KV cache would overflow (no “chat stops” / crashes)
- [ ] Support both text PDFs and scanned PDFs via on-device OCR (with size/perf-conscious approach)

### Out of Scope

- Cloud processing / server-side RAG — privacy-first means keep data on device
- “Line numbers” in PDFs — use page number + highlighted snippet instead (PDFs don’t have stable line numbers)

## Context

- Codebase already includes:
  - `:app` Jetpack Compose UI + chat flows
  - `:smollm` JNI bridge to `llama.cpp` for inference + GGUF metadata read
  - `:hf-model-hub-api` for Hugging Face model listing/search/download helpers
  - `:smolvectordb` (present as a module; likely useful for local vector storage)
- The new capability is “Document QA” (PDF library + indexing + retrieval + cited answers) integrated into the chat UX.

## Constraints

- **Distribution**: Future target is an eventual Google Play Store launch; do not force Play Store work into Phase 1 unless required.
- **Privacy/Offline**: Everything must run on-device; no document content leaves the device.
- **Citations**: Responses must include page number + snippet, and the UI should let users view the cited excerpt in context.
- **OCR**: Must support scanned PDFs. Prefer an on-device OCR option that minimizes APK bloat; allow language packs/models to be downloaded on-device if needed.
- **Reliability**: Chat must not fail when the model’s context window is exceeded; the app must degrade gracefully and continue.
- **Performance**: Indexing should be incremental with progress and cancellation; retrieval + answer should feel responsive for typical PDFs.

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Use on-device RAG | Privacy-first goal | — Pending |
| Citation format = page + highlighted snippet | Practical + user-trust | — Pending |
| Embeddings on-device (prefer `llama.cpp` embedding mode or a small local embedding model) | Avoid network + keep stack small | — Pending |
| Context overflow strategy (summary + recent messages + retrieval of relevant history + document chunks) | Prevent KV-cache/context-window failures | — Pending |
| OCR engine choice (size vs accuracy tradeoff) | Scanned PDFs required | — Pending |

---
*Last updated: 2026-03-20 after initial project initialization*


