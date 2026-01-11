# 🌟 Shelter for Mind

<div align="center">

<img src="https://github.com/abdullahsorwar/shelter_for_mind/blob/main/src/main/resources/assets/images/shelter_for_mind.png" alt="Shelter For Mind Logo" width="300" height="300">

**Your Companion for Mental Health and Wellness**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

[Features](#-features) • [Installation](#-installation) • [Usage](#-usage) • [Contributing](#-contributing) • [About](#-about-the-developers)

</div>

---

## 📖 About

**Shelter for Mind** is a comprehensive mental health and wellness application designed to help individuals track their emotional well-being, journal their thoughts, access mental health resources, and engage in calming activities. Built with JavaFX and PostgreSQL, this application provides a secure, private, and user-friendly platform for mental wellness.

### 🎯 Mission

Our mission is to make mental health support accessible, private, and empowering. We believe everyone deserves a safe space to understand and improve their emotional well-being.

## ✨ Features

### 🧘 **Mood Tracker**
- Interactive mood assessment with 5 comprehensive questions
- Visual mood history with detailed analytics
- Track stress, anxiety, energy, and sleep patterns
- Beautiful data visualization with charts and graphs
- Personalized insights based on your mood patterns

### 📝 **Personal Journal**
- Private, secure journaling space
- Express your thoughts and feelings freely
- View your journal history
- Save and manage journal entries
- Emotional outlet for daily reflections

### 📚 **Mental Health Blogs**
- Curated content on various mental health topics:
  - Stress Management
  - Anxiety Disorders
  - Depression
  - Sleep and Insomnia
  - Self-Esteem and Confidence
  - Relationship Issues
  - Work-Life Balance
  - And many more...
- Easy-to-read, informative articles
- Save favorite blogs for later reading
- Search and filter functionality

### 🎮 **Calm Activities**
- **Breathing Ball Exercise**: Guided breathing for relaxation
- **Bubble Popper**: Stress-relief mini-game
- **Gratitude Garden**: Cultivate positivity and gratitude
- Interactive and soothing experiences
- Science-backed relaxation techniques

### 👤 **User Profiles**
- Secure account creation and authentication
- Personalized dashboard
- Profile customization
- Email verification system
- Password recovery options

### 🔒 **Privacy & Security**
- End-to-end encryption for sensitive data
- Secure password hashing (BCrypt)
- Local data storage with database encryption
- No data sharing with third parties
- Complete privacy for your mental health journey

## 📦 Installation

### System Requirements

- **Operating System**: Windows 10/11, macOS 10.14+, or Linux
- **RAM**: 1GB minimum, 2GB recommended
- **Disk Space**: 200MB for application
- **Java**: Not required! (bundled with installers)
- **Database**: Hosted remotely - no local setup needed!

### Installers (Recommended - No Dependencies Required!)

Download and run the installer for your platform. Everything is bundled - no Java, Maven, or database setup needed!

| Platform | Installer |
|----------|-----------|
| **Windows** | [Download](https://github.com/abdullahsorwar/shelter_for_mind/releases/download/v1.0.3/Shelter.for.Mind-1.0.3.msi) |
| **Linux (Debian/Ubuntu)** | [Download](https://github.com/abdullahsorwar/shelter_for_mind/releases/download/v1.0.3/shelter-for-mind_1.0.3_amd64.deb) |
| **macOS** | [Download](https://github.com/abdullahsorwar/shelter_for_mind/releases/download/v1.0.3/Shelter.for.Mind-1.0.3.dmg) |

#### Linux (Debian/Ubuntu) Quick Install
```bash
# Download the .deb file
wget https://github.com/abdullahsorwar/shelter_for_mind/releases/download/v1.0.3/shelter-for-mind_1.0.3_amd64.deb

# Install
sudo dpkg -i shelter-for-mind_1.0.3_amd64.deb

# For media playback support (optional - background music and videos):
sudo apt-get install libgstreamer1.0-0 gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad gstreamer1.0-libav

# Launch
shelter-for-mind
```

**Note**: The app works fully without GStreamer, but background music and breathing exercise videos will not play.

## 🚀 Usage

### Getting Started

1. **Launch the Application**
   - Open Shelter For Mind from your applications menu

2. **Create an Account**
   - Click "Sign Up" on the welcome screen
   - Enter your details (username, email, password)
   - Verify your email
   - Complete your profile

3. **Explore Features**
   - **Dashboard**: Your central hub for all features
   - **Mood Tracker**: Take daily mood assessments
   - **Journal**: Write your thoughts and feelings
   - **Blogs**: Read mental health resources
   - **Activities**: Try calming exercises
   - **Profile**: Customize your experience

### Tips for Best Experience

- 🌅 **Morning Check-in**: Start your day with a mood assessment
- 📓 **Daily Journaling**: Write at least a few sentences each day
- 📚 **Weekly Reading**: Explore one blog article per week
- 🧘 **Regular Practice**: Use calm activities during stress
- 📊 **Track Progress**: Review your mood history monthly

## 🛠️ Building from Source (For Developers)

**Note**: If you just want to use the app, download the installer above instead!

### Prerequisites

- JDK 21 or higher
- Apache Maven 3.6+
- Git

**Note**: PostgreSQL is NOT needed - database is hosted remotely!

### Clone Repository

```bash
git clone https://github.com/abdullahsorwar/shelter_for_mind.git
cd shelter_for_mind
```

### Build and Run

```bash
# Clean and build
mvn clean package

# Run the application
mvn javafx:run
```

## 🏗️ Project Structure

```
shelter_for_mind/
├── src/
│   ├── main/
│   │   ├── java/com/the_pathfinders/
│   │   │   ├── App.java                    # Main application entry
│   │   │   ├── DashboardController.java    # Main dashboard logic
│   │   │   ├── MoodTrackerController.java  # Mood tracking features
│   │   │   ├── JournalController.java      # Journaling functionality
│   │   │   ├── BlogController.java         # Blog management
│   │   │   ├── ProfileController.java      # User profile management
│   │   │   └── db/                         # Database repositories
│   │   └── resources/
│   │       ├── com/the_pathfinders/
│   │       │   ├── css/                    # Stylesheets
│   │       │   ├── fxml/                   # UI layouts
│   │       │   └── assets/                 # Images, icons, videos
│   │       └── db.properties               # Database configuration
├── data/
│   ├── blogs/                              # Mental health blog content
│   ├── saved_blogs/                        # User-saved blogs
│   └── saved_journals/                     # User journal entries
├── pom.xml                                 # Maven configuration
├── README.md                               # This file
├── BUILD_INSTALLERS.md                     # Installer build guide
└── GITHUB_RELEASE.md                       # GitHub release guide
```

## 🧪 Testing

```bash
# Run tests
mvn test

# Run with code coverage
mvn clean test jacoco:report
```

## 📚 Technologies Used

### Frontend
- **JavaFX 21**: Modern UI framework
- **FXML**: Declarative UI design
- **CSS**: Custom styling and themes

### Backend
- **Java 21**: Core application logic
- **PostgreSQL**: Remote database management
- **HikariCP**: Connection pooling
- **BCrypt**: Password encryption
- **Encrypted Config**: Secure credential storage

### Communication
- **JavaMail**: Email verification system
- **WebSocket**: Real-time updates

### Build Tools
- **Maven**: Project management and build
- **JPackage**: Cross-platform installers

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

1. **Fork the Repository**
2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **Commit Your Changes**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
4. **Push to the Branch**
   ```bash
   git push origin feature/AmazingFeature
   ```
5. **Open a Pull Request**

### Contribution Guidelines

- Follow Java coding conventions
- Write meaningful commit messages
- Add comments for complex logic
- Test your changes thoroughly
- Update documentation as needed

## 🐛 Bug Reports

Found a bug? Please open an issue with:
- Clear description of the problem
- Steps to reproduce
- Expected vs actual behavior
- Screenshots (if applicable)
- System information (OS, Java version)

## 📝 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Mental health professionals who provided guidance
- Open-source community for amazing libraries
- JavaFX community for UI inspiration
- Beta testers for valuable feedback

## 📞 Support & Contact

- **Issues**: [GitHub Issues](https://github.com/abdullahsorwar/shelter_for_mind/issues)
- **Email**: See developer contacts below

## 🌟 About the Developers

**Shelter for Mind** is proudly developed by **the_pathfinders** team from the Department of Computer Science and Engineering, University of Dhaka.

### Contact Information

| Developer | Email |
|-----------|-------|
| **Salwa Baki** | [salwa-2023015953@cs.du.ac.bd](mailto:salwa-2023015953@cs.du.ac.bd) |
| **Md. Abdullah Bin Sorwar Chowdhury** | [mdabdullah-2023715965@cs.du.ac.bd](mailto:mdabdullah-2023715965@cs.du.ac.bd) |
| **Raisa Tabassum Payal** | [raisatabassum-2023115989@cs.du.ac.bd](mailto:raisatabassum-2023115989@cs.du.ac.bd) |
| **Arnob Saha** | [arnob-2023015999@cs.du.ac.bd](mailto:arnob-2023015999@cs.du.ac.bd) |

### About Us

We are passionate computer science students dedicated to leveraging technology for social good. Mental health awareness and accessibility are causes close to our hearts, and we've built Shelter For Mind to make a positive impact in people's lives.

**University**: University of Dhaka  
**Department**: Computer Science and Engineering  
**Year**: 2023-2024  
**Team Name**: the_pathfinders

---

<div align="center">

**Made with ❤️ by the_pathfinders**

If you find this project helpful, please consider giving it a ⭐!

[Report Bug](https://github.com/abdullahsorwar/shelter_for_mind/issues) • [Request Feature](https://github.com/abdullahsorwar/shelter_for_mind/issues) • [Documentation](https://github.com/abdullahsorwar/shelter_for_mind/wiki)

</div>

---

<div align="center">

[**Full Changelog**](https://github.com/abdullahsorwar/shelter_for_mind/blob/main/release_notes/CHANGELOG.md)


</div>