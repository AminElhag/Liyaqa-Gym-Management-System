package com.liyaqa.gym.network

import com.liyaqa.gym.network.models.*
import com.liyaqa.gym.network.services.*

/**
 * Main API client interface providing access to all gym management endpoints.
 * Organized by domain with suspend functions returning ApiResult<T>.
 */
interface GymApiClient {

    // Auth domain
    suspend fun login(email: String, password: String): ApiResult<LoginResponse>
    suspend fun logout(): ApiResult<Unit>
    suspend fun refreshToken(): ApiResult<RefreshTokenResponse>

    // Member domain
    suspend fun getMemberProfile(memberId: String): ApiResult<MemberResponse>
    suspend fun updateProfile(memberId: String, request: UpdateProfileRequest): ApiResult<MemberResponse>

    // Subscription domain
    suspend fun getSubscriptions(memberId: String, page: Int = 0, size: Int = 20): ApiResult<SubscriptionListResponse>
    suspend fun getSubscriptionById(id: String): ApiResult<SubscriptionResponse>
    suspend fun renewSubscription(subscriptionId: String, request: RenewSubscriptionRequest): ApiResult<SubscriptionResponse>
    suspend fun pauseSubscription(subscriptionId: String, request: PauseSubscriptionRequest): ApiResult<SubscriptionResponse>
    suspend fun resumeSubscription(subscriptionId: String): ApiResult<SubscriptionResponse>
    suspend fun cancelSubscription(subscriptionId: String, request: CancelSubscriptionRequest): ApiResult<SubscriptionResponse>

    // Class domain
    suspend fun getSchedules(
        startDate: String? = null,
        endDate: String? = null,
        classId: String? = null,
        page: Int = 0,
        size: Int = 20
    ): ApiResult<ScheduleListResponse>
    suspend fun getScheduleById(id: String): ApiResult<ScheduleResponse>
    suspend fun bookClass(memberId: String, request: CreateBookingRequest): ApiResult<BookingResponse>
    suspend fun getMemberBookings(memberId: String, page: Int = 0, size: Int = 20): ApiResult<BookingListResponse>
    suspend fun getBookingById(id: String): ApiResult<BookingResponse>
    suspend fun cancelBooking(bookingId: String, request: CancelBookingRequest): ApiResult<BookingResponse>

    // Payment domain
    suspend fun processPayment(request: PaymentRequest): ApiResult<PaymentResponse>
    suspend fun getInvoices(memberId: String, page: Int = 0, size: Int = 20): ApiResult<InvoiceListResponse>
    suspend fun getInvoiceById(id: String): ApiResult<InvoiceResponse>
    suspend fun payInvoice(invoiceId: String, request: PaymentRequest): ApiResult<PaymentResponse>

    // Access domain
    suspend fun checkIn(request: CheckInRequest): ApiResult<CheckInResponse>
    suspend fun checkOut(request: CheckOutRequest): ApiResult<CheckOutResponse>
    suspend fun getAccessLogs(memberId: String, page: Int = 0, size: Int = 20): ApiResult<AccessLogListResponse>
}

/**
 * Default implementation of GymApiClient that delegates to domain-specific service implementations
 */
class GymApiClientImpl(
    private val authService: AuthApiService,
    private val memberService: MemberApiService,
    private val subscriptionService: SubscriptionApiService,
    private val classService: ClassApiService,
    private val paymentService: PaymentApiService,
    private val accessService: AccessApiService,
    private val tokenStorage: TokenStorage
) : GymApiClient {

    // Auth domain
    override suspend fun login(email: String, password: String): ApiResult<LoginResponse> {
        return authService.login(email, password).also { result ->
            if (result is ApiResult.Success) {
                // Save tokens on successful login
                tokenStorage.saveAccessToken(result.data.accessToken)
                tokenStorage.saveRefreshToken(result.data.refreshToken)

                // Calculate and save token expiration
                val expiresAt = System.currentTimeMillis() + (result.data.expiresIn * 1000)
                tokenStorage.saveTokenExpiration(expiresAt)
            }
        }
    }

    override suspend fun logout(): ApiResult<Unit> {
        val refreshToken = tokenStorage.getRefreshToken() ?: return ApiResult.Success(Unit)
        return authService.logout(refreshToken).also {
            // Clear tokens regardless of API response
            tokenStorage.clearTokens()
        }
    }

    override suspend fun refreshToken(): ApiResult<RefreshTokenResponse> {
        val refreshToken = tokenStorage.getRefreshToken()
            ?: return ApiResult.Error(NetworkError.Unauthorized)

        return authService.refreshToken(refreshToken).also { result ->
            if (result is ApiResult.Success) {
                // Update tokens
                tokenStorage.saveAccessToken(result.data.accessToken)
                tokenStorage.saveRefreshToken(result.data.refreshToken)

                // Calculate and save new expiration
                val expiresAt = System.currentTimeMillis() + (result.data.expiresIn * 1000)
                tokenStorage.saveTokenExpiration(expiresAt)
            } else {
                // Clear tokens on refresh failure
                tokenStorage.clearTokens()
            }
        }
    }

    // Member domain
    override suspend fun getMemberProfile(memberId: String): ApiResult<MemberResponse> {
        return memberService.getMemberById(memberId)
    }

    override suspend fun updateProfile(
        memberId: String,
        request: UpdateProfileRequest
    ): ApiResult<MemberResponse> {
        return memberService.updateMember(memberId, request)
    }

    // Subscription domain
    override suspend fun getSubscriptions(
        memberId: String,
        page: Int,
        size: Int
    ): ApiResult<SubscriptionListResponse> {
        return subscriptionService.getMemberSubscriptions(memberId, page, size)
    }

    override suspend fun getSubscriptionById(id: String): ApiResult<SubscriptionResponse> {
        return subscriptionService.getSubscriptionById(id)
    }

    override suspend fun renewSubscription(
        subscriptionId: String,
        request: RenewSubscriptionRequest
    ): ApiResult<SubscriptionResponse> {
        return subscriptionService.renewSubscription(subscriptionId, request)
    }

    override suspend fun pauseSubscription(
        subscriptionId: String,
        request: PauseSubscriptionRequest
    ): ApiResult<SubscriptionResponse> {
        return subscriptionService.pauseSubscription(subscriptionId, request)
    }

    override suspend fun resumeSubscription(subscriptionId: String): ApiResult<SubscriptionResponse> {
        return subscriptionService.resumeSubscription(subscriptionId)
    }

    override suspend fun cancelSubscription(
        subscriptionId: String,
        request: CancelSubscriptionRequest
    ): ApiResult<SubscriptionResponse> {
        return subscriptionService.cancelSubscription(subscriptionId, request)
    }

    // Class domain
    override suspend fun getSchedules(
        startDate: String?,
        endDate: String?,
        classId: String?,
        page: Int,
        size: Int
    ): ApiResult<ScheduleListResponse> {
        return classService.getSchedules(startDate, endDate, classId, page, size)
    }

    override suspend fun getScheduleById(id: String): ApiResult<ScheduleResponse> {
        return classService.getScheduleById(id)
    }

    override suspend fun bookClass(
        memberId: String,
        request: CreateBookingRequest
    ): ApiResult<BookingResponse> {
        return classService.bookClass(memberId, request)
    }

    override suspend fun getMemberBookings(
        memberId: String,
        page: Int,
        size: Int
    ): ApiResult<BookingListResponse> {
        return classService.getMemberBookings(memberId, page, size)
    }

    override suspend fun getBookingById(id: String): ApiResult<BookingResponse> {
        return classService.getBookingById(id)
    }

    override suspend fun cancelBooking(
        bookingId: String,
        request: CancelBookingRequest
    ): ApiResult<BookingResponse> {
        return classService.cancelBooking(bookingId, request)
    }

    // Payment domain
    override suspend fun processPayment(request: PaymentRequest): ApiResult<PaymentResponse> {
        return paymentService.processPayment(request)
    }

    override suspend fun getInvoices(
        memberId: String,
        page: Int,
        size: Int
    ): ApiResult<InvoiceListResponse> {
        return paymentService.getInvoices(memberId, page, size)
    }

    override suspend fun getInvoiceById(id: String): ApiResult<InvoiceResponse> {
        return paymentService.getInvoiceById(id)
    }

    override suspend fun payInvoice(
        invoiceId: String,
        request: PaymentRequest
    ): ApiResult<PaymentResponse> {
        return paymentService.payInvoice(invoiceId, request)
    }

    // Access domain
    override suspend fun checkIn(request: CheckInRequest): ApiResult<CheckInResponse> {
        return accessService.checkIn(request)
    }

    override suspend fun checkOut(request: CheckOutRequest): ApiResult<CheckOutResponse> {
        return accessService.checkOut(request)
    }

    override suspend fun getAccessLogs(
        memberId: String,
        page: Int,
        size: Int
    ): ApiResult<AccessLogListResponse> {
        return accessService.getAccessLogs(memberId, page, size)
    }
}

/**
 * Builder for creating GymApiClient instances
 */
class GymApiClientBuilder {
    private var enableLogging: Boolean = false
    private var tokenStorage: TokenStorage = InMemoryTokenStorage()
    private var onTokenRefreshFailed: suspend () -> Unit = {}

    fun enableLogging(enabled: Boolean) = apply {
        this.enableLogging = enabled
    }

    fun tokenStorage(storage: TokenStorage) = apply {
        this.tokenStorage = storage
    }

    fun onTokenRefreshFailed(callback: suspend () -> Unit) = apply {
        this.onTokenRefreshFailed = callback
    }

    fun build(): GymApiClient {
        // Create HTTP client
        val httpClient = HttpClientFactory.create(
            enableLogging = enableLogging,
            tokenStorage = tokenStorage,
            onTokenRefreshFailed = onTokenRefreshFailed
        )

        // Create base API client
        val apiClient = KtorApiClient(httpClient)

        // Create domain services
        val authService = AuthApiServiceImpl(apiClient)
        val memberService = MemberApiServiceImpl(apiClient)
        val subscriptionService = SubscriptionApiServiceImpl(apiClient)
        val classService = ClassApiServiceImpl(apiClient)
        val paymentService = PaymentApiServiceImpl(apiClient)
        val accessService = AccessApiServiceImpl(apiClient)

        return GymApiClientImpl(
            authService = authService,
            memberService = memberService,
            subscriptionService = subscriptionService,
            classService = classService,
            paymentService = paymentService,
            accessService = accessService,
            tokenStorage = tokenStorage
        )
    }
}
