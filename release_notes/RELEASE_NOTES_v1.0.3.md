# Shelter For Mind v1.0.3 - Security Enhancement Release

**Release Date:** January 10, 2026

## 🔐 Database Security Fix

This release addresses critical security vulnerabilities by implementing encrypted configuration management for database credentials.

---

## What's Fixed

### 🛡️ Encrypted Database Credentials
- **Problem:** Database credentials were hardcoded in source code, visible in Git history and accessible to anyone with code access
- **Solution:** 
  - Implemented AES-256-CBC encryption for database configuration
  - PBKDF2 key derivation with 65,536 iterations for enhanced security
  - Credentials now stored in encrypted `config/db.enc` file
  - Master password obfuscation with optional keyfile support
  - Automatic exclusion from version control (.gitignore)

### 🔄 Configuration Management
- **Improvement:** Added automated encryption/decryption utilities
- **Features:**
  - Windows and Linux/Mac encryption scripts (`encrypt-config.bat`, `encrypt-config.sh`)
  - Graceful fallback to template properties file
  - Validation of required configuration properties
  - Clear error messages for missing or invalid configuration

### 📦 Installer Updates
- **Enhancement:** Installers now package encrypted configuration automatically
- **Validation:** Build scripts verify encrypted config exists before packaging
- **Compatibility:** All platforms (Windows MSI, Linux DEB/RPM, macOS DMG)

---

## Security Improvements

- ✅ **No hardcoded credentials** in source code
- ✅ **AES-256 encryption** with random salt and IV
- ✅ **PBKDF2 key stretching** (65,536 iterations)
- ✅ **Git exclusion** prevents accidental commits
- ✅ **File permission recommendations** for secure storage

---

## Technical Changes

- Added `EncryptedConfig.java` utility for encryption/decryption
- Updated `DB.java` to load from encrypted configuration
- Created `config/` directory with encryption scripts and documentation
- Updated `.gitignore` to exclude sensitive configuration files
- Modified installer scripts to package encrypted config
- Converted `db.properties` to template-only format

---

## Migration Guide

For existing installations:
1. Create `config/db_credentials.properties` with your database credentials
2. Run `config/encrypt-config.bat` (Windows) or `config/encrypt-config.sh` (Linux/Mac)
3. Rebuild and redeploy the application

For new installations:
- Encrypted configuration is included in installer
- No additional setup required

---

## Platform Compatibility

- ✅ **Windows:** MSI installer with encrypted config
- ✅ **Linux:** DEB/RPM installer with encrypted config
- ✅ **macOS:** DMG installer with encrypted config

---

## Upgrade Notes

- **Breaking Change:** Applications built with v1.0.3 require `config/db.enc` file or fallback to template properties
- **Recommended:** Run encryption script before building installers
- **Security:** Delete plain-text credential files after encryption

---

**Security Level:** 🔴 Critical Risk → 🟢 Low Risk

**Note:** This is a maintenance release focused on cross-platform compatibility. All features from v1.0.2 are preserved.

**Previous Release:** [v1.0.2](https://github.com/abdullahsorwar/shelter_for_mind/releases/v1.0.2) - January 5, 2026