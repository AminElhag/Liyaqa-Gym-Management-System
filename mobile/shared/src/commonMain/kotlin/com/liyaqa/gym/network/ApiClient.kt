package com.liyaqa.gym.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException

/**
 * Base API client abstract class with reified type parameters
 */
abstract class ApiClient {
    abstract val httpClient: HttpClient

    suspend inline fun <reified T> get(
        path: String,
        queryParameters: Map<String, String> = emptyMap()
    ): ApiResult<T> = safeApiCall {
        httpClient.get(path) {
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }.body<T>()
    }

    suspend inline fun <reified T, reified R> post(
        path: String,
        body: T
    ): ApiResult<R> = safeApiCall {
        httpClient.post(path) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<R>()
    }

    suspend inline fun <reified T, reified R> put(
        path: String,
        body: T
    ): ApiResult<R> = safeApiCall {
        httpClient.put(path) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<R>()
    }

    suspend inline fun <reified T> delete(path: String): ApiResult<T> = safeApiCall {
        httpClient.delete(path).body<T>()
    }

    suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
        return try {
            ApiResult.Success(apiCall())
        } catch (e: ClientRequestException) {
            handleHttpError(e.response)
        } catch (e: ServerResponseException) {
            handleHttpError(e.response)
        } catch (e: HttpRequestTimeoutException) {
            ApiResult.Error(NetworkError.Timeout)
        } catch (e: SerializationException) {
            ApiResult.Error(
                NetworkError.Unknown(
                    message = "Failed to parse response: ${e.message}",
                    cause = e
                )
            )
        } catch (e: Exception) {
            ApiResult.Error(
                NetworkError.NetworkException(
                    message = e.message ?: "Unknown network error",
                    cause = e
                )
            )
        }
    }

    suspend fun <T> handleHttpError(response: HttpResponse): ApiResult<T> {
        val statusCode = response.status.value
        val errorResponse = try {
            response.body<ErrorResponse>()
        } catch (e: Exception) {
            null
        }

        return if (statusCode == 401) {
            ApiResult.Error(NetworkError.Unauthorized)
        } else {
            ApiResult.Error(NetworkError.HttpError(statusCode, errorResponse))
        }
    }
}

/**
 * Default implementation of ApiClient using Ktor
 */
class KtorApiClient(
    override val httpClient: HttpClient
) : ApiClient()
