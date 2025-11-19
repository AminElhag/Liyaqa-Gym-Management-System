package com.liyaqa.gym.domain.entities

import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.util.UUID

/**
 * MembershipPlan entity defining membership offerings.
 * Belongs to a Branch.
 */
data class MembershipPlan(
    val id: UUID,
    val branchId: UUID,
    val name: String,
    val description: String?,
    val type: PlanType,
    val price: Money,
    val durationDays: Int?,
    val visitCount: Int?,
    val allowedTimeSlots: List<String>?, // Time ranges like "06:00-12:00"
    val features: List<String>,
    val isActive: Boolean,
    val maxActiveSubscriptions: Int?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Plan name cannot be blank" }
        require(price.isPositive()) { "Plan price must be positive" }

        when (type) {
            PlanType.DURATION -> {
                require(durationDays != null && durationDays > 0) {
                    "Duration-based plans must have positive duration in days"
                }
            }
            PlanType.VISIT_BASED -> {
                require(visitCount != null && visitCount > 0) {
                    "Visit-based plans must have positive visit count"
                }
            }
            PlanType.TIME_RESTRICTED -> {
                require(!allowedTimeSlots.isNullOrEmpty()) {
                    "Time-restricted plans must have allowed time slots"
                }
            }
        }
    }

    fun activate(): MembershipPlan {
        return copy(isActive = true, updatedAt = Instant.now())
    }

    fun deactivate(): MembershipPlan {
        return copy(isActive = false, updatedAt = Instant.now())
    }

    fun updatePrice(newPrice: Money): MembershipPlan {
        require(newPrice.isPositive()) { "Plan price must be positive" }
        return copy(price = newPrice, updatedAt = Instant.now())
    }

    fun isDurationBased(): Boolean = type == PlanType.DURATION

    fun isVisitBased(): Boolean = type == PlanType.VISIT_BASED

    fun isTimeRestricted(): Boolean = type == PlanType.TIME_RESTRICTED

    companion object {
        fun createDurationBased(
            branchId: UUID,
            name: String,
            price: Money,
            durationDays: Int,
            features: List<String> = emptyList()
        ): MembershipPlan {
            val now = Instant.now()
            return MembershipPlan(
                id = UUID.randomUUID(),
                branchId = branchId,
                name = name,
                description = null,
                type = PlanType.DURATION,
                price = price,
                durationDays = durationDays,
                visitCount = null,
                allowedTimeSlots = null,
                features = features,
                isActive = true,
                maxActiveSubscriptions = null,
                createdAt = now,
                updatedAt = now
            )
        }

        fun createVisitBased(
            branchId: UUID,
            name: String,
            price: Money,
            visitCount: Int,
            features: List<String> = emptyList()
        ): MembershipPlan {
            val now = Instant.now()
            return MembershipPlan(
                id = UUID.randomUUID(),
                branchId = branchId,
                name = name,
                description = null,
                type = PlanType.VISIT_BASED,
                price = price,
                durationDays = null,
                visitCount = visitCount,
                allowedTimeSlots = null,
                features = features,
                isActive = true,
                maxActiveSubscriptions = null,
                createdAt = now,
                updatedAt = now
            )
        }

        fun createTimeRestricted(
            branchId: UUID,
            name: String,
            price: Money,
            durationDays: Int,
            allowedTimeSlots: List<String>,
            features: List<String> = emptyList()
        ): MembershipPlan {
            val now = Instant.now()
            return MembershipPlan(
                id = UUID.randomUUID(),
                branchId = branchId,
                name = name,
                description = null,
                type = PlanType.TIME_RESTRICTED,
                price = price,
                durationDays = durationDays,
                visitCount = null,
                allowedTimeSlots = allowedTimeSlots,
                features = features,
                isActive = true,
                maxActiveSubscriptions = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Membership plan type enumeration
 */
enum class PlanType {
    DURATION,
    VISIT_BASED,
    TIME_RESTRICTED
}
