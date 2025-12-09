# EduSpot - Educational Learning Platform

EduSpot is an Android application designed to provide an interactive educational learning experience. The app integrates with Firebase for real-time data management and offers a modern, user-friendly interface for educational content.

## Features

- **Modern UI/UX**: Clean and intuitive interface built with Material Design
- **Firebase Integration**: Real-time database connectivity for dynamic content
- **Educational Content**: Interactive learning modules and resources
- **Responsive Design**: Optimized for various Android device sizes
- **Kotlin Development**: Built with modern Android development practices

## Technical Stack

- **Language**: Kotlin
- **Platform**: Android (API 24+)
- **Backend**: Firebase (Database, Analytics)
- **UI Framework**: Material Design Components
- **Architecture**: MVVM with ViewBinding
- **Dependencies**: 
  - Firebase BOM 32.7.0
  - Material Design Components
  - AndroidX Libraries
  - Kotlin Coroutines

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/eduspot/    # Main application code
│   ├── res/                         # Resources (layouts, drawables, values)
│   └── AndroidManifest.xml         # App configuration
├── build.gradle.kts                # Build configuration
├── google-services.json            # Firebase configuration
└── proguard-rules.pro              # Code obfuscation rules
```

## Prerequisites

- Android Studio Arctic Fox or later
- Android SDK API 24 or higher
- Firebase project setup
- Git (for version control)

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/eduspot.git
   cd eduspot
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Firebase Configuration**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Firebase Database and Analytics in your Firebase project

4. **Build and Run**
   - Sync project with Gradle files
   - Connect an Android device or start an emulator
   - Click "Run" to build and install the app

## Development

### Building the Project

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## Configuration

### Firebase Setup

1. Create a new Firebase project
2. Add an Android app to your project
3. Download the `google-services.json` file
4. Place it in the `app/` directory
5. Configure Firebase services in your project

### Build Configuration

The app is configured for:
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Compile SDK**: API 34

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

For questions or support, please contact the development team or create an issue in this repository.

## Acknowledgments

- Firebase for backend services
- Material Design for UI components
- Android development community for best practices

