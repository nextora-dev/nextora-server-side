# Kuppi Session File Endpoints

This document describes the request and response contracts (examples, validations, and notes) for the file-related session endpoints implemented in the Kuppi module.

Endpoints covered:
- POST /api/v1/kuppi/sessions  — createSessionwithFile (create session + 0..N files)
- PUT  /api/v1/kuppi/sessions/{sessionId}/upload — updateSessionWithFile (update session + add/replace files)
- DELETE /api/v1/kuppi/sessions/{sessionId}/soft-with-files — softDeleteSessionWithFiles (soft-delete session and remove files)
- DELETE /api/v1/kuppi/sessions/{sessionId}/permanent-with-files — permanentlyDeleteSessionWithFiles (permanently delete session and files)

Common notes
------------
- Base path: `/api/v1/kuppi/sessions`
- Authentication: Bearer token required. Authorization rules are noted per endpoint.
- File storage: Files are uploaded to S3 under `kuppi-sessions/`. The service stores per-file notes (`KuppiNote`) for every uploaded file and may store a session-level `fileUrl` (single URL or JSON array string) in the `KuppiSession` entity.
- Accepted file types: PDF, PowerPoint (ppt / pptx / presentation), images (image/*). The server validates by content-type.
- Multipart form param names supported:
  - `files` (file[])
  - `files[]` (file[])
  - `file` (single file convenience)

1) Create session with file(s)
-----------------------------
Summary: Create a new Kuppi session and optionally attach one or more files. Each uploaded file becomes a `KuppiNote` attached to the session.

HTTP
- Method: POST
- URL: /api/v1/kuppi/sessions
- Auth: requires authority `KUPPI:CREATE`
- Content-Type: multipart/form-data

Form parameters (multipart)
- title (string, required, max 200)
- subject (string, required, max 100)
- scheduledStartTime (ISO-8601 datetime, required)
- scheduledEndTime (ISO-8601 datetime, required)
- liveLink (string, required)
- description (string, optional)
- meetingPlatform (string, optional)
- files / files[] (file[], optional) — multiple allowed
- file (file, optional) — alias for single file

Server-side validations (important)
- scheduledStartTime must be before scheduledEndTime and (for creation) in the future
- Each file content-type must be one of: application/pdf, application/vnd.ms-powerpoint, application/vnd.openxmlformats..., image/*

Successful response
- Status: 201 Created
- Body: ApiResponse with data containing `KuppiSessionResponse` structure

Example successful body (trimmed):
```
{ "success": true, "message": "Session created successfully", "data": {
  "id": 67,
  "title": "Calculus tutoring",
  "subject": "Calculus",
  "scheduledStartTime": "2026-03-10T10:00:00",
  "scheduledEndTime": "2026-03-10T11:00:00",
  "liveLink": "https://meet.example/abc",
  "host": { "id": 15, "firstName": "Haritha", ... },
  "notes": [
    { "id": 101, "title": "notes.pdf", "fileType": "PDF", "fileUrl": "https://.../kuppi-sessions/abc.pdf", "fileName": "notes.pdf", "fileSize": 12345, ... },
    { "id": 102, "title": "slides.pptx", "fileType": "SLIDES", "fileUrl": "https://.../kuppi-sessions/def.pptx", ... }
  ]
}}
```

Example curl (multiple files)
```
curl -X POST "https://api.example.com/api/v1/kuppi/sessions" \
  -H "Authorization: Bearer <token>" \
  -F "title=Calculus tutoring" \
  -F "subject=Calculus" \
  -F "scheduledStartTime=2026-03-10T10:00:00" \
  -F "scheduledEndTime=2026-03-10T11:00:00" \
  -F "liveLink=https://meet.example/abc" \
  -F "files=@/path/to/file1.pdf" \
  -F "files=@/path/to/file2.png" \
  -F "files=@/path/to/file3.pptx"
```

Errors
- 400 Bad Request — validation failure (schedule or invalid file type)
- 401 Unauthorized — missing/invalid token
- 403 Forbidden — missing permission
- 500 Internal Server Error — S3 issues (server attempts cleanup on partial failures)

Behavioral notes
- The server creates one `KuppiNote` per uploaded file and attaches it to the session; the returned `data.notes` lists those notes.
- If multiple files provided, the session `fileUrl` may be stored as a JSON array string (pragmatic compatibility). Prefer a normalized table for production.
- If any upload fails during processing, the service attempts to delete already-uploaded S3 objects to avoid orphans and then returns an error.

2) Update session and add/replace files
--------------------------------------
Summary: Update session fields and optionally upload new files. You can provide an optional list `removeNoteIds` to selectively remove existing notes before adding new files. If `removeNoteIds` is omitted and new files are provided, the service removes all existing notes for the session and replaces with new ones.

HTTP
- Method: PUT
- URL: /api/v1/kuppi/sessions/{sessionId}/upload
- Auth: requires authority `KUPPI:UPDATE` and session ownership is validated by the service
- Content-Type: multipart/form-data

Path parameter
- sessionId (long, required)

Form parameters (multipart)
- Optional session fields (title, description, subject, scheduledStartTime, scheduledEndTime, liveLink, meetingPlatform)
- files / files[] (file[], optional) — new files to add
- file (file, optional)
- removeNoteIds (optional repeated param or CSV depending on client) — list of note IDs to remove

Behavior
- If `removeNoteIds` provided: only those notes are soft-deleted (and their S3 files removed) before uploading new files. Other notes remain.
- If `removeNoteIds` is not provided and `files` present: all existing notes for the session are soft-deleted (and their S3 files removed) and replaced by newly uploaded notes.
- New files are uploaded to S3 (keys are tracked); if an error occurs mid-processing, uploaded keys are deleted (best-effort) and the request fails.
- After upload each file becomes a `KuppiNote` attached to the session; the response returns the updated session with notes.

Successful response
- Status: 200 OK
- Body: ApiResponse with `KuppiSessionResponse` (updated session and notes array)

Example curl (replace and add files, removing note id 101):
```
curl -X PUT "https://api.example.com/api/v1/kuppi/sessions/67/upload" \
  -H "Authorization: Bearer <token>" \
  -F "title=Updated Title" \
  -F "removeNoteIds=101" \
  -F "files=@/path/to/new1.pdf" \
  -F "files=@/path/to/new2.png"
```

Errors
- 400 Bad Request — schedule validation or invalid file type
- 401 Unauthorized — not logged in
- 403 Forbidden — not owner or missing permission
- 404 Not Found — session not found
- 500 Internal Server Error — S3 or DB issues

3) Soft-delete session and remove files
--------------------------------------
Summary: Soft-delete the session (set deleted flag) and soft-delete its notes; remove note files from S3 (best-effort). Only the session owner may call this endpoint.

HTTP
- Method: DELETE
- URL: /api/v1/kuppi/sessions/{sessionId}/soft-with-files
- Auth: requires authority `KUPPI:DELETE` and owner

Behavior
- Validates session ownership. Prevents soft-deleting LIVE sessions.
- Finds all non-deleted notes for the session, attempts to delete their S3 objects (best-effort), soft-deletes each note, then soft-deletes the session.
- Returns 204 No Content on success.

Success
- Status: 204 No Content
- Body: empty

Errors
- 400 Bad Request — cannot delete live session
- 401 / 403 / 404 / 500 as appropriate

Example curl
```
curl -X DELETE "https://api.example.com/api/v1/kuppi/sessions/67/soft-with-files" \
  -H "Authorization: Bearer <token>"
```

4) Permanently delete session and files
---------------------------------------
Summary: Permanently remove the session row and all associated notes from the database and delete files from S3. This endpoint is intended for administrators (super-admin or similarly privileged role).

HTTP
- Method: DELETE
- URL: /api/v1/kuppi/sessions/{sessionId}/permanent-with-files
- Auth: requires `SUPER_ADMIN` or `KUPPI:PERMANENT_DELETE` (controller enforces)

Behavior
- Attempts to delete all S3 objects referenced by notes and session-level fileUrl (supports JSON array or single URL). Deletion is best-effort: S3 errors are logged and processing continues.
- Deletes note rows and finally the session row from DB.
- Returns 204 No Content on success.

Success
- Status: 204 No Content

Errors
- 401 / 403 / 404 / 500 as appropriate

Example curl
```
curl -X DELETE "https://api.example.com/api/v1/kuppi/sessions/67/permanent-with-files" \
  -H "Authorization: Bearer <admin-token>"
```

Implementation and production recommendations
--------------------------------------------
- Prefer a normalized table `kuppi_session_files` (or `kuppi_note`) to store each file row; currently we create `KuppiNote` rows for uploaded files and sometimes store a JSON array in `KuppiSession.fileUrl` for compatibility.
- Consider using a post-commit job or message queue to perform S3 deletes after DB commit to ensure stronger consistency and safe retries.
- Add tests:
  - Controller MockMvc tests for multipart create/update (0,1,N files; `files` vs `files[]` variants).
  - Service unit tests mocking `S3Service` to verify upload and cleanup behavior.
- Watch multipart size limits (`spring.servlet.multipart.max-file-size`, `max-request-size`) if clients upload large files; increase in `application.yml` as needed.

Where to find the code
- Controller: `src/main/java/lk/iit/nextora/module/kuppi/controller/KuppiSessionController.java`
- Service interface: `src/main/java/lk/iit/nextora/module/kuppi/service/KuppiSessionService.java`
- Service impl: `src/main/java/lk/iit/nextora/module/kuppi/service/impl/KuppiSessionServiceImpl.java`
- S3 helper: `src/main/java/lk/iit/nextora/config/S3/S3Service.java`
- Note entity: `src/main/java/lk/iit/nextora/module/kuppi/entity/KuppiNote.java`
- Session entity: `src/main/java/lk/iit/nextora/module/kuppi/entity/KuppiSession.java`

If you want, I can:
- Add concrete MockMvc tests that POST 3 files and assert 3 notes are returned.
- Replace the JSON-in-`fileUrl` approach with a proper `kuppi_session_files` table (schema + migration + DTO updates).
- Implement a post-commit cleanup queue for S3 deletions to guarantee eventual consistency.

---

Document generated by the code-maintenance script. If you'd like adjustments to the examples, more fields in example responses, or to include exact DTO field lists, tell me which format you prefer (compact vs. exhaustive) and I will update the document.

