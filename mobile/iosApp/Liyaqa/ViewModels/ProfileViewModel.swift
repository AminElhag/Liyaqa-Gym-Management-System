import Foundation
import Combine
import shared
import SwiftUI

@MainActor
class ProfileViewModel: ObservableObject {
    @Published var user: User?
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var showingImagePicker = false
    @Published var profileImage: UIImage?
    @Published var subscriptions: [Subscription] = []
    @Published var paymentHistory: [Payment] = []

    // Settings
    @Published var notificationsEnabled = true
    @Published var biometricEnabled = false
    @Published var darkModeEnabled = false

    private let memberRepository: MemberRepository
    private let subscriptionRepository: SubscriptionRepository
    private let paymentRepository: PaymentRepository
    private var cancellables = Set<AnyCancellable>()

    init(
        memberRepository: MemberRepository? = nil,
        subscriptionRepository: SubscriptionRepository? = nil,
        paymentRepository: PaymentRepository? = nil
    ) {
        self.memberRepository = memberRepository ?? KoinHelper.shared.getMemberRepository()
        self.subscriptionRepository = subscriptionRepository ?? KoinHelper.shared.getSubscriptionRepository()
        self.paymentRepository = paymentRepository ?? KoinHelper.shared.getPaymentRepository()

        loadSettings()
    }

    func loadProfile() async {
        isLoading = true
        errorMessage = nil

        async let userTask = loadUser()
        async let subscriptionsTask = loadSubscriptions()
        async let paymentsTask = loadPayments()

        await userTask
        await subscriptionsTask
        await paymentsTask

        isLoading = false
    }

    private func loadUser() async {
        do {
            // Load user from repository
            // user = try await memberRepository.getCurrentUser()
        } catch {
            print("Error loading user: \(error)")
        }
    }

    private func loadSubscriptions() async {
        do {
            // subscriptions = try await subscriptionRepository.getSubscriptions()
        } catch {
            print("Error loading subscriptions: \(error)")
        }
    }

    private func loadPayments() async {
        do {
            // paymentHistory = try await paymentRepository.getPaymentHistory()
        } catch {
            print("Error loading payments: \(error)")
        }
    }

    func updateProfile(fullName: String, phoneNumber: String) async {
        isLoading = true
        errorMessage = nil

        do {
            // try await memberRepository.updateProfile(fullName: fullName, phoneNumber: phoneNumber)
            await loadProfile()
            errorMessage = "Profile updated successfully"
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func uploadProfileImage(_ image: UIImage) async {
        isLoading = true
        errorMessage = nil

        do {
            guard let imageData = image.jpegData(compressionQuality: 0.7) else {
                errorMessage = "Failed to process image"
                isLoading = false
                return
            }

            // try await memberRepository.uploadProfileImage(imageData)
            profileImage = image
            errorMessage = "Profile image updated successfully"
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func toggleNotifications(_ enabled: Bool) {
        notificationsEnabled = enabled
        UserDefaults.standard.set(enabled, forKey: "notificationsEnabled")

        if enabled {
            NotificationService.shared.requestAuthorization()
        }
    }

    func toggleBiometric(_ enabled: Bool) {
        biometricEnabled = enabled
        UserDefaults.standard.set(enabled, forKey: "biometricEnabled")
    }

    func toggleDarkMode(_ enabled: Bool) {
        darkModeEnabled = enabled
        UserDefaults.standard.set(enabled, forKey: "darkModeEnabled")
    }

    private func loadSettings() {
        notificationsEnabled = UserDefaults.standard.bool(forKey: "notificationsEnabled")
        biometricEnabled = UserDefaults.standard.bool(forKey: "biometricEnabled")
        darkModeEnabled = UserDefaults.standard.bool(forKey: "darkModeEnabled")
    }

    func logout() {
        KeychainHelper.shared.deleteToken()
        KeychainHelper.shared.deleteCredentials()
    }
}

// MARK: - Supporting Models
struct Subscription: Identifiable {
    let id: String
    let planName: String
    let status: String
    let startDate: Date
    let endDate: Date
    let amount: Double
    let billingCycle: String
}

struct Payment: Identifiable {
    let id: String
    let amount: Double
    let date: Date
    let method: String
    let status: String
    let description: String
}
