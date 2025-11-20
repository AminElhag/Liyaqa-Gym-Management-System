package com.liyaqa.gym.domain.di

import com.liyaqa.gym.domain.formatters.CurrencyFormatter
import com.liyaqa.gym.domain.formatters.DateFormatter
import com.liyaqa.gym.domain.formatters.DurationFormatter
import com.liyaqa.gym.domain.rules.*
import com.liyaqa.gym.domain.usecases.*
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin DI module for domain layer
 * Includes use cases, business rules, validators, and formatters
 */
val domainModule = module {
    // Use Cases
    factoryOf(::GetMemberProfileUseCase)
    factoryOf(::BookClassUseCase)
    factoryOf(::CancelBookingUseCase)
    factoryOf(::CheckInUseCase)
    factoryOf(::GetMyBookingsUseCase)
    factoryOf(::GetSchedulesUseCase)
    factoryOf(::RenewSubscriptionUseCase)

    // Business Rules
    factory { CanBookClassRule() }
    factory { CanCancelBookingRule() }
    factory { CanRenewSubscriptionRule() }
    factory { CanCheckInRule() }

    // Formatters (platform-specific implementations)
    factory { DateFormatter() }
    factory { CurrencyFormatter() }
    factory { DurationFormatter() }
}
