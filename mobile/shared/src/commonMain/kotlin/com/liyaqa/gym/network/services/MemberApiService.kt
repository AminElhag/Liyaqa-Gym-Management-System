package com.liyaqa.gym.network.services

import com.liyaqa.gym.domain.Member
import com.liyaqa.gym.network.ApiClient
import com.liyaqa.gym.network.ApiConfig
import com.liyaqa.gym.network.ApiResult
import kotlinx.serialization.Serializable

/**
 * API service for member-related operations
 */
interface MemberApiService {
    suspend fun getMembers(
        page: Int = 0,
        size: Int = 20,
        search: String? = null
    ): ApiResult<MemberListResponse>

    suspend fun getMemberById(id: String): ApiResult<Member>
    suspend fun createMember(request: CreateMemberRequest): ApiResult<Member>
    suspend fun updateMember(id: String, request: UpdateMemberRequest): ApiResult<Member>
    suspend fun deleteMember(id: String): ApiResult<Unit>
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

    override suspend fun getMemberById(id: String): ApiResult<Member> {
        val path = ApiConfig.Endpoints.MEMBER_BY_ID.replace("{id}", id)
        return apiClient.get(path)
    }

    override suspend fun createMember(request: CreateMemberRequest): ApiResult<Member> {
        return apiClient.post(ApiConfig.Endpoints.MEMBERS, request)
    }

    override suspend fun updateMember(id: String, request: UpdateMemberRequest): ApiResult<Member> {
        val path = ApiConfig.Endpoints.MEMBER_BY_ID.replace("{id}", id)
        return apiClient.put(path, request)
    }

    override suspend fun deleteMember(id: String): ApiResult<Unit> {
        val path = ApiConfig.Endpoints.MEMBER_BY_ID.replace("{id}", id)
        return apiClient.delete(path)
    }
}

// Request/Response models
@Serializable
data class MemberListResponse(
    val content: List<Member>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int
)

@Serializable
data class CreateMemberRequest(
    val branchId: String,
    val name: String,
    val nameArabic: String? = null,
    val email: String,
    val phone: String,
    val nationalId: String? = null,
    val gender: String,
    val dateOfBirth: String? = null
)

@Serializable
data class UpdateMemberRequest(
    val name: String? = null,
    val nameArabic: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val notes: String? = null
)
