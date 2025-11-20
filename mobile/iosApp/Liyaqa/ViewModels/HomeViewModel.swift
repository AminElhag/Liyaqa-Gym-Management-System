import Foundation
import Combine
import shared

@MainActor
class HomeViewModel: ObservableObject {
    // MARK: - Published Properties
    @Published var memberName = ""
    @Published var upcomingBookings: [BookingWithDetails] = []
    @Published var activityStats: ActivityStats?
    @Published var featuredClasses: [FeaturedClass] = []
    @Published var unreadCount = 0
    @Published var isLoading = false
    @Published var errorMessage: String?

    // MARK: - Use Cases
    private let getMemberProfileUseCase: GetMemberProfileUseCase
    private let getMyBookingsUseCase: GetMyBookingsUseCase
    private let getSchedulesUseCase: GetSchedulesUseCase

    // MARK: - Current Member ID
    private var currentMemberId: String?

    init(
        getMemberProfileUseCase: GetMemberProfileUseCase? = nil,
        getMyBookingsUseCase: GetMyBookingsUseCase? = nil,
        getSchedulesUseCase: GetSchedulesUseCase? = nil
    ) {
        // Get use cases from Koin
        self.getMemberProfileUseCase = getMemberProfileUseCase ?? KoinHelper.shared.getMemberProfileUseCase()
        self.getMyBookingsUseCase = getMyBookingsUseCase ?? KoinHelper.shared.getMyBookingsUseCase()
        self.getSchedulesUseCase = getSchedulesUseCase ?? KoinHelper.shared.getSchedulesUseCase()
    }

    // MARK: - Public Methods
    func loadData() {
        guard !isLoading else { return }

        isLoading = true
        errorMessage = nil

        Task {
            // Load all data concurrently
            async let profile = loadProfile()
            async let bookings = loadBookings()
            async let stats = loadStats()
            async let classes = loadFeaturedClasses()

            // Wait for all tasks to complete
            let _ = await (profile, bookings, stats, classes)

            isLoading = false
        }
    }

    func refresh() async {
        errorMessage = nil

        async let profile = loadProfile()
        async let bookings = loadBookings()
        async let stats = loadStats()
        async let classes = loadFeaturedClasses()

        let _ = await (profile, bookings, stats, classes)
    }

    // MARK: - Private Methods
    private func loadProfile() async {
        do {
            // TODO: Get current member ID from authentication
            // For now, use a mock member ID or get from stored user data
            let memberId = currentMemberId ?? getMockMemberId()

            let result = try await getMemberProfileUseCase.invoke(
                memberId: memberId,
                forceRefresh: false
            )

            if let member = try? result.getOrThrow() {
                memberName = member.name
                currentMemberId = member.id
            } else {
                // Use default name if profile loading fails
                memberName = "Member"
            }
        } catch {
            print("Error loading member profile: \(error)")
            memberName = "Member"
            errorMessage = "Failed to load profile"
        }
    }

    private func loadBookings() async {
        do {
            let memberId = currentMemberId ?? getMockMemberId()

            let result = try await getMyBookingsUseCase.getUpcoming(
                memberId: memberId,
                forceRefresh: false
            )

            if let bookings = try? result.getOrThrow() {
                // Convert Kotlin bookings to Swift models
                // Note: This is a simplified version. You'll need to fetch full class details
                self.upcomingBookings = bookings.prefix(3).map { booking in
                    BookingWithDetails(
                        id: booking.id,
                        className: "Class \(booking.scheduleId)", // TODO: Fetch actual class name
                        instructorName: nil, // TODO: Fetch from schedule
                        startTime: Date(timeIntervalSince1970: Double(booking.bookedAt.epochSeconds)),
                        status: booking.status.name,
                        statusText: booking.statusDisplayText()
                    )
                }
            }
        } catch {
            print("Error loading bookings: \(error)")
            errorMessage = "Failed to load bookings"
        }
    }

    private func loadStats() async {
        do {
            // TODO: Implement stats loading when API is available
            // For now, use mock data or leave empty
            activityStats = ActivityStats(
                classesAttended: 0,
                classesBooked: upcomingBookings.count,
                totalWorkouts: 0,
                currentStreak: 0
            )
        } catch {
            print("Error loading stats: \(error)")
            activityStats = ActivityStats.empty
        }
    }

    private func loadFeaturedClasses() async {
        do {
            let result = try await getSchedulesUseCase.invoke(
                branchId: nil, // Get all branches or use current branch
                forceRefresh: false
            )

            if let schedules = try? result.getOrThrow() {
                // Convert to featured classes (take first 5)
                self.featuredClasses = schedules.prefix(5).map { schedule in
                    FeaturedClass(
                        id: schedule.id,
                        name: "Class \(schedule.classId)", // TODO: Fetch actual class name
                        instructorName: schedule.instructorName,
                        startTime: convertLocalDateTimeToDate(schedule.startDateTime),
                        capacity: Int(schedule.capacity),
                        availableSpots: Int(schedule.availableSpots()),
                        isFull: schedule.isFull()
                    )
                }
            }
        } catch {
            print("Error loading featured classes: \(error)")
            featuredClasses = []
        }
    }

    // MARK: - Helper Methods
    private func getMockMemberId() -> String {
        // TODO: Get actual member ID from authentication
        // For now, return a mock ID or stored user ID
        return "mock-member-id"
    }

    private func convertLocalDateTimeToDate(_ localDateTime: shared.LocalDateTime) -> Date {
        let calendar = Calendar.current
        var components = DateComponents()
        components.year = Int(localDateTime.year)
        components.month = Int(localDateTime.monthNumber)
        components.day = Int(localDateTime.dayOfMonth)
        components.hour = Int(localDateTime.hour)
        components.minute = Int(localDateTime.minute)
        components.second = Int(localDateTime.second)

        return calendar.date(from: components) ?? Date()
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
