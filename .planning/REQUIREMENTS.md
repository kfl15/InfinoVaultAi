# Requirements: SmolChat RAG (Android)

**Defined:** 2026-03-20
**Core Value:** Users can ask questions about their private PDFs and get accurate, cited answers without the data leaving the device.

## v1 Requirements

### Privacy / Offline

- [ ] **PRIV-01**: All PDF processing (text extraction, OCR, indexing, retrieval, answering) runs on-device with no document content sent over the network
- [ ] **PRIV-02**: App provides a clear “Offline / On-device” status and documents what (if anything) may use network (e.g., optional model downloads)

### PDF Library

- [ ] **PDF-01**: User can add/import PDFs into an in-app library (multiple PDFs)
- [ ] **PDF-02**: User can view/manage the library (list, metadata, size, last indexed)
- [ ] **PDF-03**: User can delete a PDF from the library and all derived index data is removed
- [ ] **PDF-04**: App indexes across all uploaded PDFs by default (with an option to scope to a subset)

### Text Extraction + OCR

- [ ] **EXTR-01**: App extracts text from normal (text-based) PDFs reliably
- [ ] **EXTR-02**: App detects scanned/low-text pages and performs on-device OCR for those pages
- [ ] **EXTR-03**: OCR is opt-in per document (or global) with clear storage/performance impact messaging
- [ ] **EXTR-04**: Extracted text includes stable page mapping so citations can reference the correct page

### Chunking + Indexing

- [ ] **INDEX-01**: App chunks extracted text into retrievable passages with stable IDs (doc/page/offset)
- [ ] **INDEX-02**: App builds embeddings on-device and stores them locally for retrieval
- [ ] **INDEX-03**: Indexing runs incrementally with progress UI and can be cancelled/resumed
- [ ] **INDEX-04**: App reindexes when a PDF is re-imported/updated, without corrupting other documents

### Retrieval

- [ ] **RAG-01**: For each user question, app retrieves top-K relevant chunks across all uploaded PDFs
- [ ] **RAG-02**: Retrieval is fast enough for interactive chat for typical document collections (with reasonable defaults for K and chunk sizes)
- [ ] **RAG-03**: Retrieval results are deduplicated/merged to reduce repeated citations

### Answering + Citations

- [ ] **ANS-01**: Assistant answers are grounded in retrieved PDF chunks (no “hallucinated” claims presented as facts)
- [ ] **ANS-02**: Each answer includes citations that show (a) PDF name, (b) page number, and (c) a snippet used as evidence
- [ ] **ANS-03**: User can tap a citation to view the highlighted snippet in context (at least the page and surrounding text)
- [ ] **ANS-04**: If evidence is insufficient, assistant says so and asks a clarifying question or suggests what to search for

### Continuous Chat / Context Window Robustness

- [ ] **CHAT-01**: Chat remains usable indefinitely; when context window is near full the app automatically compacts history (summary + keep last N turns)
- [ ] **CHAT-02**: When a chat session is reset (due to KV cache / context overflow), the assistant continues seamlessly using the compacted history + retrieved doc chunks
- [ ] **CHAT-03**: The app never crashes or “stops responding” due to context window/KV cache exhaustion; user sees a graceful “compacting context” status when it occurs

### UX / Reliability

- [ ] **UX-01**: User can see indexing status per document and overall (queued/in-progress/done/failed)
- [ ] **UX-02**: App provides clear error messages for unsupported/corrupt PDFs and OCR failures
- [ ] **UX-03**: All RAG operations are cancellable (indexing, OCR, large imports) without leaving inconsistent state

## v2 Requirements

### Multi-language OCR

- **OCR-01**: User can choose OCR language(s) per document

### Advanced Retrieval

- **RET-01**: Hybrid retrieval (BM25 + vector) for better precision on exact terms

## Out of Scope

| Feature | Reason |
|---------|--------|
| Cloud RAG / server storage | Conflicts with privacy-first on-device target |
| “Source line numbers” | PDFs don’t have stable line numbers; page + snippet is the chosen citation format |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| PRIV-01 | Phase 1 | Pending |
| PRIV-02 | Phase 1 | Pending |
| PDF-01 | Phase 1 | Pending |
| PDF-02 | Phase 1 | Pending |
| PDF-03 | Phase 1 | Pending |
| PDF-04 | Phase 2 | Pending |
| EXTR-01 | Phase 2 | Pending |
| EXTR-02 | Phase 2 | Pending |
| EXTR-03 | Phase 2 | Pending |
| EXTR-04 | Phase 2 | Pending |
| INDEX-01 | Phase 2 | Pending |
| INDEX-02 | Phase 3 | Pending |
| INDEX-03 | Phase 3 | Pending |
| INDEX-04 | Phase 3 | Pending |
| UX-01 | Phase 3 | Pending |
| UX-03 | Phase 3 | Pending |
| RAG-01 | Phase 4 | Pending |
| RAG-02 | Phase 4 | Pending |
| RAG-03 | Phase 4 | Pending |
| ANS-01 | Phase 4 | Pending |
| ANS-02 | Phase 4 | Pending |
| ANS-03 | Phase 4 | Pending |
| ANS-04 | Phase 4 | Pending |
| CHAT-01 | Phase 5 | Pending |
| CHAT-02 | Phase 5 | Pending |
| CHAT-03 | Phase 5 | Pending |
| UX-02 | Phase 1 | Pending |

**Coverage:**
- v1 requirements: 26 total
- Mapped to phases: 26
- Unmapped: 0 ✓

---
*Requirements defined: 2026-03-20*
*Last updated: 2026-03-20 after initial definition*
