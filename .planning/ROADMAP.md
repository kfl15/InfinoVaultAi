# Roadmap: SmolChat RAG (Android)

**Created:** 2026-03-20
**Granularity:** standard
**Mode:** yolo

All v1 requirements in `.planning/REQUIREMENTS.md` are mapped to exactly one phase.

| # | Phase | Goal | Requirements | Success Criteria |
|---|-------|------|--------------|------------------|
| 1 | Privacy + Document Library Skeleton | Establish privacy/offline posture, storage model, and PDF library UX skeleton | PRIV-01, PRIV-02, PDF-01, PDF-02, PDF-03, UX-02 | 4 |
| 2 | Extraction + Chunk Model | Extract text + OCR with stable page mapping and chunk IDs | EXTR-01, EXTR-02, EXTR-03, EXTR-04, INDEX-01, PDF-04 | 4 |
| 3 | On-device Indexing Store | Persist chunks + embeddings and support incremental indexing lifecycle | INDEX-02, INDEX-03, INDEX-04, UX-01, UX-03 | 5 |
| 4 | RAG Answering + Citations | Retrieve across all PDFs and generate cited answers with tappable sources | RAG-01, RAG-02, RAG-03, ANS-01, ANS-02, ANS-03, ANS-04 | 5 |
| 5 | Continuous Chat (KV/Context Fix) | Make chat robust to context-window/KV-cache limits via compaction + session rebuild | CHAT-01, CHAT-02, CHAT-03 | 4 |

---

## Phase 1: Privacy + Document Library Skeleton

Goal: Add a PDF library surface and set the app’s privacy/offline constraints clearly so all later work builds on it.

Requirements: PRIV-01, PRIV-02, PDF-01, PDF-02, PDF-03, UX-02

Success criteria:
1. User can import a PDF and see it in a local library list; deleting it removes it from the list.
2. App clearly communicates “on-device” behavior and has no document-content network calls in the import flow.
3. Corrupt/unsupported PDFs show a clear error state instead of crashing.
4. Data lives in app-private storage (or encrypted if chosen later) and survives app restarts.

## Phase 2: Extraction + Chunk Model

Goal: Turn PDFs into stable, citeable text passages (including OCR for scanned pages).

Requirements: EXTR-01, EXTR-02, EXTR-03, EXTR-04, INDEX-01, PDF-04

Success criteria:
1. For text PDFs, extracted text matches the document content and is mapped to correct page numbers.
2. For scanned PDFs, OCR can be enabled and produces usable text for retrieval.
3. Chunks have stable IDs that support citations (doc + page + offsets/snippet).
4. “Search all PDFs” is the default retrieval scope (even before vector embeddings exist).

## Phase 3: On-device Indexing Store

Goal: Store chunks + embeddings locally and support an indexing lifecycle that is fast, cancellable, and resilient.

Requirements: INDEX-02, INDEX-03, INDEX-04, UX-01, UX-03

Success criteria:
1. Indexing shows progress and can be cancelled without corrupting state.
2. App can resume indexing after app restart (or clearly restarts the job) without losing track.
3. Re-importing/updating a document reindexes only that document.
4. Index status is visible per document (queued/in-progress/done/failed).
5. Storage size growth is understandable (e.g., “text + embeddings”) and controlled.

## Phase 4: RAG Answering + Citations

Goal: Answer user questions using retrieved chunks and show trustworthy citations.

Requirements: RAG-01, RAG-02, RAG-03, ANS-01, ANS-02, ANS-03, ANS-04

Success criteria:
1. For a question, the app retrieves relevant chunks across all PDFs and includes them as citations in the answer.
2. Each citation shows PDF name + page number + snippet; tapping opens a page view with the snippet highlighted.
3. If retrieval finds no relevant evidence, assistant says so and asks a clarifying question.
4. Retrieval results are deduplicated so citations aren’t noisy/repetitive.
5. Answering latency remains interactive on a mid-range device for typical PDFs.

## Phase 5: Continuous Chat (KV/Context Fix)

Goal: Eliminate “chat stops when context window is full” by implementing automatic compaction and session rebuild.

Requirements: CHAT-01, CHAT-02, CHAT-03

Success criteria:
1. Long conversations continue indefinitely without crashes or “stuck” inference.
2. When compaction occurs, user sees a brief “compacting context” indicator and the conversation continues naturally.
3. The assistant preserves important user context via summary + retrieval (not just truncation).
4. Regression test / manual scenario demonstrates safe handling near context limits.
