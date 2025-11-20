package com.liyaqa.gym.network.services

import com.liyaqa.gym.network.ApiClient
import com.liyaqa.gym.network.ApiConfig
import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.models.AccessLogListResponse
import com.liyaqa.gym.network.models.CheckInRequest
import com.liyaqa.gym.network.models.CheckInResponse
import com.liyaqa.gym.network.models.CheckOutRequest
import com.liyaqa.gym.network.models.CheckOutResponse

/**
 * API service for access control operations
 */
interface AccessApiService {
    suspend fun checkIn(request: CheckInRequest): ApiResult<CheckInResponse>

    suspend fun checkOut(request: CheckOutRequest): ApiResult<CheckOutResponse>

    suspend fun getAccessLogs(
        memberId: String,
        page: Int = 0,
        size: Int = 20
    ): ApiResult<AccessLogListResponse>
}

/**
 * Default implementation of AccessApiService
 */
class AccessApiServiceImpl(
    private val apiClient: ApiClient
) : AccessApiService {

    override suspend fun checkIn(request: CheckInRequest): ApiResult<CheckInResponse> {
        return apiClient.post(ApiConfig.Endpoints.ACCESS_CHECK_IN, request)
    }

    override suspend fun checkOut(request: CheckOutRequest): ApiResult<CheckOutResponse> {
        return apiClient.post(ApiConfig.Endpoints.ACCESS_CHECK_OUT, request)
    }

    override suspend fun getAccessLogs(
        memberId: String,
        page: Int,
        size: Int
    ): ApiResult<AccessLogListResponse> {
        val path = ApiConfig.Endpoints.MEMBER_ACCESS_LOGS.replace("{memberId}", memberId)
        val params = mapOf(
            "page" to page.toString(),
            "size" to size.toString()
        )
        return apiClient.get(path, params)
    }
}
