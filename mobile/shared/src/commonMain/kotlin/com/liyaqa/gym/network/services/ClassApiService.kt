package com.liyaqa.gym.network.services

import com.liyaqa.gym.network.ApiClient
import com.liyaqa.gym.network.ApiConfig
import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.models.BookingListResponse
import com.liyaqa.gym.network.models.BookingResponse
import com.liyaqa.gym.network.models.CancelBookingRequest
import com.liyaqa.gym.network.models.CreateBookingRequest
import com.liyaqa.gym.network.models.ScheduleListResponse
import com.liyaqa.gym.network.models.ScheduleResponse

/**
 * API service for class and booking operations
 */
interface ClassApiService {
    suspend fun getSchedules(
        startDate: String? = null,
        endDate: String? = null,
        classId: String? = null,
        page: Int = 0,
        size: Int = 20
    ): ApiResult<ScheduleListResponse>

    suspend fun getScheduleById(id: String): ApiResult<ScheduleResponse>

    suspend fun bookClass(
        memberId: String,
        request: CreateBookingRequest
    ): ApiResult<BookingResponse>

    suspend fun getMemberBookings(
        memberId: String,
        page: Int = 0,
        size: Int = 20
    ): ApiResult<BookingListResponse>

    suspend fun getBookingById(id: String): ApiResult<BookingResponse>

    suspend fun cancelBooking(
        bookingId: String,
        request: CancelBookingRequest
    ): ApiResult<BookingResponse>
}

/**
 * Default implementation of ClassApiService
 */
class ClassApiServiceImpl(
    private val apiClient: ApiClient
) : ClassApiService {

    override suspend fun getSchedules(
        startDate: String?,
        endDate: String?,
        classId: String?,
        page: Int,
        size: Int
    ): ApiResult<ScheduleListResponse> {
        val params = mutableMapOf(
            "page" to page.toString(),
            "size" to size.toString()
        )
        startDate?.let { params["startDate"] = it }
        endDate?.let { params["endDate"] = it }
        classId?.let { params["classId"] = it }

        return apiClient.get(ApiConfig.Endpoints.CLASS_SCHEDULES, params)
    }

    override suspend fun getScheduleById(id: String): ApiResult<ScheduleResponse> {
        val path = ApiConfig.Endpoints.CLASS_SCHEDULE_BY_ID.replace("{id}", id)
        return apiClient.get(path)
    }

    override suspend fun bookClass(
        memberId: String,
        request: CreateBookingRequest
    ): ApiResult<BookingResponse> {
        return apiClient.post(ApiConfig.Endpoints.BOOKINGS, request)
    }

    override suspend fun getMemberBookings(
        memberId: String,
        page: Int,
        size: Int
    ): ApiResult<BookingListResponse> {
        val path = ApiConfig.Endpoints.MEMBER_BOOKINGS.replace("{memberId}", memberId)
        val params = mapOf(
            "page" to page.toString(),
            "size" to size.toString()
        )
        return apiClient.get(path, params)
    }

    override suspend fun getBookingById(id: String): ApiResult<BookingResponse> {
        val path = ApiConfig.Endpoints.BOOKING_BY_ID.replace("{id}", id)
        return apiClient.get(path)
    }

    override suspend fun cancelBooking(
        bookingId: String,
        request: CancelBookingRequest
    ): ApiResult<BookingResponse> {
        val path = "${ApiConfig.Endpoints.BOOKING_BY_ID.replace("{id}", bookingId)}/cancel"
        return apiClient.post(path, request)
    }
}
