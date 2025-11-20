import SwiftUI
import shared

struct ClassDetailView: View {
    let schedule: shared.ClassSchedule
    @StateObject private var viewModel: ClassDetailViewModel
    @State private var showingBookingConfirmation = false
    @State private var showingCancelConfirmation = false
    @Environment(\.dismiss) var dismiss

    init(schedule: shared.ClassSchedule) {
        self.schedule = schedule
        _viewModel = StateObject(wrappedValue: ClassDetailViewModel(schedule: schedule))
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Class header with image
                classImage

                VStack(alignment: .leading, spacing: 12) {
                    // Class name
                    Text(schedule.classId)
                        .font(.title)
                        .fontWeight(.bold)
                        .foregroundColor(.textPrimary)

                    // Trainer info
                    if let trainerName = schedule.instructorName {
                        trainerInfoSection(trainerName: trainerName)
                    }

                    Divider()

                    // Class info
                    classInfoSection

                    Divider()

                    // Description
                    descriptionSection

                    Divider()

                    // What to bring
                    whatToBringSection

                    // Availability Indicator
                    availabilityStatus
                }
                .padding()
            }
        }
        .background(Color.background)
        .navigationBarTitleDisplayMode(.inline)
        .safeAreaInset(edge: .bottom) {
            bookButton
        }
        .confirmationDialog("Confirm Booking", isPresented: $showingBookingConfirmation) {
            Button("Confirm") {
                Task {
                    await viewModel.bookClass()
                }
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Book \(schedule.classId) on \(schedule.formattedDate)?")
        }
        .confirmationDialog("Cancel Booking", isPresented: $showingCancelConfirmation) {
            Button("Cancel Booking", role: .destructive) {
                Task {
                    await viewModel.cancelBooking()
                }
            }
            Button("Keep Booking", role: .cancel) {}
        } message: {
            Text("Are you sure you want to cancel this booking?")
        }
        .alert("Error", isPresented: $viewModel.showError) {
            Button("OK", role: .cancel) {}
        } message: {
            if let errorMessage = viewModel.errorMessage {
                Text(errorMessage)
            }
        }
        .alert("Success", isPresented: $viewModel.showSuccess) {
            Button("OK", role: .cancel) {}
        } message: {
            if let successMessage = viewModel.successMessage {
                Text(successMessage)
            }
        }
    }

    // MARK: - Class Image
    private var classImage: some View {
        ZStack {
            LinearGradient(
                colors: [schedule.classTypeEnum.color.opacity(0.7), schedule.classTypeEnum.color],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .frame(height: 200)

            Image(systemName: schedule.classTypeEnum.icon)
                .font(.system(size: 80))
                .foregroundColor(.white.opacity(0.3))
        }
    }

    // MARK: - Trainer Info Section
    private func trainerInfoSection(trainerName: String) -> some View {
        HStack(spacing: 12) {
            // Trainer photo placeholder
            Circle()
                .fill(Color.liyaqaBrand.opacity(0.2))
                .frame(width: 50, height: 50)
                .overlay(
                    Image(systemName: "person.fill")
                        .foregroundColor(.liyaqaBrand)
                )

            VStack(alignment: .leading, spacing: 4) {
                Text(trainerName)
                    .font(.headline)
                    .foregroundColor(.textPrimary)

                Text("Certified Trainer")
                    .font(.caption)
                    .foregroundColor(.textSecondary)
            }

            Spacer()
        }
    }

    // MARK: - Class Info Section
    private var classInfoSection: some View {
        VStack(spacing: 12) {
            InfoRow(
                icon: "calendar",
                label: "Date",
                value: schedule.formattedDate
            )

            InfoRow(
                icon: "clock",
                label: "Time",
                value: schedule.timeRange
            )

            InfoRow(
                icon: "timer",
                label: "Duration",
                value: schedule.duration
            )

            InfoRow(
                icon: "person.3",
                label: "Spots Available",
                value: "\(Int(schedule.availableSpots()))/\(Int(schedule.capacity))"
            )

            if schedule.hasWaitlist() {
                InfoRow(
                    icon: "list.bullet",
                    label: "Waitlist",
                    value: "\(Int(schedule.waitlistCount)) people",
                    iconColor: .warning
                )
            }
        }
    }

    // MARK: - Description Section
    private var descriptionSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("About This Class")
                .font(.headline)
                .foregroundColor(.textPrimary)

            Text(schedule.notes ?? "Join us for an energizing workout session designed to help you reach your fitness goals. This class is suitable for all fitness levels and focuses on building strength, endurance, and flexibility.")
                .font(.bodyMedium)
                .foregroundColor(.textSecondary)
                .lineSpacing(4)
        }
    }

    // MARK: - What to Bring Section
    private var whatToBringSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("What to bring")
                .font(.headline)
                .foregroundColor(.textPrimary)

            VStack(alignment: .leading, spacing: 6) {
                BulletPoint(text: "Water bottle")
                BulletPoint(text: "Towel")
                BulletPoint(text: "Comfortable workout clothes")
                BulletPoint(text: "Athletic shoes")
            }
        }
    }

    // MARK: - Availability Status
    private var availabilityStatus: some View {
        HStack(spacing: 8) {
            Image(systemName: statusIcon)
                .foregroundColor(statusColor)

            Text(statusText)
                .font(.bodyMedium)
                .foregroundColor(.textPrimary)
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(statusColor.opacity(0.1))
        .cornerRadius(12)
    }

    // MARK: - Book Button
    private var bookButton: some View {
        VStack(spacing: 0) {
            Divider()

            Button {
                if viewModel.isBooked {
                    showingCancelConfirmation = true
                } else {
                    showingBookingConfirmation = true
                }
            } label: {
                HStack {
                    if viewModel.bookingInProgress {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text(buttonTitle)
                    }
                }
                .primaryButtonStyle(isEnabled: !viewModel.bookingInProgress && !schedule.isCancelled)
            }
            .disabled(viewModel.bookingInProgress || (schedule.isFull() && !viewModel.isBooked) || schedule.isCancelled)
            .padding()
            .background(Color.surface)
        }
    }

    // MARK: - Computed Properties
    private var statusIcon: String {
        if schedule.isCancelled {
            return "xmark.circle.fill"
        } else if viewModel.isBooked {
            return "checkmark.circle.fill"
        } else if schedule.isFull() {
            return "exclamationmark.circle.fill"
        } else if schedule.availableSpots() <= 3 {
            return "exclamationmark.triangle.fill"
        } else {
            return "checkmark.circle.fill"
        }
    }

    private var statusColor: Color {
        if schedule.isCancelled {
            return .gray
        } else if viewModel.isBooked {
            return .liyaqaBrand
        } else if schedule.isFull() {
            return .error
        } else if schedule.availableSpots() <= 3 {
            return .warning
        } else {
            return .success
        }
    }

    private var statusText: String {
        if schedule.isCancelled {
            return "This class has been cancelled"
        } else if viewModel.isBooked {
            return "You have booked this class"
        } else if schedule.isFull() {
            return "Class is full - Join waitlist"
        } else if schedule.availableSpots() <= 3 {
            return "Hurry! Only \(Int(schedule.availableSpots())) spots remaining"
        } else {
            return "\(Int(schedule.availableSpots())) spots available"
        }
    }

    private var buttonTitle: String {
        if viewModel.isBooked {
            return "Cancel Booking"
        } else if schedule.isFull() {
            return "Join Waitlist"
        } else {
            return "Book Class"
        }
    }
}

// MARK: - Bullet Point
struct BulletPoint: View {
    let text: String

    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            Text("•")
                .font(.bodyMedium)
                .foregroundColor(.textPrimary)

            Text(text)
                .font(.bodyMedium)
                .foregroundColor(.textSecondary)
        }
    }
}

#Preview {
    NavigationView {
        ClassDetailView(schedule: shared.ClassSchedule(
            id: "1",
            classId: "Yoga Flow",
            instructorId: "instructor-1",
            instructorName: "Sarah Johnson",
            startDateTime: shared.LocalDateTime(
                year: 2024,
                monthNumber: 12,
                dayOfMonth: 20,
                hour: 9,
                minute: 0,
                second: 0,
                nanosecond: 0
            ),
            endDateTime: shared.LocalDateTime(
                year: 2024,
                monthNumber: 12,
                dayOfMonth: 20,
                hour: 10,
                minute: 0,
                second: 0,
                nanosecond: 0
            ),
            capacity: 20,
            bookedCount: 15,
            waitlistCount: 0,
            isCancelled: false,
            cancellationReason: nil,
            notes: "A relaxing yoga session perfect for all levels.",
            createdAt: shared.Instant.Companion.shared.fromEpochMilliseconds(epochMilliseconds: 0),
            updatedAt: shared.Instant.Companion.shared.fromEpochMilliseconds(epochMilliseconds: 0)
        ))
    }
}
