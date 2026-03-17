# Election Module - Quick Reference Card

## ⚡ TL;DR - What Changed

| Issue | Before | After |
|-------|--------|-------|
| Invalid enum value | HTTP 500 | HTTP 400 with helpful message |
| Validation error | Unclear | HTTP 400 with field-level errors |
| Postman collection | Incomplete | ✅ 50+ complete endpoints |
| Error handling | Generic | ✅ Specific enum error handler |

---

## 🎯 Valid Election Types (Pick ONE)

```
✅ PRESIDENT          (Club president)
✅ VICE_PRESIDENT     (Vice president)
✅ SECRETARY          (Secretary)
✅ TREASURER          (Treasurer)
✅ GENERAL            (General election)
✅ POLL               (Poll/survey)
✅ REFERENDUM         (Referendum)

❌ PRESIDENTIAL       (NOT VALID - use PRESIDENT)
❌ PRES               (NOT VALID)
❌ PRESIDENT_ELECT    (NOT VALID)
```

---

## 🔄 Election State Machine

```
DRAFT
  ↓
  └→ OPEN_NOMINATIONS
      ↓
      └→ NOMINATION_CLOSED
          ↓
          └→ VOTING_OPEN
              ↓
              └→ VOTING_CLOSED
                  ↓
                  └→ RESULTS_PUBLISHED
                  
CANCELLED (can be set from any state)
ARCHIVED (final state after publishing)
```

---

## 📋 Candidate Status Lifecycle

```
PENDING → APPROVED → Eligible for votes
       → REJECTED  → Not eligible

APPROVED → WITHDRAWN → User withdrew candidacy
        → DISQUALIFIED → Admin disqualified

PENDING → WITHDRAWN (self-withdrawal before approval)
```

---

## 🗂️ API Path Structure

```
/api/v1/club/election
├── GET  /                          (all elections)
├── POST /                          (create)
├── GET  /{id}                      (get one)
├── PUT  /{id}                      (update)
├── DELETE /{id}                    (delete)
├── GET  /club/{clubId}             (by club)
├── GET  /status/{status}           (by status)
├── GET  /upcoming                  (upcoming)
├── GET  /votable                   (can vote)
├── GET  /search                    (search)
│
├── POST /{id}/open-nominations     (lifecycle)
├── POST /{id}/close-nominations
├── POST /{id}/open-voting
├── POST /{id}/close-voting
├── POST /{id}/publish-results
├── POST /{id}/cancel
│
├── POST /candidate/nominate        (candidates)
├── PUT  /candidate/nominate/{id}
├── DELETE /candidate/nominate/{id}
├── POST /candidate/review
├── POST /candidate/{id}/withdraw
├── GET  /{id}/candidate
├── GET  /{id}/candidate/approved
├── GET  /{id}/candidate/pending
├── GET  /candidate/{id}
├── GET  /candidate/my
│
├── POST /vote                      (voting)
├── GET  /{id}/has-voted
├── GET  /{id}/verify-vote
│
├── GET  /{id}/results              (results)
├── GET  /{id}/live-count

/api/v1/admin/election
├── GET  /                          (all - admin view)
├── DELETE /{id}/permanent          (permanent delete)
├── POST /{id}/force-*              (force operations)
└── ...and more admin endpoints
```

---

## 📝 Request Template: Create Election

```json
{
  "title": "2026 Club President Election",
  "description": "Annual election for president",
  "clubId": 1,
  "electionType": "PRESIDENT",                    ← USE EXACT VALUE
  "nominationStartTime": "2026-03-20T09:00:00",
  "nominationEndTime": "2026-03-25T17:00:00",
  "votingStartTime": "2026-03-27T09:00:00",
  "votingEndTime": "2026-03-28T17:00:00",
  "maxCandidates": 10,
  "winnersCount": 1,
  "isAnonymousVoting": true,
  "requireManifesto": true,
  "eligibilityCriteria": "Must be active member"
}
```

---

## 🔐 Permissions Quick Reference

| Operation | Permission | Role |
|-----------|-----------|------|
| Create election | ELECTION:CREATE | President, Admin |
| Update election | ELECTION:UPDATE | Creator, Admin |
| Delete election | ELECTION:DELETE | Creator, Admin |
| Open/Close voting | ELECTION:MANAGE | President, Admin |
| Review candidates | CANDIDATE:APPROVE | Committee, Admin |
| Nominate candidate | CANDIDATE:NOMINATE | Any member |
| Cast vote | VOTE:CAST | Eligible member |
| View results | VOTE:VIEW_RESULTS | After published |
| Admin operations | ELECTION:FORCE_MANAGE | Super Admin |

---

## ❌ Common Errors & Fixes

### Error 1: Invalid Enum Value
```
Status: 400 Bad Request
Message: "Invalid enum value... not one of [PRESIDENT, ...]"

Fix: Use exact value from list, case-sensitive
```

### Error 2: Election Not Found
```
Status: 404 Not Found
Message: "Election with ID 999 not found"

Fix: Verify election ID exists
```

### Error 3: Missing Required Field
```
Status: 400 Bad Request
Message: "Input validation failed"
validationErrors: {
  "electionType": "Election type is required"
}

Fix: Add all required fields to request
```

### Error 4: Wrong Election Status
```
Status: 400 Bad Request
Message: "Cannot open voting while in NOMINATION_OPEN status"

Fix: Ensure election is in correct state before transition
```

### Error 5: Insufficient Candidates
```
Status: 400 Bad Request
Message: "Cannot open voting with less than 2 approved candidates"

Fix: Get at least 2 candidates approved first
```

### Error 6: Unauthorized
```
Status: 403 Forbidden
Message: "You don't have permission for ELECTION:MANAGE"

Fix: Check your role/permissions with admin
```

---

## 🧪 Test with cURL

### Create Election
```bash
curl -X POST "http://localhost:8080/api/v1/club/election" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Test Election",
    "clubId": 1,
    "electionType": "PRESIDENT",
    "nominationStartTime": "2026-03-20T09:00:00",
    "nominationEndTime": "2026-03-25T17:00:00",
    "votingStartTime": "2026-03-27T09:00:00",
    "votingEndTime": "2026-03-28T17:00:00"
  }'
```

### Test Invalid Enum (Should get 400)
```bash
curl -X POST "http://localhost:8080/api/v1/club/election" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Test",
    "clubId": 1,
    "electionType": "PRESIDENTIAL"
  }'

# Returns HTTP 400 with helpful message
```

### Get Elections by Status
```bash
curl -X GET "http://localhost:8080/api/v1/club/election/status/VOTING_OPEN" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Cast Vote
```bash
curl -X POST "http://localhost:8080/api/v1/club/election/vote" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "electionId": 1,
    "candidateId": 5
  }'
```

---

## 📊 Response Status Codes

| Code | Meaning | Action |
|------|---------|--------|
| 200 | OK - Success | Check response body |
| 201 | Created - Resource created | Check response body for new resource |
| 204 | No Content - Deletion success | No body to check |
| 400 | Bad Request - Invalid input | Check error message & fix |
| 401 | Unauthorized - Missing/invalid token | Refresh token |
| 403 | Forbidden - No permission | Ask admin to grant permission |
| 404 | Not Found - Resource missing | Verify ID exists |
| 409 | Conflict - Can't perform action | Check election status/constraints |
| 500 | Server Error | Report bug (shouldn't happen now!) |

---

## 🎯 Testing Checklist

- [ ] Import Postman collection
- [ ] Set `baseUrl` variable
- [ ] Set `token` variable
- [ ] Run "Create Election" with PRESIDENT type → 201
- [ ] Try "Create Election" with PRESIDENTIAL type → 400
- [ ] Run "Get Elections by Status" → 200
- [ ] Run "Open Nominations" → 200
- [ ] Run "Nominate Candidate" → 201
- [ ] Check error messages are clear

---

## 🔗 Important Links

| Resource | Path |
|----------|------|
| Postman Collection | `/docs/postman/Nextora_Election_Module.postman_collection.json` |
| Full Documentation | `/ELECTION_MODULE_FIXES.md` |
| Quick Start | `/ELECTION_QUICK_START.md` |
| This Reference | `/ELECTION_QUICK_REFERENCE.md` |

---

## ⏱️ Common Timeouts & Rates

- Session timeout: 30 min
- Token expiry: Check auth config
- Rate limit: Check RateLimitFilter config
- DB connection pool: 10 connections (HikariCP)

---

## 🚀 Performance Tips

1. Use pagination: `?page=0&size=20`
2. Filter by status to narrow results
3. Use search for specific elections
4. Cache election details on client side
5. Pre-load candidate photos

---

## 📞 Support

| Issue | Solution |
|-------|----------|
| Questions | Read ELECTION_MODULE_FIXES.md |
| Bug report | Check error message in response |
| Permission issues | Contact admin |
| Integration help | Check Postman collection examples |

---

**Version:** 2.0 (Updated 2026-03-17)  
**Status:** ✅ Production Ready

