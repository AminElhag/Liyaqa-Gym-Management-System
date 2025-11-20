import Foundation
import shared

class KoinHelper {
    static let shared = KoinHelper()

    private var koin: Koin_coreKoin?

    private init() {}

    func initKoin() {
        koin = KoinHelperKt.doInitKoin()
    }

    // MARK: - Repository Getters
    func getMemberRepository() -> MemberRepository {
        guard let koin = koin else {
            fatalError("Koin not initialized. Call initKoin() first.")
        }
        return koin.get(objCClass: MemberRepository.self) as! MemberRepository
    }

    func getScheduleRepository() -> ScheduleRepository {
        guard let koin = koin else {
            fatalError("Koin not initialized. Call initKoin() first.")
        }
        return koin.get(objCClass: ScheduleRepository.self) as! ScheduleRepository
    }

    func getBookingRepository() -> BookingRepository {
        guard let koin = koin else {
            fatalError("Koin not initialized. Call initKoin() first.")
        }
        return koin.get(objCClass: BookingRepository.self) as! BookingRepository
    }

    func getSubscriptionRepository() -> SubscriptionRepository {
        guard let koin = koin else {
            fatalError("Koin not initialized. Call initKoin() first.")
        }
        return koin.get(objCClass: SubscriptionRepository.self) as! SubscriptionRepository
    }

    func getPaymentRepository() -> PaymentRepository {
        guard let koin = koin else {
            fatalError("Koin not initialized. Call initKoin() first.")
        }
        return koin.get(objCClass: PaymentRepository.self) as! PaymentRepository
    }

    // MARK: - Use Case Getters

    func getLoginUseCase() -> LoginUseCase {
        guard let koin = koin else {
            fatalError("Koin not initialized. Call initKoin() first.")
        }
        return koin.get(objCClass: LoginUseCase.self) as! LoginUseCase
    }

    func getRegisterUseCase() -> RegisterUseCase {
        guard let koin = koin else {
            fatalError("Koin not initialized. Call initKoin() first.")
        }
        return koin.get(objCClass: RegisterUseCase.self) as! RegisterUseCase
    }

    func getMemberProfileUseCase() -> GetMemberProfileUseCase {
        guard let koin = koin else {
            fatalError("Koin not initialized. Call initKoin() first.")
        }
        return koin.get(objCClass: GetMemberProfileUseCase.self) as! GetMemberProfileUseCase
    }

    func getMyBookingsUseCase() -> GetMyBookingsUseCase {
        guard let koin = koin else {
            fatalError("Koin not initialized. Call initKoin() first.")
        }
        return koin.get(objCClass: GetMyBookingsUseCase.self) as! GetMyBookingsUseCase
    }

    func getSchedulesUseCase() -> GetSchedulesUseCase {
        guard let koin = koin else {
            fatalError("Koin not initialized. Call initKoin() first.")
        }
        return koin.get(objCClass: GetSchedulesUseCase.self) as! GetSchedulesUseCase
    }
}

// MARK: - Koin Helper Extension
extension KoinHelperKt {
    static func doInitKoin() -> Koin_coreKoin {
        return KoinIOSKt.doInitKoinIos().koin
    }
}
