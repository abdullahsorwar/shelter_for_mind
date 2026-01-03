# 🚀 Complete Release Checklist for Shelter For Mind

This is your step-by-step guide to building installers and releasing on GitHub.

---

## 📋 Phase 1: Building Installers

### Step 1.1: Windows Installer

**On Windows Machine:**

1. **Install Prerequisites**
   - Download and install WiX Toolset: https://wixtoolset.org/
   - Ensure JDK 21 is installed
   - Verify Maven is installed: `mvn -version`

2. **Build the Installer**
   ```cmd
   cd "E:\2nd Year 1st Semester\shelter_for_mind"
   build-installer-windows.bat
   ```
   
3. **Verify Output**
   - Check `target/installer/` folder
   - You should see `ShelterForMind-1.0.msi`
   - File size should be ~50-100MB

4. **Test the Installer**
   - Install on a clean Windows machine
   - Verify application launches
   - Test all features

### Step 1.2: macOS Installer

**On macOS Machine:**

1. **Install Prerequisites**
   - Install Xcode Command Line Tools: `xcode-select --install`
   - Install Homebrew (if not installed): `/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"`
   - Install Maven: `brew install maven`

2. **Build the Installer**
   ```bash
   cd ~/shelter_for_mind  # or your project path
   chmod +x build-installer.sh
   ./build-installer.sh
   ```

3. **Verify Output**
   - Check `target/installer/` folder
   - You should see `ShelterForMind-1.0.dmg`

4. **Test the Installer**
   - Open DMG file
   - Drag to Applications
   - Test launching and features

### Step 1.3: Linux Installer (Ubuntu/Debian)

**On Ubuntu/Debian Machine:**

1. **Install Prerequisites**
   ```bash
   sudo apt update
   sudo apt install openjdk-21-jdk maven dpkg fakeroot
   ```

2. **Build the Installer**
   ```bash
   cd ~/shelter_for_mind
   chmod +x build-installer.sh
   ./build-installer.sh
   ```

3. **Verify Output**
   - Check `target/installer/` folder
   - You should see `shelter-for-mind_1.0-1_amd64.deb`

4. **Test the Installer**
   ```bash
   sudo dpkg -i target/installer/shelter-for-mind_1.0-1_amd64.deb
   shelter-for-mind
   ```

### Step 1.4: Linux Installer (Fedora/RHEL)

**On Fedora/Red Hat Machine:**

1. **Install Prerequisites**
   ```bash
   sudo dnf install java-21-openjdk maven rpm-build
   ```

2. **Build the Installer**
   ```bash
   # Edit build-installer.sh and change:
   INSTALLER_TYPE="rpm"
   
   chmod +x build-installer.sh
   ./build-installer.sh
   ```

3. **Verify Output**
   - You should see `shelter-for-mind-1.0-1.x86_64.rpm`

---

## 📦 Phase 2: Preparing for Release

### Step 2.1: Gather All Installers

Create a release folder and copy all installers:

```
release-v1.0.0/
├── ShelterForMind-1.0.msi              (Windows)
├── ShelterForMind-1.0.dmg              (macOS)
├── shelter-for-mind_1.0-1_amd64.deb    (Debian/Ubuntu)
└── shelter-for-mind-1.0-1.x86_64.rpm   (Fedora/RHEL)
```

### Step 2.2: Test Each Installer

Create a testing checklist:

- [ ] Windows: Install, launch, test features, uninstall
- [ ] macOS: Mount DMG, install, test, remove
- [ ] Debian: Install via dpkg, test, remove
- [ ] RPM: Install via rpm, test, remove

### Step 2.3: Calculate Checksums (Optional but Recommended)

```bash
# Windows
certutil -hashfile ShelterForMind-1.0.msi SHA256

# macOS/Linux
sha256sum ShelterForMind-1.0.dmg
sha256sum shelter-for-mind_1.0-1_amd64.deb
sha256sum shelter-for-mind-1.0-1.x86_64.rpm
```

Save checksums in `checksums.txt`.

---

## 🐙 Phase 3: GitHub Release

### Step 3.1: Initialize Git (if not done)

```bash
cd "E:\2nd Year 1st Semester\shelter_for_mind"

# Initialize git
git init

# Add all files
git add .

# Create first commit
git commit -m "Initial commit: Shelter For Mind v1.0.0"
```

### Step 3.2: Create GitHub Repository

1. **Go to GitHub**: https://github.com
2. **Click "+" → New repository**
3. **Fill in details**:
   - Repository name: `shelter-for-mind`
   - Description: "A comprehensive mental health and wellness application"
   - Public repository
   - **Don't** initialize with README
4. **Click "Create repository"**

### Step 3.3: Push to GitHub

```bash
# Add remote (replace abdullahsorwar with your GitHub username)
git remote add origin https://github.com/abdullahsorwar/shelter-for-mind.git

# Set main branch
git branch -M main

# Push to GitHub
git push -u origin main
```

### Step 3.4: Create Release on GitHub

1. **Go to your repository**: `https://github.com/abdullahsorwar/shelter-for-mind`

2. **Click "Releases"** (right sidebar)

3. **Click "Create a new release"**

4. **Fill in release details**:

   **Choose a tag**: `v1.0.0` (create new tag)
   
   **Release title**: `Shelter For Mind v1.0.0 - Initial Release`
   
   **Description**: Copy from `RELEASE_NOTES.md` or use:
   ```markdown
   # 🌟 Shelter For Mind v1.0.0
   
   Welcome to the first official release!
   
   ## ✨ Features
   - 🧘 Mood Tracker with detailed analytics
   - 📝 Personal Journal
   - 📚 40+ Mental Health Blogs
   - 🎮 Calm Activities (Breathing, Bubble Popper, Gratitude Garden)
   - 👤 Secure User Profiles
   
   ## 📦 Downloads
   
   Choose the installer for your platform:
   
   **Windows**: `ShelterForMind-1.0.msi`
   **macOS**: `ShelterForMind-1.0.dmg`
   **Linux (Debian/Ubuntu)**: `shelter-for-mind_1.0-1_amd64.deb`
   **Linux (Fedora/RHEL)**: `shelter-for-mind-1.0-1.x86_64.rpm`
   
   ## 🚀 Installation
   
   See [README.md](https://github.com/abdullahsorwar/shelter-for-mind#installation) for detailed instructions.
   
   ## 📋 Requirements
   - Java 21 (bundled with installers)
   - PostgreSQL 15+
   - 1GB RAM, 200MB disk space
   
   ## 🙏 Acknowledgments
   
   Developed by the_pathfinders team from University of Dhaka.
   ```

5. **Upload installer files**:
   - Drag and drop all installer files
   - Add `checksums.txt` if you created it

6. **Check "Set as the latest release"**

7. **Click "Publish release"**

### Step 3.5: Update README Links

After creating the release, update README.md:

Replace `abdullahsorwar` with your actual GitHub username in:
- Installation links
- Issues link
- Discussions link
- All GitHub URLs

```bash
# Make changes to README.md
git add README.md
git commit -m "Update README with GitHub username"
git push
```

---

## 🎨 Phase 4: Polish Your Repository

### Step 4.1: Add Topics

1. Go to your repository
2. Click ⚙️ next to "About"
3. Add topics: `javafx`, `mental-health`, `wellness`, `java`, `postgresql`, `mood-tracker`
4. Save

### Step 4.2: Add Description

In the same "About" section, add:
"A comprehensive mental health and wellness application built with JavaFX"

### Step 4.3: Enable Issues and Discussions

1. Go to Settings
2. Enable Issues (should be on by default)
3. Enable Discussions (optional but recommended)

### Step 4.4: Create Project Board (Optional)

1. Go to Projects
2. Create new project for tracking issues and features

---

## 📢 Phase 5: Announce Your Release

### Step 5.1: Social Media

**LinkedIn Post Template**:
```
🌟 Excited to announce the release of Shelter For Mind v1.0.0!

A comprehensive mental health and wellness application designed to help individuals track their emotional well-being, journal their thoughts, and access mental health resources.

✨ Key Features:
- Mood Tracker with analytics
- Personal Journal
- Mental Health Blog Library
- Calming Activities
- Secure User Profiles

💻 Built with: JavaFX | PostgreSQL | Java 21

🔗 GitHub: [your-link]

Developed by the_pathfinders team at University of Dhaka.

#MentalHealth #Wellness #OpenSource #JavaFX #SoftwareDevelopment
```

### Step 5.2: University/Department Announcement

Share with:
- CS Department at University of Dhaka
- University mailing lists
- Student clubs and organizations

### Step 5.3: Tech Communities

Post on:
- Reddit: r/java, r/opensource, r/mentalhealth
- Dev.to
- Hashnode
- Twitter/X

---

## ✅ Final Checklist

Before announcing, verify:

### Code Quality
- [ ] All features working
- [ ] No critical bugs
- [ ] Code is clean and documented
- [ ] Database schema is stable

### Documentation
- [ ] README.md is complete and accurate
- [ ] All links work (especially GitHub URLs)
- [ ] Installation instructions are clear
- [ ] Developer emails are correct
- [ ] Screenshots/logos are included

### Installers
- [ ] Windows installer tested
- [ ] macOS installer tested
- [ ] Linux (Debian) installer tested
- [ ] Linux (RPM) installer tested
- [ ] All installers work on clean systems

### GitHub Repository
- [ ] Code pushed to GitHub
- [ ] Release created with v1.0.0 tag
- [ ] All installers uploaded
- [ ] Release notes are complete
- [ ] Repository description added
- [ ] Topics added
- [ ] LICENSE file present
- [ ] CONTRIBUTING.md present

### Legal/Ethics
- [ ] No sensitive data in code
- [ ] All credentials removed
- [ ] License chosen and applied
- [ ] All team members credited

---

## 🆘 Troubleshooting

### "Git push failed"
```bash
git pull origin main --rebase
git push origin main
```

### "Cannot create installer"
- Verify JDK 21+ is installed
- Check Maven installation
- Ensure platform-specific tools are installed

### "Database connection failed"
- Update db.properties with correct credentials
- Don't commit db.properties with real passwords

### "Missing dependencies"
```bash
mvn clean install -U
```

---

## 📞 Need Help?

**Team Contacts:**
- Salwa Baki: salwa-2023015953@cs.du.ac.bd
- Md. Abdullah: mdabdullah-2023715965@cs.du.ac.bd
- Raisa Tabassum: raisatabassum-2023115989@cs.du.ac.bd
- Arnob Saha: arnob-2023015999@cs.du.ac.bd

**Resources:**
- [GitHub Docs](https://docs.github.com)
- [Maven Central](https://mvnrepository.com)
- [JavaFX Documentation](https://openjfx.io)

---

## 🎉 Congratulations!

You've successfully:
✅ Built installers for all platforms
✅ Released on GitHub
✅ Created comprehensive documentation
✅ Made your project accessible to users

Your application is now ready for the world! 🚀

---

**Made with ❤️ by the_pathfinders**
*University of Dhaka, Department of Computer Science and Engineering*
