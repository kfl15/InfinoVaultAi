# Phase 1: Privacy + Document Library Skeleton - Context

**Gathered:** 2026-03-20
**Status:** Ready for planning

<domain>
## Phase Boundary

Deliver a local PDF document library baseline for InfinoVault:
- Import PDFs via file picker and Android share-to-app
- Immediately copy accepted PDFs into app-private storage so they remain available offline
- Show a document library list (filename, import date, page count if available) with newest-first sorting
- Provide delete from the library list (and define deletion semantics that will also apply to future citation/source UI)
- Reject corrupt/unsupported PDFs cleanly with a clear user-facing error
- Establish clear privacy/offline messaging (documents stay on-device; no cloud upload/storage implied)

Not in this phase: extraction/OCR/chunking/embeddings/retrieval/answering.

</domain>

<decisions>
## Implementation Decisions

### PDF import UX
- Support both import paths:
  - File picker import
  - Android share-to-app import
- After import, immediately copy the PDF into app-private storage (not a reference to external storage) so the app is usable offline.
- If a PDF is corrupt/unsupported, reject it cleanly and show a clear user-facing error (no crashes, no silent failures).

### Library list UX
- Show: filename, import date, and page count (when available).
- Default sort: newest imported first.
- Include a delete action in the library list UI.
- Do not show placeholder indexing status in Phase 1 (only show indexing-related UI when real indexing state exists).

### Delete behavior (hard delete)
- Allow delete entry points from:
  - The document library list
  - The citation/source-related document UI (future UI; semantics decided now)
- Ask for confirmation before deleting.
- No undo required in Phase 1.
- Deleting a PDF must remove:
  - The document itself
  - All chunks
  - All embeddings
  - All derived index data tied to that PDF

### Privacy/offline messaging
- Clearly state the app is designed for offline/private use and documents stay on-device.
- If any model/resource download is ever needed, explain it separately and clearly.
- Do not imply cloud upload/storage.

### Claude's Discretion
- Exact wording and placement of the privacy/offline copy (within the constraints above)
- Exact library row layout (spacing/typography) as long as required fields are visible

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase definition
- `.planning/ROADMAP.md` — Phase 1 boundary + success criteria
- `.planning/REQUIREMENTS.md` — v1 requirements (notably `PDF-01..04`, `PDF-03`, `PRIV-01..02`, `UX-02`)
- `.planning/PROJECT.md` — product definition + privacy/offline constraints

### Existing import/share patterns
- `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/ImportModelScreen.kt` — existing `ACTION_OPEN_DOCUMENT` picker pattern + error dialog style
- `app/src/main/AndroidManifest.xml` — existing share-to-app intent filter (currently `text/plain`) that can guide adding a PDF share intent

### Codebase structure context
- `.planning/codebase/STRUCTURE.md` — module boundaries and where to place new library screens/data
- `.planning/codebase/CONVENTIONS.md` — Compose/Koin/Room patterns to match

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/ImportModelScreen.kt`: demonstrates a picker flow (`ACTION_OPEN_DOCUMENT`) and clean user error via `createAlertDialog`.
- `app/src/main/java/io/shubham0204/smollmandroid/ui/components/createAlertDialog`: reusable UX pattern for “reject cleanly with a clear error”.

### Established Patterns
- Compose screens live under `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/*` with ViewModels using Koin + StateFlow.
- Persistence uses Room in `app/src/main/java/io/shubham0204/smollmandroid/data/*` (Phase 1 should follow the same style for a document library table).

### Integration Points
- App-level navigation/entry routing exists in `app/src/main/java/io/shubham0204/smollmandroid/MainActivity.kt`.
- Share-to-app intent handling exists for chat in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatActivity.kt` and `app/src/main/AndroidManifest.xml` (add a parallel PDF share path for document import).

</code_context>

<specifics>
## Specific Ideas

- Copy imported PDFs into app-private storage immediately, so the app works offline after import.
- Phase 1 should not show “indexed” placeholders; only show indexing UI once real indexing exists.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 01-privacy-document-library-skeleton*
*Context gathered: 2026-03-20*
