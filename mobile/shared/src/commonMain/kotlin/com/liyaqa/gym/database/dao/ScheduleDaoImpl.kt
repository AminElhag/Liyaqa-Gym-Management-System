package com.liyaqa.gym.database.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.liyaqa.gym.database.ClassScheduleEntity
import com.liyaqa.gym.database.LiyaqaDatabase
import com.liyaqa.gym.domain.ClassSchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

/**
 * Implementation of ScheduleDao using SQLDelight
 */
class ScheduleDaoImpl(
    private val database: LiyaqaDatabase
) : ScheduleDao {

    private val queries = database.classScheduleEntityQueries

    override suspend fun getAll(): List<ClassSchedule> = withContext(Dispatchers.Default) {
        queries.selectAll().executeAsList().map { it.toDomain() }
    }

    override suspend fun getById(id: String): ClassSchedule? = withContext(Dispatchers.Default) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getByClassId(classId: String): List<ClassSchedule> =
        withContext(Dispatchers.Default) {
            queries.selectByClassId(classId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getByDateRange(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): List<ClassSchedule> = withContext(Dispatchers.Default) {
        queries.selectByDateRange(
            startDateTime.toString(),
            endDateTime.toString()
        ).executeAsList().map { it.toDomain() }
    }

    override suspend fun getUpcoming(limit: Long): List<ClassSchedule> =
        withContext(Dispatchers.Default) {
            queries.selectUpcoming(limit).executeAsList().map { it.toDomain() }
        }

    override suspend fun getByInstructor(instructorId: String): List<ClassSchedule> =
        withContext(Dispatchers.Default) {
            queries.selectByInstructor(instructorId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getAvailable(limit: Long): List<ClassSchedule> =
        withContext(Dispatchers.Default) {
            queries.selectAvailable(limit).executeAsList().map { it.toDomain() }
        }

    override fun observeAll(): Flow<List<ClassSchedule>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeById(id: String): Flow<ClassSchedule?> {
        return queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }
    }

    override fun observeByDateRange(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Flow<List<ClassSchedule>> {
        return queries.selectByDateRange(
            startDateTime.toString(),
            endDateTime.toString()
        )
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun save(schedule: ClassSchedule) = withContext(Dispatchers.Default) {
        database.transaction {
            schedule.toEntity().let { entity ->
                queries.insert(
                    id = entity.id,
                    classId = entity.classId,
                    className = entity.className,
                    classNameArabic = entity.classNameArabic,
                    classType = entity.classType,
                    classLevel = entity.classLevel,
                    instructorId = entity.instructorId,
                    instructorName = entity.instructorName,
                    startDateTime = entity.startDateTime,
                    endDateTime = entity.endDateTime,
                    capacity = entity.capacity,
                    bookedCount = entity.bookedCount,
                    waitlistCount = entity.waitlistCount,
                    isCancelled = entity.isCancelled,
                    cancellationReason = entity.cancellationReason,
                    notes = entity.notes,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    cachedAt = entity.cachedAt
                )
            }
        }
    }

    override suspend fun saveAll(schedules: List<ClassSchedule>) = withContext(Dispatchers.Default) {
        database.transaction {
            schedules.forEach { schedule ->
                schedule.toEntity().let { entity ->
                    queries.insert(
                        id = entity.id,
                        classId = entity.classId,
                        className = entity.className,
                        classNameArabic = entity.classNameArabic,
                        classType = entity.classType,
                        classLevel = entity.classLevel,
                        instructorId = entity.instructorId,
                        instructorName = entity.instructorName,
                        startDateTime = entity.startDateTime,
                        endDateTime = entity.endDateTime,
                        capacity = entity.capacity,
                        bookedCount = entity.bookedCount,
                        waitlistCount = entity.waitlistCount,
                        isCancelled = entity.isCancelled,
                        cancellationReason = entity.cancellationReason,
                        notes = entity.notes,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt,
                        cachedAt = entity.cachedAt
                    )
                }
            }
        }
    }

    override suspend fun updateBookedCount(
        id: String,
        bookedCount: Int,
        waitlistCount: Int
    ) = withContext(Dispatchers.Default) {
        queries.updateBookedCount(
            bookedCount = bookedCount.toLong(),
            waitlistCount = waitlistCount.toLong(),
            updatedAt = Clock.System.now().toString(),
            id = id
        )
    }

    override suspend fun cancelSchedule(
        id: String,
        cancellationReason: String
    ) = withContext(Dispatchers.Default) {
        queries.cancelSchedule(
            cancellationReason = cancellationReason,
            updatedAt = Clock.System.now().toString(),
            id = id
        )
    }

    override suspend fun deleteById(id: String) = withContext(Dispatchers.Default) {
        queries.deleteById(id)
    }

    override suspend fun clearOldSchedules(endDateTime: LocalDateTime) =
        withContext(Dispatchers.Default) {
            queries.deleteOldSchedules(endDateTime.toString())
        }

    override suspend fun clearAll() = withContext(Dispatchers.Default) {
        queries.deleteAll()
    }

    override suspend fun count(): Long = withContext(Dispatchers.Default) {
        queries.count().executeAsOne()
    }

    /**
     * Convert database entity to domain model
     * Note: ClassSchedule domain model doesn't include all class details from the entity
     * We extract only the fields defined in ClassSchedule
     */
    private fun ClassScheduleEntity.toDomain(): ClassSchedule {
        return ClassSchedule(
            id = id,
            classId = classId,
            instructorId = instructorId,
            instructorName = instructorName,
            startDateTime = LocalDateTime.parse(startDateTime),
            endDateTime = LocalDateTime.parse(endDateTime),
            capacity = capacity.toInt(),
            bookedCount = bookedCount.toInt(),
            waitlistCount = waitlistCount.toInt(),
            isCancelled = isCancelled != 0L,
            cancellationReason = cancellationReason,
            notes = notes,
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt)
        )
    }

    /**
     * Convert domain model to database entity
     * Note: We need to provide placeholder values for class details
     * These should be populated from the GymClass when caching from API
     */
    private fun ClassSchedule.toEntity(): ClassScheduleEntity {
        return ClassScheduleEntity(
            id = id,
            classId = classId,
            className = "", // Should be populated from GymClass
            classNameArabic = null,
            classType = "", // Should be populated from GymClass
            classLevel = "", // Should be populated from GymClass
            instructorId = instructorId,
            instructorName = instructorName,
            startDateTime = startDateTime.toString(),
            endDateTime = endDateTime.toString(),
            capacity = capacity.toLong(),
            bookedCount = bookedCount.toLong(),
            waitlistCount = waitlistCount.toLong(),
            isCancelled = if (isCancelled) 1L else 0L,
            cancellationReason = cancellationReason,
            notes = notes,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
            cachedAt = Clock.System.now().toString()
        )
    }
}
