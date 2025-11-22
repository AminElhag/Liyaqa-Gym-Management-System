# Google Services Configuration

## Current Status
The current `google-services.json` file is a **PLACEHOLDER** for development purposes only.

## To Use Firebase in Production

1. **Create a Firebase Project:**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or select existing one
   - Add an Android app to your project

2. **Configure Your App:**
   - Package name: `com.liyaqa.gym.android`
   - Download the `google-services.json` file from Firebase Console
   - Replace the placeholder file with your downloaded file

3. **Enable Firebase Services:**
   - Firebase Cloud Messaging (for push notifications)
   - Firebase Analytics (for app analytics)
   - Firebase Crashlytics (for crash reporting)

## Services Currently Configured

The app uses these Firebase services (see `build.gradle.kts`):
- `firebase-messaging-ktx` - Push notifications
- `firebase-analytics-ktx` - App analytics
- `firebase-crashlytics-ktx` - Crash reporting

## Development Note

The placeholder configuration allows the app to build and run, but Firebase features will not work until you configure a real Firebase project.