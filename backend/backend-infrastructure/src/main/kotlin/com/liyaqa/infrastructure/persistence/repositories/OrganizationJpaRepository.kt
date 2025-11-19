package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.infrastructure.persistence.entities.OrganizationJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository for OrganizationJpaEntity.
 */
@Repository
interface OrganizationJpaRepository : JpaRepository<OrganizationJpaEntity, UUID> {

    @Query("SELECT o FROM OrganizationJpaEntity o WHERE o.name = :name AND o.isDeleted = false")
    fun findByName(@Param("name") name: String): Optional<OrganizationJpaEntity>

    @Query("SELECT o FROM OrganizationJpaEntity o WHERE o.isDeleted = false")
    fun findAllActive(): List<OrganizationJpaEntity>
}
