# Liyaqa Android App

This is the Android mobile application for the Liyaqa Gym Management System, built with Jetpack Compose and following modern Android development best practices.

## Tech Stack

### Core
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern declarative UI framework
- **Material Design 3** - Latest Material Design guidelines
- **Single Activity Architecture** - Using Navigation Compose

### Architecture & DI
- **Hilt** - Dependency injection for Android-specific components
- **Koin** - Dependency injection for shared Kotlin Multiplatform module
- **MVVM** - Model-View-ViewModel architecture pattern
- **Clean Architecture** - Separation of concerns with layers

### Jetpack Libraries
- **Navigation Compose** - Type-safe navigation
- **ViewModel** - UI state management
- **LiveData** - Observable data holder
- **Lifecycle** - Lifecycle-aware components

### UI & UX
- **Accompanist** - Compose utilities
  - Permissions - Runtime permission handling
  - System UI Controller - Status and navigation bar theming

### Camera & Scanning
- **CameraX** - Modern camera API
- **ML Kit Barcode Scanning** - QR code scanning

### Fitness & Health
- **Google Fit API** - Fitness data integration
- **Activity Recognition** - Workout tracking
- **Body Sensors** - Heart rate and health metrics

### Firebase
- **Firebase Cloud Messaging (FCM)** - Push notifications
- **Firebase Analytics** - User analytics
- **Firebase Crashlytics** - Crash reporting

### Networking & Data
- **Coil** - Image loading and caching
- **Coroutines** - Asynchronous programming
- **Kotlin Serialization** - JSON serialization

## Project Structure

```
androidApp/
├── src/main/
│   ├── kotlin/com/liyaqa/android/
│   │   ├── di/                    # Hilt dependency injection modules
│   │   │   ├── AppModule.kt       # App-level dependencies
│   │   │   └── NetworkModule.kt   # Network dependencies
│   │   ├── service/               # Android services
│   │   │   └── LiyaqaFirebaseMessagingService.kt
│   │   ├── ui/
│   │   │   ├── theme/             # Material Design 3 theme
│   │   │   │   ├── Color.kt       # Color palette
│   │   │   │   ├── Theme.kt       # Theme composition
│   │   │   │   ├── Type.kt        # Typography
│   │   │   │   └── Shape.kt       # Shapes
│   │   │   ├── screens/           # Screen composables
│   │   │   │   ├── splash/        # Splash screen
│   │   │   │   └── home/          # Home screen with bottom navigation
│   │   │   ├── components/        # Reusable UI components
│   │   │   └── navigation/        # Navigation graph
│   │   │       └── NavGraph.kt
│   │   ├── viewmodels/            # ViewModels for UI state
│   │   ├── utils/                 # Utility classes
│   │   │   ├── PermissionUtils.kt
│   │   │   └── DateTimeUtils.kt
│   │   ├── LiyaqaApplication.kt   # Application class
│   │   └── MainActivity.kt        # Main activity
│   ├── res/
│   │   ├── drawable/              # Drawable resources
│   │   ├── values/                # Values (strings, colors, themes)
│   │   └── xml/                   # XML configurations
│   └── AndroidManifest.xml
├── build.gradle.kts               # Module build configuration
├── google-services.json           # Firebase configuration
└── README.md                      # This file
```

## Setup Instructions

### 1. Firebase Configuration

**IMPORTANT:** The `google-services.json` file in this project is a placeholder and will not work.

To set up Firebase:

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Add an Android app with package name: `com.liyaqa.gym.android`
4. Download the `google-services.json` file
5. Replace the placeholder file at `mobile/androidApp/google-services.json`

Enable the following Firebase services:
- **Cloud Messaging** - For push notifications
- **Analytics** - For user analytics
- **Crashlytics** - For crash reporting

### 2. Google Fit API Configuration

To enable fitness tracking:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Enable the **Fitness API**
3. Create OAuth 2.0 credentials
4. Add the SHA-1 fingerprint of your signing certificate

### 3. Build Configuration

Minimum requirements:
- Android Studio Ladybug or newer
- JDK 21
- Android SDK 34
- Gradle 8.7+

### 4. Running the App

```bash
# Build and run on connected device or emulator
./gradlew :mobile:androidApp:installDebug

# Run tests
./gradlew :mobile:androidApp:test

# Build release APK
./gradlew :mobile:androidApp:assembleRelease
```

## Features

### Implemented
- ✅ Material Design 3 theming with light/dark mode
- ✅ Single Activity Architecture with Compose Navigation
- ✅ Firebase Cloud Messaging integration
- ✅ Deep linking support for notifications
- ✅ Notification channels for different types
- ✅ Dependency injection with Hilt and Koin
- ✅ Permission utilities for runtime permissions
- ✅ Date/time utilities

### To Be Implemented
- ⏳ Authentication screens (login, register)
- ⏳ Class list and details screens
- ⏳ Booking management
- ⏳ QR code scanner for check-in
- ⏳ Profile and settings screens
- ⏳ Fitness tracking integration
- ⏳ Payment integration
- ⏳ Offline-first data synchronization

## Permissions

The app requires the following permissions:

### Required Permissions
- **INTERNET** - Network communication
- **ACCESS_NETWORK_STATE** - Check network connectivity

### Optional Permissions (Runtime)
- **CAMERA** - QR code scanning for check-in
- **POST_NOTIFICATIONS** - Push notifications (Android 13+)
- **ACTIVITY_RECOGNITION** - Fitness tracking
- **BODY_SENSORS** - Heart rate monitoring

## Deep Linking

The app supports deep linking for:

### HTTPS Links
- `https://liyaqa.com/booking/{bookingId}` - Open booking details
- `https://liyaqa.com/class/{classId}` - Open class details

### Custom Scheme
- `liyaqa://booking/{bookingId}` - Open booking details
- `liyaqa://class/{classId}` - Open class details
- `liyaqa://promotion/{promotionId}` - Open promotion details

## Theme Customization

The app uses Material Design 3 with support for:
- Dynamic color (Android 12+)
- Light and dark themes
- Custom brand colors for Liyaqa
- RTL support for Arabic

To customize colors, edit:
- `ui/theme/Color.kt` - Color definitions
- `res/values/colors.xml` - XML color resources

## Contributing

1. Follow the existing code style and architecture
2. Use Kotlin coding conventions
3. Write unit tests for new features
4. Update documentation as needed

## License

Copyright © 2024 Liyaqa. All rights reserved.
