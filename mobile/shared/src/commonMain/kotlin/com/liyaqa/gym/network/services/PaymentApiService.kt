package com.liyaqa.gym.network.services

import com.liyaqa.gym.network.ApiClient
import com.liyaqa.gym.network.ApiConfig
import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.models.InvoiceListResponse
import com.liyaqa.gym.network.models.InvoiceResponse
import com.liyaqa.gym.network.models.PaymentRequest
import com.liyaqa.gym.network.models.PaymentResponse

/**
 * API service for payment operations
 */
interface PaymentApiService {
    suspend fun processPayment(request: PaymentRequest): ApiResult<PaymentResponse>

    suspend fun getInvoices(
        memberId: String,
        page: Int = 0,
        size: Int = 20
    ): ApiResult<InvoiceListResponse>

    suspend fun getInvoiceById(id: String): ApiResult<InvoiceResponse>

    suspend fun payInvoice(
        invoiceId: String,
        request: PaymentRequest
    ): ApiResult<PaymentResponse>
}

/**
 * Default implementation of PaymentApiService
 */
class PaymentApiServiceImpl(
    private val apiClient: ApiClient
) : PaymentApiService {

    override suspend fun processPayment(request: PaymentRequest): ApiResult<PaymentResponse> {
        return apiClient.post(ApiConfig.Endpoints.PAYMENTS, request)
    }

    override suspend fun getInvoices(
        memberId: String,
        page: Int,
        size: Int
    ): ApiResult<InvoiceListResponse> {
        val path = ApiConfig.Endpoints.MEMBER_INVOICES.replace("{memberId}", memberId)
        val params = mapOf(
            "page" to page.toString(),
            "size" to size.toString()
        )
        return apiClient.get(path, params)
    }

    override suspend fun getInvoiceById(id: String): ApiResult<InvoiceResponse> {
        val path = ApiConfig.Endpoints.INVOICE_BY_ID.replace("{id}", id)
        return apiClient.get(path)
    }

    override suspend fun payInvoice(
        invoiceId: String,
        request: PaymentRequest
    ): ApiResult<PaymentResponse> {
        val path = "${ApiConfig.Endpoints.INVOICE_BY_ID.replace("{id}", invoiceId)}/pay"
        return apiClient.post(path, request)
    }
}
