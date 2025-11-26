package com.liyaqa.gym.presentation.controller.platform

import com.liyaqa.gym.application.platform.commands.CreateTenantCommand
import com.liyaqa.gym.application.platform.commands.ReactivateTenantCommand
import com.liyaqa.gym.application.platform.commands.SuspendTenantCommand
import com.liyaqa.gym.application.platform.usecases.CreateTenantUseCase
import com.liyaqa.gym.application.platform.usecases.ReactivateTenantUseCase
import com.liyaqa.gym.application.platform.usecases.SuspendTenantUseCase
import com.liyaqa.gym.domain.entities.tenant.TenantStatus
import com.liyaqa.gym.domain.entities.tenant.TenantSubscriptionPlan
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.presentation.dto.platform.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Tenant Management Controller
 * Handles platform admin operations for managing tenants
 */
@RestController
@RequestMapping("/api/v1/platform/tenants")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
@Tag(name = "Platform Tenant Management", description = "Platform administrator tenant management endpoints")
class TenantManagementController(
    private val createTenantUseCase: CreateTenantUseCase,
    private val suspendTenantUseCase: SuspendTenantUseCase,
    private val reactivateTenantUseCase: ReactivateTenantUseCase,
    private val tenantRepository: TenantRepository
) {

    private val logger = LoggerFactory.getLogger(TenantManagementController::class.java)

    /**
     * Create a new tenant
     */
    @PostMapping
    @Operation(summary = "Create tenant", description = "Create a new tenant on the platform")
    fun createTenant(
        @RequestBody @Valid request: CreateTenantRequest,
        @AuthenticationPrincipal adminId: String?
    ): ResponseEntity<TenantResponse> {
        logger.info("Creating new tenant: ${request.name}")

        // TODO: Get actual admin ID from security context
        val createdByAdminId = UUID.fromString("00000000-0000-0000-0000-000000000001")

        val command = CreateTenantCommand(
            name = request.name,
            nameArabic = request.nameArabic,
            slug = request.slug,
            businessType = request.businessType,
            plan = request.plan,
            billingCycle = request.billingCycle,
            contactInfo = request.contactInfo.toDomain(),
            address = request.address.toDomain(),
            vatNumber = request.vatNumber,
            commercialRegistration = request.commercialRegistration,
            ownerName = request.ownerName,
            ownerEmail = request.ownerEmail,
            ownerPhone = request.ownerPhone,
            startWithTrial = request.startWithTrial,
            createdByAdminId = createdByAdminId
        )

        val result = runBlocking { createTenantUseCase.execute(command) }

        return result.fold(
            onSuccess = { tenantId ->
                // Fetch the created tenant
                val tenant = runBlocking {
                    tenantRepository.findById(tenantId).getOrNull()?.orElse(null)
                } ?: throw IllegalStateException("Tenant not found after creation")

                logger.info("Tenant created successfully: $tenantId")
                ResponseEntity.status(HttpStatus.CREATED).body(tenant.toResponse())
            },
            onFailure = { error ->
                logger.error("Failed to create tenant", error)
                throw error
            }
        )
    }

    /**
     * List all tenants with filtering and pagination
     */
    @GetMapping
    @Operation(summary = "List tenants", description = "List all tenants with optional filtering")
    fun listTenants(
        @Parameter(description = "Filter by status")
        @RequestParam(required = false) status: TenantStatus?,
        @Parameter(description = "Filter by plan")
        @RequestParam(required = false) plan: TenantSubscriptionPlan?,
        @Parameter(description = "Search term (name, slug, email)")
        @RequestParam(required = false) search: String?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<Page<TenantSummaryResponse>> {
        logger.info("Listing tenants - status: $status, plan: $plan, search: $search")

        // TODO: Implement proper pagination with repository
        // For now, return empty page
        val tenants = runBlocking {
            tenantRepository.findAll().getOrNull() ?: emptyList()
        }

        // Apply filters
        val filtered = tenants.filter { tenant ->
            (status == null || tenant.status == status) &&
                    (plan == null || tenant.subscriptionPlan == plan) &&
                    (search == null || tenant.name.contains(search, ignoreCase = true) ||
                            tenant.slug.contains(search, ignoreCase = true) ||
                            tenant.contactInfo.primaryContactEmail.contains(search, ignoreCase = true))
        }

        val summaries = filtered.map { it.toSummaryResponse() }
        val page = PageImpl(summaries, pageable, summaries.size.toLong())

        return ResponseEntity.ok(page)
    }

    /**
     * Get tenant details by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get tenant details", description = "Get detailed information about a specific tenant")
    fun getTenantDetails(@PathVariable id: UUID): ResponseEntity<TenantDetailResponse> {
        logger.info("Getting tenant details: $id")

        val tenant = runBlocking {
            tenantRepository.findById(id).getOrNull()?.orElse(null)
        } ?: throw IllegalArgumentException("Tenant not found: $id")

        // TODO: Get actual counts from repositories
        val response = tenant.toDetailResponse(
            currentBranches = 0,
            currentMembers = 0,
            currentStaff = 0
        )

        return ResponseEntity.ok(response)
    }

    /**
     * Update tenant information
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update tenant", description = "Update tenant information")
    fun updateTenant(
        @PathVariable id: UUID,
        @RequestBody @Valid request: UpdateTenantRequest
    ): ResponseEntity<TenantResponse> {
        logger.info("Updating tenant: $id")

        // TODO: Implement UpdateTenantUseCase when available
        val tenant = runBlocking {
            tenantRepository.findById(id).getOrNull()?.orElse(null)
        } ?: throw IllegalArgumentException("Tenant not found: $id")

        // For now, just return the existing tenant
        logger.warn("UpdateTenantUseCase not yet implemented - returning existing tenant")
        return ResponseEntity.ok(tenant.toResponse())
    }

    /**
     * Suspend a tenant
     */
    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend tenant", description = "Suspend a tenant account")
    fun suspendTenant(
        @PathVariable id: UUID,
        @RequestBody request: SuspendTenantRequest
    ): ResponseEntity<Unit> {
        logger.info("Suspending tenant: $id - reason: ${request.reason}")

        val command = SuspendTenantCommand(
            tenantId = id,
            reason = request.reason
        )

        runBlocking {
            suspendTenantUseCase.execute(command)
        }

        logger.info("Tenant suspended successfully: $id")
        return ResponseEntity.ok().build()
    }

    /**
     * Reactivate a suspended tenant
     */
    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate tenant", description = "Reactivate a suspended tenant")
    fun reactivateTenant(@PathVariable id: UUID): ResponseEntity<Unit> {
        logger.info("Reactivating tenant: $id")

        val command = ReactivateTenantCommand(tenantId = id)

        runBlocking {
            reactivateTenantUseCase.execute(command)
        }

        logger.info("Tenant reactivated successfully: $id")
        return ResponseEntity.ok().build()
    }

    /**
     * Delete a tenant (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete tenant", description = "Soft delete a tenant (Super admin only)")
    fun deleteTenant(@PathVariable id: UUID): ResponseEntity<Unit> {
        logger.info("Deleting tenant: $id")

        // TODO: Implement DeleteTenantUseCase when available
        val tenant = runBlocking {
            tenantRepository.findById(id).getOrNull()?.orElse(null)
        } ?: throw IllegalArgumentException("Tenant not found: $id")

        val deletedTenant = tenant.softDelete()
        runBlocking {
            tenantRepository.save(deletedTenant)
        }

        logger.info("Tenant deleted successfully: $id")
        return ResponseEntity.noContent().build()
    }
}
