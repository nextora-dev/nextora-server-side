# Nextora Kuppi Module API Documentation

## Overview

The Kuppi Module provides endpoints for managing peer-to-peer learning sessions and educational notes. It supports three types of users with different permission levels:

- **Normal Students** - Can view sessions, search, and download notes
- **Kuppi Students** - Can create, manage their own sessions and upload notes
- **Admin/Super Admin** - Can moderate all content and view platform statistics

**Base URL:** `http://localhost:8080/api/v1`

---

## Table of Contents

1. [Authentication](#authentication)
2. [Role-Based Access](#role-based-access)
3. [Kuppi Sessions Controller](#1-kuppi-sessions-controller)
4. [Kuppi Notes Controller](#2-kuppi-notes-controller)
5. [Kuppi Admin Controller](#3-kuppi-admin-controller)
6. [Quick Reference](#quick-reference)
7. [Postman Collection](#postman-collection)

---

## Authentication

All requests require JWT Bearer token authentication:

```
Authorization: Bearer <your_jwt_token>
```

---

## Role-Based Access

### Permission Matrix

| Permission | Student | Kuppi Student | Academic Staff | Admin | Super Admin |
|------------|---------|---------------|----------------|-------|-------------|
| `KUPPI:READ` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `KUPPI:CREATE` | ❌ | ✅ | ❌ | ✅ | ✅ |
| `KUPPI:UPDATE` | ❌ | ✅ | ❌ | ✅ | ✅ |
| `KUPPI:DELETE` | ❌ | ✅ | ❌ | ✅ | ✅ |
| `KUPPI:CANCEL` | ❌ | ✅ | ❌ | ✅ | ✅ |
| `KUPPI:RESCHEDULE` | ❌ | ✅ | ❌ | ✅ | ✅ |
| `KUPPI:APPROVE` | ❌ | ❌ | ✅ | ✅ | ✅ |
| `KUPPI:VIEW_ANALYTICS` | ❌ | ✅ | ❌ | ✅ | ✅ |
| `KUPPI:VIEW_STATS` | ❌ | ❌ | ❌ | ✅ | ✅ |
| `KUPPI:ADMIN_UPDATE` | ❌ | ❌ | ❌ | ✅ | ✅ |
| `KUPPI:ADMIN_DELETE` | ❌ | ❌ | ❌ | ✅ | ✅ |
| `KUPPI:PERMANENT_DELETE` | ❌ | ❌ | ❌ | ❌ | ✅ |
| `KUPPI_NOTE:READ` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `KUPPI_NOTE:CREATE` | ❌ | ✅ | ❌ | ✅ | ✅ |
| `KUPPI_NOTE:UPDATE` | ❌ | ✅ | ❌ | ✅ | ✅ |
| `KUPPI_NOTE:DELETE` | ❌ | ✅ | ❌ | ✅ | ✅ |
| `KUPPI_NOTE:DOWNLOAD` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `KUPPI_NOTE:SEARCH` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `KUPPI_NOTE:ADMIN_UPDATE` | ❌ | ❌ | ❌ | ✅ | ✅ |
| `KUPPI_NOTE:ADMIN_DELETE` | ❌ | ❌ | ❌ | ✅ | ✅ |
| `KUPPI_NOTE:PERMANENT_DELETE` | ❌ | ❌ | ❌ | ❌ | ✅ |

---

## 1. Kuppi Sessions Controller

**Base Path:** `/api/v1/kuppi/sessions`

### 1.1 Get All Sessions

View all approved Kuppi sessions with pagination.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions` |
| **Permission** | `KUPPI:READ` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions?page=0&size=10&sortBy=scheduledStartTime&sortDirection=ASC" \
  -H "Authorization: Bearer <token>"
```

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number (0-indexed) |
| size | int | 10 | Items per page |
| sortBy | string | scheduledStartTime | Sort field |
| sortDirection | string | ASC | Sort direction (ASC/DESC) |

---

### 1.2 Get Session by ID

View a specific Kuppi session by ID.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/{sessionId}` |
| **Permission** | `KUPPI:READ` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/1" \
  -H "Authorization: Bearer <token>"
```

---

### 1.3 Search Sessions

Search sessions by keyword (title, description, subject).

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/search` |
| **Permission** | `KUPPI:READ` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/search?keyword=programming&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 1.4 Search by Subject

Search sessions by subject name.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/search/subject` |
| **Permission** | `KUPPI:READ` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/search/subject?subject=Data%20Structures&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 1.5 Search by Host

Search sessions by host/lecturer name.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/search/host` |
| **Permission** | `KUPPI:READ` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/search/host?hostName=John&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 1.6 Search by Date Range

Search sessions by date range.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/search/date` |
| **Permission** | `KUPPI:READ` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/search/date?startDate=2026-02-01T00:00:00&endDate=2026-02-28T23:59:59&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 1.7 Get Upcoming Sessions

Get all upcoming Kuppi sessions.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/upcoming` |
| **Permission** | `KUPPI:READ` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/upcoming?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 1.8 Create Session

Create a new Kuppi session.

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/v1/kuppi/sessions` |
| **Permission** | `KUPPI:CREATE` |
| **Roles** | Kuppi Student, Admin, Super Admin |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/kuppi/sessions" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Data Structures - Binary Trees",
    "description": "Learn about binary trees, traversals, and operations",
    "subject": "Data Structures",
    "scheduledStartTime": "2026-02-25T14:00:00",
    "scheduledEndTime": "2026-02-25T16:00:00",
    "maxParticipants": 30,
    "location": "Online - Zoom",
    "meetingLink": "https://zoom.us/j/123456789"
  }'
```

**Request Body:**
```json
{
  "title": "Data Structures - Binary Trees",
  "description": "Learn about binary trees, traversals, and operations",
  "subject": "Data Structures",
  "scheduledStartTime": "2026-02-25T14:00:00",
  "scheduledEndTime": "2026-02-25T16:00:00",
  "maxParticipants": 30,
  "location": "Online - Zoom",
  "meetingLink": "https://zoom.us/j/123456789"
}
```

---

### 1.9 Update Session

Update own Kuppi session.

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/kuppi/sessions/{sessionId}` |
| **Permission** | `KUPPI:UPDATE` |
| **Roles** | Kuppi Student (own session only), Admin, Super Admin |

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/kuppi/sessions/1" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Session Title",
    "description": "Updated description",
    "maxParticipants": 40
  }'
```

---

### 1.10 Cancel Session

Cancel own Kuppi session with optional reason.

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/v1/kuppi/sessions/{sessionId}/cancel` |
| **Permission** | `KUPPI:CANCEL` |
| **Roles** | Kuppi Student (own session only), Admin, Super Admin |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/kuppi/sessions/1/cancel?reason=Schedule%20conflict" \
  -H "Authorization: Bearer <token>"
```

---

### 1.11 Reschedule Session

Reschedule own Kuppi session.

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/v1/kuppi/sessions/{sessionId}/reschedule` |
| **Permission** | `KUPPI:RESCHEDULE` |
| **Roles** | Kuppi Student (own session only), Admin, Super Admin |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/kuppi/sessions/1/reschedule?newStartTime=2026-02-26T14:00:00&newEndTime=2026-02-26T16:00:00" \
  -H "Authorization: Bearer <token>"
```

---

### 1.12 Delete Session (Soft Delete)

Delete own Kuppi session (soft delete).

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/kuppi/sessions/{sessionId}` |
| **Permission** | `KUPPI:DELETE` |
| **Roles** | Kuppi Student (own session only), Admin, Super Admin |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/kuppi/sessions/1" \
  -H "Authorization: Bearer <token>"
```

---

### 1.13 Get My Sessions

Get own created sessions.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/my` |
| **Permission** | `KUPPI:READ` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/my?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 1.14 Get My Analytics

Get analytics for own sessions and notes.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/my/analytics` |
| **Permission** | `KUPPI:VIEW_ANALYTICS` |
| **Roles** | Kuppi Student, Admin, Super Admin |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/my/analytics" \
  -H "Authorization: Bearer <token>"
```

---

## 2. Kuppi Notes Controller

**Base Path:** `/api/v1/kuppi/notes`

### 2.1 Get All Notes

View all approved notes with pagination.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes` |
| **Permission** | `KUPPI_NOTE:READ` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 2.2 Get Notes for Session

View notes for a specific session.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes/session/{sessionId}` |
| **Permission** | `KUPPI_NOTE:READ` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes/session/1" \
  -H "Authorization: Bearer <token>"
```

---

### 2.3 Get Note by ID

View a specific note.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes/{noteId}` |
| **Permission** | `KUPPI_NOTE:READ` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes/1" \
  -H "Authorization: Bearer <token>"
```

---

### 2.4 Download Note (Record Download)

Record a note download (if download is allowed).

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes/{noteId}/download` |
| **Permission** | `KUPPI_NOTE:DOWNLOAD` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes/1/download" \
  -H "Authorization: Bearer <token>"
```

---

### 2.5 Download Note File

Download the actual note file from S3.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes/{noteId}/download/file` |
| **Permission** | `KUPPI_NOTE:DOWNLOAD` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes/1/download/file" \
  -H "Authorization: Bearer <token>" \
  --output downloaded_note.pdf
```

---

### 2.6 Search Notes

Search notes by keyword.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes/search` |
| **Permission** | `KUPPI_NOTE:SEARCH` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes/search?keyword=algorithms&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 2.7 Upload Note with File

Upload a new note with file to S3.

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/v1/kuppi/notes/upload` |
| **Content-Type** | `multipart/form-data` |
| **Permission** | `KUPPI_NOTE:CREATE` |
| **Roles** | Kuppi Student, Admin, Super Admin |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/kuppi/notes/upload" \
  -H "Authorization: Bearer <token>" \
  -F "title=Data Structures Notes" \
  -F "description=Complete notes on binary trees" \
  -F "sessionId=1" \
  -F "allowDownload=true" \
  -F "file=@/path/to/notes.pdf"
```

**Form Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| title | string | Yes | Note title |
| description | string | No | Note description |
| sessionId | long | No | Associated session ID |
| allowDownload | boolean | No | Allow download (default: true) |
| file | file | Yes | Note file (PDF, DOC, etc.) |

---

### 2.8 Update Note with File

Update own note with optional new file.

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/kuppi/notes/{noteId}/upload` |
| **Content-Type** | `multipart/form-data` |
| **Permission** | `KUPPI_NOTE:UPDATE` |
| **Roles** | Kuppi Student (own note only), Admin, Super Admin |

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/kuppi/notes/1/upload" \
  -H "Authorization: Bearer <token>" \
  -F "title=Updated Note Title" \
  -F "description=Updated description" \
  -F "allowDownload=true" \
  -F "file=@/path/to/updated_notes.pdf"
```

---

### 2.9 Delete Note

Delete own note (soft delete).

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/kuppi/notes/{noteId}` |
| **Permission** | `KUPPI_NOTE:DELETE` |
| **Roles** | Kuppi Student (own note only), Admin, Super Admin |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/kuppi/notes/1" \
  -H "Authorization: Bearer <token>"
```

---

### 2.10 Get My Notes

Get own uploaded notes.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes/my` |
| **Permission** | `KUPPI_NOTE:READ` |
| **Roles** | All authenticated users |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes/my?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

## 3. Kuppi Admin Controller

**Base Path:** `/api/v1/admin/kuppi`

### 3.1 Admin Update Session

Edit any Kuppi session (admin override).

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/admin/kuppi/sessions/{sessionId}` |
| **Permission** | `KUPPI:ADMIN_UPDATE` |
| **Roles** | Admin, Super Admin |

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/admin/kuppi/sessions/1" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Admin Updated Title",
    "description": "Admin updated description"
  }'
```

---

### 3.2 Admin Delete Session (Soft Delete)

Soft delete any Kuppi session.

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/admin/kuppi/sessions/{sessionId}` |
| **Permission** | `KUPPI:ADMIN_DELETE` |
| **Roles** | Admin, Super Admin |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/admin/kuppi/sessions/1" \
  -H "Authorization: Bearer <token>"
```

---

### 3.3 Permanently Delete Session ⚠️

Permanently delete a Kuppi session from the database.

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/admin/kuppi/sessions/{sessionId}/permanent` |
| **Permission** | `KUPPI:PERMANENT_DELETE` |
| **Roles** | Super Admin only |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/admin/kuppi/sessions/1/permanent" \
  -H "Authorization: Bearer <token>"
```

---

### 3.4 Admin Update Note

Edit any note (admin override).

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/admin/kuppi/notes/{noteId}` |
| **Permission** | `KUPPI_NOTE:ADMIN_UPDATE` |
| **Roles** | Admin, Super Admin |

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/admin/kuppi/notes/1" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Admin Updated Note Title",
    "description": "Admin updated description",
    "allowDownload": false
  }'
```

---

### 3.5 Admin Delete Note (Soft Delete)

Soft delete any note.

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/admin/kuppi/notes/{noteId}` |
| **Permission** | `KUPPI_NOTE:ADMIN_DELETE` |
| **Roles** | Admin, Super Admin |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/admin/kuppi/notes/1" \
  -H "Authorization: Bearer <token>"
```

---

### 3.6 Permanently Delete Note ⚠️

Permanently delete a note from the database.

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/admin/kuppi/notes/{noteId}/permanent` |
| **Permission** | `KUPPI_NOTE:PERMANENT_DELETE` |
| **Roles** | Super Admin only |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/admin/kuppi/notes/1/permanent" \
  -H "Authorization: Bearer <token>"
```

---

### 3.7 Get Platform Statistics

Get Kuppi platform usage statistics.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/stats` |
| **Permission** | `KUPPI:VIEW_STATS` |
| **Roles** | Admin, Super Admin |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/stats" \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
{
  "success": true,
  "message": "Platform statistics retrieved successfully",
  "data": {
    "totalSessions": 150,
    "activeSessions": 45,
    "completedSessions": 100,
    "cancelledSessions": 5,
    "totalNotes": 320,
    "totalDownloads": 1500,
    "totalParticipants": 2500,
    "topSubjects": [
      { "subject": "Data Structures", "sessionCount": 25 },
      { "subject": "Algorithms", "sessionCount": 20 }
    ]
  }
}
```

---

## Quick Reference

### All Kuppi Endpoints Summary

| # | Method | Endpoint | Permission | Roles |
|---|--------|----------|------------|-------|
| **Kuppi Sessions** |||||
| 1 | GET | `/api/v1/kuppi/sessions` | KUPPI:READ | All |
| 2 | GET | `/api/v1/kuppi/sessions/{sessionId}` | KUPPI:READ | All |
| 3 | GET | `/api/v1/kuppi/sessions/search` | KUPPI:READ | All |
| 4 | GET | `/api/v1/kuppi/sessions/search/subject` | KUPPI:READ | All |
| 5 | GET | `/api/v1/kuppi/sessions/search/host` | KUPPI:READ | All |
| 6 | GET | `/api/v1/kuppi/sessions/search/date` | KUPPI:READ | All |
| 7 | GET | `/api/v1/kuppi/sessions/upcoming` | KUPPI:READ | All |
| 8 | POST | `/api/v1/kuppi/sessions` | KUPPI:CREATE | Kuppi Student, Admin |
| 9 | PUT | `/api/v1/kuppi/sessions/{sessionId}` | KUPPI:UPDATE | Kuppi Student, Admin |
| 10 | POST | `/api/v1/kuppi/sessions/{sessionId}/cancel` | KUPPI:CANCEL | Kuppi Student, Admin |
| 11 | POST | `/api/v1/kuppi/sessions/{sessionId}/reschedule` | KUPPI:RESCHEDULE | Kuppi Student, Admin |
| 12 | DELETE | `/api/v1/kuppi/sessions/{sessionId}` | KUPPI:DELETE | Kuppi Student, Admin |
| 13 | GET | `/api/v1/kuppi/sessions/my` | KUPPI:READ | All |
| 14 | GET | `/api/v1/kuppi/sessions/my/analytics` | KUPPI:VIEW_ANALYTICS | Kuppi Student, Admin |
| **Kuppi Notes** |||||
| 15 | GET | `/api/v1/kuppi/notes` | KUPPI_NOTE:READ | All |
| 16 | GET | `/api/v1/kuppi/notes/session/{sessionId}` | KUPPI_NOTE:READ | All |
| 17 | GET | `/api/v1/kuppi/notes/{noteId}` | KUPPI_NOTE:READ | All |
| 18 | GET | `/api/v1/kuppi/notes/{noteId}/download` | KUPPI_NOTE:DOWNLOAD | All |
| 19 | GET | `/api/v1/kuppi/notes/{noteId}/download/file` | KUPPI_NOTE:DOWNLOAD | All |
| 20 | GET | `/api/v1/kuppi/notes/search` | KUPPI_NOTE:SEARCH | All |
| 21 | POST | `/api/v1/kuppi/notes/upload` | KUPPI_NOTE:CREATE | Kuppi Student, Admin |
| 22 | PUT | `/api/v1/kuppi/notes/{noteId}/upload` | KUPPI_NOTE:UPDATE | Kuppi Student, Admin |
| 23 | DELETE | `/api/v1/kuppi/notes/{noteId}` | KUPPI_NOTE:DELETE | Kuppi Student, Admin |
| 24 | GET | `/api/v1/kuppi/notes/my` | KUPPI_NOTE:READ | All |
| **Kuppi Admin** |||||
| 25 | PUT | `/api/v1/admin/kuppi/sessions/{sessionId}` | KUPPI:ADMIN_UPDATE | Admin, Super Admin |
| 26 | DELETE | `/api/v1/admin/kuppi/sessions/{sessionId}` | KUPPI:ADMIN_DELETE | Admin, Super Admin |
| 27 | DELETE | `/api/v1/admin/kuppi/sessions/{sessionId}/permanent` | KUPPI:PERMANENT_DELETE | Super Admin |
| 28 | PUT | `/api/v1/admin/kuppi/notes/{noteId}` | KUPPI_NOTE:ADMIN_UPDATE | Admin, Super Admin |
| 29 | DELETE | `/api/v1/admin/kuppi/notes/{noteId}` | KUPPI_NOTE:ADMIN_DELETE | Admin, Super Admin |
| 30 | DELETE | `/api/v1/admin/kuppi/notes/{noteId}/permanent` | KUPPI_NOTE:PERMANENT_DELETE | Super Admin |
| 31 | GET | `/api/v1/admin/kuppi/stats` | KUPPI:VIEW_STATS | Admin, Super Admin |

**Total Endpoints: 31**

---

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "Validation error message",
  "errors": ["Field-specific errors"],
  "timestamp": "2026-02-19T10:30:00"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Unauthorized - Invalid or expired token",
  "timestamp": "2026-02-19T10:30:00"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied - Insufficient permissions",
  "timestamp": "2026-02-19T10:30:00"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Session/Note not found",
  "timestamp": "2026-02-19T10:30:00"
}
```

---

*Documentation generated on: February 19, 2026*

