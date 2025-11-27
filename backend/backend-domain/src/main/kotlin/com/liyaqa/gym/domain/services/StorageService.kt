package com.liyaqa.gym.domain.services

import java.io.InputStream

/**
 * Interface for file storage service.
 * Implementations can use S3, Azure Blob Storage, or local file system.
 */
interface StorageService {

    /**
     * Upload a file to storage.
     *
     * @param file The file to upload
     * @param path The path where to store the file
     * @param allowedTypes List of allowed MIME types
     * @param maxSizeMB Maximum file size in MB
     * @return Result containing the upload result with URL
     */
    suspend fun upload(
        file: FileUpload,
        path: String,
        allowedTypes: List<String>,
        maxSizeMB: Int
    ): Result<UploadResult>

    /**
     * Delete a file from storage.
     *
     * @param url The URL or path of the file to delete
     * @return Result indicating success or failure
     */
    suspend fun delete(url: String): Result<Unit>

    /**
     * Get a signed URL for secure file access.
     *
     * @param path The file path
     * @param expirationMinutes How long the URL should be valid
     * @return Result containing the signed URL
     */
    suspend fun getSignedUrl(path: String, expirationMinutes: Int = 60): Result<String>
}

/**
 * Domain model for file upload (framework-agnostic)
 */
data class FileUpload(
    val fileName: String,
    val contentType: String,
    val size: Long,
    val inputStream: InputStream
)

/**
 * Result of a file upload operation
 */
data class UploadResult(
    val url: String,
    val path: String,
    val fileName: String,
    val fileSize: Long,
    val contentType: String
)
