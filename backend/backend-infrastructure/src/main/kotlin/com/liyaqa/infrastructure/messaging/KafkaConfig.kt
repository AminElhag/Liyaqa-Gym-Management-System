package com.liyaqa.infrastructure.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.retry.annotation.EnableRetry

/**
 * Kafka configuration for event publishing and consumption
 *
 * Configures:
 * - Kafka topics for different event types
 * - KafkaTemplate with JSON serialization
 * - Producer properties for reliability and performance
 */
@Configuration
@EnableRetry
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.producer.acks:all}")
    private lateinit var acks: String

    @Value("\${spring.kafka.producer.retries:3}")
    private var retries: Int = 3

    @Value("\${spring.kafka.producer.compression-type:snappy}")
    private lateinit var compressionType: String

    companion object {
        // Topic names
        const val MEMBER_EVENTS_TOPIC = "member-events"
        const val SUBSCRIPTION_EVENTS_TOPIC = "subscription-events"
        const val BOOKING_EVENTS_TOPIC = "booking-events"
        const val PAYMENT_EVENTS_TOPIC = "payment-events"
        const val ACCESS_EVENTS_TOPIC = "access-events"
        const val NOTIFICATION_EVENTS_TOPIC = "notification-events"

        // Topic configuration
        private const val TOPIC_PARTITIONS = 3
        private const val TOPIC_REPLICAS = 1 // Increase in production
    }

    /**
     * Object mapper configured for domain event serialization
     */
    @Bean
    fun kafkaObjectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT)
    }

    /**
     * Producer factory with JSON serialization
     */
    @Bean
    fun producerFactory(kafkaObjectMapper: ObjectMapper): ProducerFactory<String, Any> {
        val configProps = mutableMapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,

            // Reliability settings
            ProducerConfig.ACKS_CONFIG to acks,
            ProducerConfig.RETRIES_CONFIG to retries,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,

            // Performance settings
            ProducerConfig.COMPRESSION_TYPE_CONFIG to compressionType,
            ProducerConfig.BATCH_SIZE_CONFIG to 16384,
            ProducerConfig.LINGER_MS_CONFIG to 10,
            ProducerConfig.BUFFER_MEMORY_CONFIG to 33554432,

            // Timeout settings
            ProducerConfig.MAX_BLOCK_MS_CONFIG to 5000,
            ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG to 30000,
            ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG to 120000,

            // JSON serializer configuration
            JsonSerializer.ADD_TYPE_INFO_HEADERS to false
        )

        val factory = DefaultKafkaProducerFactory<String, Any>(configProps)
        factory.setValueSerializer(JsonSerializer(kafkaObjectMapper))
        return factory
    }

    /**
     * Kafka template for publishing events
     */
    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

    // Topic definitions

    @Bean
    fun memberEventsTopic(): NewTopic {
        return TopicBuilder
            .name(MEMBER_EVENTS_TOPIC)
            .partitions(TOPIC_PARTITIONS)
            .replicas(TOPIC_REPLICAS)
            .config("retention.ms", "604800000") // 7 days
            .config("cleanup.policy", "delete")
            .build()
    }

    @Bean
    fun subscriptionEventsTopic(): NewTopic {
        return TopicBuilder
            .name(SUBSCRIPTION_EVENTS_TOPIC)
            .partitions(TOPIC_PARTITIONS)
            .replicas(TOPIC_REPLICAS)
            .config("retention.ms", "2592000000") // 30 days
            .config("cleanup.policy", "delete")
            .build()
    }

    @Bean
    fun bookingEventsTopic(): NewTopic {
        return TopicBuilder
            .name(BOOKING_EVENTS_TOPIC)
            .partitions(TOPIC_PARTITIONS)
            .replicas(TOPIC_REPLICAS)
            .config("retention.ms", "604800000") // 7 days
            .config("cleanup.policy", "delete")
            .build()
    }

    @Bean
    fun paymentEventsTopic(): NewTopic {
        return TopicBuilder
            .name(PAYMENT_EVENTS_TOPIC)
            .partitions(TOPIC_PARTITIONS)
            .replicas(TOPIC_REPLICAS)
            .config("retention.ms", "31536000000") // 365 days (important for financial records)
            .config("cleanup.policy", "delete")
            .build()
    }

    @Bean
    fun accessEventsTopic(): NewTopic {
        return TopicBuilder
            .name(ACCESS_EVENTS_TOPIC)
            .partitions(TOPIC_PARTITIONS)
            .replicas(TOPIC_REPLICAS)
            .config("retention.ms", "2592000000") // 30 days
            .config("cleanup.policy", "delete")
            .build()
    }

    @Bean
    fun notificationEventsTopic(): NewTopic {
        return TopicBuilder
            .name(NOTIFICATION_EVENTS_TOPIC)
            .partitions(TOPIC_PARTITIONS)
            .replicas(TOPIC_REPLICAS)
            .config("retention.ms", "259200000") // 3 days
            .config("cleanup.policy", "delete")
            .build()
    }
}
