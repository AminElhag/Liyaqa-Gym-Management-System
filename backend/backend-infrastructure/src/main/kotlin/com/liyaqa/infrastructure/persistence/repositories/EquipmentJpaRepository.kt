package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.EquipmentCategory
import com.liyaqa.gym.domain.entities.EquipmentStatus
import com.liyaqa.infrastructure.persistence.entities.EquipmentJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository for EquipmentJpaEntity.
 */
@Repository
interface EquipmentJpaRepository : JpaRepository<EquipmentJpaEntity, UUID> {

    @Query("SELECT e FROM EquipmentJpaEntity e WHERE e.branchId = :branchId AND e.isDeleted = false")
    fun findByBranchId(@Param("branchId") branchId: UUID): List<EquipmentJpaEntity>

    @Query("SELECT e FROM EquipmentJpaEntity e WHERE e.category = :category AND e.isDeleted = false")
    fun findByCategory(@Param("category") category: EquipmentCategory): List<EquipmentJpaEntity>

    @Query("SELECT e FROM EquipmentJpaEntity e WHERE e.status = :status AND e.isDeleted = false")
    fun findByStatus(@Param("status") status: EquipmentStatus): List<EquipmentJpaEntity>

    @Query(
        """
        SELECT e FROM EquipmentJpaEntity e
        WHERE e.branchId = :branchId
        AND e.status = 'OPERATIONAL'
        AND e.isDeleted = false
        """
    )
    fun findOperationalByBranchId(@Param("branchId") branchId: UUID): List<EquipmentJpaEntity>

    @Query("SELECT e FROM EquipmentJpaEntity e WHERE e.serialNumber = :serialNumber AND e.isDeleted = false")
    fun findBySerialNumber(@Param("serialNumber") serialNumber: String): Optional<EquipmentJpaEntity>

    @Query("SELECT e FROM EquipmentJpaEntity e WHERE e.qrCode = :qrCode AND e.isDeleted = false")
    fun findByQrCode(@Param("qrCode") qrCode: String): Optional<EquipmentJpaEntity>

    @Query(
        """
        SELECT e FROM EquipmentJpaEntity e
        WHERE e.nextMaintenanceDate IS NOT NULL
        AND e.nextMaintenanceDate <= :date
        AND e.status = 'OPERATIONAL'
        AND e.isDeleted = false
        ORDER BY e.nextMaintenanceDate ASC
        """
    )
    fun findNeedingMaintenance(@Param("date") date: LocalDate): List<EquipmentJpaEntity>

    @Query("SELECT e FROM EquipmentJpaEntity e WHERE e.name LIKE %:name% AND e.isDeleted = false")
    fun findByNameContaining(@Param("name") name: String): List<EquipmentJpaEntity>
}
