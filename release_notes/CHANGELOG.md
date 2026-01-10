# Changelog

All notable changes to Shelter For Mind will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Mobile application version
- Multi-language support
- Dark mode theme
- Export mood data to PDF
- Integration with wearable devices
- AI-powered mood insights

## [1.0.3] - 2026-01-10

### Security
- **CRITICAL**: Removed hardcoded database credentials from source code
- Implemented AES-256-CBC encryption for database configuration

### Fixed
- Database password no longer visible in Git history or source code
- Prevented accidental credential commits with .gitignore rules

## [1.0.2] - 2026-01-05

### Fixed
- Linux media playback compatibility (audio converted to WAV format)
- Dashboard background disappearing when navigating between menus
- Video playback fallback to static image on unsupported platforms

## [1.0.1] - 2026-01-04

### Fixed
- Blog content now properly included in packaged installer
- User data saved to application directory (`~/.shelter_for_mind/`)

## [1.0.0] - 2026-01-03

### Added
- Mood Tracker with 5 questions and visual history
- Personal Journal with entry management
- Mental Health Blogs library (40+ articles)
- Calm Activities (Breathing Exercise, Bubble Popper, Gratitude Garden)
- User authentication and profile system
- Password hashing and email verification

### Technical
- JavaFX 21, PostgreSQL, HikariCP, JavaMail, Maven
- JPackage installer support

---

## How to Read This Changelog

- **Added**: New features
- **Changed**: Changes in existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security updates

---

For detailed information about each version, see the [Releases page](https://github.com/abdullahsorwar/shelter_for_mind/releases).