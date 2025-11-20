# Liyaqa Gym Management - Shared Module

This is the Kotlin Multiplatform shared module for the Liyaqa Gym Management mobile applications (Android & iOS).

## Architecture

The shared module follows a clean architecture pattern with the following layers:

### Domain Layer (`domain/`)
Contains the core business models and entities:
- `Member` - Gym member entity
- `Subscription` - Membership subscription
- `GymClass` - Fitness class definition
- `ClassSchedule` - Scheduled class instances
- `Booking` - Class booking/reservation
- `ContactInfo` - Value object for contact information
- Enums: `Gender`, `MemberStatus`, `SubscriptionStatus`, `BookingStatus`, `ClassType`, `ClassLevel`

### Network Layer (`network/`)
Handles all API communication using Ktor:
- `ApiClient` - Base HTTP client interface
- `KtorApiClient` - Ktor implementation
- `HttpClientFactory` - Configures Ktor with JSON serialization, logging, auth, etc.
- `ApiConfig` - API endpoints and configuration
- `NetworkError` - Error handling and result wrappers
- `services/` - Specific API service implementations (MemberApiService, etc.)

### Database Layer (`database/`)
Local SQLDelight database for offline caching:
- Schema files (`.sq`): Member, Subscription, ClassSchedule, Booking
- `DatabaseDriverFactory` - Platform-specific database driver (expect/actual)
- Queries for CRUD operations and complex lookups

### Cache Layer (`cache/`)
Caching strategies and utilities:
- `CacheStrategy` - Defines cache behavior (NetworkFirst, CacheFirst, etc.)
- `CachedResult` - Wrapper for cached data with freshness info

### Data Layer (`data/`)
Repository pattern for data access:
- `Repository` - Base repository interface
- `DataResult` - Result wrapper with cache metadata

### Dependency Injection (`di/`)
Koin modules for DI:
- `sharedModule` - Common DI configuration
- `platformModule` - Platform-specific dependencies (expect/actual)

### Utils (`utils/`)
Utility functions and extensions:
- `DateTimeExtensions` - DateTime formatting and manipulation helpers

## Technology Stack

- **Kotlin Multiplatform** - Share code between Android and iOS
- **Ktor Client** - Networking and HTTP client
- **Kotlinx Serialization** - JSON serialization/deserialization
- **SQLDelight** - Type-safe SQL database
- **Kotlinx Coroutines** - Asynchronous programming
- **Kotlinx DateTime** - Cross-platform date/time handling
- **Koin** - Dependency injection

## Platform Targets

- **Android** - `androidTarget` with JVM target 21
- **iOS** - `iosX64`, `iosArm64`, `iosSimulatorArm64`

## Usage

### Android

```kotlin
// In your Android Application class
import com.liyaqa.gym.di.platformModule
import com.liyaqa.gym.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(platformModule, sharedModule())
        }
    }
}
```

### iOS

```swift
// In your iOS app initialization
import shared

func initKoin() {
    KoinKt.doInitKoin { koin in
        // Configure Koin if needed
    }
}
```

## Building

The shared module is built as part of the main project:

```bash
./gradlew :mobile:shared:build
```

### iOS Framework

The iOS framework is automatically generated and can be integrated into Xcode:

```bash
./gradlew :mobile:shared:linkDebugFrameworkIosSimulatorArm64
```

## API Configuration

Update `ApiConfig.BASE_URL` in `network/ApiConfig.kt` to point to your backend server:

```kotlin
const val BASE_URL = "https://your-api-server.com/api/v1"
```

## Database Migrations

SQLDelight schema is defined in `.sq` files. To modify the database:

1. Update the `.sq` files in `src/commonMain/sqldelight/`
2. Rebuild the project to regenerate database code
3. Handle migrations in platform-specific code if needed

## Testing

Unit tests can be written in `src/commonTest/`:

```kotlin
class MemberTest {
    @Test
    fun testMemberAge() {
        // Test common code
    }
}
```

## Future Enhancements

- [ ] Add more API services (Subscription, Class, Booking)
- [ ] Implement repository layer with caching
- [ ] Add authentication token management
- [ ] Implement offline-first data sync
- [ ] Add more comprehensive error handling
- [ ] Write unit tests for domain logic
- [ ] Add integration tests for API services
