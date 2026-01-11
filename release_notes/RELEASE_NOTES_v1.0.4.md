# Shelter For Mind v1.0.4 - UI/UX Improvements

**Release Date:** January 11, 2026

## ✨ User Interface Enhancements

This release focuses on polishing the user interface with smooth animations, responsive layouts, and consistent styling across all menus.

---

## What's New & Fixed

### 🎨 Login/Signup Improvements
- **Fade Transitions:** Added smooth fade in/out effects when switching between login and signup forms
- **Visual Polish:** Improved form switching experience with 300ms animations

### 📱 Dashboard Responsive Layout
- **Smart Positioning:** Menu buttons and mood tracker now scale proportionally with window size
- **No More Static UI:** All dashboard elements maintain correct positions when resizing or maximizing
- **Dynamic Alignment:** Buttons automatically reposition based on window dimensions (8-21% from edges)

### 🖼️ Profile Menu Consistency
- **Clean Design:** Removed drop shadows from all submenu boxes to match Mood Analysis style
- **Unified Look:** All sections (Basic Info, Journals, Blogs, Mood Analysis) now have consistent flat design
- **Hidden Scrollbars:** Removed visible scroll pipes for cleaner appearance
- **Transparent Backgrounds:** Eliminated ash-colored container backgrounds for seamless gradient view

### 🐛 Window Management Fixes
- **Screen Centering:** Application window now properly centers on screen at startup
- **Splash Screen:** Fixed initial window positioning issues

---

## Technical Changes

- Updated `LoginSignupController.java` with fade transition logic in `switchMenu()`
- Enhanced `DashboardController.java` with responsive layout system using `setupResponsiveLayout()` and `updateElementPositions()`
- Refined `profile.css` - removed shadows, made backgrounds transparent, hidden scrollbars
- Modified `dashboard.fxml` to use percentage-based AnchorPane constraints

---

## Upgrade Instructions

### For Existing Users:
1. Download the new installer
2. Install v1.0.4 (will update existing installation)
3. Launch and enjoy the improved UI!

---

## Platform Compatibility

- ✅ **Windows:** Fully tested and working
- ✅ **Linux:** All features supported
- ✅ **macOS:** Compatible

---

**Note:** This is a maintenance release focused on cross-platform compatibility. All features from v1.0.2 are preserved.

**Previous Release:** [v1.0.3](https://github.com/abdullahsorwar/shelter_for_mind/releases/v1.0.3) - January 10, 2026