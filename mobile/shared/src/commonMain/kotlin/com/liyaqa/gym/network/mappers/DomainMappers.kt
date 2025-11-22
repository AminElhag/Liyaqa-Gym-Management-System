package com.liyaqa.gym.network.mappers

import com.liyaqa.gym.domain.*
import com.liyaqa.gym.network.models.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Extension functions to map network response models to domain models
 */

/**
 * Convert MemberResponse to Member domain model
 */
fun MemberResponse.toDomain(): Member {
    return Member(
        id = id,
        branchId = branchId,
        name = name,
        nameArabic = nameArabic,
        contactInfo = ContactInfo(email = email, phone = phone),
        nationalId = nationalId,
        gender = gender,
        dateOfBirth = dateOfBirth?.let { LocalDate.parse(it) },
        status = status,
        profilePhotoUrl = profilePhotoUrl,
        emergencyContactName = emergencyContactName,
        emergencyContactPhone = emergencyContactPhone,
        notes = notes,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )
}

/**
 * Convert SubscriptionResponse to Subscription domain model
 */
fun SubscriptionResponse.toDomain(): Subscription {
    return Subscription(
        id = id,
        memberId = memberId,
        planId = planId,
        planName = planName,
        startDate = LocalDate.parse(startDate),
        endDate = endDate?.let { LocalDate.parse(it) },
        status = status,
        autoRenew = autoRenew,
        remainingVisits = remainingVisits,
        pausedAt = pausedAt?.let { LocalDate.parse(it) },
        pausedUntil = pausedUntil?.let { LocalDate.parse(it) },
        cancelledAt = cancelledAt?.let { Instant.parse(it) },
        cancellationReason = cancellationReason,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )
}

/**
 * Convert BookingResponse to Booking domain model
 */
fun BookingResponse.toDomain(): Booking {
    return Booking(
        id = id,
        memberId = memberId,
        scheduleId = scheduleId,
        status = BookingStatus.valueOf(status),
        bookedAt = Instant.parse(bookedAt),
        waitlistPosition = waitlistPosition,
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
 * Convert ScheduleResponse to ClassSchedule domain model
 */
fun ScheduleResponse.toDomain(): ClassSchedule {
    return ClassSchedule(
        id = id,
        classId = classId,
        instructorId = instructorId,
        instructorName = instructorName,
        startDateTime = LocalDateTime.parse(startDateTime),
        endDateTime = LocalDateTime.parse(endDateTime),
        capacity = capacity,
        bookedCount = bookedCount,
        waitlistCount = waitlistCount,
        isCancelled = isCancelled,
        cancellationReason = cancellationReason,
        notes = notes,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )
}

/**
 * Convert PaymentResponse to Payment domain model
 */
fun PaymentResponse.toDomain(): Payment {
    return Payment(
        id = id,
        memberId = memberId,
        subscriptionId = subscriptionId,
        amount = amount,
        paymentMethod = paymentMethod,
        status = status,
        transactionId = transactionId,
        description = description,
        paidAt = paidAt?.let { Instant.parse(it) },
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )
}

/**
 * Convert InvoiceResponse to Invoice domain model
 */
fun InvoiceResponse.toDomain(): Invoice {
    return Invoice(
        id = id,
        memberId = memberId,
        subscriptionId = subscriptionId,
        amount = amount,
        paidAmount = paidAmount,
        status = status,
        dueDate = LocalDate.parse(dueDate),
        paidAt = paidAt?.let { Instant.parse(it) },
        items = items.map { it.toDomain() },
        notes = notes,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )
}

/**
 * Convert InvoiceItem network model to domain model
 */
fun com.liyaqa.gym.network.models.InvoiceItem.toDomain(): com.liyaqa.gym.domain.InvoiceItem {
    return com.liyaqa.gym.domain.InvoiceItem(
        description = description,
        quantity = quantity,
        unitPrice = unitPrice,
        amount = amount
    )
}
