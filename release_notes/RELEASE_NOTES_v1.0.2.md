# Shelter For Mind v1.0.2 - Linux Compatibility Fix and Mac Installer Addition

**Release Date:** January 5, 2026

## 🐧 Linux Media Playback Fix

This release addresses media playback issues on Linux systems and improves cross-platform compatibility. Also, this version includes the mac version of the app.

---

## What's Fixed

### 🎵 Audio Playback on Linux
- **Problem:** Background music and meditation sounds failed to play on Linux due to JavaFX Media codec limitations
- **Solution:** 
  - Converted all audio files from MP3/MP4 to WAV format (PCM uncompressed)
  - WAV format has native support on Linux systems
  - Background music and meditation sounds now work perfectly on Linux

### 🎬 Video Background Fallback
- **Problem:** Background video failed to initialize on Linux, causing white screen or errors
- **Solution:** 
  - Implemented intelligent retry mechanism (up to 20 attempts)
  - Automatic fallback to static background image when video fails
  - Graceful degradation ensures app works on all platforms
  - Video still works on Windows/Mac where supported

### 🔄 Dashboard Navigation Fix
- **Problem:** Background image disappeared when navigating away and back to dashboard
- **Solution:** 
  - Fixed resource reattachment logic for both video and image backgrounds
  - Proper cleanup and rebinding of UI elements across scene transitions
  - Background now persists correctly during navigation

---

## Technical Changes

- Updated `MusicManager.java` to use WAV format (`bg_music.wav`)
- Updated `MeditationController.java` meditation sounds to WAV format
- Enhanced `VideoManager.java` with fallback image support and retry logic
- Removed unused MP3/MP4 audio files from distribution
- Improved cross-platform media compatibility

---

## Platform Compatibility

- ✅ **Windows:** Full video and audio support
- ✅ **Linux:** Audio works perfectly, static background image fallback
- ✅ **Mac:** Newly Added

---

**Note:** This is a maintenance release focused on cross-platform compatibility. All features from v1.0.1 are preserved.

**Previous Release:** [v1.0.1](https://github.com/abdullahsorwar/shelter_for_mind/releases/v1.0.1) - January 4, 2026