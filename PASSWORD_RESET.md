# Password Reset System Documentation

## Overview
The shelter_of_mind application now includes a secure email-based password reset system for keeper (admin) accounts. This feature allows keepers who have forgotten their passwords to reset them securely without admin intervention.

## Architecture

### Components

1. **Database Layer** (`KeeperRepository.java`)
   - `PasswordResetToken` class: Holds token data (token, keeperId, expiresAt, used)
   - `createPasswordResetToken(keeperId)`: Generates UUID token, stores in DB with 1-hour expiration
   - `validateResetToken(token)`: Checks if token is valid, not expired, and not used
   - `resetPassword(token, newHashedPassword)`: Updates password and marks token as used
   - `getKeeperEmail(keeperId)`: Retrieves keeper's email address (made public for access)

2. **Database Schema** (`DbMigrations.java`)
   ```sql
   CREATE TABLE keeper_password_resets (
     token         TEXT PRIMARY KEY,
     keeper_id     TEXT NOT NULL REFERENCES keepers(keeper_id) ON DELETE CASCADE,
     expires_at    TIMESTAMPTZ NOT NULL,
     used          BOOLEAN DEFAULT FALSE,
     created_at    TIMESTAMPTZ DEFAULT NOW()
   );
   
   -- Indexes for performance
   CREATE INDEX idx_password_resets_keeper ON keeper_password_resets(keeper_id);
   CREATE INDEX idx_password_resets_expiry ON keeper_password_resets(expires_at, used);
   ```

3. **Email Service** (`EmailService.java`)
   - `sendPasswordResetEmail(email, keeperId, token)`: Sends reset link to keeper's email
   - Email includes:
     - Reset link: `http://localhost:8080/reset-password?token={token}`
     - Warning about 1-hour expiration
     - Security notice if request was not made by keeper

4. **HTTP Server** (`PasswordResetServer.java`)
   - Lightweight HTTP server on port 8080
   - Handles `/reset-password?token={token}` requests
   - Displays HTML confirmation page
   - Triggers JavaFX application to show password reset form
   - Auto-started on app launch, stopped on app close

5. **Admin Login UI** (`AdminLoginController.java`)
   - "Forgot Password?" link on login page
   - Shows dialog prompting for Keeper ID
   - Validates keeper exists
   - Generates token and sends reset email
   - Shows success message with keeper's email

6. **Password Reset UI** (`PasswordResetController.java` + `password_reset.fxml` + `password_reset.css`)
   - Dedicated password reset form with:
     - New password field
     - Confirm password field
     - Show/hide password toggle
     - Validation (minimum 6 characters, passwords match)
   - Token validation on page load
   - SHA-256 password hashing before storage
   - Light/dark theme support matching admin login page
   - Fully responsive layout (adapts to window size)
   - Gradient background matching admin login theme
   - Smooth animations and transitions
   - CSS-based styling with theme classes

## User Flow

### Step 1: Request Password Reset
1. Keeper clicks "Forgot Password?" on admin login page
2. Dialog prompts for Keeper ID
3. System validates keeper exists
4. System generates unique UUID token (expires in 1 hour)
5. Email sent to keeper's registered email address
6. Success message displays (shows email for confirmation)

### Step 2: Receive Reset Email
```
Subject: shelter_of_mind - Password Reset Request

Hello keeper "{keeper_id}",

We received a request to reset your password for your shelter_of_mind keeper account.

Click the link below to reset your password:
http://localhost:8080/reset-password?token={uuid_token}

This link will expire in 1 hour.

If you did not request a password reset, please ignore this email. 
Your password will remain unchanged.

Regards,
The keepers of souls
shelter_of_mind
```

### Step 3: Click Reset Link
1. Keeper clicks link in email
2. Browser opens `http://localhost:8080/reset-password?token={token}`
3. HTTP server handles request:
   - Displays HTML confirmation page
   - Triggers JavaFX app to open password reset form
   - Browser window can be closed

### Step 4: Set New Password
1. Password reset form opens in application
2. System validates token immediately:
   - Checks token exists
   - Checks not expired (< 1 hour old)
   - Checks not already used
3. If valid, keeper enters:
   - New password (min 6 characters)
   - Confirm password (must match)
4. Keeper clicks "RESET PASSWORD"
5. System:
   - Validates password meets requirements
   - Hashes password using SHA-256
   - Updates keeper's password in database
   - Marks token as used (prevents reuse)
6. Success message displays
7. Keeper redirected to login page

## Security Features

1. **Token Security**
   - UUID (Universally Unique Identifier) prevents guessing
   - One-time use only (marked as used after successful reset)
   - 1-hour expiration window
   - Stored in separate table with foreign key constraint
   - Cascade delete if keeper account deleted

2. **Password Security**
   - SHA-256 hashing before storage
   - Minimum 6 character requirement
   - Password confirmation required
   - No password hints or recovery questions

3. **Email Security**
   - Reset link sent only to registered email
   - Clear warning if request was unauthorized
   - No password included in email

4. **Database Security**
   - Transactional password updates (commit/rollback)
   - Indexed queries for performance
   - Foreign key constraints maintain data integrity

5. **Rate Limiting Considerations**
   - TODO: Consider adding rate limiting to prevent token flooding
   - TODO: Consider limiting concurrent reset requests per keeper

## Configuration

### Email Settings (`EmailService.java`)
```java
private static final String SMTP_HOST = "smtp.gmail.com";
private static final String SMTP_PORT = "587";
private static final String FROM_EMAIL = "the.pathfinders.dev@gmail.com";
```

### HTTP Server Settings (`PasswordResetServer.java`)
```java
private static final int PORT = 8080;
```

### Token Expiration (`KeeperRepository.java`)
```java
// Token expires in 1 hour
VALUES (?, ?, NOW() + INTERVAL '1 hour', false, NOW())
```

## Testing

### Manual Testing Checklist

1. **Forgot Password Flow**
   - [ ] Click "Forgot Password?" link
   - [ ] Enter valid Keeper ID
   - [ ] Verify email received
   - [ ] Verify email contains correct reset link

2. **Token Validation**
   - [ ] Click reset link within 1 hour → Should work
   - [ ] Click reset link after 1 hour → Should show expired error
   - [ ] Click reset link twice → Second time should show already used error
   - [ ] Enter invalid/random token → Should show invalid token error

3. **Password Reset**
   - [ ] Enter mismatched passwords → Should show error
   - [ ] Enter password < 6 chars → Should show error
   - [ ] Enter valid matching passwords → Should succeed
   - [ ] Login with old password → Should fail
   - [ ] Login with new password → Should succeed

4. **Error Handling**
   - [ ] Enter non-existent Keeper ID → Should show "Keeper ID not found"
   - [ ] Disconnect from internet → Should show email send error
   - [ ] Invalid token format → Should show validation error

5. **UI/UX**
   - [ ] Theme toggle works on password reset page
   - [ ] Password show/hide toggle works
   - [ ] "Back to Login" link works
   - [ ] Animations play smoothly
   - [ ] Form clears properly

## Future Enhancements

1. **Security Improvements**
   - Add CAPTCHA to prevent automated requests
   - Implement rate limiting (max 3 requests per hour per keeper)
   - Add IP tracking for security audit logs
   - Email notification when password successfully changed

2. **User Experience**
   - Allow reset by email address instead of just Keeper ID
   - Add password strength indicator
   - Send confirmation email after successful reset
   - Show countdown timer for token expiration

3. **Monitoring**
   - Log all password reset attempts
   - Dashboard for admins to view reset activity
   - Alert system for suspicious activity (multiple failed attempts)

4. **Production Considerations**
   - Use HTTPS instead of HTTP for reset links
   - Configure proper domain instead of localhost
   - Set up email templates with HTML styling
   - Add email delivery monitoring

## Troubleshooting

### Issue: Email not received
- Check spam/junk folder
- Verify keeper's email is correct in database
- Check EmailService credentials
- Verify SMTP settings allow less secure apps (Gmail)

### Issue: Reset link doesn't work
- Verify HTTP server started (check console: "Password reset server started on port 8080")
- Check firewall isn't blocking port 8080
- Verify token hasn't expired (check `expires_at` in database)
- Ensure application is running when clicking link

### Issue: Token validation fails
- Check database connectivity
- Verify `keeper_password_resets` table exists
- Check token format (should be UUID string)
- Ensure token not already used (`used` column = false)

### Issue: Password update fails
- Check database permissions
- Verify keeper account exists
- Check SHA-256 hashing is working
- Review transaction logs for rollback reasons

## Database Queries for Debugging

### Check active reset tokens
```sql
SELECT token, keeper_id, expires_at, used, created_at 
FROM keeper_password_resets 
WHERE used = false 
  AND expires_at > NOW()
ORDER BY created_at DESC;
```

### Check keeper's reset history
```sql
SELECT * FROM keeper_password_resets 
WHERE keeper_id = 'your_keeper_id' 
ORDER BY created_at DESC;
```

### Clean up expired tokens
```sql
DELETE FROM keeper_password_resets 
WHERE expires_at < NOW() 
   OR used = true;
```

### Verify keeper email
```sql
SELECT keeper_id, keeper_email 
FROM keepers 
WHERE keeper_id = 'your_keeper_id';
```

## Maintenance

### Regular Tasks
1. Clean up expired/used tokens (run weekly):
   ```sql
   DELETE FROM keeper_password_resets 
   WHERE expires_at < NOW() - INTERVAL '7 days';
   ```

2. Monitor failed reset attempts
3. Review security logs for suspicious patterns
4. Update email templates as needed

### Backup Considerations
- `keeper_password_resets` table should be backed up
- Old tokens can be purged after 30 days
- Keep audit trail of successful resets

## Contact
For questions or issues with the password reset system, contact the development team or refer to the main project documentation.
