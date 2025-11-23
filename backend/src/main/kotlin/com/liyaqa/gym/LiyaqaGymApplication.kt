package com.liyaqa.gym

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.kafka.annotation.EnableKafka

/**
 * Main entry point for the Liyaqa Gym Management System.
 *
 * This Spring Boot application manages gym operations including:
 * - Member management
 * - Class scheduling and booking
 * - Trainer management
 * - Payment processing
 * - Attendance tracking
 */
@SpringBootApplication(
    exclude = [
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration::class,
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration::class
    ]
)
@EnableCaching
@EnableJpaAuditing
@EnableKafka
class LiyaqaGymApplication

fun main(args: Array<String>) {
    runApplication<LiyaqaGymApplication>(*args)
}
