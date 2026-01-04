# Shelter For Mind v1.0.1 - Bug Fix Release

**Release Date:** January 4, 2026

## 🐛 Critical Bug Fix

This release addresses a critical issue where blog content was not accessible in the installed application.

---

## What's Fixed

### 🔧 Blog Content Access Issue
- **Problem:** After installing the application via the MSI installer, users could not access the mental health blog library
- **Cause:** Blog data files were stored outside the application package and were not included during installation
- **Solution:** 
  - Moved all blog content files into the application resources
  - Updated the blog loader to read from packaged resources instead of external files
  - Blogs are now fully accessible in the installed application

### 📁 User Data Storage
- **Improvement:** User-saved data (saved journals and blogs) now properly stored in the user's home directory
- **Location:** `~/.shelter_for_mind/` (or `C:\Users\YourName\.shelter_for_mind\` on Windows)
- **Benefits:**
  - Ensures write permissions in installed applications
  - Data persists across application updates
  - No permission issues when saving favorites

---

## Technical Changes

### Files Modified:
- `BlogContentLoader.java` - Changed from filesystem to classpath resource loading
- `SavedJournalsManager.java` - Updated to use user home directory for data storage
- `SavedBlogsManager.java` - Updated to use user home directory for data storage
- Project structure - Moved `/data/blogs/` to `/src/main/resources/data/blogs/`

---

## Upgrade Instructions

### For New Installations:
1. Download the installer
2. Run the installer
3. Launch "Shelter for Mind" from your applications menu

### For Existing Users (v1.0.0):
1. Uninstall the previous version
2. Install v1.0.1 using the new installer
3. Your saved data will be preserved (journals, saved blogs, user preferences)

**Note:** If you had saved blogs or journals in v1.0.0, they were stored in the application's data folder. In v1.0.1, the app will create a new storage location in your home directory. You may need to re-save your favorite blogs and journals.

---

## Download

Get the installer for your platform:
- **Windows:** `Shelter-for-Mind-1.0.1.msi`

---

## Support

If you encounter any issues:
1. Check the [README.md](README.md) for troubleshooting tips
2. Report bugs by creating an issue on our repository
3. Contact: the_pathfinders team

---

## Acknowledgments

Thank you to our users for reporting the blog access issue. Your feedback helps us improve!

---

**Previous Release:** [v1.0.0](RELEASE_NOTES.md) - January 3, 2026
