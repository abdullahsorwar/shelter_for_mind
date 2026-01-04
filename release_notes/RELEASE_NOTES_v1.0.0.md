# 🌟 Shelter for Mind v1.0.0

**Release Date**: January 3, 2026  
**Tag**: v1.0.0  
**Stability**: Stable

---

## 🎉 Welcome!

This is the first official release of **Shelter for Mind** - a comprehensive mental health and wellness application designed to support your emotional well-being journey.

---

## ✨ Features

### 🧘 Mood Tracker
- Interactive assessment with 5 comprehensive questions
- Track stress, anxiety, energy, and sleep patterns
- Visual mood history with beautiful charts and graphs
- Personalized insights based on your responses
- Data-driven analytics to understand your emotional patterns

### 📝 Personal Journal
- Private, secure space for your thoughts and feelings
- Save and manage unlimited journal entries
- Review your emotional journey over time
- Export options for personal records
- Completely private and encrypted

### 📚 Mental Health Blog Library
- **40+ curated articles** covering:
  - Stress Management
  - Anxiety and Depression
  - Sleep and Insomnia
  - Relationships and Social Issues
  - Self-Esteem and Confidence
  - Work-Life Balance
  - Specific Phobias and Disorders
  - And much more...
- Easy-to-read, professionally written content
- Save favorites for quick access
- Search and filter functionality

### 🎮 Calm Activities
- **Breathing Ball Exercise**: Guided breathing for instant relaxation
- **Bubble Popper**: Fun stress-relief mini-game
- **Gratitude Garden**: Cultivate positivity and thankfulness
- Science-backed relaxation techniques
- Interactive and engaging experiences

### 👤 User Profile System
- Secure account creation with email verification
- Personalized dashboard
- Profile customization options
- Password recovery system
- Privacy-first design

### 🔒 Security & Privacy
- BCrypt password encryption
- Secure database connections with HikariCP
- Local data storage
- No third-party data sharing
- Complete control over your information

---

## 📦 Installation

### System Requirements
- **OS**: Windows 10/11, macOS 10.14+, or Linux
- **RAM**: 1GB minimum, 2GB recommended
- **Storage**: 200MB for application
- **Database**: PostgreSQL 15+ (for backend setup)

### Download Installers

Choose the installer for your operating system:

| Platform | File | Size |
|----------|------|------|
| **Windows** | `ShelterForMind-1.0.msi` | ~250MB |
| **Linux (Debian/Ubuntu)** | `shelter-for-mind_1.0_amd64.deb` | ~250MB |

### Quick Installation

#### Windows
1. Download `ShelterForMind-1.0.msi`
2. Double-click and follow the installation wizard
3. Launch from Start Menu

#### Linux
```bash
# Debian/Ubuntu
sudo dpkg -i shelter-for-mind_1.0-1_amd64.deb

# For media playback (optional - background music and videos):
sudo apt-get install libgstreamer1.0-0 gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad gstreamer1.0-libav

# For media playback (optional):
sudo dnf install gstreamer1 gstreamer1-plugins-base gstreamer1-plugins-good gstreamer1-plugins-bad-free gstreamer1-libav
```

**Note**: Linux version requires GStreamer for media playback (background music and videos). All other features work without it.

For detailed installation instructions, see the [BUILD_INSTALLERS.md](https://github.com/abdullahsorwar/shelter_for_mind/blob/v1.0.0/BUILD_INSTALLERS.md).

---

## 🚀 Getting Started

1. **Launch the application**
2. **Create your account** (Sign Up)
3. **Verify your email** (check spam folder if needed)
4. **Complete your profile**
5. **Start your wellness journey!**

### First Steps
- Take your first mood assessment
- Write your first journal entry
- Browse the mental health blogs
- Try the Breathing Ball exercise

---

## 🔧 Technical Details

### Built With
- **Frontend**: JavaFX 21
- **Backend**: Java 21
- **Database**: PostgreSQL 15+
- **Build Tool**: Maven 3.6+
- **Security**: BCrypt, HikariCP
- **Communication**: JavaMail, WebSocket

### Architecture
- Model-View-Controller (MVC) pattern
- Modular JavaFX application
- Repository pattern for data access
- Connection pooling for efficiency
- Secure authentication system

---

## 📊 What's Included

### Application Components
- Complete desktop application
- Embedded database schema
- 40+ blog articles (text files)
- Sample data for testing
- User guide and documentation

### Documentation
- README.md - Complete project overview
- BUILD_INSTALLERS.md - How to build installers
- GITHUB_RELEASE.md - Release guidelines
- CONTRIBUTING.md - Contribution guide
- CHANGELOG.md - Version history
- LICENSE - GNU GPL-3.0 License

---

## 🐛 Known Issues

None reported at release time. If you encounter any issues, please:
1. Check our [FAQ](https://github.com/abdullahsorwar/shelter_for_mind/wiki/FAQ)
2. Search [existing issues](https://github.com/abdullahsorwar/shelter_for_mind/issues)
3. Create a [new issue](https://github.com/abdullahsorwar/shelter_for_mind/issues/new) with details

---

## ⚠️ Known Issues

### Linux Media Playback
- **Issue**: Background music and breathing exercise videos may not play on Linux
- **Cause**: JavaFX media requires GStreamer libraries not bundled with the installer
- **Workaround**: Install GStreamer manually (see installation instructions above)
- **Impact**: Low - all other features work perfectly
- **Status**: Documented workaround available

### Database Connection
- **Issue**: First connection may take 5-10 seconds if database is sleeping
- **Solution**: App automatically retries with built-in delay
- **Impact**: Minimal - only affects first launch

---

## 🤝 Contributing

We welcome contributions! Here's how you can help:

- 🐛 **Report bugs** - Open an issue
- 💡 **Suggest features** - Start a discussion
- 🔧 **Submit pull requests** - Fix bugs or add features
- 📝 **Improve documentation** - Help others understand
- 🌍 **Translate** - Make it accessible worldwide

See [CONTRIBUTING.md](https://github.com/abdullahsorwar/shelter_for_mind/blob/main/CONTRIBUTING.md) for guidelines.

---

## 💬 Community & Support

### Get Help
- 📖 [Documentation](https://github.com/abdullahsorwar/shelter_for_mind/wiki)
- 💬 [Discussions](https://github.com/abdullahsorwar/shelter_for_mind/discussions)
- 🐛 [Issues](https://github.com/abdullahsorwar/shelter_for_mind/issues)
- 📧 [Email Team](mailto:salwa-2023015953@cs.du.ac.bd)

### Stay Connected
- ⭐ Star us on GitHub
- 👁️ Watch for updates
- 🍴 Fork and experiment
- 📢 Share with others

---

## 🙏 Acknowledgments

### Development Team - the_pathfinders

This project was developed by passionate students from the University of Dhaka:

- **Salwa Baki** - [salwa-2023015953@cs.du.ac.bd](mailto:salwa-2023015953@cs.du.ac.bd)
- **Md. Abdullah Bin Sorwar Chowdhury** - [mdabdullah-2023715965@cs.du.ac.bd](mailto:mdabdullah-2023715965@cs.du.ac.bd)
- **Raisa Tabassum Payal** - [raisatabassum-2023115989@cs.du.ac.bd](mailto:raisatabassum-2023115989@cs.du.ac.bd)
- **Arnob Saha** - [arnob-2023015999@cs.du.ac.bd](mailto:arnob-2023015999@cs.du.ac.bd)

### Special Thanks
- University of Dhaka, Department of Computer Science and Engineering
- Mental health professionals who provided guidance
- Beta testers who gave valuable feedback
- Open-source community for amazing tools and libraries

---

## 📄 License

This project is licensed under the GNU GPL-3.0 License - see the [LICENSE](https://github.com/abdullahsorwar/shelter_for_mind/blob/main/LICENSE) file for details.

---

## 🌟 Star History

If you find this project helpful, please consider giving it a ⭐ on GitHub!

---

## 📊 Verification

### Checksums (SHA-256)

```
[Add checksums for each installer file]
```

To verify your download:
```bash
# Windows
certutil -hashfile ShelterForMind-1.0.msi SHA256

# macOS/Linux
sha256sum ShelterForMind-1.0.dmg
```

---

## 🎯 Project Stats

- **Lines of Code**: ~15,000+
- **Files**: 63 Java files, 20+ FXML files, 40+ blog articles
- **Development Time**: [Your timeline]
- **Technologies**: 10+ libraries and frameworks
- **Supported Platforms**: Windows, macOS, Linux

---

<div align="center">

## 🚀 Ready to Begin Your Wellness Journey?

[Download Now](#) | [View Documentation](https://github.com/abdullahsorwar/shelter_for_mind) | [Report Issues](https://github.com/abdullahsorwar/shelter_for_mind/issues)

**Made with ❤️ by the_pathfinders**

*University of Dhaka, Department of Computer Science and Engineering*

---

**Full Changelog**: https://github.com/abdullahsorwar/shelter_for_mind/commits/v1.0.0

</div>