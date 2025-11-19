package com.liyaqa.infrastructure.messaging

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.stereotype.Component

/**
 * Error handler for Kafka consumer errors
 *
 * Handles:
 * - Deserialization errors
 * - Processing errors
 * - Connection errors
 * - Provides logging and retry logic
 */
@Component
class KafkaErrorHandler : CommonErrorHandler {

    private val logger = LoggerFactory.getLogger(KafkaErrorHandler::class.java)

    override fun handleBatch(
        thrownException: Exception,
        data: org.springframework.kafka.listener.ConsumerRecords<*, *>,
        consumer: Consumer<*, *>,
        container: MessageListenerContainer,
        invokeListener: Runnable
    ) {
        logger.error(
            "Error processing batch of {} records: {}",
            data.count(),
            thrownException.message,
            thrownException
        )

        // Log each record in the batch for debugging
        data.forEach { record ->
            logger.error(
                "Failed record - Topic: {}, Partition: {}, Offset: {}, Key: {}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key()
            )
        }

        // In production, you might want to:
        // - Send failed records to a dead letter queue (DLQ)
        // - Implement retry logic with exponential backoff
        // - Alert monitoring systems
    }

    override fun handleOne(
        thrownException: Exception,
        record: ConsumerRecord<*, *>,
        consumer: Consumer<*, *>,
        container: MessageListenerContainer
    ) {
        logger.error(
            "Error processing record - Topic: {}, Partition: {}, Offset: {}, Key: {}, Error: {}",
            record.topic(),
            record.partition(),
            record.offset(),
            record.key(),
            thrownException.message,
            thrownException
        )

        // In production, implement:
        // 1. Send to Dead Letter Queue (DLQ)
        sendToDeadLetterQueue(record, thrownException)

        // 2. Record metrics for monitoring
        recordErrorMetric(record, thrownException)

        // 3. Alert if error rate exceeds threshold
        checkErrorRateAndAlert(record.topic())
    }

    private fun sendToDeadLetterQueue(record: ConsumerRecord<*, *>, error: Exception) {
        logger.warn(
            "Would send to DLQ - Topic: {}, Partition: {}, Offset: {}, Error: {}",
            record.topic(),
            record.partition(),
            record.offset(),
            error.message
        )
        // TODO: Implement DLQ publishing
        // - Create DLQ topics (e.g., "member-events-dlq")
        // - Publish failed message with error details
        // - Include original headers and metadata
    }

    private fun recordErrorMetric(record: ConsumerRecord<*, *>, error: Exception) {
        logger.debug("Recording error metric for topic: {}", record.topic())
        // TODO: Integrate with metrics system (Prometheus, Micrometer, etc.)
    }

    private fun checkErrorRateAndAlert(topic: String) {
        logger.debug("Checking error rate for topic: {}", topic)
        // TODO: Implement error rate monitoring and alerting
    }
}
