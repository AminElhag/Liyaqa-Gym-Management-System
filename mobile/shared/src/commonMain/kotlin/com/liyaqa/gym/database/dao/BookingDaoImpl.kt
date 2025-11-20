package com.liyaqa.gym.database.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.liyaqa.gym.database.BookingEntity
import com.liyaqa.gym.database.LiyaqaDatabase
import com.liyaqa.gym.domain.Booking
import com.liyaqa.gym.domain.BookingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Implementation of BookingDao using SQLDelight
 */
class BookingDaoImpl(
    private val database: LiyaqaDatabase
) : BookingDao {

    private val queries = database.bookingEntityQueries

    override suspend fun getAll(): List<Booking> = withContext(Dispatchers.Default) {
        queries.selectAll().executeAsList().map { it.toDomain() }
    }

    override suspend fun getById(id: String): Booking? = withContext(Dispatchers.Default) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getByMemberId(memberId: String): List<Booking> =
        withContext(Dispatchers.Default) {
            queries.selectByMemberId(memberId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getByScheduleId(scheduleId: String): List<Booking> =
        withContext(Dispatchers.Default) {
            queries.selectByScheduleId(scheduleId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getByMemberAndSchedule(
        memberId: String,
        scheduleId: String
    ): Booking? = withContext(Dispatchers.Default) {
        queries.selectByMemberAndSchedule(memberId, scheduleId)
            .executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getUpcomingByMember(memberId: String): List<Booking> =
        withContext(Dispatchers.Default) {
            queries.selectUpcomingByMember(memberId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getByStatus(status: String): List<Booking> =
        withContext(Dispatchers.Default) {
            queries.selectByStatus(status).executeAsList().map { it.toDomain() }
        }

    override suspend fun getWaitlistedBySchedule(scheduleId: String): List<Booking> =
        withContext(Dispatchers.Default) {
            queries.selectWaitlistedBySchedule(scheduleId).executeAsList().map { it.toDomain() }
        }

    override fun observeAll(): Flow<List<Booking>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeById(id: String): Flow<Booking?> {
        return queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }
    }

    override fun observeByMemberId(memberId: String): Flow<List<Booking>> {
        return queries.selectByMemberId(memberId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun save(booking: Booking) = withContext(Dispatchers.Default) {
        database.transaction {
            booking.toEntity().let { entity ->
                queries.insert(
                    id = entity.id,
                    memberId = entity.memberId,
                    scheduleId = entity.scheduleId,
                    status = entity.status,
                    bookedAt = entity.bookedAt,
                    waitlistPosition = entity.waitlistPosition,
                    confirmedAt = entity.confirmedAt,
                    checkedInAt = entity.checkedInAt,
                    cancelledAt = entity.cancelledAt,
                    cancellationReason = entity.cancellationReason,
                    noShowMarkedAt = entity.noShowMarkedAt,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    cachedAt = entity.cachedAt
                )
            }
        }
    }

    override suspend fun saveAll(bookings: List<Booking>) = withContext(Dispatchers.Default) {
        database.transaction {
            bookings.forEach { booking ->
                save(booking)
            }
        }
    }

    override suspend fun updateStatus(id: String, status: String) =
        withContext(Dispatchers.Default) {
            queries.updateStatus(
                status = status,
                updatedAt = Clock.System.now().toString(),
                id = id
            )
        }

    override suspend fun cancel(
        id: String,
        cancellationReason: String
    ) = withContext(Dispatchers.Default) {
        queries.cancel(
            cancelledAt = Clock.System.now().toString(),
            cancellationReason = cancellationReason,
            updatedAt = Clock.System.now().toString(),
            id = id
        )
    }

    override suspend fun checkIn(id: String) = withContext(Dispatchers.Default) {
        queries.checkIn(
            checkedInAt = Clock.System.now().toString(),
            updatedAt = Clock.System.now().toString(),
            id = id
        )
    }

    override suspend fun deleteById(id: String) = withContext(Dispatchers.Default) {
        queries.deleteById(id)
    }

    override suspend fun deleteByMemberId(memberId: String) = withContext(Dispatchers.Default) {
        queries.deleteByMemberId(memberId)
    }

    override suspend fun deleteByScheduleId(scheduleId: String) = withContext(Dispatchers.Default) {
        queries.deleteByScheduleId(scheduleId)
    }

    override suspend fun clearOldBookings(endDateTime: String) =
        withContext(Dispatchers.Default) {
            queries.deleteOldBookings(endDateTime)
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
    private fun BookingEntity.toDomain(): Booking {
        return Booking(
            id = id,
            memberId = memberId,
            scheduleId = scheduleId,
            status = BookingStatus.valueOf(status),
            bookedAt = Instant.parse(bookedAt),
            waitlistPosition = waitlistPosition?.toInt(),
            confirmedAt = confirmedAt?.let { Instant.parse(it) },
            checkedInAt = checkedInAt?.let { Instant.parse(it) },
            cancelledAt = cancelledAt?.let { Instant.parse(it) },
            cancellationReason = cancellationReason,
            noShowMarkedAt = noShowMarkedAt?.let { Instant.parse(it) },
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt)
        )
    }

    /**
     * Convert domain model to database entity
     */
    private fun Booking.toEntity(): BookingEntity {
        return BookingEntity(
            id = id,
            memberId = memberId,
            scheduleId = scheduleId,
            status = status.name,
            bookedAt = bookedAt.toString(),
            waitlistPosition = waitlistPosition?.toLong(),
            confirmedAt = confirmedAt?.toString(),
            checkedInAt = checkedInAt?.toString(),
            cancelledAt = cancelledAt?.toString(),
            cancellationReason = cancellationReason,
            noShowMarkedAt = noShowMarkedAt?.toString(),
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
            cachedAt = Clock.System.now().toString()
        )
    }
}
