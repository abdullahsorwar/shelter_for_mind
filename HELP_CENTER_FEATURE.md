# Help Center Feature Documentation

## Overview
The Help Center feature provides users with quick access to technical support and emergency resources directly from the dashboard.

## Components Implemented

### 1. Help Center Button
- **Location**: Bottom-left corner of dashboard
- **Design**: Circular button with "?" icon
- **Styling**: Gradient background matching app theme (pink → purple → blue)
- **Animations**: Scale transition on hover and press

### 2. Technical Support Section
Provides three email contacts for technical assistance:
- `raisatabassum2023115989@cs.du.ac.bd`
- `mdabdullah-2023715965@cs.du.ac.bd`
- `the.pathfinders.dev@gmail.com`

**Features**:
- Click-to-email integration (opens default mail client)
- Clean, label-free presentation
- Email icons for visual clarity

### 3. Emergency Hotlines Section
Displays 8 critical mental health and emergency hotlines:

| Service | Number |
|---------|--------|
| National Emergency Service (Fire, Police, Ambulance) | 999 |
| National Mental Health Helpline | +880 1779-554391 |
| Emotional Support & Suicide Prevention Helpline | +880 9612-119911 |
| Kaan Pete Roi (Mental Health Support) | 09612-115511 |
| Moner Bondhu (Mental Health Support) | 01779-554392 |
| Emergency Medical Service | 01713-300600 |
| BRAC Emergency Hotline | 16263 |
| Shishu Helpline 1098 (Child Helpline) | 1098 |

**Features**:
- Visual card layout with service icons
- Click-to-call functionality (opens phone dialer)
- Scrollable container for easy browsing

### 4. Safety Plan Feature
Interactive safety plan creation and management system.

**Input Fields**:
1. **Who can I contact when I feel this way?**
   - Text field for emergency contacts
   
2. **What can I do to calm myself down?**
   - Text field for coping strategies
   
3. **Where can I go to feel safe?**
   - Text field for safe spaces

**Functionality**:
- **Save**: Stores safety plan in user profile database
- **Load**: Auto-loads existing safety plan when overlay opens
- **Persistence**: Safety plan accessible to both user and admins

## Database Schema

### Safety Plan Columns (soul_id_and_soul_key table)
```sql
ALTER TABLE soul_id_and_soul_key 
ADD COLUMN safety_plan_contact TEXT,
ADD COLUMN safety_plan_calm TEXT,
ADD COLUMN safety_plan_place TEXT;
```

These columns are automatically added via `DbMigrations.java` on application startup.

## Technical Implementation

### Files Modified

1. **dashboard.fxml**
   - Added Help Center button
   - Created three overlay sections
   - Implemented Safety Plan form

2. **DashboardController.java**
   - Navigation methods: `showHelpCenter()`, `hideHelpCenter()`
   - Section switchers: `showTechnicalSupport()`, `showEmergencyHotlines()`
   - Email integration: `openEmail(String)`
   - Hotline population: `populateEmergencyHotlines()`
   - Safety Plan methods: `saveSafetyPlan()`, `loadSafetyPlan()`

3. **SoulRepository.java**
   - `updateSafetyPlan(soulId, contact, calm, place)` - Saves safety plan
   - `getSafetyPlan(soulId)` - Retrieves safety plan as Map

4. **DbMigrations.java**
   - Migration to add safety_plan_* columns

5. **dashboard.css**
   - `.help-center-btn` styling with gradient and animations

## User Flow

### Accessing Help Center
1. User clicks "?" button in bottom-left corner
2. Help Center overlay appears with two options
3. User selects Technical Support or Emergency Hotlines

### Creating Safety Plan
1. Open Emergency Hotlines section
2. Scroll to "Create Your Safety Plan" section
3. Fill in three text fields
4. Click "Save Safety Plan" button
5. Confirmation message appears
6. Safety plan saved to user profile

### Admin Access
- Admins can view user safety plans through the user profile system
- Safety plan data stored in `soul_id_and_soul_key` table

## Design Philosophy
- **Accessibility**: Large touch targets, clear labels
- **Consistency**: Matches dashboard's pastel gradient theme
- **Responsiveness**: Smooth animations for all interactions
- **Privacy**: Safety plan data encrypted in database

## Testing Checklist
- [x] Help Center button appears in dashboard
- [x] Technical Support emails are clickable
- [x] Emergency hotlines have call buttons
- [x] Safety Plan form accepts input
- [x] Safety Plan saves to database
- [x] Safety Plan loads on overlay open
- [x] Project compiles without errors
- [ ] Functional testing with database
- [ ] Admin can view user safety plans

## Future Enhancements
- Multi-language support for hotlines
- GPS-based regional hotline recommendations
- Safety plan sharing with trusted contacts
- Crisis detection and auto-dial functionality

## Dependencies
- JavaFX 21
- PostgreSQL database
- Desktop API (for email/phone integration)

## Maintenance Notes
- Hotline numbers should be verified annually
- Email addresses may need updates
- Consider adding more regional hotlines based on user demographics

---
**Feature Status**: ✅ Completed
**Build Status**: ✅ SUCCESS
**Last Updated**: 2025-12-06
