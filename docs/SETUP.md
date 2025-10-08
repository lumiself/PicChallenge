# PicChallenge Setup Guide

## 🚀 Quick Start

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or later
- JDK 11 or later
- Android SDK with API level 21+ (Android 5.0+)
- Git for version control

### 1. Clone the Repository
```bash
git clone https://github.com/[your-username]/picchallenge-android.git
cd picchallenge-android
```

### 2. Open in Android Studio
1. Launch Android Studio
2. Select "Open an existing Android Studio project"
3. Navigate to the cloned folder and click "OK"
4. Wait for Gradle sync to complete

### 3. Build the Project
```bash
# Build debug version
./gradlew assembleDebug

# Or build release version (requires signing configuration)
./gradlew assembleRelease
```

### 4. Run the App
- Connect an Android device or start an emulator
- Click the "Run" button in Android Studio
- Select your device/emulator

## 🔧 Configuration

### API Endpoint Setup
The app connects to a WordPress backend with the Photo Contest plugin. To configure your endpoint:

1. Open the app
2. Go to Settings (gear icon)
3. Enter your WordPress site URL
4. The app will automatically detect the API endpoints

### Required WordPress Plugins
- **Photo Contest Plugin**: For contest and photo management
- **JWT Authentication**: For secure API access (if you add login features later)
- **WP REST API**: Core WordPress REST API (usually included)

## 📱 Development

### Project Structure
```
picchallenge-android/
├── app/
│   ├── src/main/java/com/example/picchallenge/
│   │   ├── data/          # Data layer (models, repositories, API)
│   │   ├── di/            # Dependency injection modules
│   │   ├── ui/            # UI layer (screens, components, themes)
│   │   └── utils/         # Utility classes
│   └── src/main/res/      # Resources (layouts, drawables, etc.)
├── docs/                  # Documentation
├── screenshots/           # App screenshots
└── README.md             # Project overview
```

### Key Files
- `MainActivity.kt` - Main activity with navigation
- `ContestListScreen.kt` - Contest browsing interface
- `ContestDetailScreen.kt` - Contest details and voting
- `PhotoContestApiService.kt` - API service interface
- `ContestRepository.kt` - Data repository

### Architecture
The app follows Clean Architecture with MVVM pattern:
- **UI Layer**: Jetpack Compose screens and ViewModels
- **Domain Layer**: Business logic and models
- **Data Layer**: Repositories and API services

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

## 📦 Building for Release

### 1. Generate Signing Key
Follow the instructions in `KEYSTORE_GENERATION_STEPS.md`

### 2. Configure Signing
Create `keystore.properties` file:
```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=your_key_alias
storeFile=../your_keystore_file.jks
```

### 3. Build Release APK/AAB
```bash
# Build APK
./gradlew assembleRelease

# Build App Bundle (recommended for Play Store)
./gradlew bundleRelease
```

## 🐛 Troubleshooting

### Common Issues

1. **Gradle Sync Failed**
   - Check internet connection
   - Update Android Studio
   - Clear Gradle cache: `./gradlew clean`

2. **Build Errors**
   - Ensure all dependencies are up to date
   - Check Kotlin version compatibility
   - Verify Android SDK is properly installed

3. **API Connection Issues**
   - Verify WordPress site URL is correct
   - Check if Photo Contest plugin is installed
   - Ensure proper SSL certificates

### Getting Help
- Check existing issues on GitHub
- Create a new issue with detailed description
- Include error logs and device information

## 📋 Play Store Preparation

1. **Screenshots**: Take screenshots of main features
2. **Description**: Prepare app description (see README.md)
3. **Privacy Policy**: Link to `privacy-policy.html`
4. **Content Rating**: Complete content rating questionnaire
5. **App Signing**: Configure in Play Console

## 🔗 Useful Links
- [Android Developer Documentation](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design Guidelines](https://material.io/design)
- [WordPress Photo Contest Plugin](https://wordpress.org/plugins/search/photo+contest/)

---
**Happy coding!** 🎉
