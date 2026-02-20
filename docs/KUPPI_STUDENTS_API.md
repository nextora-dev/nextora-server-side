# Kuppi Students API Documentation

## Overview

This document provides complete API documentation for the Kuppi Students module endpoints. These endpoints allow users to view and search for Kuppi Students - students who are approved to host Kuppi (peer tutoring) sessions.

**Base URL:** `/api/v1/kuppi/students`

**Authentication:** All endpoints require authentication with `KUPPI:READ` permission (available to all authenticated students).

---

## Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/kuppi/students` | Get all Kuppi students (paginated) |
| GET | `/api/v1/kuppi/students/{studentId}` | Get Kuppi student details by ID |
| GET | `/api/v1/kuppi/students/search/name` | Search Kuppi students by name |
| GET | `/api/v1/kuppi/students/search/subject` | Search Kuppi students by subject |
| GET | `/api/v1/kuppi/students/faculty/{faculty}` | Get Kuppi students by faculty |
| GET | `/api/v1/kuppi/students/top-rated` | Get top-rated Kuppi students |

---

## 1. Get All Kuppi Students

Retrieve a paginated list of all active Kuppi students who can host sessions.

### Request

```
GET /api/v1/kuppi/students
```

#### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | integer | No | 0 | Page number (0-based) |
| `size` | integer | No | 10 | Number of items per page |
| `sortBy` | string | No | kuppiRating | Field to sort by |
| `sortDirection` | string | No | DESC | Sort direction (ASC or DESC) |

#### Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | Bearer {access_token} |

### Request Example

```http
GET /api/v1/kuppi/students?page=0&size=10&sortBy=kuppiRating&sortDirection=DESC
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Response

#### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Kuppi students retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "studentId": "IIT2023001",
        "firstName": "John",
        "lastName": "Doe",
        "fullName": "John Doe",
        "email": "john.doe@student.iit.ac.lk",
        "profilePictureUrl": "https://example.com/profiles/john.jpg",
        "batch": "2023",
        "program": "BSc (Hons) Computer Science",
        "faculty": "COMPUTING",
        "kuppiSubjects": ["Data Structures", "Algorithms", "Database Systems"],
        "kuppiExperienceLevel": "INTERMEDIATE",
        "kuppiSessionsCompleted": 15,
        "kuppiRating": 4.8,
        "kuppiAvailability": "Weekdays 6PM-9PM, Weekends flexible",
        "totalSessionsHosted": 20,
        "totalViews": 1250,
        "upcomingSessions": 3,
        "isActive": true
      },
      {
        "id": 2,
        "studentId": "IIT2022015",
        "firstName": "Jane",
        "lastName": "Smith",
        "fullName": "Jane Smith",
        "email": "jane.smith@student.iit.ac.lk",
        "profilePictureUrl": null,
        "batch": "2022",
        "program": "BSc (Hons) Software Engineering",
        "faculty": "COMPUTING",
        "kuppiSubjects": ["Mathematics", "Statistics"],
        "kuppiExperienceLevel": "ADVANCED",
        "kuppiSessionsCompleted": 30,
        "kuppiRating": 4.9,
        "kuppiAvailability": "Weekends only",
        "totalSessionsHosted": 35,
        "totalViews": 2100,
        "upcomingSessions": 1,
        "isActive": true
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false
  },
  "timestamp": "2026-02-20T10:30:00"
}
```

#### Error Responses

| Status Code | Description |
|-------------|-------------|
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Insufficient permissions |

---

## 2. Get Kuppi Student by ID

Retrieve detailed information about a specific Kuppi student including their profile, statistics, and session history.

### Request

```
GET /api/v1/kuppi/students/{studentId}
```

#### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `studentId` | Long | Yes | The unique ID of the Kuppi student |

#### Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | Bearer {access_token} |

### Request Example

```http
GET /api/v1/kuppi/students/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Response

#### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Kuppi student details retrieved successfully",
  "data": {
    "id": 1,
    "studentId": "IIT2023001",
    "firstName": "John",
    "lastName": "Doe",
    "fullName": "John Doe",
    "email": "john.doe@student.iit.ac.lk",
    "profilePictureUrl": "https://example.com/profiles/john.jpg",
    "batch": "2023",
    "program": "BSc (Hons) Computer Science",
    "faculty": "COMPUTING",
    "kuppiSubjects": ["Data Structures", "Algorithms", "Database Systems"],
    "kuppiExperienceLevel": "INTERMEDIATE",
    "kuppiSessionsCompleted": 15,
    "kuppiRating": 4.8,
    "kuppiAvailability": "Weekdays 6PM-9PM, Weekends flexible",
    "totalSessionsHosted": 20,
    "completedSessions": 15,
    "liveSessions": 1,
    "scheduledSessions": 3,
    "cancelledSessions": 1,
    "totalViews": 1250,
    "totalNotesUploaded": 12,
    "recentSessions": [
      {
        "id": 101,
        "title": "Data Structures - Trees and Graphs",
        "subject": "Data Structures",
        "status": "COMPLETED",
        "scheduledStartTime": "2026-02-18T18:00:00",
        "scheduledEndTime": "2026-02-18T19:30:00",
        "viewCount": 45
      },
      {
        "id": 98,
        "title": "Algorithm Analysis Basics",
        "subject": "Algorithms",
        "status": "COMPLETED",
        "scheduledStartTime": "2026-02-15T17:00:00",
        "scheduledEndTime": "2026-02-15T18:30:00",
        "viewCount": 62
      }
    ],
    "upcomingSessions": [
      {
        "id": 105,
        "title": "SQL Fundamentals",
        "subject": "Database Systems",
        "status": "SCHEDULED",
        "scheduledStartTime": "2026-02-22T18:00:00",
        "scheduledEndTime": "2026-02-22T19:30:00",
        "viewCount": 0
      },
      {
        "id": 108,
        "title": "Binary Search Trees",
        "subject": "Data Structures",
        "status": "SCHEDULED",
        "scheduledStartTime": "2026-02-25T17:00:00",
        "scheduledEndTime": "2026-02-25T18:30:00",
        "viewCount": 0
      }
    ],
    "kuppiApprovedAt": "2026-01-15T09:30:00",
    "memberSince": "2023-09-01T08:00:00",
    "isActive": true
  },
  "timestamp": "2026-02-20T10:35:00"
}
```

#### Error Responses

| Status Code | Description | Response Body |
|-------------|-------------|---------------|
| 401 | Unauthorized | `{"success": false, "message": "Authentication required"}` |
| 404 | Not Found | `{"success": false, "message": "Kuppi Student not found with id: 999"}` |

---

## 3. Search Kuppi Students by Name

Search for Kuppi students by their first name or last name.

### Request

```
GET /api/v1/kuppi/students/search/name
```

#### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `name` | string | Yes | - | Name to search for (first or last name) |
| `page` | integer | No | 0 | Page number (0-based) |
| `size` | integer | No | 10 | Number of items per page |

#### Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | Bearer {access_token} |

### Request Example

```http
GET /api/v1/kuppi/students/search/name?name=John&page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Response

#### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "studentId": "IIT2023001",
        "firstName": "John",
        "lastName": "Doe",
        "fullName": "John Doe",
        "email": "john.doe@student.iit.ac.lk",
        "profilePictureUrl": "https://example.com/profiles/john.jpg",
        "batch": "2023",
        "program": "BSc (Hons) Computer Science",
        "faculty": "COMPUTING",
        "kuppiSubjects": ["Data Structures", "Algorithms"],
        "kuppiExperienceLevel": "INTERMEDIATE",
        "kuppiSessionsCompleted": 15,
        "kuppiRating": 4.8,
        "kuppiAvailability": "Weekdays 6PM-9PM",
        "totalSessionsHosted": 20,
        "totalViews": 1250,
        "upcomingSessions": 3,
        "isActive": true
      },
      {
        "id": 15,
        "studentId": "IIT2022030",
        "firstName": "Johnny",
        "lastName": "Walker",
        "fullName": "Johnny Walker",
        "email": "johnny.walker@student.iit.ac.lk",
        "profilePictureUrl": null,
        "batch": "2022",
        "program": "BSc (Hons) Information Technology",
        "faculty": "COMPUTING",
        "kuppiSubjects": ["Networking", "Security"],
        "kuppiExperienceLevel": "BEGINNER",
        "kuppiSessionsCompleted": 5,
        "kuppiRating": 4.2,
        "kuppiAvailability": "Weekends",
        "totalSessionsHosted": 8,
        "totalViews": 320,
        "upcomingSessions": 1,
        "isActive": true
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 2,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-02-20T10:40:00"
}
```

---

## 4. Search Kuppi Students by Subject

Search for Kuppi students who teach a specific subject.

### Request

```
GET /api/v1/kuppi/students/search/subject
```

#### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `subject` | string | Yes | - | Subject to search for |
| `page` | integer | No | 0 | Page number (0-based) |
| `size` | integer | No | 10 | Number of items per page |

#### Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | Bearer {access_token} |

### Request Example

```http
GET /api/v1/kuppi/students/search/subject?subject=Mathematics&page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Response

#### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "content": [
      {
        "id": 2,
        "studentId": "IIT2022015",
        "firstName": "Jane",
        "lastName": "Smith",
        "fullName": "Jane Smith",
        "email": "jane.smith@student.iit.ac.lk",
        "profilePictureUrl": null,
        "batch": "2022",
        "program": "BSc (Hons) Software Engineering",
        "faculty": "COMPUTING",
        "kuppiSubjects": ["Mathematics", "Statistics", "Calculus"],
        "kuppiExperienceLevel": "ADVANCED",
        "kuppiSessionsCompleted": 30,
        "kuppiRating": 4.9,
        "kuppiAvailability": "Weekends only",
        "totalSessionsHosted": 35,
        "totalViews": 2100,
        "upcomingSessions": 1,
        "isActive": true
      },
      {
        "id": 8,
        "studentId": "IIT2021045",
        "firstName": "David",
        "lastName": "Wilson",
        "fullName": "David Wilson",
        "email": "david.wilson@student.iit.ac.lk",
        "profilePictureUrl": "https://example.com/profiles/david.jpg",
        "batch": "2021",
        "program": "BSc (Hons) Computer Science",
        "faculty": "COMPUTING",
        "kuppiSubjects": ["Mathematics", "Discrete Math"],
        "kuppiExperienceLevel": "ADVANCED",
        "kuppiSessionsCompleted": 45,
        "kuppiRating": 4.7,
        "kuppiAvailability": "Flexible",
        "totalSessionsHosted": 50,
        "totalViews": 3200,
        "upcomingSessions": 2,
        "isActive": true
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 5,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-02-20T10:45:00"
}
```

---

## 5. Get Kuppi Students by Faculty

Retrieve Kuppi students belonging to a specific faculty.

### Request

```
GET /api/v1/kuppi/students/faculty/{faculty}
```

#### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `faculty` | string | Yes | Faculty name (e.g., COMPUTING, ENGINEERING, BUSINESS) |

#### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | integer | No | 0 | Page number (0-based) |
| `size` | integer | No | 10 | Number of items per page |

#### Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | Bearer {access_token} |

### Request Example

```http
GET /api/v1/kuppi/students/faculty/COMPUTING?page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Response

#### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Kuppi students retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "studentId": "IIT2023001",
        "firstName": "John",
        "lastName": "Doe",
        "fullName": "John Doe",
        "email": "john.doe@student.iit.ac.lk",
        "profilePictureUrl": "https://example.com/profiles/john.jpg",
        "batch": "2023",
        "program": "BSc (Hons) Computer Science",
        "faculty": "COMPUTING",
        "kuppiSubjects": ["Data Structures", "Algorithms"],
        "kuppiExperienceLevel": "INTERMEDIATE",
        "kuppiSessionsCompleted": 15,
        "kuppiRating": 4.8,
        "kuppiAvailability": "Weekdays 6PM-9PM",
        "totalSessionsHosted": 20,
        "totalViews": 1250,
        "upcomingSessions": 3,
        "isActive": true
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 18,
    "totalPages": 2,
    "first": true,
    "last": false
  },
  "timestamp": "2026-02-20T10:50:00"
}
```

#### Error Responses

| Status Code | Description | Response Body |
|-------------|-------------|---------------|
| 404 | Faculty not found | `{"success": false, "message": "Faculty not found with name: INVALID_FACULTY"}` |

### Available Faculty Values

| Value | Description |
|-------|-------------|
| `COMPUTING` | Faculty of Computing |
| `ENGINEERING` | Faculty of Engineering |
| `BUSINESS` | Faculty of Business |
| `SCIENCE` | Faculty of Science |
| `HUMANITIES` | Faculty of Humanities |

---

## 6. Get Top-Rated Kuppi Students

Retrieve the highest-rated Kuppi students based on their session ratings.

### Request

```
GET /api/v1/kuppi/students/top-rated
```

#### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | integer | No | 0 | Page number (0-based) |
| `size` | integer | No | 10 | Number of items per page |

#### Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | Bearer {access_token} |

### Request Example

```http
GET /api/v1/kuppi/students/top-rated?page=0&size=5
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Response

#### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Top-rated Kuppi students retrieved successfully",
  "data": {
    "content": [
      {
        "id": 2,
        "studentId": "IIT2022015",
        "firstName": "Jane",
        "lastName": "Smith",
        "fullName": "Jane Smith",
        "email": "jane.smith@student.iit.ac.lk",
        "profilePictureUrl": null,
        "batch": "2022",
        "program": "BSc (Hons) Software Engineering",
        "faculty": "COMPUTING",
        "kuppiSubjects": ["Mathematics", "Statistics"],
        "kuppiExperienceLevel": "ADVANCED",
        "kuppiSessionsCompleted": 30,
        "kuppiRating": 4.95,
        "kuppiAvailability": "Weekends only",
        "totalSessionsHosted": 35,
        "totalViews": 2100,
        "upcomingSessions": 1,
        "isActive": true
      },
      {
        "id": 1,
        "studentId": "IIT2023001",
        "firstName": "John",
        "lastName": "Doe",
        "fullName": "John Doe",
        "email": "john.doe@student.iit.ac.lk",
        "profilePictureUrl": "https://example.com/profiles/john.jpg",
        "batch": "2023",
        "program": "BSc (Hons) Computer Science",
        "faculty": "COMPUTING",
        "kuppiSubjects": ["Data Structures", "Algorithms"],
        "kuppiExperienceLevel": "INTERMEDIATE",
        "kuppiSessionsCompleted": 15,
        "kuppiRating": 4.8,
        "kuppiAvailability": "Weekdays 6PM-9PM",
        "totalSessionsHosted": 20,
        "totalViews": 1250,
        "upcomingSessions": 3,
        "isActive": true
      },
      {
        "id": 8,
        "studentId": "IIT2021045",
        "firstName": "David",
        "lastName": "Wilson",
        "fullName": "David Wilson",
        "email": "david.wilson@student.iit.ac.lk",
        "profilePictureUrl": "https://example.com/profiles/david.jpg",
        "batch": "2021",
        "program": "BSc (Hons) Computer Science",
        "faculty": "COMPUTING",
        "kuppiSubjects": ["Mathematics", "Discrete Math"],
        "kuppiExperienceLevel": "ADVANCED",
        "kuppiSessionsCompleted": 45,
        "kuppiRating": 4.7,
        "kuppiAvailability": "Flexible",
        "totalSessionsHosted": 50,
        "totalViews": 3200,
        "upcomingSessions": 2,
        "isActive": true
      }
    ],
    "pageNumber": 0,
    "pageSize": 5,
    "totalElements": 25,
    "totalPages": 5,
    "first": true,
    "last": false
  },
  "timestamp": "2026-02-20T10:55:00"
}
```

---

## Data Models

### KuppiStudentResponse (List View)

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Unique identifier |
| `studentId` | String | University student ID |
| `firstName` | String | First name |
| `lastName` | String | Last name |
| `fullName` | String | Full name (first + last) |
| `email` | String | Email address |
| `profilePictureUrl` | String | Profile picture URL (nullable) |
| `batch` | String | Academic batch/year |
| `program` | String | Degree program |
| `faculty` | String | Faculty (COMPUTING, ENGINEERING, etc.) |
| `kuppiSubjects` | Set<String> | Subjects the student can teach |
| `kuppiExperienceLevel` | String | Experience level (BEGINNER, INTERMEDIATE, ADVANCED) |
| `kuppiSessionsCompleted` | Integer | Number of completed sessions |
| `kuppiRating` | Double | Average rating (0.0 - 5.0) |
| `kuppiAvailability` | String | Availability description |
| `totalSessionsHosted` | Long | Total sessions created |
| `totalViews` | Long | Total views across all sessions |
| `upcomingSessions` | Long | Number of upcoming scheduled sessions |
| `isActive` | Boolean | Whether the student is active |

### KuppiStudentDetailResponse (Detail View)

Includes all fields from `KuppiStudentResponse` plus:

| Field | Type | Description |
|-------|------|-------------|
| `completedSessions` | Long | Number of completed sessions |
| `liveSessions` | Long | Number of currently live sessions |
| `scheduledSessions` | Long | Number of scheduled sessions |
| `cancelledSessions` | Long | Number of cancelled sessions |
| `totalNotesUploaded` | Long | Total notes uploaded |
| `recentSessions` | List<SessionSummary> | Last 5 sessions |
| `upcomingSessions` | List<SessionSummary> | Next 5 scheduled sessions |
| `kuppiApprovedAt` | LocalDateTime | When Kuppi student status was approved |
| `memberSince` | LocalDateTime | Account creation date |

### SessionSummary (Nested in Detail View)

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Session ID |
| `title` | String | Session title |
| `subject` | String | Session subject |
| `status` | String | Session status (SCHEDULED, LIVE, COMPLETED, CANCELLED) |
| `scheduledStartTime` | LocalDateTime | Start time |
| `scheduledEndTime` | LocalDateTime | End time |
| `viewCount` | Long | Number of views |

---

## Common Error Responses

### 401 Unauthorized

```json
{
  "success": false,
  "message": "Full authentication is required to access this resource",
  "timestamp": "2026-02-20T10:30:00"
}
```

### 403 Forbidden

```json
{
  "success": false,
  "message": "Access Denied: You don't have permission to access this resource",
  "timestamp": "2026-02-20T10:30:00"
}
```

### 404 Not Found

```json
{
  "success": false,
  "message": "Kuppi Student not found with id: 999",
  "timestamp": "2026-02-20T10:30:00"
}
```

### 500 Internal Server Error

```json
{
  "success": false,
  "message": "An unexpected error occurred",
  "timestamp": "2026-02-20T10:30:00"
}
```

---

## Pagination Response Structure

All paginated endpoints return a `PagedResponse` wrapper:

| Field | Type | Description |
|-------|------|-------------|
| `content` | List<T> | List of items for current page |
| `pageNumber` | Integer | Current page number (0-based) |
| `pageSize` | Integer | Number of items per page |
| `totalElements` | Long | Total number of items |
| `totalPages` | Integer | Total number of pages |
| `first` | Boolean | Is this the first page? |
| `last` | Boolean | Is this the last page? |

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-02-20 | Initial release - Kuppi Students endpoints |

