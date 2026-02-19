# Kuppi Student Application API Documentation

## Overview

The Kuppi Student Application feature allows normal students to apply to become Kuppi Students. Once approved by Admin, Super Admin, or Academic Staff, students gain the `KUPPI_STUDENT` role and can create Kuppi sessions and upload notes.

**Base URL:** `http://localhost:8080/api/v1`

---

## Flow

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Normal Student │     │   Admin/Academic │     │  Kuppi Student  │
│   (ROLE_STUDENT)│     │      Staff       │     │ (ROLE_STUDENT + │
│                 │     │                  │     │  KUPPI_STUDENT) │
└────────┬────────┘     └────────┬─────────┘     └────────┬────────┘
         │                       │                        │
         │ 1. Submit Application │                        │
         │──────────────────────>│                        │
         │                       │                        │
         │                       │ 2. Review Application  │
         │                       │    (Approve/Reject)    │
         │                       │                        │
         │ 3. If Approved        │                        │
         │<──────────────────────│                        │
         │                       │                        │
         │ 4. Gains KUPPI_STUDENT role                    │
         │───────────────────────────────────────────────>│
         │                       │                        │
         │                       │        5. Can Create   │
         │                       │           Sessions     │
         │                       │           & Notes      │
         │                       │                        │
```

---

## Role-Based Access

| Endpoint | Normal Student | Kuppi Student | Academic Staff | Admin | Super Admin |
|----------|---------------|---------------|----------------|-------|-------------|
| Submit Application | ✅ | ❌ | ❌ | ❌ | ❌ |
| View Own Applications | ✅ | ✅ | ❌ | ❌ | ❌ |
| Cancel Own Application | ✅ | ❌ | ❌ | ❌ | ❌ |
| Check Can Apply | ✅ | ✅ | ❌ | ❌ | ❌ |
| View All Applications | ❌ | ❌ | ✅ | ✅ | ✅ |
| Approve Application | ❌ | ❌ | ✅ | ✅ | ✅ |
| Reject Application | ❌ | ❌ | ✅ | ✅ | ✅ |
| View Stats | ❌ | ❌ | ✅ | ✅ | ✅ |
| Permanent Delete | ❌ | ❌ | ❌ | ❌ | ✅ |
| Revoke Kuppi Role | ❌ | ❌ | ❌ | ❌ | ✅ |

---

## 1. Student Endpoints

**Base Path:** `/api/v1/kuppi/applications`

### 1.1 Submit Application

Submit an application to become a Kuppi Student.

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/v1/kuppi/applications` |
| **Permission** | `KUPPI_APPLICATION:SUBMIT` |
| **Roles** | Normal Student only |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/kuppi/applications" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "motivation": "I want to help fellow students understand complex programming concepts. I have strong knowledge in data structures and algorithms and enjoy teaching.",
    "relevantExperience": "Tutored junior students for 2 semesters, conducted study groups",
    "subjectsToTeach": ["Data Structures", "Algorithms", "Java Programming"],
    "preferredExperienceLevel": "INTERMEDIATE",
    "availability": "Weekday evenings 6-9 PM, Weekend mornings",
    "currentGpa": 3.75,
    "currentSemester": "Year 3 Semester 1"
  }'
```

**Request Body:**
```json
{
  "motivation": "string (50-1000 chars, required)",
  "relevantExperience": "string (max 500 chars, optional)",
  "subjectsToTeach": ["string"] (1-10 subjects, required),
  "preferredExperienceLevel": "BEGINNER|INTERMEDIATE|ADVANCED (required)",
  "availability": "string (max 500 chars, optional)",
  "currentGpa": 0.0-4.0 (required),
  "currentSemester": "string (required)"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Application submitted successfully",
  "data": {
    "id": 1,
    "status": "PENDING",
    "statusDisplayName": "Pending",
    "studentId": 123,
    "studentUserId": "IT21234567",
    "studentName": "John Doe",
    "studentEmail": "john@example.com",
    "motivation": "...",
    "subjectsToTeach": ["Data Structures", "Algorithms"],
    "submittedAt": "2026-02-19T10:30:00",
    "canBeApproved": true,
    "canBeRejected": true,
    "canBeCancelled": true,
    "isFinalState": false
  }
}
```

---

### 1.2 Get My Applications

Get all applications submitted by the current user.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/applications/my` |
| **Permission** | `KUPPI_APPLICATION:VIEW_OWN` |
| **Roles** | Student (Normal or Kuppi) |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/applications/my" \
  -H "Authorization: Bearer <token>"
```

---

### 1.3 Get My Active Application

Get the current pending or under-review application.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/applications/active` |
| **Permission** | `KUPPI_APPLICATION:VIEW_OWN` |
| **Roles** | Student |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/applications/active" \
  -H "Authorization: Bearer <token>"
```

---

### 1.4 Cancel My Application

Cancel own application (only if pending or under review).

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/kuppi/applications/{applicationId}` |
| **Permission** | `KUPPI_APPLICATION:CANCEL` |
| **Roles** | Normal Student |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/kuppi/applications/1" \
  -H "Authorization: Bearer <token>"
```

---

### 1.5 Check If Can Apply

Check if the current user can submit a new application.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/applications/can-apply` |
| **Permission** | `KUPPI_APPLICATION:VIEW_OWN` |
| **Roles** | Student |

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

### 1.6 Check If Kuppi Student

Check if the current user is already a Kuppi Student.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/kuppi/applications/is-kuppi-student` |
| **Permission** | `KUPPI_APPLICATION:VIEW_OWN` |
| **Roles** | Student |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/kuppi/applications/is-kuppi-student" \
  -H "Authorization: Bearer <token>"
```

---

## 2. Admin/Academic Staff Endpoints

**Base Path:** `/api/v1/admin/kuppi/applications`

### 2.1 Get All Applications

Get all applications with pagination.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |
| **Roles** | Academic Staff, Admin, Super Admin |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/applications?page=0&size=10&sortBy=createdAt&sortDirection=DESC" \
  -H "Authorization: Bearer <token>"
```

---

### 2.2 Get Applications by Status

Get applications filtered by status.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications/status/{status}` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |
| **Roles** | Academic Staff, Admin, Super Admin |

**Available Statuses:** `PENDING`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`, `CANCELLED`

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/applications/status/PENDING?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 2.3 Get Pending Applications

Get all pending applications awaiting review.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications/pending` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |
| **Roles** | Academic Staff, Admin, Super Admin |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/applications/pending?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 2.4 Get Active Applications

Get all active applications (pending + under review).

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications/active` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |
| **Roles** | Academic Staff, Admin, Super Admin |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/applications/active?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 2.5 Get Application by ID

Get detailed information about a specific application.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications/{applicationId}` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |
| **Roles** | Academic Staff, Admin, Super Admin |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/applications/1" \
  -H "Authorization: Bearer <token>"
```

---

### 2.6 Search Applications

Search applications by student name, email, or student ID.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications/search` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |
| **Roles** | Academic Staff, Admin, Super Admin |

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/kuppi/applications/search?keyword=john&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

### 2.7 Get Application Statistics

Get statistics about Kuppi Student applications.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/kuppi/applications/stats` |
| **Permission** | `KUPPI_APPLICATION:STATS` |
| **Roles** | Academic Staff, Admin, Super Admin |

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

### 2.8 Approve Application

Approve a Kuppi Student application. This grants the `KUPPI_STUDENT` role.

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/admin/kuppi/applications/{applicationId}/approve` |
| **Permission** | `KUPPI_APPLICATION:APPROVE` |
| **Roles** | Academic Staff, Admin, Super Admin |

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/admin/kuppi/applications/1/approve" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "reviewNotes": "Strong academic record and good communication skills. Approved."
  }'
```

---

### 2.9 Reject Application

Reject a Kuppi Student application.

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/admin/kuppi/applications/{applicationId}/reject` |
| **Permission** | `KUPPI_APPLICATION:REJECT` |
| **Roles** | Academic Staff, Admin, Super Admin |

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/admin/kuppi/applications/1/reject" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "rejectionReason": "GPA below minimum requirement of 3.0",
    "reviewNotes": "Student can reapply after improving grades"
  }'
```

---

### 2.10 Mark as Under Review

Mark a pending application as under review.

| Property | Value |
|----------|-------|
| **Method** | `PUT` |
| **URL** | `/api/v1/admin/kuppi/applications/{applicationId}/under-review` |
| **Permission** | `KUPPI_APPLICATION:VIEW_ALL` |
| **Roles** | Academic Staff, Admin, Super Admin |

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/v1/admin/kuppi/applications/1/under-review" \
  -H "Authorization: Bearer <token>"
```

---

## 3. Super Admin Endpoints

### 3.1 Permanently Delete Application

⚠️ **DANGER:** Permanently delete an application from the database.

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/admin/kuppi/applications/{applicationId}/permanent` |
| **Permission** | `KUPPI_APPLICATION:PERMANENT_DELETE` |
| **Roles** | Super Admin only |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/admin/kuppi/applications/1/permanent" \
  -H "Authorization: Bearer <token>"
```

---

### 3.2 Revoke Kuppi Student Role

Revoke the Kuppi Student role from a student.

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/admin/kuppi/applications/revoke/{studentId}` |
| **Permission** | `KUPPI_APPLICATION:REVOKE` |
| **Roles** | Super Admin only |

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/admin/kuppi/applications/revoke/123?reason=Policy%20violation" \
  -H "Authorization: Bearer <token>"
```

---

## Quick Reference

| # | Method | Endpoint | Permission | Roles |
|---|--------|----------|------------|-------|
| **Student Endpoints** |||||
| 1 | POST | `/api/v1/kuppi/applications` | KUPPI_APPLICATION:SUBMIT | Normal Student |
| 2 | GET | `/api/v1/kuppi/applications/my` | KUPPI_APPLICATION:VIEW_OWN | Student |
| 3 | GET | `/api/v1/kuppi/applications/active` | KUPPI_APPLICATION:VIEW_OWN | Student |
| 4 | DELETE | `/api/v1/kuppi/applications/{id}` | KUPPI_APPLICATION:CANCEL | Normal Student |
| 5 | GET | `/api/v1/kuppi/applications/can-apply` | KUPPI_APPLICATION:VIEW_OWN | Student |
| 6 | GET | `/api/v1/kuppi/applications/is-kuppi-student` | KUPPI_APPLICATION:VIEW_OWN | Student |
| **Admin Endpoints** |||||
| 7 | GET | `/api/v1/admin/kuppi/applications` | KUPPI_APPLICATION:VIEW_ALL | Academic Staff, Admin |
| 8 | GET | `/api/v1/admin/kuppi/applications/status/{status}` | KUPPI_APPLICATION:VIEW_ALL | Academic Staff, Admin |
| 9 | GET | `/api/v1/admin/kuppi/applications/pending` | KUPPI_APPLICATION:VIEW_ALL | Academic Staff, Admin |
| 10 | GET | `/api/v1/admin/kuppi/applications/active` | KUPPI_APPLICATION:VIEW_ALL | Academic Staff, Admin |
| 11 | GET | `/api/v1/admin/kuppi/applications/{id}` | KUPPI_APPLICATION:VIEW_ALL | Academic Staff, Admin |
| 12 | GET | `/api/v1/admin/kuppi/applications/search` | KUPPI_APPLICATION:VIEW_ALL | Academic Staff, Admin |
| 13 | GET | `/api/v1/admin/kuppi/applications/stats` | KUPPI_APPLICATION:STATS | Academic Staff, Admin |
| 14 | PUT | `/api/v1/admin/kuppi/applications/{id}/approve` | KUPPI_APPLICATION:APPROVE | Academic Staff, Admin |
| 15 | PUT | `/api/v1/admin/kuppi/applications/{id}/reject` | KUPPI_APPLICATION:REJECT | Academic Staff, Admin |
| 16 | PUT | `/api/v1/admin/kuppi/applications/{id}/under-review` | KUPPI_APPLICATION:VIEW_ALL | Academic Staff, Admin |
| **Super Admin Endpoints** |||||
| 17 | DELETE | `/api/v1/admin/kuppi/applications/{id}/permanent` | KUPPI_APPLICATION:PERMANENT_DELETE | Super Admin |
| 18 | DELETE | `/api/v1/admin/kuppi/applications/revoke/{studentId}` | KUPPI_APPLICATION:REVOKE | Super Admin |

**Total Endpoints: 18**

---

## Application Status Flow

```
PENDING ──────> UNDER_REVIEW ──────> APPROVED
    │                │                   │
    │                │                   └── Student becomes KUPPI_STUDENT
    │                │
    │                └──────────────> REJECTED
    │
    └──────────────────────────────> CANCELLED (by student)
```

---

*Documentation generated on: February 19, 2026*

