# Keeper (Admin) Signup and Email Verification System

## Overview
Implemented a complete keeper signup and approval workflow with email verification.

## Database Structure

### Tables Created

#### 1. `keeper_signups`
Stores pending keeper signup requests.
```sql
CREATE TABLE keeper_signups (
  keeper_id       TEXT PRIMARY KEY,
  email           TEXT NOT NULL UNIQUE,
  password_hash   TEXT NOT NULL,
  email_verified  BOOLEAN DEFAULT FALSE,
  status          TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  approved_at     TIMESTAMPTZ,
  approved_by     TEXT
)
```

#### 2. `keepers`
Stores approved keepers who can log in.
```sql
CREATE TABLE keepers (
  keeper_id       TEXT PRIMARY KEY,
  email           TEXT NOT NULL UNIQUE,
  password_hash   TEXT NOT NULL,
  approved_at     TIMESTAMPTZ DEFAULT NOW(),
  approved_by     TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  last_login      TIMESTAMPTZ
)
```

## Workflow

### 1. Keeper Signup Process
1. User fills in: Email, Keeper ID, Password
2. System validates:
   - Email format is valid
   - Keeper ID is unique (3-20 chars, lowercase, alphanumeric + underscore)
   - Password is strong (minimum 8 characters)
   - Email is not already registered
3. Password is hashed using SHA-256
4. Signup request is saved to `keeper_signups` table with `email_verified=false`, `status='PENDING'`

### 2. Email Verification
1. Verification email is sent to the provided email address
2. Email contains a unique verification link: `http://localhost:8080/verify-keeper?token={token}&keeper_id={keeper_id}`
3. User clicks the link in their email
4. Browser opens showing success page
5. Database is updated: `email_verified=true` for that keeper_id
6. Existing keepers are notified (console log for now)

### 3. Admin Approval (To Be Implemented)
1. Existing keepers can view pending signups via admin dashboard
2. They can approve or reject requests
3. When approved:
   - Record is moved from `keeper_signups` to `keepers` table
   - Status is updated to 'APPROVED'
   - Approval notification email is sent to the new keeper
4. New keeper can now log in

### 4. Keeper Login
1. User enters Keeper ID and Password
2. System checks:
   - If keeper exists in `keepers` table (approved)
   - If not, checks `keeper_signups` for pending request
   - Shows appropriate message:
     - "Account not found" - No signup exists
     - "Email not verified" - Signup exists but email not verified
     - "Approval pending" - Email verified, waiting for admin approval
     - "Account rejected" - Request was rejected
3. If approved, authenticates password hash
4. On success, navigates to keeper dashboard (to be implemented)

## Email Templates

### Verification Email
- **Subject:** shelter_of_mind - Keeper Account Email Verification
- **Content:** Includes verification link and explanation of approval process

### Approval Notification Email
- **Subject:** shelter_of_mind - Keeper Account Approved! 🎉
- **Content:** Congratulations message and login instructions

## Files Created/Modified

### New Files
1. **KeeperRepository.java**
   - Database operations for keepers
   - Signup request management
   - Authentication logic
   - Password hashing

### Modified Files
1. **DbMigrations.java**
   - Added `keeper_signups` table
   - Added `keepers` table
   - Added indexes for email lookups

2. **EmailService.java**
   - Added `sendKeeperVerificationEmail()`
   - Added `sendKeeperApprovalNotification()`
   - Added `notifyExistingKeepersOfNewSignup()`

3. **VerificationServer.java**
   - Added `/verify-keeper` endpoint
   - Added `KeeperVerificationHandler` inner class
   - Updates `keeper_signups.email_verified` on verification

4. **AdminLoginController.java**
   - Integrated signup with email verification
   - Added validation for email, keeper_id, password
   - Added login authentication with proper status checks
   - Background threads for database operations

## Security Features
- ✅ Password hashing (SHA-256)
- ✅ Email format validation
- ✅ Keeper ID format validation (lowercase only)
- ✅ Unique email constraint
- ✅ Unique keeper_id constraint
- ✅ Email verification required
- ✅ Admin approval required
- ✅ Secure verification tokens (UUID + timestamp)
- ✅ Single-use verification links

## Next Steps (To Be Implemented)
1. **Admin Dashboard:**
   - View pending keeper signup requests
   - Approve/reject requests with one click
   - View list of all approved keepers
   - Email notification to all existing keepers when new signup arrives

2. **Keeper Dashboard:**
   - Access after successful login
   - Manage users (souls)
   - View analytics
   - Moderate content
   - Manage pending approvals

3. **Enhanced Security:**
   - Implement proper BCrypt for password hashing
   - Add token expiration (24 hours)
   - Add rate limiting for signup attempts
   - Add CAPTCHA for bot protection

4. **Email Improvements:**
   - HTML email templates with styling
   - Resend verification email functionality
   - Email notification to existing keepers with approval link

## Usage

### For New Keepers
1. Click "Soul Keeper" button on initial page
2. Click "Create account" link
3. Fill in Email, Keeper ID, Password
4. Click "SIGN UP"
5. Check email and click verification link
6. Wait for approval email
7. Once approved, log in with credentials

### For Existing Keepers (After Dashboard Implementation)
1. Log in to keeper dashboard
2. Navigate to "Pending Approvals" section
3. Review new keeper signup requests
4. Approve or reject with one click
5. Approved keepers receive email notification

## Testing
1. Start the application
2. Navigate to keeper login page
3. Click "Create account"
4. Enter test email (must be valid and accessible)
5. Enter test keeper_id (e.g., "test_keeper_123")
6. Enter password (minimum 8 characters)
7. Click SIGN UP
8. Check email inbox for verification link
9. Click verification link
10. Return to app and try to log in (will show "Approval pending" message)
11. Use admin dashboard to approve the request (when implemented)
12. Receive approval email
13. Log in successfully
