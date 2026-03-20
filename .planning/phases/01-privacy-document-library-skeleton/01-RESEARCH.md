# Phase 1 - Research: Privacy + Document Library Skeleton

## Goal

Plan Phase 1 implementation for InfinoVault's local PDF library baseline:
- Import PDFs via file picker + share-to-app
- Copy PDFs into app-private storage for offline use
- List documents (filename, import date, page count if available) sorted newest-first
- Delete with confirmation (hard delete: PDF + derived data)
- Clean error handling for corrupt/unsupported PDFs
- Clear privacy/offline messaging

## Relevant Android / platform primitives

### File picker (Storage Access Framework)
- Use `Intent.ACTION_OPEN_DOCUMENT` (or `ActivityResultContracts.OpenDocument`) to pick a PDF.
- Persist access only long enough to copy into app-private storage; after copy, the app can run offline without holding URI grants.
- Implementation typically uses `ContentResolver.openInputStream(uri)` and streams to a file in `context.filesDir`.

Codebase reference pattern:
- Existing picker flow for GGUF import in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/model_download/ImportModelScreen.kt`.

### Share-to-app
- Add an `intent-filter` for `ACTION_SEND` with `application/pdf` (and optionally `application/octet-stream` with extension checks) to an Activity.
- Existing share-to-app pattern exists for `text/plain` in `app/src/main/AndroidManifest.xml` and handling in `app/src/main/java/io/shubham0204/smollmandroid/ui/screens/chat/ChatActivity.kt`.

### App-private storage
- Store PDFs under app-private directory (e.g., `context.filesDir` or `context.noBackupFilesDir`).
- Record canonical path + metadata in Room.
- Avoid using public Downloads for documents (privacy-first).

### Page count
- For some PDFs, page count can be derived using `android.graphics.pdf.PdfRenderer` with a `ParcelFileDescriptor`.
- Not all PDFs may be readable; treat page count as optional (nullable) and compute best-effort.

## Data model considerations (Phase 1)

- Room table for `Document` / `PdfDocument`:
  - id (UUID or auto)
  - displayName (original filename)
  - importedAt (epoch millis)
  - pageCount (nullable int)
  - storedPath (app-private file path)
  - fileSizeBytes (optional)

- Repository API should support:
  - `importFromUri(uri, source)` (copies file, extracts metadata)
  - `listDocuments()` sorted newest-first
  - `deleteDocument(documentId)` (hard delete contract: also deletes derived data later)

## UX notes aligned with decisions

- Do not show indexing placeholders in Phase 1.
- Deletion must be available from:
  - Library list
  - Document details screen (future "citation/source UI" landing place)

## Validation Architecture

We can validate Phase 1 with a mix of automated and manual checks.

Automated (repo already has test infrastructure):
- Unit tests: `./gradlew :app:testDebugUnitTest`
- Static checks: `./gradlew :app:lintDebug` (optional but useful)

Manual (must for SAF/share intents):
- Import a PDF via picker and via share-to-app; confirm it is copied into app-private storage and remains available offline.
- Delete from library list and from document details screen; confirm PDF removed and derived-data purge hooks invoked.
- Attempt corrupt/unsupported PDF; confirm a clear error UI.

