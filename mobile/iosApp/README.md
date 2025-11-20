# Liyaqa iOS App

Native iOS application for the Liyaqa Gym Management System built with SwiftUI and Kotlin Multiplatform.

## Overview

The Liyaqa iOS app provides a comprehensive gym management experience for members, featuring class bookings, check-ins, membership management, and health integration.

## Features

- **Authentication**: Secure login with email/password, biometric authentication (Face ID/Touch ID), and Sign in with Apple
- **Home Dashboard**: View upcoming classes, recent activity, and membership status
- **Class Management**: Browse, search, filter, and book fitness classes
- **QR Code Check-In**: Scan QR codes or enter member ID for gym check-in
- **Profile Management**: Update personal information, view payment history, and manage subscriptions
- **Settings**: Configure notifications, biometric login, dark mode, and HealthKit integration
- **HealthKit Integration**: Sync workout data with Apple Health

## Project Structure

```
iosApp/
├── Liyaqa.xcodeproj/          # Xcode project configuration
├── Liyaqa/
│   ├── App/                   # App entry point
│   │   ├── LiyaqaApp.swift    # Main app structure
│   │   └── KoinHelper.kt      # Koin DI initialization
│   ├── Views/
│   │   ├── Auth/              # Login, Register screens
│   │   ├── Home/              # Dashboard
│   │   ├── Classes/           # Class list, detail, check-in
│   │   ├── Profile/           # Profile, settings
│   │   └── Components/        # Reusable SwiftUI components
│   ├── ViewModels/            # Observable view models
│   │   ├── AuthViewModel.swift
│   │   ├── HomeViewModel.swift
│   │   ├── ClassViewModel.swift
│   │   └── ProfileViewModel.swift
│   ├── Services/              # iOS-specific services
│   │   ├── AuthService.swift
│   │   ├── NotificationService.swift
│   │   ├── HealthKitService.swift
│   │   └── BiometricAuthService.swift
│   ├── Utils/                 # Extensions, helpers
│   │   ├── KeychainHelper.swift
│   │   ├── KoinHelper.swift
│   │   ├── Extensions.swift
│   │   └── NavigationRouter.swift
│   ├── Resources/
│   │   └── Theme/             # Colors, typography, view modifiers
│   └── Info.plist             # App configuration and permissions
└── README.md
```

## Requirements

- iOS 15.0+
- Xcode 15.0+
- Swift 5.9+
- Kotlin Multiplatform shared framework

## Setup

### 1. Build Shared Framework

Before opening the iOS project, you need to build the shared Kotlin Multiplatform framework:

```bash
cd mobile/shared
./gradlew :shared:assembleXCFramework
```

This will generate the framework at:
- Debug: `shared/build/XCFrameworks/debug/shared.framework`
- Release: `shared/build/XCFrameworks/release/shared.framework`

### 2. Open Xcode Project

```bash
cd mobile/iosApp
open Liyaqa.xcodeproj
```

### 3. Configure Development Team

1. Select the Liyaqa target in Xcode
2. Go to Signing & Capabilities
3. Select your development team
4. Xcode will automatically configure provisioning

### 4. Configure Capabilities

The following capabilities are already configured in the project:

- **Push Notifications**: For class reminders and updates
- **HealthKit**: For workout data synchronization
- **Sign in with Apple**: For OAuth authentication
- **Background Modes**: For remote notifications

### 5. Run the App

Select a simulator or device and press Cmd+R to build and run.

## Architecture

### SwiftUI + MVVM

The app follows the MVVM (Model-View-ViewModel) architecture pattern:

- **Views**: SwiftUI views that render the UI
- **ViewModels**: ObservableObject classes that manage state and business logic
- **Models**: Data structures representing domain entities
- **Services**: Platform-specific services (HealthKit, Notifications, Auth)

### Kotlin Multiplatform Integration

The app integrates with the shared Kotlin Multiplatform module for:

- Network requests via Ktor
- Local database with SQLDelight
- Business logic and repositories
- Data models

Integration is done through:
- Koin DI for dependency injection
- Swift-friendly Kotlin APIs
- Shared repository pattern

## Key Features

### Authentication

```swift
// Email/Password Login
await viewModel.login()

// Biometric Login
await viewModel.loginWithBiometrics()

// Registration
await viewModel.register()
```

### Class Booking

```swift
// Browse classes
await viewModel.loadClasses()

// Book a class
await viewModel.bookClass(gymClass)

// Cancel booking
await viewModel.cancelBooking(gymClass)
```

### QR Code Check-In

The app supports two check-in methods:
1. QR code scanning using the device camera
2. Manual member ID entry

### HealthKit Integration

```swift
// Request authorization
HealthKitService.shared.requestAuthorization()

// Save workout
try await HealthKitService.shared.saveWorkout(
    activityType: .traditionalStrengthTraining,
    start: startDate,
    end: endDate,
    calories: 250.0
)

// Get today's calories
let calories = try await HealthKitService.shared.getTodayCaloriesBurned()
```

### Notifications

```swift
// Request permission
NotificationService.shared.requestAuthorization()

// Schedule class reminder
NotificationService.shared.scheduleClassReminder(
    className: "HIIT Training",
    classDate: date
)
```

## Theme System

### Colors

The app uses a Material Design 3 inspired color system with custom Liyaqa brand colors:

```swift
Color.liyaqaBrand        // Primary brand color (#00BFA5)
Color.liyaqaBrandVariant // Brand variant (#00897B)
Color.liyaqaAccent       // Accent color (#FF6F00)
```

All colors support dark mode automatically.

### Typography

Material Design 3 typography scale:

```swift
Text("Title").font(.titleLarge)
Text("Body").font(.bodyMedium)
Text("Label").font(.labelSmall)
```

### View Modifiers

Custom view modifiers for consistent styling:

```swift
// Card style
VStack { ... }
    .cardStyle()

// Primary button
Text("Submit")
    .primaryButtonStyle()

// Text field
TextField("Email", text: $email)
    .textFieldStyle(icon: "envelope.fill")

// Loading state
view.loading(isLoading)

// Empty state
view.emptyState(
    isEmpty: items.isEmpty,
    icon: "calendar",
    title: "No Classes",
    message: "No classes available"
)
```

## Security

### Keychain Storage

Sensitive data is stored securely in the iOS Keychain:

```swift
// Save token
KeychainHelper.shared.saveToken("token_value")

// Retrieve token
let token = KeychainHelper.shared.getToken()

// Delete token
KeychainHelper.shared.deleteToken()
```

### Biometric Authentication

Support for Face ID and Touch ID:

```swift
let result = try await BiometricAuthService.shared.authenticate()
```

## Deep Linking

The app supports deep links with the `liyaqa://` URL scheme:

- `liyaqa://class?id=123` - Open class detail
- `liyaqa://profile` - Open profile
- `liyaqa://checkin` - Open check-in

## Permissions

The app requests the following permissions (configured in Info.plist):

- **Camera**: For QR code scanning
- **Photos**: For profile picture updates
- **HealthKit**: For workout data sync
- **Notifications**: For class reminders
- **Location (When In Use)**: For location-based check-in
- **Face ID**: For biometric authentication

## Build Configurations

### Debug

- Logs enabled
- Development server endpoints
- Debug framework from shared module

### Release

- Logs disabled
- Production server endpoints
- Release framework from shared module
- Code optimization enabled

## Testing

Run tests in Xcode:

```bash
# All tests
cmd+u

# Specific test
cmd+u (select test case)
```

## Troubleshooting

### Framework Not Found

If you get "framework not found" errors:

1. Build the shared framework: `cd mobile/shared && ./gradlew :shared:assembleXCFramework`
2. Clean build folder in Xcode: `Cmd+Shift+K`
3. Rebuild: `Cmd+B`

### Signing Issues

1. Go to Signing & Capabilities
2. Select your development team
3. Let Xcode automatically manage signing

### HealthKit Not Working

1. Ensure HealthKit capability is enabled
2. Check Info.plist for usage descriptions
3. Request authorization before accessing data

## Contributing

1. Follow Swift style guide
2. Use SwiftUI for all UI components
3. Follow MVVM architecture pattern
4. Add unit tests for view models
5. Use async/await for asynchronous operations

## License

Copyright © 2024 Liyaqa. All rights reserved.
