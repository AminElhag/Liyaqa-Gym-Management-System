package com.liyaqa.gym.application.classmanagement.dto

import com.liyaqa.gym.domain.entities.Booking
import com.liyaqa.gym.domain.entities.ClassSchedule
import org.springframework.stereotype.Component

/**
 * Mapper for converting between Booking domain entities and DTOs.
 */
@Component
class BookingMapper {

    /**
     * Converts a Booking domain entity to a BookingDTO.
     */
    fun toDTO(booking: Booking): BookingDTO {
        return BookingDTO(
            id = booking.id,
            memberId = booking.memberId,
            scheduleId = booking.scheduleId,
            status = booking.status,
            bookedAt = booking.bookedAt,
            waitlistPosition = booking.waitlistPosition,
            confirmedAt = booking.confirmedAt,
            checkedInAt = booking.checkedInAt,
            cancelledAt = booking.cancelledAt,
            cancellationReason = booking.cancellationReason,
            noShowMarkedAt = booking.noShowMarkedAt,
            createdAt = booking.createdAt,
            updatedAt = booking.updatedAt
        )
    }

    /**
     * Converts a list of Booking entities to BookingDTOs.
     */
    fun toDTOList(bookings: List<Booking>): List<BookingDTO> {
        return bookings.map { toDTO(it) }
    }
}
