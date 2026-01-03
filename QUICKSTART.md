# Quick Start Guide - Shelter For Mind

## For End Users

### Installation

#### Windows
1. Download `ShelterForMind-1.0.msi`
2. Double-click to install
3. Launch from Start Menu

#### macOS
1. Download `ShelterForMind-1.0.dmg`
2. Open DMG file
3. Drag to Applications
4. Launch from Applications

#### Linux
```bash
# Debian/Ubuntu
sudo dpkg -i shelter-for-mind_1.0-1_amd64.deb

# Red Hat/Fedora
sudo rpm -i shelter-for-mind-1.0-1.x86_64.rpm
```

### First-Time Setup

1. **Launch Application**
2. **Create Account**
   - Click "Sign Up"
   - Enter username, email, password
   - Verify email (check spam folder)
3. **Complete Profile**
   - Add basic information
   - Customize preferences
4. **Start Using**
   - Take mood assessment
   - Write in journal
   - Explore blogs
   - Try calm activities

## For Developers

### Quick Development Setup

```bash
# Clone repository
git clone https://github.com/abdullahsorwar/shelter-for-mind.git
cd shelter-for-mind

# Build
mvn clean install

# Run
mvn javafx:run
```

### Database Setup

```sql
-- Create database
CREATE DATABASE shelter_for_mind;

-- Run schema (see data/schema.sql)
```

Update `src/main/resources/db.properties`:
```properties
db.url=jdbc:postgresql://localhost:5432/shelter_for_mind
db.user=abdullahsorwar
db.password=your_password
```

### Building Installers

#### Windows
```bash
build-installer-windows.bat
```

#### macOS/Linux
```bash
chmod +x build-installer.sh
./build-installer.sh
```

## Common Issues

### "Cannot connect to database"
- Ensure PostgreSQL is running
- Check db.properties credentials
- Verify database exists

### "Java version error"
- Install JDK 21 or higher
- Set JAVA_HOME environment variable

### "Module not found"
- Run `mvn clean install`
- Delete `target` folder and rebuild

## Getting Help

- 📖 Full Documentation: [README.md](README.md)
- 🔧 Build Guide: [BUILD_INSTALLERS.md](BUILD_INSTALLERS.md)
- 🚀 Release Guide: [GITHUB_RELEASE.md](GITHUB_RELEASE.md)
- 🐛 Report Issues: [GitHub Issues](https://github.com/abdullahsorwar/shelter-for-mind/issues)
- 📧 Contact Team: See README.md for emails

## Quick Commands

```bash
# Development
mvn javafx:run                  # Run application
mvn clean test                  # Run tests
mvn clean package               # Build JAR

# Building
mvn javafx:jlink                # Create custom runtime
mvn jpackage:jpackage           # Create installer

# Specific installers
mvn jpackage:jpackage -Djpackage.type=msi   # Windows
mvn jpackage:jpackage -Djpackage.type=dmg   # macOS
mvn jpackage:jpackage -Djpackage.type=deb   # Debian/Ubuntu
mvn jpackage:jpackage -Djpackage.type=rpm   # Red Hat/Fedora
```

## Project Structure

```
src/main/java/          - Java source code
src/main/resources/     - FXML, CSS, assets
data/                   - Blog content, data files
target/                 - Build output
pom.xml                - Maven configuration
```

## Need More Help?

Check our documentation:
- [README.md](README.md) - Complete project overview
- [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guidelines
- [BUILD_INSTALLERS.md](BUILD_INSTALLERS.md) - Detailed build instructions
- [GITHUB_RELEASE.md](GITHUB_RELEASE.md) - Release process

---

**Made with ❤️ by the_pathfinders**
