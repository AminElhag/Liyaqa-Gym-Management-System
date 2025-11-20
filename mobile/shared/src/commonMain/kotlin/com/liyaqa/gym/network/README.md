# Gym Management Network Layer

This directory contains the complete network layer implementation for the Liyaqa Gym Management mobile application. It provides a comprehensive, type-safe API client for interacting with the gym management backend.

## Architecture

The network layer is organized into several key components:

### 1. Core Components

- **`GymApiClient`**: Main facade interface providing access to all API endpoints
- **`ApiClient`**: Low-level HTTP client interface (GET, POST, PUT, DELETE)
- **`HttpClientFactory`**: Factory for creating configured Ktor HttpClient instances
- **`TokenStorage`**: Interface for storing and retrieving authentication tokens
- **`AuthInterceptor`**: Handles JWT token injection and refresh logic

### 2. Domain Services

Located in `services/`:
- **`AuthApiService`**: Authentication operations (login, logout, refresh)
- **`MemberApiService`**: Member profile operations
- **`SubscriptionApiService`**: Subscription management
- **`ClassApiService`**: Class schedules and bookings
- **`PaymentApiService`**: Payment processing and invoices
- **`AccessApiService`**: Gym access check-in/check-out

### 3. Data Models

Located in `models/`:
- **`AuthModels.kt`**: Login, logout, token refresh DTOs
- **`MemberModels.kt`**: Member profile DTOs
- **`SubscriptionModels.kt`**: Subscription DTOs
- **`ClassModels.kt`**: Class schedule and booking DTOs
- **`PaymentModels.kt`**: Payment and invoice DTOs
- **`AccessModels.kt`**: Access log DTOs

### 4. Error Handling

- **`NetworkError`**: Sealed class hierarchy for different error types
  - `HttpError`: HTTP status code errors
  - `NetworkException`: Network connectivity errors
  - `Unauthorized`: 401 authentication errors
  - `Timeout`: Request timeout errors
  - `Unknown`: Unexpected errors

- **`ApiResult<T>`**: Result wrapper for API calls
  - `Success<T>`: Contains successful response data
  - `Error`: Contains NetworkError

## Usage

### Basic Setup

```kotlin
// 1. Create token storage (implement platform-specific version)
val tokenStorage = InMemoryTokenStorage() // or platform-specific implementation

// 2. Build API client
val apiClient = GymApiClientBuilder()
    .enableLogging(true) // Enable for development
    .tokenStorage(tokenStorage)
    .onTokenRefreshFailed {
        // Handle token refresh failure (e.g., navigate to login)
    }
    .build()
```

### Authentication

```kotlin
// Login
val result = apiClient.login("user@example.com", "password")
result.onSuccess { response ->
    println("Logged in: ${response.member.name}")
    println("Token: ${response.accessToken}")
}.onError { error ->
    println("Login failed: ${error.message}")
}

// Logout
apiClient.logout()

// Refresh token (handled automatically by interceptor)
apiClient.refreshToken()
```

### Member Profile

```kotlin
// Get member profile
val profile = apiClient.getMemberProfile(memberId)

// Update profile
val updateRequest = UpdateProfileRequest(
    name = "John Doe",
    phone = "+966501234567"
)
apiClient.updateProfile(memberId, updateRequest)
```

### Subscriptions

```kotlin
// Get member subscriptions
val subscriptions = apiClient.getSubscriptions(memberId, page = 0, size = 20)

// Renew subscription
val renewRequest = RenewSubscriptionRequest(
    planId = "plan-123",
    startDate = "2025-01-01",
    autoRenew = true
)
apiClient.renewSubscription(subscriptionId, renewRequest)

// Pause subscription
val pauseRequest = PauseSubscriptionRequest(
    pauseUntil = "2025-02-01",
    reason = "Vacation"
)
apiClient.pauseSubscription(subscriptionId, pauseRequest)
```

### Class Schedules & Bookings

```kotlin
// Get class schedules
val schedules = apiClient.getSchedules(
    startDate = "2025-01-01",
    endDate = "2025-01-31",
    page = 0,
    size = 20
)

// Book a class
val bookingRequest = CreateBookingRequest(scheduleId = "schedule-123")
apiClient.bookClass(memberId, bookingRequest)

// Cancel booking
val cancelRequest = CancelBookingRequest(reason = "Schedule conflict")
apiClient.cancelBooking(bookingId, cancelRequest)

// Get member bookings
apiClient.getMemberBookings(memberId)
```

### Payments & Invoices

```kotlin
// Process payment
val paymentRequest = PaymentRequest(
    subscriptionId = "sub-123",
    amount = 500.0,
    paymentMethod = PaymentMethod.CREDIT_CARD,
    description = "Monthly subscription"
)
apiClient.processPayment(paymentRequest)

// Get invoices
apiClient.getInvoices(memberId)

// Pay specific invoice
apiClient.payInvoice(invoiceId, paymentRequest)
```

### Access Control

```kotlin
// Check in
val checkInRequest = CheckInRequest(
    memberId = memberId,
    branchId = branchId,
    notes = "Regular check-in"
)
apiClient.checkIn(checkInRequest)

// Check out
val checkOutRequest = CheckOutRequest(accessLogId = "log-123")
apiClient.checkOut(checkOutRequest)

// Get access logs
apiClient.getAccessLogs(memberId)
```

## Error Handling

All API calls return `ApiResult<T>`, which can be handled using extension functions:

```kotlin
val result = apiClient.login(email, password)

// Pattern 1: Using onSuccess/onError
result
    .onSuccess { response ->
        // Handle success
        println("Login successful")
    }
    .onError { error ->
        // Handle error
        when (error) {
            is NetworkError.Unauthorized -> println("Invalid credentials")
            is NetworkError.Timeout -> println("Request timed out")
            is NetworkError.NetworkException -> println("Network error: ${error.message}")
            is NetworkError.HttpError -> println("HTTP ${error.statusCode}: ${error.message}")
            is NetworkError.Unknown -> println("Unknown error: ${error.message}")
        }
    }

// Pattern 2: Using when expression
when (result) {
    is ApiResult.Success -> {
        val data = result.data
        // Use data
    }
    is ApiResult.Error -> {
        val error = result.error
        // Handle error
    }
}

// Pattern 3: Get or null
val data = result.getOrNull()
if (data != null) {
    // Use data
} else {
    // Handle error
}
```

## Retry Logic

The network layer includes automatic retry logic for transient failures:

- **Server errors (5xx)**: Retries up to 3 times
- **Network exceptions**: Retries up to 3 times
- **Specific status codes**: 408 (Timeout), 429 (Too Many Requests), 503 (Service Unavailable), 504 (Gateway Timeout)
- **Backoff strategy**: Exponential backoff with max delay of 10 seconds

## Authentication Flow

1. **Login**: Call `apiClient.login()` - tokens are automatically stored
2. **Token Injection**: AuthInterceptor adds JWT to all requests
3. **Token Refresh**: Automatic refresh when token expires
4. **Logout**: Call `apiClient.logout()` - tokens are cleared

## Platform-Specific Engines

The HTTP client uses platform-specific engines:

- **Android**: OkHttp engine (`ktor-client-android`)
- **iOS**: Darwin engine (`ktor-client-darwin`) using NSURLSession

These are configured automatically via the `platformEngine()` expect/actual function.

## Configuration

### API Base URL

Configure in `ApiConfig.kt`:
```kotlin
const val BASE_URL = "http://localhost:8080/api/v1"
```

### Timeout Settings

```kotlin
const val TIMEOUT_MILLIS = 30_000L // 30 seconds
```

### Logging

Enable/disable logging when building the client:
```kotlin
val apiClient = GymApiClientBuilder()
    .enableLogging(BuildConfig.DEBUG) // Only in debug builds
    .build()
```

## Testing

For testing, use the `InMemoryTokenStorage` implementation:

```kotlin
val testStorage = InMemoryTokenStorage()
val testClient = GymApiClientBuilder()
    .tokenStorage(testStorage)
    .enableLogging(false)
    .build()
```

## Thread Safety

All API calls are suspend functions and thread-safe. Use with Kotlin Coroutines:

```kotlin
// In a ViewModel or similar
viewModelScope.launch {
    val result = apiClient.getSchedules()
    // Handle result on main thread
}
```

## Best Practices

1. **Dependency Injection**: Inject `GymApiClient` as a singleton
2. **Error Handling**: Always handle errors appropriately
3. **Loading States**: Show loading indicators during API calls
4. **Token Storage**: Implement secure, platform-specific token storage
5. **Logging**: Disable logging in production builds
6. **Coroutines**: Use appropriate coroutine scopes (viewModelScope, lifecycleScope)
7. **Caching**: Consider implementing a caching layer for frequently accessed data

## Example: Complete Flow

```kotlin
class MemberViewModel(
    private val apiClient: GymApiClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            apiClient.login(email, password)
                .onSuccess { response ->
                    _uiState.value = UiState.Success(response.member)
                }
                .onError { error ->
                    _uiState.value = UiState.Error(error.message)
                }
        }
    }
}
```
