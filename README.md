# PicChallenge - Model & Everyday Issues Photo Contests

A beautiful Android app for browsing photography contests featuring models and individuals addressing everyday life themes. Built with modern Android development practices and ready for Play Store publication.

## 🌟 Features

- **Browse Themed Contests**: Discover photo contests focused on everyday issues and life themes
- **Model & Individual Photography**: View stunning photos from models and everyday people
- **Vote & Engage**: Support your favorite contestants by voting for their photos
- **Blog Content**: Read photography tips, contest news, and lifestyle articles
- **Privacy-Friendly**: No login required - just download and start exploring
- **Offline Ready**: Images are cached for better performance

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt/Dagger
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Custom caching solution
- **Async Programming**: Coroutines + Flow

## 📱 App Architecture

```
┌─────────────────────────────────────┐
│            UI Layer                 │
│  ┌─────────────┐ ┌─────────────┐   │
│  │   Screens   │ │  ViewModels │   │
│  └──────┬──────┘ └──────┬──────┘   │
└─────────┼──────────────┼───────────┘
          │              │
┌─────────▼──────────────▼───────────┐
│         Domain Layer               │
│  ┌─────────────┐ ┌─────────────┐   │
│  │   Models    │ │   Use Cases │   │
│  └─────────────┘ └─────────────┘   │
└─────────┬──────────────┬───────────┘
          │              │
┌─────────▼──────────────▼───────────┐
│        Data Layer                  │
│  ┌─────────────┐ ┌─────────────┐   │
│  │ Repository  │ │   API Service│   │
│  └─────────────┘ └─────────────┘   │
└─────────────────────────────────────┘
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK 21+ (Android 5.0+)

### Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/[your-username]/picchallenge-android.git
   cd picchallenge-android
   ```

2. Open the project in Android Studio

3. Build and run:
   ```bash
   ./gradlew assembleDebug
   ```

## 📸 Screenshots

*[Add your screenshots here]*

## 🔧 Configuration

The app connects to a WordPress backend with the Photo Contest plugin. Configure your API endpoint in the app settings.

## 📋 Play Store Ready Features

- ✅ No login required (privacy-friendly)
- ✅ Material Design 3 compliance
- ✅ Android best practices implemented
- ✅ Optimized for performance
- ✅ Ready for app bundle generation

## 📝 Privacy Policy

This app does not collect any personal data. View our [privacy policy](privacy-policy.html) for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Contact

For questions or support, please open an issue on GitHub.

---

**Made with ❤️ for photography enthusiasts and contest lovers**
