import Foundation
import Combine
import shared

@MainActor
class ClassDetailViewModel: ObservableObject {
    // MARK: - Published Properties
    @Published var schedule: shared.ClassSchedule
    @Published var isBooked = false
    @Published var currentBooking: shared.Booking?
    @Published var isLoading = false
    @Published var bookingInProgress = false
    @Published var errorMessage: String?
    @Published var showError = false
    @Published var showSuccess = false
    @Published var successMessage: String?

    // MARK: - Private Properties
    private let bookingRepository: BookingRepository
    private var cancellables = Set<AnyCancellable>()
    private var currentMemberId: String?

    // MARK: - Initialization
    init(
        schedule: shared.ClassSchedule,
        bookingRepository: BookingRepository? = nil
    ) {
        self.schedule = schedule
        self.bookingRepository = bookingRepository ?? KoinHelper.shared.getBookingRepository()

        Task {
            await loadBookingStatus()
        }
    }

    // MARK: - Public Methods
    func loadBookingStatus() async {
        guard let memberId = getMemberId() else { return }

        do {
            let result = try await bookingRepository.getMyBookings(
                memberId: memberId,
                forceRefresh: false
            )

            if let bookings = try? result.getOrThrow() {
                // Check if this schedule is booked
                currentBooking = bookings.first { booking in
                    booking.scheduleId == schedule.id &&
                    (booking.status == .confirmed || booking.status == .waitlisted)
                }
                isBooked = currentBooking != nil
            }
        } catch {
            print("Error loading booking status: \(error)")
        }
    }

    func bookClass() async {
        guard let memberId = getMemberId() else {
            errorMessage = "Please log in to book a class"
            showError = true
            return
        }

        bookingInProgress = true
        errorMessage = nil
        showError = false

        do {
            // Create booking through repository
            let result = try await bookingRepository.createBooking(
                memberId: memberId,
                scheduleId: schedule.id
            )

            if let booking = try? result.getOrThrow() {
                currentBooking = booking
                isBooked = true
                successMessage = booking.isWaitlisted() ?
                    "Added to waitlist successfully!" :
                    "Class booked successfully!"
                showSuccess = true

                // Update schedule booked count locally
                // Note: In production, you'd refresh from server
            } else {
                throw NSError(domain: "BookingError", code: -1, userInfo: [
                    NSLocalizedDescriptionKey: "Failed to create booking"
                ])
            }

            bookingInProgress = false
        } catch {
            errorMessage = "Failed to book class: \(error.localizedDescription)"
            showError = true
            bookingInProgress = false
        }
    }

    func cancelBooking() async {
        guard let booking = currentBooking else { return }

        bookingInProgress = true
        errorMessage = nil
        showError = false

        do {
            let result = try await bookingRepository.cancelBooking(
                bookingId: booking.id,
                reason: "Cancelled by user"
            )

            if let _ = try? result.getOrThrow() {
                currentBooking = nil
                isBooked = false
                successMessage = "Booking cancelled successfully"
                showSuccess = true
            } else {
                throw NSError(domain: "BookingError", code: -1, userInfo: [
                    NSLocalizedDescriptionKey: "Failed to cancel booking"
                ])
            }

            bookingInProgress = false
        } catch {
            errorMessage = "Failed to cancel booking: \(error.localizedDescription)"
            showError = true
            bookingInProgress = false
        }
    }

    func refresh() async {
        await loadBookingStatus()
    }

    // MARK: - Private Methods
    private func getMemberId() -> String? {
        // TODO: Get actual member ID from authentication
        // For now, return a mock ID
        return currentMemberId ?? "mock-member-id"
    }
}
