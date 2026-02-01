# Push Notification Module

## Overview

This module provides Firebase Cloud Messaging (FCM) based push notification capabilities for the Nextora application. It supports:

- Multi-device token management per user
- Role-based notification targeting
- Async notification sending (non-blocking)
- Automatic cleanup of stale/invalid tokens
- Production-ready security with JWT authentication

## Architecture

```
infrastructure/notification/push/
├── config/
│   ├── FirebaseConfig.java      # Firebase Admin SDK initialization
│   └── AsyncConfig.java         # Async executor configuration
├── controller/
│   └── PushNotificationController.java  # REST API endpoints
├── dto/
│   ├── request/
│   │   ├── RegisterTokenRequest.java    # Token registration payload
│   │   └── SendNotificationRequest.java # Send notification payload
│   └── response/
│       ├── NotificationResponse.java    # Send result response
│       └── TokenRegistrationResponse.java
├── entity/
│   └── FcmToken.java            # Token persistence entity
├── exception/
│   └── PushNotificationException.java
├── repository/
│   └── FcmTokenRepository.java  # Token CRUD operations
├── scheduler/
│   └── FcmTokenCleanupScheduler.java  # Stale token cleanup
└── service/
    ├── PushNotificationService.java     # Service interface
    └── impl/
        └── PushNotificationServiceImpl.java
```

## API Endpoints

### Token Management

#### Register Token
```http
POST /api/v1/push/token
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "token": "fcm_token_from_client",
  "deviceInfo": "Chrome on Windows"  // Optional
}
```

#### Remove Token (Logout)
```http
DELETE /api/v1/push/token?token=fcm_token_value
Authorization: Bearer <jwt_token>
```

#### Remove All Tokens (Logout All Devices)
```http
DELETE /api/v1/push/token/all
Authorization: Bearer <jwt_token>
```

### Send Notifications (Admin Only)

#### Send to Specific Users
```http
POST /api/v1/push/send
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json

{
  "title": "New Announcement",
  "body": "Important update for all students",
  "userIds": [1, 2, 3],
  "data": {
    "type": "announcement",
    "announcementId": "123"
  },
  "clickAction": "/announcements/123"
}
```

#### Send to Role
```http
POST /api/v1/push/send
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json

{
  "title": "Election Reminder",
  "body": "Voting ends in 24 hours!",
  "targetRole": "ROLE_STUDENT",
  "imageUrl": "https://example.com/election-banner.png",
  "data": {
    "type": "election",
    "electionId": "456"
  }
}
```

### Check Status
```http
GET /api/v1/push/status
Authorization: Bearer <jwt_token>

Response:
{
  "success": true,
  "data": {
    "enabled": true
  }
}
```

## Configuration

### Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `FIREBASE_ENABLED` | No | `false` (dev) / `true` (prod) | Enable push notifications |
| `FIREBASE_SERVICE_ACCOUNT_PATH` | Yes (if enabled) | - | Path to Firebase service account JSON |

### application.yml
```yaml
firebase:
  enabled: ${FIREBASE_ENABLED:false}
  service-account:
    path: ${FIREBASE_SERVICE_ACCOUNT_PATH:}
  token-cleanup:
    enabled: true
    days-old: 30
    cron: "0 0 3 * * *"  # Daily at 3 AM
```

## Firebase Setup

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing
3. Enable Cloud Messaging

### 2. Generate Service Account Key
1. Go to Project Settings → Service Accounts
2. Click "Generate new private key"
3. Save the JSON file securely (NEVER commit to git!)

### 3. Configure Environment
```bash
# Development
export FIREBASE_ENABLED=true
export FIREBASE_SERVICE_ACCOUNT_PATH=/path/to/serviceAccountKey.json

# Or use .env file (with spring-dotenv)
FIREBASE_ENABLED=true
FIREBASE_SERVICE_ACCOUNT_PATH=/path/to/serviceAccountKey.json
```

## Usage Examples

### Programmatic Notification Sending

```java
@Service
@RequiredArgsConstructor
public class AnnouncementService {
    
    private final PushNotificationService pushNotificationService;
    
    public void createAnnouncement(Announcement announcement) {
        // Save announcement...
        
        // Send push notification to all students (async)
        Map<String, String> data = Map.of(
            "type", "announcement",
            "id", announcement.getId().toString()
        );
        
        pushNotificationService.sendToRoleAsync(
            UserRole.ROLE_STUDENT,
            announcement.getTitle(),
            announcement.getSummary(),
            data
        );
    }
}
```

### Event-Driven Notifications

```java
@Component
@RequiredArgsConstructor
public class ElectionNotificationListener {
    
    private final PushNotificationService pushNotificationService;
    
    @EventListener
    public void onElectionStarted(ElectionStartedEvent event) {
        Map<String, String> data = Map.of(
            "type", "election_started",
            "electionId", event.getElectionId().toString()
        );
        
        pushNotificationService.sendToRoleAsync(
            UserRole.ROLE_STUDENT,
            "Voting is Now Open!",
            "Cast your vote for " + event.getElectionName(),
            data
        );
    }
}
```

## Notification Payload Structure

### Standard Payload (sent to FCM)
```json
{
  "notification": {
    "title": "Notification Title",
    "body": "Notification body text",
    "image": "https://example.com/image.png"
  },
  "data": {
    "click_action": "/path/to/navigate",
    "type": "custom_type",
    "customKey": "customValue"
  },
  "android": {
    "priority": "HIGH",
    "ttl": "3600s"
  },
  "webpush": {
    "notification": {
      "icon": "/icons/notification-icon.png"
    },
    "fcmOptions": {
      "link": "/path/to/navigate"
    }
  },
  "apns": {
    "payload": {
      "aps": {
        "sound": "default"
      }
    }
  }
}
```

## Security Considerations

1. **Service Account Key**: Never commit to version control. Use environment variables or secrets management.

2. **Token Validation**: Invalid/expired tokens are automatically removed when detected during send operations.

3. **Authorization**: 
   - Token registration: Any authenticated user
   - Send notifications: Requires `PUSH:SEND` permission (Admin roles)

4. **Token Ownership**: Tokens are reassigned when a user logs in on a device previously used by another user.

## Database Schema

```sql
CREATE TABLE fcm_tokens (
    id              BIGINT PRIMARY KEY,
    token           VARCHAR(512) NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL,
    role            VARCHAR(30) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_at    TIMESTAMP,
    device_info     VARCHAR(255),
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);

-- Indexes
CREATE INDEX idx_fcm_token_user_id ON fcm_tokens(user_id);
CREATE INDEX idx_fcm_token_role ON fcm_tokens(role);
CREATE INDEX idx_fcm_token_active ON fcm_tokens(is_active);
```

## Troubleshooting

### Notifications Not Sending
1. Check if Firebase is enabled: `GET /api/v1/push/status`
2. Verify service account path is correct
3. Check logs for Firebase initialization errors

### Invalid Token Errors
- Tokens are automatically cleaned up
- Check `last_used_at` to identify stale tokens
- Verify frontend is refreshing tokens correctly

### Performance
- Batch sending is used (up to 500 tokens per batch)
- Async methods don't block the main thread
- Consider using topics for very large audiences
