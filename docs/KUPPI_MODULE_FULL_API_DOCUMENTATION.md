# Kuppi Module - Complete API Documentation

**Base URL:** `http://localhost:8080/api/v1`  
**Version:** 1.0.0  
**Last Updated:** February 19, 2026

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Session Endpoints](#1-session-endpoints)
4. [Notes Endpoints](#2-notes-endpoints)
5. [Application Endpoints](#3-application-endpoints)
6. [Admin Endpoints](#4-admin-endpoints)
7. [Super Admin Endpoints](#5-super-admin-endpoints)
8. [DTOs Reference](#6-dtos-reference)
9. [Error Responses](#7-error-responses)

---

## Overview

The Kuppi Module provides peer-to-peer learning functionality where students can:
- **Normal Students**: View sessions, download notes, apply to become Kuppi Students
- **Kuppi Students**: Create sessions, upload notes, view analytics
- **Admin/Academic Staff**: Manage all sessions, notes, approve/reject applications
- **Super Admin**: Permanent delete, revoke roles

### Role Hierarchy
```
ROLE_SUPER_ADMIN > ROLE_ADMIN > ROLE_ACADEMIC_STAFF > ROLE_STUDENT (KUPPI_STUDENT) > ROLE_STUDENT (NORMAL)
```

---

## Authentication

All endpoints require JWT Bearer token authentication.

**Header:**
```
Authorization: Bearer <access_token>
```

---

## 1. Session Endpoints

**Base Path:** `/api/v1/kuppi/sessions`

### 1.1 Get All Sessions

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions` |
| **Permission** | `KUPPI:READ` |
| **Roles** | All Students |

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number (0-indexed) |
| size | int | 10 | Items per page |
| sortBy | string | scheduledStartTime | Sort field |
| sortDirection | string | ASC | ASC or DESC |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions?page=0&size=10&sortBy=scheduledStartTime&sortDirection=ASC" \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
{
  "success": true,
  "message": "Sessions retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Data Structures - Binary Trees",
        "description": "Understanding binary trees and traversals",
        "subject": "Data Structures",
        "sessionType": "LIVE",
        "status": "SCHEDULED",
        "scheduledStartTime": "2026-02-20T14:00:00",
        "scheduledEndTime": "2026-02-20T16:00:00",
        "liveLink": "https://meet.google.com/abc-xyz-123",
        "meetingPlatform": "Google Meet",
        "viewCount": 45,
        "hostId": 123,
        "hostName": "John Doe",
        "hostEmail": "john@example.com",
        "createdAt": "2026-02-18T10:00:00",
        "updatedAt": "2026-02-18T10:00:00",
        "isActive": true,
        "canJoin": true,
        "notes": []
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false,
    "empty": false
  }
}
```

---

### 1.2 Get Session by ID

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/{sessionId}` |
| **Permission** | `KUPPI:READ` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/1" \
  -H "Authorization: Bearer <token>"
```

**Response:** Same as single session object above.

---

### 1.3 Search Sessions

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/search` |
| **Permission** | `KUPPI:READ` |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| keyword | string | Yes | Search term |
| page | int | No | Page number |
| size | int | No | Items per page |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/search?keyword=algorithms&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 1.4 Search by Subject

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/search/subject` |
| **Permission** | `KUPPI:READ` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/search/subject?subject=Data%20Structures&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 1.5 Search by Host

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/search/host` |
| **Permission** | `KUPPI:READ` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/search/host?hostName=John&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 1.6 Search by Date Range

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/search/date` |
| **Permission** | `KUPPI:READ` |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| startDate | datetime | Yes | ISO format (2026-02-20T00:00:00) |
| endDate | datetime | Yes | ISO format (2026-02-28T23:59:59) |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/search/date?startDate=2026-02-20T00:00:00&endDate=2026-02-28T23:59:59&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 1.7 Get Upcoming Sessions

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/upcoming` |
| **Permission** | `KUPPI:READ` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/upcoming?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 1.8 Create Session (Kuppi Students Only)

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/v1/kuppi/sessions` |
| **Permission** | `KUPPI:CREATE` |
| **Roles** | Kuppi Students only |

**Request Body:**
```json
{
  "title": "Data Structures - Binary Trees",
  "description": "Understanding binary trees, traversals (inorder, preorder, postorder), and basic operations",
  "subject": "Data Structures",
  "scheduledStartTime": "2026-02-25T14:00:00",
  "scheduledEndTime": "2026-02-25T16:00:00",
  "liveLink": "https://meet.google.com/abc-xyz-123",
  "meetingPlatform": "Google Meet"
}
```

**Validation Rules:**
| Field | Rules |
|-------|-------|
| title | Required, max 200 chars |
| description | Optional, max 2000 chars |
| subject | Required, max 100 chars |
| scheduledStartTime | Required, must be future |
| scheduledEndTime | Required, must be future |
| liveLink | Required, max 500 chars |
| meetingPlatform | Optional, max 200 chars |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/kuppi/sessions" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Data Structures - Binary Trees",
    "description": "Understanding binary trees and traversals",
    "subject": "Data Structures",
    "scheduledStartTime": "2026-02-25T14:00:00",
    "scheduledEndTime": "2026-02-25T16:00:00",
    "liveLink": "https://meet.google.com/abc-xyz-123",
    "meetingPlatform": "Google Meet"
  }'
```

**Response:** `201 Created` with session object.

---

### 1.9 Update Session

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/kuppi/sessions/{sessionId}` |
| **Permission** | `KUPPI:UPDATE` |

**Request Body:** (All fields optional)
```json
{
  "title": "Updated Title",
  "description": "Updated description",
  "subject": "Updated Subject",
  "scheduledStartTime": "2026-02-25T15:00:00",
  "scheduledEndTime": "2026-02-25T17:00:00",
  "liveLink": "https://meet.google.com/new-link",
  "meetingPlatform": "Zoom"
}
```

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/kuppi/sessions/1" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title",
    "liveLink": "https://meet.google.com/new-link"
  }'
```

---

### 1.10 Cancel Session

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/v1/kuppi/sessions/{sessionId}/cancel` |
| **Permission** | `KUPPI:CANCEL` |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/kuppi/sessions/1/cancel?reason=Unable%20to%20host%20due%20to%20schedule%20conflict" \
  -H "Authorization: Bearer <token>"
```

---

### 1.11 Reschedule Session

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/v1/kuppi/sessions/{sessionId}/reschedule` |
| **Permission** | `KUPPI:RESCHEDULE` |

**Query Parameters:**
| Parameter | Type | Required |
|-----------|------|----------|
| newStartTime | datetime | Yes |
| newEndTime | datetime | Yes |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/kuppi/sessions/1/reschedule?newStartTime=2026-02-26T14:00:00&newEndTime=2026-02-26T16:00:00" \
  -H "Authorization: Bearer <token>"
```

---

### 1.12 Delete Session (Soft Delete)

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/kuppi/sessions/{sessionId}` |
| **Permission** | `KUPPI:DELETE` |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/kuppi/sessions/1" \
  -H "Authorization: Bearer <token>"
```

---

### 1.13 Get My Sessions

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/my` |
| **Permission** | `KUPPI:READ` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/my?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 1.14 Get My Analytics

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/sessions/my/analytics` |
| **Permission** | `KUPPI:VIEW_ANALYTICS` |
| **Roles** | Kuppi Students |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/sessions/my/analytics" \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
{
  "success": true,
  "message": "Analytics retrieved successfully",
  "data": {
    "totalSessions": 15,
    "completedSessions": 10,
    "upcomingSessions": 5,
    "totalSessionViews": 450,
    "totalNotes": 25,
    "totalNoteViews": 1200,
    "mostViewedSessionId": 5,
    "mostViewedSessionTitle": "Algorithms - Sorting",
    "mostViewedSessionViews": 120,
    "mostDownloadedNoteId": 12,
    "mostDownloadedNoteTitle": "Sorting Algorithms Notes",
    "mostDownloadedNoteDownloads": 85
  }
}
```

---

## 2. Notes Endpoints

**Base Path:** `/api/v1/kuppi/notes`

### 2.1 Get All Notes

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes` |
| **Permission** | `KUPPI_NOTE:READ` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
{
  "success": true,
  "message": "Notes retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Binary Trees Complete Guide",
        "description": "Comprehensive notes on binary trees",
        "fileType": "PDF",
        "fileUrl": "https://s3.amazonaws.com/bucket/notes/file.pdf",
        "fileName": "binary_trees.pdf",
        "fileSize": 2500000,
        "formattedFileSize": "2.50 MB",
        "allowDownload": true,
        "downloadCount": 45,
        "viewCount": 120,
        "sessionId": 1,
        "sessionTitle": "Data Structures - Binary Trees",
        "uploadedById": 123,
        "uploaderName": "John Doe",
        "createdAt": "2026-02-18T10:00:00",
        "updatedAt": "2026-02-18T10:00:00",
        "isActive": true
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 50,
    "totalPages": 5,
    "first": true,
    "last": false,
    "empty": false
  }
}
```

---

### 2.2 Get Notes for Session

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes/session/{sessionId}` |
| **Permission** | `KUPPI_NOTE:READ` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes/session/1" \
  -H "Authorization: Bearer <token>"
```

---

### 2.3 Get Note by ID

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes/{noteId}` |
| **Permission** | `KUPPI_NOTE:READ` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes/1" \
  -H "Authorization: Bearer <token>"
```

---

### 2.4 Download Note (Record Download)

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes/{noteId}/download` |
| **Permission** | `KUPPI_NOTE:DOWNLOAD` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes/1/download" \
  -H "Authorization: Bearer <token>"
```

---

### 2.5 Download Note File (Actual File)

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes/{noteId}/download/file` |
| **Permission** | `KUPPI_NOTE:DOWNLOAD` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes/1/download/file" \
  -H "Authorization: Bearer <token>" \
  -o downloaded_file.pdf
```

**Response:** Binary file content with headers:
```
Content-Type: application/pdf
Content-Disposition: attachment; filename="binary_trees.pdf"
Content-Length: 2500000
```

---

### 2.6 Search Notes

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes/search` |
| **Permission** | `KUPPI_NOTE:SEARCH` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes/search?keyword=algorithms&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 2.7 Upload Note with File (Kuppi Students)

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/v1/kuppi/notes/upload` |
| **Content-Type** | `multipart/form-data` |
| **Permission** | `KUPPI_NOTE:CREATE` |
| **Roles** | Kuppi Students |

**Form Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| title | string | Yes | Note title (max 200 chars) |
| description | string | No | Note description (max 1000 chars) |
| sessionId | long | No | Associated session ID |
| allowDownload | boolean | No | Allow download (default: true) |
| file | file | Yes | The file to upload |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/kuppi/notes/upload" \
  -H "Authorization: Bearer <token>" \
  -F "title=Binary Trees Complete Guide" \
  -F "description=Comprehensive notes on binary trees" \
  -F "sessionId=1" \
  -F "allowDownload=true" \
  -F "file=@/path/to/binary_trees.pdf"
```

**Response:** `201 Created` with note object.

---

### 2.8 Update Note with File

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/kuppi/notes/{noteId}/upload` |
| **Content-Type** | `multipart/form-data` |
| **Permission** | `KUPPI_NOTE:UPDATE` |

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/kuppi/notes/1/upload" \
  -H "Authorization: Bearer <token>" \
  -F "title=Updated Title" \
  -F "description=Updated description" \
  -F "file=@/path/to/updated_file.pdf"
```

---

### 2.9 Delete Note

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/kuppi/notes/{noteId}` |
| **Permission** | `KUPPI_NOTE:DELETE` |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/kuppi/notes/1" \
  -H "Authorization: Bearer <token>"
```

---

### 2.10 Get My Notes

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/notes/my` |
| **Permission** | `KUPPI_NOTE:READ` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/notes/my?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

## 3. Application Endpoints

**Base Path:** `/api/v1/kuppi/applications`

### 3.1 Submit Application

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/v1/kuppi/applications` |
| **Permission** | `KUPPI_APPLICATION:SUBMIT` |
| **Roles** | Normal Students only |

**Request Body:**
```json
{
  "motivation": "I want to help fellow students understand complex programming concepts. I have strong knowledge in data structures and algorithms and enjoy teaching. I believe peer-to-peer learning is very effective and I want to contribute to the learning community.",
  "relevantExperience": "Tutored junior students for 2 semesters, conducted study groups for exam preparation",
  "subjectsToTeach": ["Data Structures", "Algorithms", "Java Programming", "Object Oriented Programming"],
  "preferredExperienceLevel": "INTERMEDIATE",
  "availability": "Weekday evenings 6-9 PM, Weekend mornings 9 AM - 12 PM",
  "currentGpa": 3.75,
  "currentSemester": "Year 3 Semester 1"
}
```

**Validation Rules:**
| Field | Rules |
|-------|-------|
| motivation | Required, 50-1000 chars |
| relevantExperience | Optional, max 500 chars |
| subjectsToTeach | Required, 1-10 items |
| preferredExperienceLevel | Required, BEGINNER/INTERMEDIATE/ADVANCED |
| availability | Optional, max 500 chars |
| currentGpa | Required, 0.0-4.0 |
| currentSemester | Required, max 50 chars |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/kuppi/applications" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "motivation": "I want to help fellow students understand complex programming concepts...",
    "relevantExperience": "Tutored junior students for 2 semesters",
    "subjectsToTeach": ["Data Structures", "Algorithms"],
    "preferredExperienceLevel": "INTERMEDIATE",
    "availability": "Weekday evenings 6-9 PM",
    "currentGpa": 3.75,
    "currentSemester": "Year 3 Semester 1"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Application submitted successfully",
  "data": {
    "id": 47,
    "status": "PENDING",
    "statusDisplayName": "Pending",
    "studentId": 123,
    "studentUserId": "IT21234567",
    "studentName": "John Doe",
    "studentEmail": "john@example.com",
    "studentBatch": "2021",
    "studentProgram": "BSc (Hons) in Information Technology",
    "studentFaculty": "COMPUTING",
    "studentProfilePictureUrl": null,
    "motivation": "I want to help fellow students...",
    "relevantExperience": "Tutored junior students for 2 semesters",
    "subjectsToTeach": ["Data Structures", "Algorithms"],
    "preferredExperienceLevel": "INTERMEDIATE",
    "availability": "Weekday evenings 6-9 PM",
    "currentGpa": 3.75,
    "currentSemester": "Year 3 Semester 1",
    "reviewedById": null,
    "reviewedByName": null,
    "reviewedByEmail": null,
    "reviewedAt": null,
    "reviewNotes": null,
    "rejectionReason": null,
    "submittedAt": "2026-02-19T10:30:00",
    "approvedAt": null,
    "rejectedAt": null,
    "cancelledAt": null,
    "createdAt": "2026-02-19T10:30:00",
    "updatedAt": "2026-02-19T10:30:00",
    "canBeApproved": true,
    "canBeRejected": true,
    "canBeCancelled": true,
    "isFinalState": false
  }
}
```

---

### 3.2 Get My Applications

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/applications/my` |
| **Permission** | `KUPPI_APPLICATION:VIEW_OWN` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/applications/my" \
  -H "Authorization: Bearer <token>"
```

---

### 3.3 Get My Active Application

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/applications/active` |
| **Permission** | `KUPPI_APPLICATION:VIEW_OWN` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/applications/active" \
  -H "Authorization: Bearer <token>"
```

---

### 3.4 Cancel My Application

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/kuppi/applications/{applicationId}` |
| **Permission** | `KUPPI_APPLICATION:CANCEL` |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/kuppi/applications/47" \
  -H "Authorization: Bearer <token>"
```

---

### 3.5 Check If Can Apply

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/applications/can-apply` |
| **Permission** | `KUPPI_APPLICATION:VIEW_OWN` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/applications/can-apply" \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
{
  "success": true,
  "message": "Eligibility check completed",
  "data": {
    "canApply": true
  }
}
```

---

### 3.6 Check If Kuppi Student

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/applications/is-kuppi-student` |
| **Permission** | `KUPPI_APPLICATION:VIEW_OWN` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/applications/is-kuppi-student" \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
{
  "success": true,
  "message": "Status check completed",
  "data": {
    "isKuppiStudent": false
  }
}
```

---

## 4. Admin Endpoints

**Base Path:** `/api/v1/admin/kuppi`

### 4.1 Get All Applications

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |
| **Roles** | Admin, Super Admin, Academic Staff |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/applications?page=0&size=10&sortBy=createdAt&sortDirection=DESC" \
  -H "Authorization: Bearer <token>"
```

---

### 4.2 Get Applications by Status

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications/status/{status}` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |

**Available Statuses:** `PENDING`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`, `CANCELLED`

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/applications/status/PENDING?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 4.3 Get Pending Applications

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications/pending` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/applications/pending?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 4.4 Get Active Applications

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications/active` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/applications/active?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 4.5 Get Application by ID

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications/{applicationId}` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/applications/47" \
  -H "Authorization: Bearer <token>"
```

---

### 4.6 Search Applications

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications/search` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/applications/search?keyword=john&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 4.7 Get Application Statistics

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications/stats` |
| **Permission** | `KUPPI_APPLICATION:STATS` |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/applications/stats" \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
{
  "success": true,
  "message": "Statistics retrieved successfully",
  "data": {
    "totalApplications": 150,
    "pendingApplications": 25,
    "underReviewApplications": 10,
    "approvedApplications": 100,
    "rejectedApplications": 10,
    "cancelledApplications": 5,
    "applicationsToday": 3,
    "totalKuppiStudents": 95
  }
}
```

---

### 4.8 Approve Application

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/admin/kuppi/applications/{applicationId}/approve` |
| **Permission** | `KUPPI_APPLICATION:APPROVE` |

**Request Body:**
```json
{
  "reviewNotes": "Strong academic record and good communication skills. Approved to conduct Kuppi sessions."
}
```

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/admin/kuppi/applications/47/approve" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "reviewNotes": "Strong academic record. Approved."
  }'
```

---

### 4.9 Reject Application

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/admin/kuppi/applications/{applicationId}/reject` |
| **Permission** | `KUPPI_APPLICATION:REJECT` |

**Request Body:**
```json
{
  "rejectionReason": "GPA below minimum requirement of 3.0",
  "reviewNotes": "Student can reapply after improving grades."
}
```

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/admin/kuppi/applications/47/reject" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "rejectionReason": "GPA below minimum requirement",
    "reviewNotes": "Can reapply after improving grades."
  }'
```

---

### 4.10 Mark as Under Review

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/admin/kuppi/applications/{applicationId}/under-review` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/admin/kuppi/applications/47/under-review" \
  -H "Authorization: Bearer <token>"
```

---

### 4.11 Admin Update Session

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/admin/kuppi/sessions/{sessionId}` |
| **Permission** | `KUPPI:ADMIN_UPDATE` |

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/admin/kuppi/sessions/1" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Admin Updated Title"
  }'
```

---

### 4.12 Admin Delete Session

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/admin/kuppi/sessions/{sessionId}` |
| **Permission** | `KUPPI:ADMIN_DELETE` |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/admin/kuppi/sessions/1" \
  -H "Authorization: Bearer <token>"
```

---

### 4.13 Admin Update Note

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/admin/kuppi/notes/{noteId}` |
| **Permission** | `KUPPI_NOTE:ADMIN_UPDATE` |

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/admin/kuppi/notes/1" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Admin Updated Note Title"
  }'
```

---

### 4.14 Admin Delete Note

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/admin/kuppi/notes/{noteId}` |
| **Permission** | `KUPPI_NOTE:ADMIN_DELETE` |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/admin/kuppi/notes/1" \
  -H "Authorization: Bearer <token>"
```

---

### 4.15 Get Platform Statistics

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/stats` |
| **Permission** | `KUPPI:VIEW_STATS` |

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
    "totalSessions": 250,
    "totalNotes": 500,
    "totalParticipants": 1500,
    "totalKuppiStudents": 95,
    "completedSessions": 200,
    "cancelledSessions": 20,
    "totalViews": 15000,
    "totalDownloads": 8000,
    "averagePlatformRating": 4.5,
    "sessionsThisWeek": 25,
    "sessionsThisMonth": 80,
    "newKuppiStudentsThisMonth": 10
  }
}
```

---

## 5. Super Admin Endpoints

**Base Path:** `/api/v1/super-admin/kuppi`

### 5.1 Permanently Delete Application

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/super-admin/kuppi/applications/{applicationId}/permanent` |
| **Permission** | `KUPPI_APPLICATION:PERMANENT_DELETE` |
| **Roles** | Super Admin only |

⚠️ **DANGER:** This action is **IRREVERSIBLE**.

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/super-admin/kuppi/applications/47/permanent" \
  -H "Authorization: Bearer <token>"
```

---

### 5.2 Revoke Kuppi Student Role

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/super-admin/kuppi/applications/revoke/{studentId}` |
| **Permission** | `KUPPI_APPLICATION:REVOKE` |
| **Roles** | Super Admin only |

**Query Parameters:**
| Parameter | Type | Required |
|-----------|------|----------|
| reason | string | Yes |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/super-admin/kuppi/applications/revoke/123?reason=Policy%20violation" \
  -H "Authorization: Bearer <token>"
```

---

### 5.3 Permanently Delete Session

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

### 5.4 Permanently Delete Note

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

## 6. DTOs Reference

### Request DTOs

#### CreateKuppiSessionRequest
```json
{
  "title": "string (required, max 200)",
  "description": "string (optional, max 2000)",
  "subject": "string (required, max 100)",
  "scheduledStartTime": "datetime (required, future)",
  "scheduledEndTime": "datetime (required, future)",
  "liveLink": "string (required, max 500)",
  "meetingPlatform": "string (optional, max 200)"
}
```

#### UpdateKuppiSessionRequest
```json
{
  "title": "string (optional, max 200)",
  "description": "string (optional, max 2000)",
  "subject": "string (optional, max 100)",
  "scheduledStartTime": "datetime (optional, future)",
  "scheduledEndTime": "datetime (optional, future)",
  "liveLink": "string (optional, max 500)",
  "meetingPlatform": "string (optional, max 200)"
}
```

#### CreateKuppiNoteRequest (multipart/form-data)
```
title: string (required, max 200)
description: string (optional, max 1000)
sessionId: long (optional)
allowDownload: boolean (optional, default: true)
file: file (required)
```

#### UpdateKuppiNoteRequest (multipart/form-data)
```
title: string (optional, max 200)
description: string (optional, max 1000)
allowDownload: boolean (optional)
file: file (optional)
```

#### KuppiApplicationRequest
```json
{
  "motivation": "string (required, 50-1000 chars)",
  "relevantExperience": "string (optional, max 500)",
  "subjectsToTeach": ["string"] (required, 1-10 items),
  "preferredExperienceLevel": "BEGINNER|INTERMEDIATE|ADVANCED (required)",
  "availability": "string (optional, max 500)",
  "currentGpa": "number (required, 0.0-4.0)",
  "currentSemester": "string (required, max 50)"
}
```

#### ReviewKuppiApplicationRequest
```json
{
  "reviewNotes": "string (optional, max 1000)",
  "rejectionReason": "string (required for reject, max 500)"
}
```

### Response DTOs

#### KuppiSessionResponse
```json
{
  "id": "long",
  "title": "string",
  "description": "string",
  "subject": "string",
  "sessionType": "LIVE|RECORDED",
  "status": "SCHEDULED|IN_PROGRESS|COMPLETED|CANCELLED",
  "scheduledStartTime": "datetime",
  "scheduledEndTime": "datetime",
  "liveLink": "string",
  "meetingPlatform": "string",
  "viewCount": "long",
  "hostId": "long",
  "hostName": "string",
  "hostEmail": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "isActive": "boolean",
  "canJoin": "boolean",
  "notes": "[KuppiNoteResponse]"
}
```

#### KuppiNoteResponse
```json
{
  "id": "long",
  "title": "string",
  "description": "string",
  "fileType": "string",
  "fileUrl": "string",
  "fileName": "string",
  "fileSize": "long",
  "formattedFileSize": "string",
  "allowDownload": "boolean",
  "downloadCount": "long",
  "viewCount": "long",
  "sessionId": "long",
  "sessionTitle": "string",
  "uploadedById": "long",
  "uploaderName": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "isActive": "boolean"
}
```

#### KuppiApplicationResponse
```json
{
  "id": "long",
  "status": "PENDING|UNDER_REVIEW|APPROVED|REJECTED|CANCELLED",
  "statusDisplayName": "string",
  "studentId": "long",
  "studentUserId": "string",
  "studentName": "string",
  "studentEmail": "string",
  "studentBatch": "string",
  "studentProgram": "string",
  "studentFaculty": "string",
  "studentProfilePictureUrl": "string",
  "motivation": "string",
  "relevantExperience": "string",
  "subjectsToTeach": ["string"],
  "preferredExperienceLevel": "string",
  "availability": "string",
  "currentGpa": "number",
  "currentSemester": "string",
  "reviewedById": "long",
  "reviewedByName": "string",
  "reviewedByEmail": "string",
  "reviewedAt": "datetime",
  "reviewNotes": "string",
  "rejectionReason": "string",
  "submittedAt": "datetime",
  "approvedAt": "datetime",
  "rejectedAt": "datetime",
  "cancelledAt": "datetime",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "canBeApproved": "boolean",
  "canBeRejected": "boolean",
  "canBeCancelled": "boolean",
  "isFinalState": "boolean"
}
```

---

## 7. Error Responses

### Standard Error Response
```json
{
  "success": false,
  "message": "Error message",
  "error": {
    "timestamp": "2026-02-19T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Detailed error message",
    "path": "/api/v1/kuppi/sessions"
  }
}
```

### Common HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request - Validation error |
| 401 | Unauthorized - Missing/invalid token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Duplicate resource |
| 500 | Internal Server Error |

---

## Endpoints Summary

### Session Endpoints (14)
| # | Method | Endpoint | Permission |
|---|--------|----------|------------|
| 1 | GET | `/api/v1/kuppi/sessions` | KUPPI:READ |
| 2 | GET | `/api/v1/kuppi/sessions/{sessionId}` | KUPPI:READ |
| 3 | GET | `/api/v1/kuppi/sessions/search` | KUPPI:READ |
| 4 | GET | `/api/v1/kuppi/sessions/search/subject` | KUPPI:READ |
| 5 | GET | `/api/v1/kuppi/sessions/search/host` | KUPPI:READ |
| 6 | GET | `/api/v1/kuppi/sessions/search/date` | KUPPI:READ |
| 7 | GET | `/api/v1/kuppi/sessions/upcoming` | KUPPI:READ |
| 8 | POST | `/api/v1/kuppi/sessions` | KUPPI:CREATE |
| 9 | PUT | `/api/v1/kuppi/sessions/{sessionId}` | KUPPI:UPDATE |
| 10 | POST | `/api/v1/kuppi/sessions/{sessionId}/cancel` | KUPPI:CANCEL |
| 11 | POST | `/api/v1/kuppi/sessions/{sessionId}/reschedule` | KUPPI:RESCHEDULE |
| 12 | DELETE | `/api/v1/kuppi/sessions/{sessionId}` | KUPPI:DELETE |
| 13 | GET | `/api/v1/kuppi/sessions/my` | KUPPI:READ |
| 14 | GET | `/api/v1/kuppi/sessions/my/analytics` | KUPPI:VIEW_ANALYTICS |

### Notes Endpoints (10)
| # | Method | Endpoint | Permission |
|---|--------|----------|------------|
| 1 | GET | `/api/v1/kuppi/notes` | KUPPI_NOTE:READ |
| 2 | GET | `/api/v1/kuppi/notes/session/{sessionId}` | KUPPI_NOTE:READ |
| 3 | GET | `/api/v1/kuppi/notes/{noteId}` | KUPPI_NOTE:READ |
| 4 | GET | `/api/v1/kuppi/notes/{noteId}/download` | KUPPI_NOTE:DOWNLOAD |
| 5 | GET | `/api/v1/kuppi/notes/{noteId}/download/file` | KUPPI_NOTE:DOWNLOAD |
| 6 | GET | `/api/v1/kuppi/notes/search` | KUPPI_NOTE:SEARCH |
| 7 | POST | `/api/v1/kuppi/notes/upload` | KUPPI_NOTE:CREATE |
| 8 | PUT | `/api/v1/kuppi/notes/{noteId}/upload` | KUPPI_NOTE:UPDATE |
| 9 | DELETE | `/api/v1/kuppi/notes/{noteId}` | KUPPI_NOTE:DELETE |
| 10 | GET | `/api/v1/kuppi/notes/my` | KUPPI_NOTE:READ |

### Application Endpoints - Student (6)
| # | Method | Endpoint | Permission |
|---|--------|----------|------------|
| 1 | POST | `/api/v1/kuppi/applications` | KUPPI_APPLICATION:SUBMIT |
| 2 | GET | `/api/v1/kuppi/applications/my` | KUPPI_APPLICATION:VIEW_OWN |
| 3 | GET | `/api/v1/kuppi/applications/active` | KUPPI_APPLICATION:VIEW_OWN |
| 4 | DELETE | `/api/v1/kuppi/applications/{applicationId}` | KUPPI_APPLICATION:CANCEL |
| 5 | GET | `/api/v1/kuppi/applications/can-apply` | KUPPI_APPLICATION:VIEW_OWN |
| 6 | GET | `/api/v1/kuppi/applications/is-kuppi-student` | KUPPI_APPLICATION:VIEW_OWN |

### Admin Endpoints (15)
| # | Method | Endpoint | Permission |
|---|--------|----------|------------|
| 1 | GET | `/api/v1/admin/kuppi/applications` | KUPPI_APPLICATION:VIEW_ALL |
| 2 | GET | `/api/v1/admin/kuppi/applications/status/{status}` | KUPPI_APPLICATION:VIEW_ALL |
| 3 | GET | `/api/v1/admin/kuppi/applications/pending` | KUPPI_APPLICATION:VIEW_ALL |
| 4 | GET | `/api/v1/admin/kuppi/applications/active` | KUPPI_APPLICATION:VIEW_ALL |
| 5 | GET | `/api/v1/admin/kuppi/applications/{applicationId}` | KUPPI_APPLICATION:VIEW_ALL |
| 6 | GET | `/api/v1/admin/kuppi/applications/search` | KUPPI_APPLICATION:VIEW_ALL |
| 7 | GET | `/api/v1/admin/kuppi/applications/stats` | KUPPI_APPLICATION:STATS |
| 8 | PUT | `/api/v1/admin/kuppi/applications/{applicationId}/approve` | KUPPI_APPLICATION:APPROVE |
| 9 | PUT | `/api/v1/admin/kuppi/applications/{applicationId}/reject` | KUPPI_APPLICATION:REJECT |
| 10 | PUT | `/api/v1/admin/kuppi/applications/{applicationId}/under-review` | KUPPI_APPLICATION:VIEW_ALL |
| 11 | PUT | `/api/v1/admin/kuppi/sessions/{sessionId}` | KUPPI:ADMIN_UPDATE |
| 12 | DELETE | `/api/v1/admin/kuppi/sessions/{sessionId}` | KUPPI:ADMIN_DELETE |
| 13 | PUT | `/api/v1/admin/kuppi/notes/{noteId}` | KUPPI_NOTE:ADMIN_UPDATE |
| 14 | DELETE | `/api/v1/admin/kuppi/notes/{noteId}` | KUPPI_NOTE:ADMIN_DELETE |
| 15 | GET | `/api/v1/admin/kuppi/stats` | KUPPI:VIEW_STATS |

### Super Admin Endpoints (4)
| # | Method | Endpoint | Permission |
|---|--------|----------|------------|
| 1 | DELETE | `/api/v1/super-admin/kuppi/applications/{applicationId}/permanent` | KUPPI_APPLICATION:PERMANENT_DELETE |
| 2 | DELETE | `/api/v1/super-admin/kuppi/applications/revoke/{studentId}` | KUPPI_APPLICATION:REVOKE |
| 3 | DELETE | `/api/v1/admin/kuppi/sessions/{sessionId}/permanent` | KUPPI:PERMANENT_DELETE |
| 4 | DELETE | `/api/v1/admin/kuppi/notes/{noteId}/permanent` | KUPPI_NOTE:PERMANENT_DELETE |

---

**Total Endpoints: 49**

---

*Documentation generated on: February 19, 2026*

