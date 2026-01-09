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
- Added PBKDF2 key derivation with 65,536 iterations for enhanced security
- Created encrypted configuration system (`config/db.enc`)
- Added encryption/decryption utilities for credential management

### Added
- `EncryptedConfig.java` utility for secure configuration management
- Automated encryption scripts (`encrypt-config.bat`, `encrypt-config.sh`)
- Configuration validation with clear error messages
- Graceful fallback to template properties file
- Optional keyfile support for enhanced master password security

### Changed
- Database credentials now loaded from encrypted `config/db.enc` file
- Updated `DB.java` to use dynamic configuration loading
- Converted `db.properties` to template-only format (no real credentials)
- Updated `.gitignore` to exclude sensitive configuration files
- Modified installer scripts to package encrypted configuration

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