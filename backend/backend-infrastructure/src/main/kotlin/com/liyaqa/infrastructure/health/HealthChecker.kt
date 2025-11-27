package com.liyaqa.infrastructure.health

import com.liyaqa.gym.domain.entities.health.ComponentHealth
import com.liyaqa.gym.domain.entities.health.HealthStatus
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.stereotype.Component
import javax.sql.DataSource

/**
 * Base interface for health checkers
 */
interface HealthChecker {
    suspend fun check(): ComponentHealth
}

/**
 * Database health checker
 */
@Component
class DatabaseHealthChecker(
    private val dataSource: DataSource
) : HealthChecker {

    private val logger = LoggerFactory.getLogger(DatabaseHealthChecker::class.java)

    override suspend fun check(): ComponentHealth {
        val startTime = System.currentTimeMillis()

        return try {
            withTimeout(5000) { // 5 second timeout
                dataSource.connection.use { connection ->
                    connection.createStatement().use { statement ->
                        statement.executeQuery("SELECT 1").use { resultSet ->
                            if (resultSet.next()) {
                                val responseTime = System.currentTimeMillis() - startTime
                                ComponentHealth(
                                    status = if (responseTime < 1000) HealthStatus.HEALTHY else HealthStatus.DEGRADED,
                                    message = "Database connection successful",
                                    responseTimeMs = responseTime,
                                    details = mapOf(
                                        "driver" to connection.metaData.driverName,
                                        "url" to connection.metaData.url
                                    )
                                )
                            } else {
                                ComponentHealth(
                                    status = HealthStatus.UNHEALTHY,
                                    message = "Database query returned no results"
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Database health check failed", e)
            ComponentHealth(
                status = HealthStatus.UNHEALTHY,
                message = "Database connection failed: ${e.message}"
            )
        }
    }
}

/**
 * Redis health checker
 */
@Component
class RedisHealthChecker(
    private val redisConnectionFactory: RedisConnectionFactory
) : HealthChecker {

    private val logger = LoggerFactory.getLogger(RedisHealthChecker::class.java)

    override suspend fun check(): ComponentHealth {
        val startTime = System.currentTimeMillis()

        return try {
            withTimeout(5000) { // 5 second timeout
                val connection = redisConnectionFactory.connection
                val pong = connection.ping()
                connection.close()

                val responseTime = System.currentTimeMillis() - startTime
                ComponentHealth(
                    status = if (responseTime < 500) HealthStatus.HEALTHY else HealthStatus.DEGRADED,
                    message = "Redis connection successful",
                    responseTimeMs = responseTime,
                    details = mapOf(
                        "response" to (pong ?: "OK")
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Redis health check failed", e)
            ComponentHealth(
                status = HealthStatus.UNHEALTHY,
                message = "Redis connection failed: ${e.message}"
            )
        }
    }
}

/**
 * Kafka health checker
 */
@Component
class KafkaHealthChecker(
    private val kafkaAdmin: KafkaAdmin
) : HealthChecker {

    private val logger = LoggerFactory.getLogger(KafkaHealthChecker::class.java)

    override suspend fun check(): ComponentHealth {
        val startTime = System.currentTimeMillis()

        return try {
            withTimeout(5000) { // 5 second timeout
                // Try to list topics to verify Kafka connectivity
                val adminClient = org.apache.kafka.clients.admin.AdminClient.create(kafkaAdmin.configurationProperties)
                val topics = adminClient.listTopics().names().get()
                adminClient.close()

                val responseTime = System.currentTimeMillis() - startTime
                ComponentHealth(
                    status = if (responseTime < 2000) HealthStatus.HEALTHY else HealthStatus.DEGRADED,
                    message = "Kafka connection successful",
                    responseTimeMs = responseTime,
                    details = mapOf(
                        "topicCount" to topics.size
                    )
                )
            }
        } catch (e: Exception) {
            logger.warn("Kafka health check failed - this is normal if Kafka is not configured: ${e.message}")
            // Return healthy status if Kafka is optional
            ComponentHealth(
                status = HealthStatus.HEALTHY,
                message = "Kafka not configured (optional component)",
                details = mapOf(
                    "optional" to true
                )
            )
        }
    }
}
