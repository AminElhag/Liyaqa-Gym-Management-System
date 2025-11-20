package com.liyaqa.gym.network.services

import com.liyaqa.gym.network.ApiClient
import com.liyaqa.gym.network.ApiConfig
import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.models.CancelSubscriptionRequest
import com.liyaqa.gym.network.models.PauseSubscriptionRequest
import com.liyaqa.gym.network.models.RenewSubscriptionRequest
import com.liyaqa.gym.network.models.SubscriptionListResponse
import com.liyaqa.gym.network.models.SubscriptionResponse

/**
 * API service for subscription operations
 */
interface SubscriptionApiService {
    suspend fun getMemberSubscriptions(
        memberId: String,
        page: Int = 0,
        size: Int = 20
    ): ApiResult<SubscriptionListResponse>

    suspend fun getSubscriptionById(id: String): ApiResult<SubscriptionResponse>

    suspend fun renewSubscription(
        subscriptionId: String,
        request: RenewSubscriptionRequest
    ): ApiResult<SubscriptionResponse>

    suspend fun pauseSubscription(
        subscriptionId: String,
        request: PauseSubscriptionRequest
    ): ApiResult<SubscriptionResponse>

    suspend fun resumeSubscription(subscriptionId: String): ApiResult<SubscriptionResponse>

    suspend fun cancelSubscription(
        subscriptionId: String,
        request: CancelSubscriptionRequest
    ): ApiResult<SubscriptionResponse>
}

/**
 * Default implementation of SubscriptionApiService
 */
class SubscriptionApiServiceImpl(
    private val apiClient: ApiClient
) : SubscriptionApiService {

    override suspend fun getMemberSubscriptions(
        memberId: String,
        page: Int,
        size: Int
    ): ApiResult<SubscriptionListResponse> {
        val path = ApiConfig.Endpoints.MEMBER_SUBSCRIPTIONS.replace("{memberId}", memberId)
        val params = mapOf(
            "page" to page.toString(),
            "size" to size.toString()
        )
        return apiClient.get(path, params)
    }

    override suspend fun getSubscriptionById(id: String): ApiResult<SubscriptionResponse> {
        val path = ApiConfig.Endpoints.SUBSCRIPTION_BY_ID.replace("{id}", id)
        return apiClient.get(path)
    }

    override suspend fun renewSubscription(
        subscriptionId: String,
        request: RenewSubscriptionRequest
    ): ApiResult<SubscriptionResponse> {
        val path = "${ApiConfig.Endpoints.SUBSCRIPTION_BY_ID.replace("{id}", subscriptionId)}/renew"
        return apiClient.post(path, request)
    }

    override suspend fun pauseSubscription(
        subscriptionId: String,
        request: PauseSubscriptionRequest
    ): ApiResult<SubscriptionResponse> {
        val path = "${ApiConfig.Endpoints.SUBSCRIPTION_BY_ID.replace("{id}", subscriptionId)}/pause"
        return apiClient.post(path, request)
    }

    override suspend fun resumeSubscription(subscriptionId: String): ApiResult<SubscriptionResponse> {
        val path = "${ApiConfig.Endpoints.SUBSCRIPTION_BY_ID.replace("{id}", subscriptionId)}/resume"
        return apiClient.post(path, Unit)
    }

    override suspend fun cancelSubscription(
        subscriptionId: String,
        request: CancelSubscriptionRequest
    ): ApiResult<SubscriptionResponse> {
        val path = "${ApiConfig.Endpoints.SUBSCRIPTION_BY_ID.replace("{id}", subscriptionId)}/cancel"
        return apiClient.post(path, request)
    }
}
