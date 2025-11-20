import Foundation
import Combine
import shared

@MainActor
class ClassListViewModel: ObservableObject {
    // MARK: - Published Properties
    @Published var schedules: [shared.ClassSchedule] = []
    @Published var schedulesByTime: [SchedulesByTime] = []
    @Published var selectedFilters: Set<String> = ["All"]
    @Published var availableFilters: [ClassType] = ClassType.allCases
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var showError = false

    // MARK: - Private Properties
    private let scheduleRepository: ScheduleRepository
    private let bookingRepository: BookingRepository
    private var cancellables = Set<AnyCancellable>()
    private var currentBranchId: String?

    // MARK: - Initialization
    init(
        scheduleRepository: ScheduleRepository? = nil,
        bookingRepository: BookingRepository? = nil
    ) {
        self.scheduleRepository = scheduleRepository ?? KoinHelper.shared.getScheduleRepository()
        self.bookingRepository = bookingRepository ?? KoinHelper.shared.getBookingRepository()

        setupFilterObserver()
    }

    // MARK: - Public Methods
    func loadSchedules(for date: Date) async {
        isLoading = true
        errorMessage = nil
        showError = false

        do {
            let result = try await scheduleRepository.getSchedules(
                branchId: currentBranchId,
                forceRefresh: false
            )

            if let allSchedules = try? result.getOrThrow() {
                // Filter schedules for the selected date
                let filteredSchedules = allSchedules.filter { schedule in
                    let scheduleDate = convertLocalDateTimeToDate(schedule.startDateTime)
                    return Calendar.current.isDate(scheduleDate, inSameDayAs: date)
                }

                schedules = filteredSchedules
                applyFiltersAndGroup()
            } else {
                schedules = []
                schedulesByTime = []
            }

            isLoading = false
        } catch {
            errorMessage = "Failed to load classes: \(error.localizedDescription)"
            showError = true
            isLoading = false
            schedules = []
            schedulesByTime = []
        }
    }

    func refresh(for date: Date) async {
        await loadSchedules(for: date)
    }

    // MARK: - Private Methods
    private func setupFilterObserver() {
        $selectedFilters
            .debounce(for: .milliseconds(100), scheduler: RunLoop.main)
            .sink { [weak self] _ in
                self?.applyFiltersAndGroup()
            }
            .store(in: &cancellables)
    }

    private func applyFiltersAndGroup() {
        var filtered = schedules

        // Apply filters if not "All"
        if !selectedFilters.contains("All") && !selectedFilters.isEmpty {
            filtered = schedules.filter { schedule in
                // For now, match by class name since we don't have explicit categories
                // You can enhance this logic based on your actual data model
                selectedFilters.contains { filter in
                    // Simple contains check - enhance based on your needs
                    return true // For now, show all
                }
            }
        }

        // Group by time slot
        schedulesByTime = groupSchedulesByTime(filtered)
    }

    private func groupSchedulesByTime(_ schedules: [shared.ClassSchedule]) -> [SchedulesByTime] {
        let grouped = Dictionary(grouping: schedules) { schedule -> String in
            let date = convertLocalDateTimeToDate(schedule.startDateTime)
            let formatter = DateFormatter()
            formatter.timeStyle = .short
            return formatter.string(from: date)
        }

        return grouped.map { timeSlot, schedules in
            SchedulesByTime(timeSlot: timeSlot, schedules: schedules.sorted {
                convertLocalDateTimeToDate($0.startDateTime) < convertLocalDateTimeToDate($1.startDateTime)
            })
        }.sorted { first, second in
            // Sort by the first schedule's time in each group
            guard let firstSchedule = first.schedules.first,
                  let secondSchedule = second.schedules.first else {
                return false
            }

            let firstDate = convertLocalDateTimeToDate(firstSchedule.startDateTime)
            let secondDate = convertLocalDateTimeToDate(secondSchedule.startDateTime)

            return firstDate < secondDate
        }
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
