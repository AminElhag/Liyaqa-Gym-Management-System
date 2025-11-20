package com.liyaqa.gym.database.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.liyaqa.gym.database.LiyaqaDatabase
import com.liyaqa.gym.database.MemberEntity
import com.liyaqa.gym.domain.ContactInfo
import com.liyaqa.gym.domain.Gender
import com.liyaqa.gym.domain.Member
import com.liyaqa.gym.domain.MemberStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Implementation of MemberDao using SQLDelight
 */
class MemberDaoImpl(
    private val database: LiyaqaDatabase
) : MemberDao {

    private val queries = database.memberEntityQueries

    override suspend fun getAll(): List<Member> = withContext(Dispatchers.Default) {
        queries.selectAll().executeAsList().map { it.toDomain() }
    }

    override suspend fun getById(id: String): Member? = withContext(Dispatchers.Default) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getByBranchId(branchId: String): List<Member> =
        withContext(Dispatchers.Default) {
            queries.selectByBranchId(branchId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getByStatus(status: String): List<Member> =
        withContext(Dispatchers.Default) {
            queries.selectByStatus(status).executeAsList().map { it.toDomain() }
        }

    override suspend fun search(query: String): List<Member> =
        withContext(Dispatchers.Default) {
            queries.search(query, query, query, query).executeAsList().map { it.toDomain() }
        }

    override fun observeAll(): Flow<List<Member>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeById(id: String): Flow<Member?> {
        return queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }
    }

    override suspend fun save(member: Member) = withContext(Dispatchers.Default) {
        database.transaction {
            member.toEntity().let { entity ->
                queries.insert(
                    id = entity.id,
                    branchId = entity.branchId,
                    name = entity.name,
                    nameArabic = entity.nameArabic,
                    email = entity.email,
                    phone = entity.phone,
                    nationalId = entity.nationalId,
                    gender = entity.gender,
                    dateOfBirth = entity.dateOfBirth,
                    status = entity.status,
                    profilePhotoUrl = entity.profilePhotoUrl,
                    emergencyContactName = entity.emergencyContactName,
                    emergencyContactPhone = entity.emergencyContactPhone,
                    notes = entity.notes,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    cachedAt = entity.cachedAt
                )
            }
        }
    }

    override suspend fun saveAll(members: List<Member>) = withContext(Dispatchers.Default) {
        database.transaction {
            members.forEach { member ->
                save(member)
            }
        }
    }

    override suspend fun deleteById(id: String) = withContext(Dispatchers.Default) {
        queries.deleteById(id)
    }

    override suspend fun clearAll() = withContext(Dispatchers.Default) {
        queries.deleteAll()
    }

    override suspend fun count(): Long = withContext(Dispatchers.Default) {
        queries.count().executeAsOne()
    }

    /**
     * Convert database entity to domain model
     */
    private fun MemberEntity.toDomain(): Member {
        return Member(
            id = id,
            branchId = branchId,
            name = name,
            nameArabic = nameArabic,
            contactInfo = ContactInfo(email = email, phone = phone),
            nationalId = nationalId,
            gender = Gender.valueOf(gender),
            dateOfBirth = dateOfBirth?.let { LocalDate.parse(it) },
            status = MemberStatus.valueOf(status),
            profilePhotoUrl = profilePhotoUrl,
            emergencyContactName = emergencyContactName,
            emergencyContactPhone = emergencyContactPhone,
            notes = notes,
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt)
        )
    }

    /**
     * Convert domain model to database entity
     */
    private fun Member.toEntity(): MemberEntity {
        return MemberEntity(
            id = id,
            branchId = branchId,
            name = name,
            nameArabic = nameArabic,
            email = contactInfo.email,
            phone = contactInfo.phone,
            nationalId = nationalId,
            gender = gender.name,
            dateOfBirth = dateOfBirth?.toString(),
            status = status.name,
            profilePhotoUrl = profilePhotoUrl,
            emergencyContactName = emergencyContactName,
            emergencyContactPhone = emergencyContactPhone,
            notes = notes,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
            cachedAt = Clock.System.now().toString()
        )
    }
}
