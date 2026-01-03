# Contributing to Shelter For Mind

First off, thank you for considering contributing to Shelter For Mind! It's people like you that make this project such a great tool for mental health and wellness.

## Code of Conduct

This project and everyone participating in it is governed by our commitment to creating a welcoming and inclusive environment. By participating, you are expected to uphold this code.

### Our Pledge

- Use welcoming and inclusive language
- Be respectful of differing viewpoints and experiences
- Gracefully accept constructive criticism
- Focus on what is best for the community
- Show empathy towards other community members

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When you create a bug report, include as many details as possible:

- **Use a clear and descriptive title**
- **Describe the exact steps to reproduce the problem**
- **Provide specific examples** (code snippets, screenshots)
- **Describe the behavior you observed and what you expected**
- **Include your environment details** (OS, Java version, database version)

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

- **Use a clear and descriptive title**
- **Provide a detailed description of the suggested enhancement**
- **Explain why this enhancement would be useful**
- **List any similar features in other applications** (if applicable)

### Pull Requests

1. **Fork the repository** and create your branch from `main`
2. **Make your changes** following our coding standards
3. **Test your changes** thoroughly
4. **Update documentation** if needed
5. **Submit a pull request**

#### Pull Request Guidelines

- Follow the Java coding conventions
- Include meaningful commit messages
- Add comments for complex logic
- Update the README.md if needed
- Test on multiple platforms if possible

## Development Setup

### Prerequisites

- JDK 21 or higher
- Apache Maven 3.6+
- PostgreSQL 15+
- Git

### Setting Up Your Development Environment

1. Clone your fork:
```bash
git clone https://github.com/YOUR-USERNAME/shelter-for-mind.git
cd shelter-for-mind
```

2. Add upstream remote:
```bash
git remote add upstream https://github.com/ORIGINAL-OWNER/shelter-for-mind.git
```

3. Install dependencies:
```bash
mvn clean install
```

4. Set up database (see README.md)

5. Run the application:
```bash
mvn javafx:run
```

### Coding Standards

#### Java Code Style

- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Use meaningful variable and method names
- Follow camelCase for variables and methods
- Follow PascalCase for classes
- Add JavaDoc comments for public methods

Example:
```java
/**
 * Calculates the mood score based on user responses.
 * 
 * @param responses List of user responses
 * @return Calculated mood score between 0-100
 */
public int calculateMoodScore(List<String> responses) {
    // Implementation
}
```

#### CSS Style

- Use kebab-case for class names
- Group related properties
- Add comments for complex styling

#### FXML

- Use meaningful fx:id values
- Keep layouts clean and readable
- Add comments for complex structures

### Commit Message Guidelines

Format: `type(scope): subject`

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

Examples:
```
feat(mood-tracker): Add mood history visualization
fix(journal): Resolve text wrapping issue
docs(readme): Update installation instructions
```

### Branch Naming

- `feature/feature-name` - For new features
- `bugfix/bug-description` - For bug fixes
- `hotfix/critical-fix` - For urgent fixes
- `docs/documentation-update` - For documentation

## Testing

### Running Tests

```bash
mvn test
```

### Writing Tests

- Write unit tests for new features
- Aim for high code coverage
- Test edge cases and error handling

## Documentation

- Update README.md for user-facing changes
- Add/update JavaDoc for code changes
- Update relevant wiki pages
- Include screenshots for UI changes

## Areas We Need Help With

- 🎨 **UI/UX Design**: Improve interface and user experience
- 🧪 **Testing**: Write and improve tests
- 📝 **Documentation**: Improve guides and API docs
- 🌐 **Internationalization**: Add support for multiple languages
- 🐛 **Bug Fixes**: Fix reported issues
- ✨ **New Features**: Implement requested features
- 📱 **Mobile Version**: Consider mobile adaptation
- ♿ **Accessibility**: Improve accessibility features

## Recognition

Contributors will be recognized in:
- README.md Contributors section
- Release notes
- Project website (if applicable)

## Questions?

Feel free to reach out to the team:
- Open a discussion on GitHub
- Email any team member (see README.md for contacts)

## Thank You!

Your contributions make this project better for everyone dealing with mental health challenges. Every bug fix, feature addition, and documentation improvement helps create a better experience for our users.

---

**Happy Contributing! 🎉**

*the_pathfinders Team*
