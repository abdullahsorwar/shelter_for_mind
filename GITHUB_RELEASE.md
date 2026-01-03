# GitHub Release Guide for Shelter For Mind

This guide walks you through releasing your application on GitHub.

## Prerequisites

- GitHub account
- Git installed on your computer
- Application installers built for all platforms

## Step 1: Initialize Git Repository (if not done)

```bash
cd "E:\2nd Year 1st Semester\shelter_for_mind"
git init
```

## Step 2: Create .gitignore

The project already has a `.gitignore`. Verify it includes:

```
target/
.idea/
.vscode/
*.iml
.DS_Store
*.class
*.jar
*.war
db.properties
```

## Step 3: Create GitHub Repository

1. Go to https://github.com
2. Click the "+" icon in the top right → "New repository"
3. Fill in details:
   - **Repository name**: `shelter-for-mind`
   - **Description**: "A mental health and wellness application built with JavaFX"
   - **Visibility**: Public (or Private if you prefer)
   - **DO NOT** initialize with README (you already have one)
4. Click "Create repository"

## Step 4: Link Local Repository to GitHub

After creating the repository, GitHub will show commands. Use these:

```bash
# Add all files
git add .

# Create initial commit
git commit -m "Initial commit: Shelter For Mind v1.0"

# Add remote origin (replace abdullahsorwar)
git remote add origin https://github.com/abdullahsorwar/shelter-for-mind.git

# Push to GitHub
git branch -M main
git push -u origin main
```

## Step 5: Create a New Release

### Option 1: Via GitHub Web Interface (Recommended)

1. Go to your repository: `https://github.com/abdullahsorwar/shelter-for-mind`
2. Click on "Releases" (right sidebar)
3. Click "Create a new release"
4. Fill in release details:

   **Tag version**: `v1.0.0`
   
   **Release title**: `Shelter For Mind v1.0.0 - Initial Release`
   
   **Description**:
   ```markdown
   # 🌟 Shelter For Mind v1.0.0
   
   Welcome to the first release of Shelter For Mind - your companion for mental health and wellness!
   
   ## ✨ Features
   
   - 🧘 **Mood Tracker**: Monitor your emotional wellness journey
   - 📝 **Journal**: Express your thoughts and feelings privately
   - 📚 **Mental Health Blogs**: Access curated content on various mental health topics
   - 🎮 **Calm Activities**: Relaxation exercises and mindfulness tools
   - 👤 **User Profiles**: Personalized experience with secure authentication
   - 🔒 **Privacy First**: Your data is secure and private
   
   ## 📦 Installation
   
   Choose the installer for your platform:
   
   - **Windows**: Download `ShelterForMind-1.0.msi` or `ShelterForMind-1.0.exe`
   - **macOS**: Download `ShelterForMind-1.0.dmg`
   - **Linux**: 
     - Debian/Ubuntu: Download `shelter-for-mind_1.0-1_amd64.deb`
     - Red Hat/Fedora: Download `shelter-for-mind-1.0-1.x86_64.rpm`
   
   ## 🚀 Quick Start
   
   1. Download the installer for your platform
   2. Run the installer and follow the setup wizard
   3. Launch Shelter For Mind
   4. Create your account or sign in
   5. Start your wellness journey!
   
   ## 📋 Requirements
   
   - Java 21 or higher (bundled with installers)
   - PostgreSQL database (for backend)
   - 512MB RAM minimum, 1GB recommended
   - 200MB disk space
   
   ## 🐛 Known Issues
   
   - None reported yet!
   
   ## 📞 Support
   
   For questions, issues, or feedback:
   - Open an issue on GitHub
   - Contact the development team (see README for emails)
   
   ## 👥 Credits
   
   Developed by **the_pathfinders** team from University of Dhaka.
   
   ---
   
   **Full Changelog**: Initial Release
   ```

5. **Upload installer files**:
   - Click "Attach binaries by dropping them here or selecting them"
   - Upload all installer files from `target/installer/`:
     - Windows: `.msi` or `.exe`
     - macOS: `.dmg` or `.pkg`
     - Linux: `.deb` and `.rpm`
   - You can also upload the source code JAR

6. Check "Set as the latest release"
7. Click "Publish release"

### Option 2: Via Git Command Line

```bash
# Create and push a tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# Then go to GitHub web interface to add installers and description
```

## Step 6: Upload Release Assets via GitHub CLI (Optional)

If you have GitHub CLI installed:

```bash
# Install GitHub CLI first (if not installed)
# Windows: winget install GitHub.cli
# macOS: brew install gh
# Linux: See https://github.com/cli/cli/blob/trunk/docs/install_linux.md

# Authenticate
gh auth login

# Create release with assets
gh release create v1.0.0 \
  --title "Shelter For Mind v1.0.0 - Initial Release" \
  --notes-file RELEASE_NOTES.md \
  target/installer/*.msi \
  target/installer/*.dmg \
  target/installer/*.deb \
  target/installer/*.rpm
```

## Step 7: Add Topics and Description to Repository

1. Go to your repository homepage
2. Click the gear icon (⚙️) next to "About"
3. Add description: "A comprehensive mental health and wellness application built with JavaFX"
4. Add topics: `javafx`, `mental-health`, `wellness`, `java`, `postgresql`, `mood-tracker`, `journal`
5. Add website (if you have one)
6. Save changes

## Step 8: Create Additional Documentation

Consider adding these files to your repository:

### CONTRIBUTING.md
```markdown
# Contributing to Shelter For Mind

We welcome contributions! Please feel free to submit issues and pull requests.

## How to Contribute

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request
```

### LICENSE
Choose a license (MIT, Apache 2.0, GPL, etc.) and add it to your repository.

## Step 9: Promote Your Release

1. **Share on Social Media**: LinkedIn, Twitter, Facebook
2. **Write a Blog Post**: Explain the project and its features
3. **Submit to Lists**: 
   - Product Hunt
   - AlternativeTo
   - SourceForge
4. **Create a Video Demo**: YouTube showcase

## Step 10: Monitor and Maintain

- **Watch Repository**: Click "Watch" to get notified of issues
- **Respond to Issues**: Help users with problems
- **Plan Updates**: Track feature requests and bugs
- **Release Updates**: Follow semantic versioning (v1.1.0, v1.2.0, etc.)

## Automated Releases with GitHub Actions (Advanced)

Create `.github/workflows/release.yml`:

```yaml
name: Build and Release

on:
  push:
    tags:
      - 'v*'

jobs:
  build-windows:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Build with Maven
        run: mvn clean package
      - name: Create Windows Installer
        run: mvn javafx:jlink jpackage:jpackage -Djpackage.type=msi
      - name: Upload Installer
        uses: actions/upload-artifact@v3
        with:
          name: windows-installer
          path: target/installer/*.msi

  build-macos:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Build with Maven
        run: mvn clean package
      - name: Create macOS Installer
        run: mvn javafx:jlink jpackage:jpackage -Djpackage.type=dmg
      - name: Upload Installer
        uses: actions/upload-artifact@v3
        with:
          name: macos-installer
          path: target/installer/*.dmg

  build-linux:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Build with Maven
        run: mvn clean package
      - name: Create Linux Installers
        run: |
          mvn javafx:jlink jpackage:jpackage -Djpackage.type=deb
          mvn jpackage:jpackage -Djpackage.type=rpm
      - name: Upload Installers
        uses: actions/upload-artifact@v3
        with:
          name: linux-installers
          path: target/installer/*

  create-release:
    needs: [build-windows, build-macos, build-linux]
    runs-on: ubuntu-latest
    steps:
      - name: Download all artifacts
        uses: actions/download-artifact@v3
      - name: Create Release
        uses: softprops/action-gh-release@v1
        with:
          files: |
            windows-installer/*
            macos-installer/*
            linux-installers/*
```

## Quick Checklist

- [ ] Repository created on GitHub
- [ ] Code pushed to GitHub
- [ ] README.md updated with full information
- [ ] Installers built for all platforms
- [ ] Release created with version tag
- [ ] All installer files uploaded
- [ ] Release notes written
- [ ] Repository description and topics added
- [ ] License added
- [ ] Release announced

## Tips for Success

1. **Use Semantic Versioning**: v1.0.0, v1.1.0, v2.0.0
2. **Write Clear Release Notes**: What's new, what's fixed, what's changed
3. **Include Screenshots**: Visual appeal matters
4. **Test Installers**: Try installing on clean systems
5. **Keep Dependencies Updated**: Security and compatibility
6. **Document Everything**: Good docs = happy users
7. **Respond Quickly**: Engage with your community

---

**Need Help?** Check out:
- [GitHub Docs - Releases](https://docs.github.com/en/repositories/releasing-projects-on-github)
- [Semantic Versioning](https://semver.org/)
- [Choosing a License](https://choosealicense.com/)
