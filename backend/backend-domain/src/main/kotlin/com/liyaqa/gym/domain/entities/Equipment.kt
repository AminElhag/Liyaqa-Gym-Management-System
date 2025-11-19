package com.liyaqa.gym.domain.entities

import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Equipment entity representing gym equipment.
 * Belongs to a Branch.
 */
data class Equipment(
    val id: UUID,
    val branchId: UUID,
    val name: String,
    val nameArabic: String?,
    val category: EquipmentCategory,
    val manufacturer: String?,
    val model: String?,
    val serialNumber: String?,
    val purchaseDate: LocalDate,
    val purchasePrice: Money?,
    val warrantyExpiryDate: LocalDate?,
    val status: EquipmentStatus,
    val location: String?,
    val qrCode: String?,
    val notes: String?,
    val lastMaintenanceDate: LocalDate?,
    val nextMaintenanceDate: LocalDate?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Equipment name cannot be blank" }
        warrantyExpiryDate?.let {
            require(!it.isBefore(purchaseDate)) {
                "Warranty expiry date cannot be before purchase date"
            }
        }
        nextMaintenanceDate?.let { next ->
            lastMaintenanceDate?.let { last ->
                require(!next.isBefore(last)) {
                    "Next maintenance date cannot be before last maintenance date"
                }
            }
        }
    }

    fun isOperational(): Boolean = status == EquipmentStatus.OPERATIONAL

    fun isUnderMaintenance(): Boolean = status == EquipmentStatus.UNDER_MAINTENANCE

    fun isOutOfService(): Boolean = status == EquipmentStatus.OUT_OF_SERVICE

    fun isWarrantyValid(): Boolean {
        return warrantyExpiryDate?.isAfter(LocalDate.now()) ?: false
    }

    fun needsMaintenance(): Boolean {
        return nextMaintenanceDate?.isBefore(LocalDate.now()) ?: false
    }

    fun markAsOperational(): Equipment {
        return copy(status = EquipmentStatus.OPERATIONAL, updatedAt = Instant.now())
    }

    fun markAsUnderMaintenance(): Equipment {
        return copy(status = EquipmentStatus.UNDER_MAINTENANCE, updatedAt = Instant.now())
    }

    fun markAsOutOfService(): Equipment {
        return copy(status = EquipmentStatus.OUT_OF_SERVICE, updatedAt = Instant.now())
    }

    fun recordMaintenance(maintenanceDate: LocalDate, nextMaintenanceDate: LocalDate?): Equipment {
        return copy(
            lastMaintenanceDate = maintenanceDate,
            nextMaintenanceDate = nextMaintenanceDate,
            status = EquipmentStatus.OPERATIONAL,
            updatedAt = Instant.now()
        )
    }

    fun updateLocation(newLocation: String): Equipment {
        return copy(location = newLocation, updatedAt = Instant.now())
    }

    companion object {
        fun create(
            branchId: UUID,
            name: String,
            nameArabic: String?,
            category: EquipmentCategory,
            purchaseDate: LocalDate,
            manufacturer: String? = null,
            model: String? = null,
            serialNumber: String? = null,
            purchasePrice: Money? = null,
            warrantyExpiryDate: LocalDate? = null
        ): Equipment {
            val now = Instant.now()
            return Equipment(
                id = UUID.randomUUID(),
                branchId = branchId,
                name = name,
                nameArabic = nameArabic,
                category = category,
                manufacturer = manufacturer,
                model = model,
                serialNumber = serialNumber,
                purchaseDate = purchaseDate,
                purchasePrice = purchasePrice,
                warrantyExpiryDate = warrantyExpiryDate,
                status = EquipmentStatus.OPERATIONAL,
                location = null,
                qrCode = null,
                notes = null,
                lastMaintenanceDate = null,
                nextMaintenanceDate = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Equipment category enumeration
 */
enum class EquipmentCategory {
    CARDIO,
    STRENGTH,
    FREE_WEIGHTS,
    FUNCTIONAL,
    FLEXIBILITY,
    CYCLING,
    ROWING,
    TREADMILL,
    ELLIPTICAL,
    ACCESSORIES,
    OTHER
}

/**
 * Equipment status enumeration
 */
enum class EquipmentStatus {
    OPERATIONAL,
    UNDER_MAINTENANCE,
    OUT_OF_SERVICE,
    RETIRED
}
