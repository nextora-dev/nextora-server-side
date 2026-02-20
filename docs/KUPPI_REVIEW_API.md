# Kuppi Review API Documentation

## Base URLs

| Controller | Base URL |
|-----------|----------|
| Student/Tutor | `/api/v1/kuppi/reviews` |
| Admin | `/api/v1/admin/kuppi/reviews` |

---

## Student Endpoints

### 1. Create Review
**POST** `/api/v1/kuppi/reviews`

**Request:**
```json
{
  "sessionId": 123,
  "rating": 5,
  "comment": "Great session! Very helpful."
}
```

**Response:**
```json
{
  "success": true,
  "message": "Review created successfully",
  "data": {
    "id": 1,
    "sessionId": 123,
    "sessionTitle": "Data Structures Tutorial",
    "reviewerId": 456,
    "reviewerName": "John Doe",
    "tutorId": 789,
    "tutorName": "Jane Smith",
    "rating": 5,
    "comment": "Great session! Very helpful.",
    "tutorResponse": null,
    "tutorResponseAt": null,
    "createdAt": "2026-02-20T10:00:00",
    "updatedAt": "2026-02-20T10:00:00"
  },
  "timestamp": "2026-02-20T10:00:00"
}
```

---

### 2. Update Review
**PUT** `/api/v1/kuppi/reviews/{reviewId}`

**Request:**
```json
{
  "rating": 4,
  "comment": "Good session, but could be better."
}
```

**Response:** Same as Create Review

---

### 3. Delete Review
**DELETE** `/api/v1/kuppi/reviews/{reviewId}`

**Response:**
```json
{
  "success": true,
  "message": "Review deleted successfully",
  "data": null,
  "timestamp": "2026-02-20T10:10:00"
}
```

---

### 4. Get Review by ID
**GET** `/api/v1/kuppi/reviews/{reviewId}`

**Response:** Same as Create Review data

---

### 5. Get My Reviews
**GET** `/api/v1/kuppi/reviews/my?page=0&size=10`

**Response:**
```json
{
  "success": true,
  "message": "Reviews retrieved",
  "data": {
    "content": [...],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-02-20T12:00:00"
}
```

---

### 6. Get Session Reviews
**GET** `/api/v1/kuppi/reviews/session/{sessionId}?page=0&size=10`

**Response:** Same paginated format

---

### 7. Get Tutor Reviews
**GET** `/api/v1/kuppi/reviews/tutor/{tutorId}?page=0&size=10`

**Response:** Same paginated format

---

## Tutor Endpoints

### 8. Add Tutor Response
**POST** `/api/v1/kuppi/reviews/{reviewId}/tutor-response`

**Request:**
```json
{
  "responseText": "Thank you for your feedback!"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Response added",
  "data": {
    "id": 1,
    "sessionId": 123,
    "sessionTitle": "Data Structures Tutorial",
    "reviewerId": 456,
    "reviewerName": "John Doe",
    "tutorId": 789,
    "tutorName": "Jane Smith",
    "rating": 5,
    "comment": "Great session!",
    "tutorResponse": "Thank you for your feedback!",
    "tutorResponseAt": "2026-02-20T11:00:00",
    "createdAt": "2026-02-20T10:00:00",
    "updatedAt": "2026-02-20T11:00:00"
  },
  "timestamp": "2026-02-20T11:00:00"
}
```

---

### 9. Get Reviews for My Hosted Sessions
**GET** `/api/v1/kuppi/reviews/my-hosted?page=0&size=10`

**Response:** Same paginated format

---

## Admin Endpoints

### 10. Get All Reviews
**GET** `/api/v1/admin/kuppi/reviews?page=0&size=10`

**Response:** Same paginated format

---

### 11. Get Review by ID (Admin)
**GET** `/api/v1/admin/kuppi/reviews/{reviewId}`

**Response:** Same as Get Review by ID

---

### 12. Admin Delete Review
**DELETE** `/api/v1/admin/kuppi/reviews/{reviewId}`

**Response:**
```json
{
  "success": true,
  "message": "Review deleted successfully",
  "data": null,
  "timestamp": "2026-02-20T12:00:00"
}
```

---

## API Constants Reference

| Constant | Value |
|----------|-------|
| `KUPPI_REVIEWS` | `/api/v1/kuppi/reviews` |
| `KUPPI_REVIEW_BY_ID` | `/{reviewId}` |
| `KUPPI_REVIEW_SESSION` | `/session/{sessionId}` |
| `KUPPI_REVIEW_TUTOR` | `/tutor/{tutorId}` |
| `KUPPI_REVIEW_TUTOR_RESPONSE` | `/{reviewId}/tutor-response` |
| `KUPPI_REVIEW_MY_HOSTED` | `/my-hosted` |
| `KUPPI_MY` | `/my` |
| `KUPPI_ADMIN_REVIEWS` | `/api/v1/admin/kuppi/reviews` |
| `KUPPI_ADMIN_REVIEW_BY_ID` | `/{reviewId}` |

---

## Error Responses

```json
{
  "success": false,
  "message": "Cannot review deleted session",
  "data": null,
  "timestamp": "2026-02-20T12:00:00"
}
```

| Status | Error |
|--------|-------|
| 400 | Cannot review deleted session |
| 400 | Can only review completed sessions |
| 400 | Cannot review your own session |
| 400 | Already reviewed this session |
| 400 | Already responded to this review |
| 401 | Can only update your own review |
| 401 | Can only delete your own review |
| 401 | Only tutor can respond |
| 404 | Review not found |
| 404 | Session not found |

