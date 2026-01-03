# Building Installers for Shelter For Mind

This guide will help you create platform-specific installers for Windows, macOS, and Linux.

## Prerequisites

- **Java Development Kit (JDK) 21** or higher
- **Apache Maven 3.6+**
- Platform-specific tools:
  - **Windows**: WiX Toolset 3.11+ (for `.msi` installer)
  - **macOS**: Xcode Command Line Tools
  - **Linux**: `rpm-build` or `dpkg` depending on your distribution

## Step 1: Build the Application JAR

First, clean and package your application:

```bash
mvn clean package
```

This creates a JAR file in the `target` directory.

## Step 2: Create Runtime Image with jlink

Create a custom Java runtime with only the required modules:

```bash
mvn javafx:jlink
```

This creates a custom runtime in `target/shelter_of_mind`.

## Step 3: Create Platform-Specific Installers

### For Windows (.exe and .msi)

**Requirements**: Install WiX Toolset from https://wixtoolset.org/

```bash
# Create MSI installer
jpackage --input target/classes ^
  --name "Shelter For Mind" ^
  --main-jar shelter_of_mind-1.0.jar ^
  --main-class com.the_pathfinders.App ^
  --type msi ^
  --dest target/installer ^
  --app-version 1.0 ^
  --vendor "the_pathfinders" ^
  --icon logo_new.png ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut
```

**Alternative**: Using Maven plugin:

```bash
mvn javafx:jlink jpackage:jpackage -Djpackage.type=msi
```

### For macOS (.dmg or .pkg)

**Requirements**: macOS with Xcode Command Line Tools

```bash
# Create DMG installer
jpackage --input target/classes \
  --name "Shelter For Mind" \
  --main-jar shelter_of_mind-1.0.jar \
  --main-class com.the_pathfinders.App \
  --type dmg \
  --dest target/installer \
  --app-version 1.0 \
  --vendor "the_pathfinders" \
  --icon logo_new.png \
  --mac-package-name "ShelterForMind"
```

**Alternative**: Using Maven plugin:

```bash
mvn javafx:jlink jpackage:jpackage -Djpackage.type=dmg
```

### For Linux (.deb and .rpm)

**Requirements**: 
- For `.deb`: `dpkg` and `fakeroot`
- For `.rpm`: `rpm-build`

```bash
# Create DEB package (Debian/Ubuntu)
jpackage --input target/classes \
  --name "shelter-for-mind" \
  --main-jar shelter_of_mind-1.0.jar \
  --main-class com.the_pathfinders.App \
  --type deb \
  --dest target/installer \
  --app-version 1.0 \
  --vendor "the_pathfinders" \
  --icon logo_new.png \
  --linux-shortcut \
  --linux-menu-group "Education;Health"

# Create RPM package (Red Hat/Fedora/CentOS)
jpackage --input target/classes \
  --name "shelter-for-mind" \
  --main-jar shelter_of_mind-1.0.jar \
  --main-class com.the_pathfinders.App \
  --type rpm \
  --dest target/installer \
  --app-version 1.0 \
  --vendor "The Pathfinders" \
  --icon logo_new.png \
  --linux-shortcut \
  --linux-menu-group "Education;Health"
```

**Alternative**: Using Maven plugin:

```bash
# For DEB
mvn javafx:jlink jpackage:jpackage -Djpackage.type=deb

# For RPM
mvn javafx:jlink jpackage:jpackage -Djpackage.type=rpm
```

## Step 4: Simplified Build Script

Create a build script to automate the process:

### For Windows (build-installers.bat):

```batch
@echo off
echo Building Shelter For Mind Installers...

echo Step 1: Clean and package...
call mvn clean package

echo Step 2: Creating custom runtime...
call mvn javafx:jlink

echo Step 3: Creating Windows installer...
call mvn jpackage:jpackage -Djpackage.type=msi

echo Done! Installer available in target/installer/
pause
```

### For macOS/Linux (build-installers.sh):

```bash
#!/bin/bash
echo "Building Shelter For Mind Installers..."

echo "Step 1: Clean and package..."
mvn clean package

echo "Step 2: Creating custom runtime..."
mvn javafx:jlink

echo "Step 3: Creating installer..."
# Uncomment the one you need:
# mvn jpackage:jpackage -Djpackage.type=dmg  # macOS
# mvn jpackage:jpackage -Djpackage.type=deb  # Debian/Ubuntu
# mvn jpackage:jpackage -Djpackage.type=rpm  # Red Hat/Fedora

echo "Done! Installer available in target/installer/"
```

Make it executable: `chmod +x build-installers.sh`

## Step 5: Verify the Installer

After building, your installer will be in `target/installer/`:

- **Windows**: `Shelter For Mind-1.0.msi` or `Shelter For Mind-1.0.exe`
- **macOS**: `Shelter For Mind-1.0.dmg` or `Shelter For Mind-1.0.pkg`
- **Linux**: `shelter-for-mind_1.0-1_amd64.deb` or `shelter-for-mind-1.0-1.x86_64.rpm`

Test the installer on the target platform before distribution!

## Troubleshooting

### Issue: "jpackage not found"
- Ensure JDK 21+ is installed and in PATH
- JPackage is included in JDK 14+

### Issue: WiX Toolset not found (Windows)
- Download and install from https://wixtoolset.org/
- Add WiX bin directory to PATH

### Issue: Module not found errors
- Run `mvn clean install` first
- Verify `module-info.java` if using JPMS

### Issue: Icon not applied
- Ensure icon file exists and is in correct format:
  - Windows: `.ico`
  - macOS: `.icns`
  - Linux: `.png`

### Issue: Media not playing on Linux (Background music/videos)
**Problem**: JavaFX media support requires GStreamer libraries on Linux, which are not bundled by jpackage.

**Solution**: Users must install GStreamer manually:
```bash
# Ubuntu/Debian
sudo apt-get install libgstreamer1.0-0 gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad gstreamer1.0-libav

# Fedora/RHEL
sudo dnf install gstreamer1 gstreamer1-plugins-base gstreamer1-plugins-good gstreamer1-plugins-bad-free gstreamer1-libav

# Arch Linux
sudo pacman -S gstreamer gst-plugins-base gst-plugins-good gst-plugins-bad gst-libav
```

**Note**: The application functions normally without media. Only background music and breathing exercise videos will be affected. All other features work perfectly.

## Cross-Platform Building

Note: You generally need to build installers on their respective platforms:
- Windows installers must be built on Windows
- macOS installers must be built on macOS
- Linux installers can be built on Linux

For cross-platform builds, consider using CI/CD services like GitHub Actions with different runners.

## Additional Resources

- [JPackage Documentation](https://docs.oracle.com/en/java/javase/21/jpackage/)
- [JavaFX Maven Plugin](https://github.com/openjfx/javafx-maven-plugin)
- [JPackage Maven Plugin](https://github.com/petr-panteleyev/jpackage-maven-plugin)
