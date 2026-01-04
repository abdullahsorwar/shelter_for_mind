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

## [1.0.1] - 2026-01-04

### Fixed
- 🐛 **Critical Fix**: Blog content now properly included in packaged installer
  - Moved blog data files to resources directory for JAR packaging
  - Changed file loading from filesystem to classpath resources
  - Blogs are now accessible in installed application
  
- 📁 **Data Storage**: User data now saved to proper application directory
  - Saved journals and blogs now stored in `~/.shelter_for_mind/`
  - Ensures write permissions in installed applications
  - Data persists across app updates

## [1.0.0] - 2026-01-03

### Added
- 🧘 **Mood Tracker** with 5 comprehensive questions
  - Track stress, anxiety, energy, sleep patterns
  - Visual mood history with charts
  - Personalized insights
  
- 📝 **Personal Journal** feature
  - Private, secure journaling space
  - View and manage journal history
  - Save journal entries locally
  
- 📚 **Mental Health Blogs** library
  - 40+ curated articles on mental health topics
  - Topics include stress, anxiety, depression, relationships, etc.
  - Save favorite blogs feature
  - Search and filter functionality
  
- 🎮 **Calm Activities**
  - Breathing Ball Exercise for guided relaxation
  - Bubble Popper stress-relief game
  - Gratitude Garden for positivity cultivation
  
- 👤 **User Profile System**
  - Secure account creation and authentication
  - Personalized dashboard
  - Profile customization
  - Email verification
  - Password recovery
  
- 🔒 **Security Features**
  - BCrypt password hashing
  - Secure database connections
  - Email verification system
  - Session management
  
- 🎨 **UI/UX Improvements**
  - Modern, clean interface design
  - Responsive layouts
  - Smooth animations and transitions
  - Custom CSS styling
  - Intuitive navigation

### Technical
- JavaFX 21 implementation
- PostgreSQL database integration
- HikariCP connection pooling
- JavaMail for email services
- WebSocket for real-time updates
- Maven build system
- JPackage installer generation

### Documentation
- Comprehensive README.md
- Installation guide
- Building instructions
- API documentation
- Contributing guidelines

### Known Issues
- None reported in initial release

## Version History

- **1.0.0** (2026-01-03)

---

## How to Read This Changelog

- **Added**: New features
- **Changed**: Changes in existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security updates

---

For detailed information about each version, see the [Releases page](https://github.com/abdullahsorwar/shelter-for-mind/releases).
