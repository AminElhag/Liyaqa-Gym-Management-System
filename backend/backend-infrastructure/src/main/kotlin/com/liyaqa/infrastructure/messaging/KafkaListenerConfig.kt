package com.liyaqa.infrastructure.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer

/**
 * Kafka consumer configuration for event listening
 *
 * Configures:
 * - Kafka listener container factory
 * - Consumer properties for reliability
 * - JSON deserialization
 * - Error handling
 */
@EnableKafka
@Configuration
class KafkaListenerConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    @Value("\${spring.kafka.consumer.auto-offset-reset:earliest}")
    private lateinit var autoOffsetReset: String

    @Value("\${spring.kafka.consumer.enable-auto-commit:false}")
    private var enableAutoCommit: Boolean = false

    @Value("\${spring.kafka.listener.ack-mode:manual}")
    private lateinit var ackMode: String

    /**
     * Consumer factory with JSON deserialization and error handling
     */
    @Bean
    fun consumerFactory(kafkaObjectMapper: ObjectMapper): ConsumerFactory<String, String> {
        val configProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to enableAutoCommit,

            // Deserializers with error handling
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS to StringDeserializer::class.java,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to StringDeserializer::class.java,

            // Performance settings
            ConsumerConfig.FETCH_MIN_BYTES_CONFIG to 1,
            ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG to 500,
            ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG to 1048576,
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 500,
            ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG to 300000,

            // Session settings
            ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to 30000,
            ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG to 10000,

            // JSON deserializer settings
            JsonDeserializer.TRUSTED_PACKAGES to "*",
            JsonDeserializer.USE_TYPE_INFO_HEADERS to false,
            JsonDeserializer.VALUE_DEFAULT_TYPE to "java.lang.String"
        )

        return DefaultKafkaConsumerFactory(configProps)
    }

    /**
     * Kafka listener container factory for @KafkaListener annotations
     */
    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>
    ): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory

        // Acknowledgment mode
        factory.containerProperties.ackMode = when (ackMode.uppercase()) {
            "MANUAL" -> ContainerProperties.AckMode.MANUAL
            "MANUAL_IMMEDIATE" -> ContainerProperties.AckMode.MANUAL_IMMEDIATE
            "BATCH" -> ContainerProperties.AckMode.BATCH
            "RECORD" -> ContainerProperties.AckMode.RECORD
            else -> ContainerProperties.AckMode.MANUAL
        }

        // Concurrency
        factory.setConcurrency(3)

        // Error handling
        factory.setCommonErrorHandler(KafkaErrorHandler())

        return factory
    }
}
