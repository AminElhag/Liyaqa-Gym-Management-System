package com.liyaqa.gym.application.platform.usecases

import com.liyaqa.gym.application.platform.commands.CreateTenantCommand
import com.liyaqa.gym.domain.entities.tenant.*
import com.liyaqa.gym.domain.exceptions.SlugAlreadyExistsException
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import com.liyaqa.gym.domain.repositories.TenantUserRepository
import com.liyaqa.gym.domain.services.EmailService
import com.liyaqa.gym.domain.services.EventPublisher
import com.liyaqa.gym.domain.services.TenantCreatedEvent
import com.liyaqa.gym.domain.valueobjects.Money
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDate
import java.util.*
import kotlin.text.Charsets.UTF_8

/**
 * Use case for creating a new tenant on the platform.
 * Handles tenant creation, owner account setup, subscription initialization, and welcome email.
 */
@Service
class CreateTenantUseCase(
    private val tenantRepository: TenantRepository,
    private val tenantUserRepository: TenantUserRepository,
    private val subscriptionRepository: TenantSubscriptionRepository,
    private val emailService: EmailService,
    private val eventPublisher: EventPublisher
) {

    suspend fun execute(command: CreateTenantCommand): Result<UUID> {
        return try {
            // Validate command
            validateTenantData(command)

            // Check slug availability
            val slugExists = tenantRepository.existsBySlug(command.slug).getOrThrow()
            if (slugExists) {
                return Result.failure(SlugAlreadyExistsException(command.slug))
            }

            // Determine plan limits
            val limits = determinePlanLimits(command.plan)

            // Calculate subscription dates
            val startDate = LocalDate.now()
            val endDate = calculateEndDate(startDate, command.billingCycle)
            val trialEndDate = if (command.startWithTrial) startDate.plusDays(14) else null

            // Create tenant
            val tenant = Tenant(
                id = UUID.randomUUID(),
                name = command.name,
                nameArabic = command.nameArabic,
                slug = command.slug,
                businessType = command.businessType,
                status = if (command.startWithTrial) TenantStatus.TRIAL else TenantStatus.PENDING,
                subscriptionPlan = command.plan,
                subscriptionStartDate = startDate,
                subscriptionEndDate = trialEndDate ?: endDate,
                billingCycle = command.billingCycle,
                maxBranches = limits.maxBranches,
                maxMembers = limits.maxMembers,
                maxStaff = limits.maxStaff,
                features = determinePlanFeatures(command.plan),
                contactInfo = command.contactInfo,
                address = command.address,
                vatNumber = command.vatNumber,
                commercialRegistration = command.commercialRegistration,
                logo = null,
                favicon = null,
                splashScreen = null,
                brandColors = null,
                customDomain = null,
                domainVerificationToken = null,
                domainVerificationStatus = null,
                domainVerifiedAt = null,
                sslCertificateId = null,
                emailFromName = null,
                emailFromAddress = null,
                smsFromName = null,
                socialLinks = null,
                iosAppId = null,
                androidAppId = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                createdBy = command.createdByAdminId,
                isDeleted = false
            )

            tenantRepository.save(tenant).getOrThrow()

            // Create tenant owner account
            val ownerPassword = generateSecurePassword()
            val tenantUser = TenantUser.createOwner(
                tenantId = tenant.id,
                name = command.ownerName,
                email = command.ownerEmail,
                phone = command.ownerPhone,
                passwordHash = hashPassword(ownerPassword)
            )

            tenantUserRepository.save(tenantUser).getOrThrow()

            // Create subscription record
            val subscription = TenantSubscription(
                id = UUID.randomUUID(),
                tenantId = tenant.id,
                plan = command.plan,
                status = if (trialEndDate != null) SubscriptionStatus.TRIAL else SubscriptionStatus.INCOMPLETE,
                amount = determinePlanPrice(command.plan, command.billingCycle),
                billingCycle = command.billingCycle,
                startDate = startDate,
                endDate = trialEndDate ?: endDate,
                autoRenew = true,
                paymentMethod = null,
                nextBillingDate = trialEndDate ?: startDate,
                trialEndsAt = trialEndDate,
                cancelledAt = null,
                cancellationReason = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            subscriptionRepository.save(subscription).getOrThrow()

            // Publish event
            eventPublisher.publish(
                TenantCreatedEvent(
                    tenantId = tenant.id,
                    tenantName = tenant.name,
                    ownerEmail = tenantUser.email,
                    occurredAt = Instant.now()
                )
            )

            // Send welcome email with credentials
            emailService.sendTenantWelcomeEmail(
                to = tenantUser.email,
                tenantName = tenant.name,
                username = tenantUser.email,
                temporaryPassword = ownerPassword,
                loginUrl = "https://${tenant.slug}.liyaqa.com/login",
                trialEndsAt = trialEndDate
            )

            Result.success(tenant.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validateTenantData(command: CreateTenantCommand) {
        // Additional validation beyond command validation
        require(command.name.length >= 3) { "Tenant name must be at least 3 characters" }
        require(command.slug.length >= 3) { "Tenant slug must be at least 3 characters" }
    }

    private fun determinePlanLimits(plan: TenantSubscriptionPlan): TenantLimits {
        return when (plan) {
            TenantSubscriptionPlan.STARTER -> TenantLimits(
                maxBranches = 1,
                maxMembers = 500,
                maxStaff = 10,
                maxStorageMB = 1024
            )
            TenantSubscriptionPlan.PROFESSIONAL -> TenantLimits(
                maxBranches = 3,
                maxMembers = 2000,
                maxStaff = 50,
                maxStorageMB = 5120
            )
            TenantSubscriptionPlan.ENTERPRISE -> TenantLimits(
                maxBranches = Int.MAX_VALUE,
                maxMembers = Int.MAX_VALUE,
                maxStaff = Int.MAX_VALUE,
                maxStorageMB = Long.MAX_VALUE
            )
        }
    }

    private fun determinePlanFeatures(plan: TenantSubscriptionPlan): Set<PlatformFeature> {
        return plan.includedFeatures
    }

    private fun determinePlanPrice(
        plan: TenantSubscriptionPlan,
        billingCycle: BillingCycle
    ): Money {
        val monthlyPrice = plan.monthlyPriceSAR.toDouble()

        val amount = when (billingCycle) {
            BillingCycle.MONTHLY -> monthlyPrice
            BillingCycle.QUARTERLY -> monthlyPrice * 3 * 0.95 // 5% discount
            BillingCycle.ANNUAL -> monthlyPrice * 12 * 0.85 // 15% discount
        }

        return Money.sar(amount)
    }

    private fun calculateEndDate(startDate: LocalDate, billingCycle: BillingCycle): LocalDate {
        return when (billingCycle) {
            BillingCycle.MONTHLY -> startDate.plusMonths(1)
            BillingCycle.QUARTERLY -> startDate.plusMonths(3)
            BillingCycle.ANNUAL -> startDate.plusYears(1)
        }
    }

    /**
     * Generate a secure random password for the tenant owner.
     * This password will be sent to the owner via email and should be changed on first login.
     */
    private fun generateSecurePassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        val random = SecureRandom()
        return (1..16)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }

    /**
     * Hash password using a secure hashing algorithm.
     * TODO: Replace with actual BCrypt or Argon2 implementation
     */
    private fun hashPassword(password: String): String {
        // Placeholder implementation - should use BCrypt or Argon2
        return "hashed_$password"
    }
}

/**
 * Data class representing tenant resource limits.
 */
data class TenantLimits(
    val maxBranches: Int,
    val maxMembers: Int,
    val maxStaff: Int,
    val maxStorageMB: Long
)
