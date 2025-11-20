package com.liyaqa.gym.network.services

import com.liyaqa.gym.network.ApiClient
import com.liyaqa.gym.network.ApiConfig
import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.models.MemberListResponse
import com.liyaqa.gym.network.models.MemberResponse
import com.liyaqa.gym.network.models.UpdateProfileRequest

/**
 * API service for member-related operations
 */
interface MemberApiService {
    suspend fun getMembers(
        page: Int = 0,
        size: Int = 20,
        search: String? = null
    ): ApiResult<MemberListResponse>

    suspend fun getMemberById(id: String): ApiResult<MemberResponse>
    suspend fun updateMember(id: String, request: UpdateProfileRequest): ApiResult<MemberResponse>
}

/**
 * Default implementation of MemberApiService
 */
class MemberApiServiceImpl(
    private val apiClient: ApiClient
) : MemberApiService {

    override suspend fun getMembers(
        page: Int,
        size: Int,
        search: String?
    ): ApiResult<MemberListResponse> {
        val params = mutableMapOf(
            "page" to page.toString(),
            "size" to size.toString()
        )
        search?.let { params["search"] = it }

        return apiClient.get(ApiConfig.Endpoints.MEMBERS, params)
    }

    override suspend fun getMemberById(id: String): ApiResult<MemberResponse> {
        val path = ApiConfig.Endpoints.MEMBER_BY_ID.replace("{id}", id)
        return apiClient.get(path)
    }

    override suspend fun updateMember(id: String, request: UpdateProfileRequest): ApiResult<MemberResponse> {
        val path = ApiConfig.Endpoints.MEMBER_BY_ID.replace("{id}", id)
        return apiClient.put(path, request)
    }
}
