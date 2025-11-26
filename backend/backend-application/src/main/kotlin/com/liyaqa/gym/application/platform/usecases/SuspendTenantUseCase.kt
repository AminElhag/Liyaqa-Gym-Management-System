package com.liyaqa.gym.application.platform.usecases

import com.liyaqa.gym.application.platform.commands.SuspendTenantCommand
import com.liyaqa.gym.domain.entities.tenant.TenantStatus
import com.liyaqa.gym.domain.exceptions.InvalidTenantStatusException
import com.liyaqa.gym.domain.exceptions.TenantNotFoundException
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.domain.services.EmailService
import com.liyaqa.gym.domain.services.EventPublisher
import com.liyaqa.gym.domain.services.TenantAccessRevoker
import com.liyaqa.gym.domain.services.TenantSuspendedEvent
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Use case for suspending a tenant.
 * Suspends access for all tenant users and sends notification.
 */
@Service
class SuspendTenantUseCase(
    private val tenantRepository: TenantRepository,
    private val accessRevoker: TenantAccessRevoker,
    private val emailService: EmailService,
    private val eventPublisher: EventPublisher
) {

    suspend fun execute(command: SuspendTenantCommand): Result<Unit> {
        return try {
            // Find tenant
            val tenantOpt = tenantRepository.findById(command.tenantId).getOrThrow()
            if (!tenantOpt.isPresent) {
                return Result.failure(TenantNotFoundException(command.tenantId))
            }

            val tenant = tenantOpt.get()

            // Validate that tenant can be suspended
            if (tenant.status == TenantStatus.SUSPENDED) {
                return Result.failure(
                    InvalidTenantStatusException(
                        tenant.id,
                        tenant.status.name,
                        "Tenant is already suspended"
                    )
                )
            }

            if (tenant.status == TenantStatus.CANCELLED) {
                return Result.failure(
                    InvalidTenantStatusException(
                        tenant.id,
                        tenant.status.name,
                        "Cannot suspend a cancelled tenant"
                    )
                )
            }

            // Update status
            val updatedTenant = tenant.suspend(command.reason)
            tenantRepository.save(updatedTenant).getOrThrow()

            // Revoke access for all users
            accessRevoker.revokeTenantAccess(tenant.id).getOrThrow()

            // Publish event
            eventPublisher.publish(
                TenantSuspendedEvent(
                    tenantId = tenant.id,
                    tenantName = tenant.name,
                    reason = command.reason,
                    occurredAt = Instant.now()
                )
            )

            // Send notification
            emailService.sendTenantSuspensionNotice(
                tenant = updatedTenant,
                reason = command.reason
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
