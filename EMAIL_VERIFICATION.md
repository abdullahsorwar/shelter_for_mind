# Email Verification System

## Overview
The email verification system uses a combination of:
- **JavaMail API** - Sends verification emails via Gmail SMTP
- **Local HTTP Server** - Handles verification link clicks (port 8080)
- **WebSocket Server** - Real-time updates to the UI (port 8081)

## How It Works

1. **User clicks "Verify Email" button** in Profile page
2. **Verification servers start automatically** (HTTP on port 8080, WebSocket on port 8081)
3. **Email is sent** to the user's email address with a verification link
4. **User clicks the link** in their email
5. **Browser opens** showing success/error page
6. **Database is updated** (email_verified = true)
7. **WebSocket notifies the app** in real-time
8. **Verify button disappears** automatically

## Configuration

### Email Settings (in EmailService.java)
```java
FROM_EMAIL = "the.pathfinders.dev@gmail.com"
APP_PASSWORD = "uaqs kqnu lrrz calf"  // Gmail App Password
```

### Server Ports
- HTTP Verification Server: `8080`
- WebSocket Server: `8081`

## Email Template

**Subject:** shelter_of_mind email Verification

**Body:**
```
Hello there, our good soul "{soul_id}"!

Use the following link to verify your email:

http://localhost:8080/verify?token={token}&soul_id={soul_id}

Have a good day!

Regards,
The keepers of your soul
shelter_of_mind
```

## Components

### 1. EmailService.java
- Sends verification emails using Gmail SMTP
- Generates secure verification tokens
- Uses TLS encryption

### 2. VerificationServer.java
- HTTP server on port 8080
- Handles GET requests to `/verify`
- Validates tokens and updates database
- Shows beautiful success/error HTML pages

### 3. VerificationWebSocketServer.java
- WebSocket server on port 8081
- Real-time communication with JavaFX app
- Notifies when email is verified

### 4. VerificationManager.java
- Singleton that coordinates everything
- Starts/stops both servers
- Manages verification flow

### 5. ProfileController.java (Updated)
- Integrates with verification system
- WebSocket client for real-time updates
- Auto-hides verify button when verified

## Security Features

- ✅ Unique random tokens per verification
- ✅ Token validation before marking as verified
- ✅ Tokens are single-use (removed after verification)
- ✅ TLS encryption for email sending
- ✅ Gmail App Password (not plain password)

## Testing

1. **Start the app** and go to Profile page
2. **Enter a valid email** and save
3. **Click "Verify Email"** button
4. **Check your email inbox** (the.pathfinders.dev@gmail.com will be sender)
5. **Click the verification link** in the email
6. **Browser opens** with success message
7. **Return to app** - verify button should disappear automatically!

## Troubleshooting

### Email not sending
- Check internet connection
- Verify Gmail App Password is correct
- Check if Gmail account is active
- Look for errors in console

### Verification link doesn't work
- Ensure HTTP server is running (port 8080)
- Check if port 8080 is not blocked by firewall
- Verify the link format is correct

### Button doesn't disappear
- Check WebSocket connection (port 8081)
- Verify WebSocket server is running
- Look for WebSocket errors in console

### Port already in use
- Stop any other applications using ports 8080 or 8081
- Restart the application

## Future Enhancements

- [ ] Email resend functionality (rate limiting)
- [ ] Token expiration (e.g., 24 hours)
- [ ] Email templates with HTML styling
- [ ] Support for other email providers
- [ ] Verification history tracking
