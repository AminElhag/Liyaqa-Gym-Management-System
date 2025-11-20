import Foundation
import Combine
import shared

@MainActor
class ClassViewModel: ObservableObject {
    @Published var classes: [GymClass] = []
    @Published var filteredClasses: [GymClass] = []
    @Published var selectedClass: GymClass?
    @Published var searchText = ""
    @Published var selectedFilter: ClassFilter = .all
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var bookingInProgress = false

    private let scheduleRepository: ScheduleRepository
    private let bookingRepository: BookingRepository
    private var cancellables = Set<AnyCancellable>()

    enum ClassFilter: String, CaseIterable {
        case all = "All"
        case yoga = "Yoga"
        case cardio = "Cardio"
        case strength = "Strength"
        case hiit = "HIIT"
        case pilates = "Pilates"
    }

    init(
        scheduleRepository: ScheduleRepository? = nil,
        bookingRepository: BookingRepository? = nil
    ) {
        self.scheduleRepository = scheduleRepository ?? KoinHelper.shared.getScheduleRepository()
        self.bookingRepository = bookingRepository ?? KoinHelper.shared.getBookingRepository()

        setupSearchAndFilter()
    }

    private func setupSearchAndFilter() {
        // Combine search text and filter changes
        Publishers.CombineLatest($searchText, $selectedFilter)
            .debounce(for: .milliseconds(300), scheduler: RunLoop.main)
            .sink { [weak self] searchText, filter in
                self?.applyFilters(searchText: searchText, filter: filter)
            }
            .store(in: &cancellables)
    }

    func loadClasses() async {
        isLoading = true
        errorMessage = nil

        do {
            // Fetch classes from repository
            // classes = try await scheduleRepository.getClasses()
            // For now, use mock data
            classes = []
            filteredClasses = classes
            isLoading = false
        } catch {
            errorMessage = error.localizedDescription
            isLoading = false
        }
    }

    func bookClass(_ gymClass: GymClass) async {
        bookingInProgress = true
        errorMessage = nil

        do {
            // Book class through repository
            // try await bookingRepository.bookClass(classId: gymClass.id)

            // Show success message
            errorMessage = "Class booked successfully!"
            bookingInProgress = false

            // Reload classes to update availability
            await loadClasses()
        } catch {
            errorMessage = error.localizedDescription
            bookingInProgress = false
        }
    }

    func cancelBooking(_ gymClass: GymClass) async {
        bookingInProgress = true
        errorMessage = nil

        do {
            // Cancel booking through repository
            // try await bookingRepository.cancelBooking(classId: gymClass.id)

            errorMessage = "Booking cancelled successfully"
            bookingInProgress = false

            await loadClasses()
        } catch {
            errorMessage = error.localizedDescription
            bookingInProgress = false
        }
    }

    private func applyFilters(searchText: String, filter: ClassFilter) {
        var result = classes

        // Apply category filter
        if filter != .all {
            result = result.filter { $0.name.lowercased().contains(filter.rawValue.lowercased()) }
        }

        // Apply search filter
        if !searchText.isEmpty {
            result = result.filter {
                $0.name.lowercased().contains(searchText.lowercased()) ||
                $0.instructor.lowercased().contains(searchText.lowercased())
            }
        }

        filteredClasses = result
    }

    func refresh() async {
        await loadClasses()
    }
}
