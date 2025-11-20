import Foundation
import Combine
import shared

@MainActor
class HomeViewModel: ObservableObject {
    @Published var upcomingClasses: [GymClass] = []
    @Published var recentActivities: [Activity] = []
    @Published var membershipInfo: MembershipInfo?
    @Published var stats: MemberStats?
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let scheduleRepository: ScheduleRepository
    private let memberRepository: MemberRepository
    private var cancellables = Set<AnyCancellable>()

    init(
        scheduleRepository: ScheduleRepository? = nil,
        memberRepository: MemberRepository? = nil
    ) {
        // Get repositories from Koin
        self.scheduleRepository = scheduleRepository ?? KoinHelper.shared.getScheduleRepository()
        self.memberRepository = memberRepository ?? KoinHelper.shared.getMemberRepository()
    }

    func loadData() async {
        isLoading = true
        errorMessage = nil

        async let classesTask = loadUpcomingClasses()
        async let activitiesTask = loadRecentActivities()
        async let membershipTask = loadMembershipInfo()
        async let statsTask = loadStats()

        await classesTask
        await activitiesTask
        await membershipTask
        await statsTask

        isLoading = false
    }

    private func loadUpcomingClasses() async {
        do {
            // Fetch upcoming classes from repository
            // This would use the shared Kotlin repository
            // upcomingClasses = try await scheduleRepository.getUpcomingClasses()

            // Mock data for now
            upcomingClasses = []
        } catch {
            print("Error loading upcoming classes: \(error)")
        }
    }

    private func loadRecentActivities() async {
        do {
            // Fetch recent activities
            recentActivities = []
        } catch {
            print("Error loading activities: \(error)")
        }
    }

    private func loadMembershipInfo() async {
        do {
            // Fetch membership info
            // membershipInfo = try await memberRepository.getMembershipInfo()
        } catch {
            print("Error loading membership: \(error)")
        }
    }

    private func loadStats() async {
        do {
            // Fetch member stats
            // stats = try await memberRepository.getStats()
        } catch {
            print("Error loading stats: \(error)")
        }
    }

    func refresh() async {
        await loadData()
    }
}

// MARK: - Supporting Models
struct Activity: Identifiable {
    let id: String
    let type: ActivityType
    let title: String
    let subtitle: String
    let date: Date
    let icon: String

    enum ActivityType {
        case classBooked
        case classAttended
        case paymentMade
        case membershipRenewed
    }
}

struct MembershipInfo {
    let status: MembershipStatus
    let expiryDate: Date
    let daysRemaining: Int
    let plan: String

    enum MembershipStatus {
        case active
        case expiringSoon
        case expired
    }
}

struct MemberStats {
    let classesAttended: Int
    let classesBooked: Int
    let totalWorkouts: Int
    let currentStreak: Int
}

struct GymClass: Identifiable {
    let id: String
    let name: String
    let instructor: String
    let date: Date
    let duration: Int
    let capacity: Int
    let enrolled: Int
    let imageUrl: String?
}
