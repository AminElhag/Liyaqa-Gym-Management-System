import SwiftUI

struct ClassDetailView: View {
    let gymClass: GymClass
    @StateObject private var viewModel = ClassViewModel()
    @State private var showingBookingConfirmation = false
    @Environment(\.dismiss) var dismiss

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Class Image
                classImage

                // Class Info
                VStack(alignment: .leading, spacing: 16) {
                    // Title and Instructor
                    VStack(alignment: .leading, spacing: 8) {
                        Text(gymClass.name)
                            .font(.headlineMedium)
                            .foregroundColor(.textPrimary)

                        HStack {
                            Image(systemName: "person.fill")
                                .font(.caption)
                            Text(gymClass.instructor)
                                .font(.bodyMedium)
                        }
                        .foregroundColor(.textSecondary)
                    }

                    Divider()

                    // Details Grid
                    VStack(spacing: 12) {
                        DetailRow(
                            icon: "calendar",
                            title: "Date",
                            value: gymClass.date.formatted(date: .abbreviated, time: .omitted)
                        )

                        DetailRow(
                            icon: "clock",
                            title: "Time",
                            value: gymClass.date.formatted(date: .omitted, time: .shortened)
                        )

                        DetailRow(
                            icon: "timer",
                            title: "Duration",
                            value: "\(gymClass.duration) minutes"
                        )

                        DetailRow(
                            icon: "person.3.fill",
                            title: "Capacity",
                            value: "\(gymClass.enrolled)/\(gymClass.capacity)"
                        )
                    }

                    Divider()

                    // Description
                    VStack(alignment: .leading, spacing: 8) {
                        Text("About this class")
                            .font(.titleSmall)
                            .foregroundColor(.textPrimary)

                        Text("Join us for an energizing workout session designed to help you reach your fitness goals. This class is suitable for all fitness levels and focuses on building strength, endurance, and flexibility.")
                            .font(.bodyMedium)
                            .foregroundColor(.textSecondary)
                            .lineSpacing(4)
                    }

                    Divider()

                    // What to Bring
                    VStack(alignment: .leading, spacing: 8) {
                        Text("What to bring")
                            .font(.titleSmall)
                            .foregroundColor(.textPrimary)

                        VStack(alignment: .leading, spacing: 6) {
                            BulletPoint(text: "Water bottle")
                            BulletPoint(text: "Towel")
                            BulletPoint(text: "Comfortable workout clothes")
                            BulletPoint(text: "Athletic shoes")
                        }
                    }

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
        .alert("Book Class", isPresented: $showingBookingConfirmation) {
            Button("Cancel", role: .cancel) { }
            Button("Confirm") {
                Task {
                    await viewModel.bookClass(gymClass)
                }
            }
        } message: {
            Text("Are you sure you want to book \(gymClass.name)?")
        }
    }

    // MARK: - Class Image
    private var classImage: some View {
        RoundedRectangle(cornerRadius: 0)
            .fill(
                LinearGradient(
                    colors: [Color.liyaqaBrand.opacity(0.7), Color.liyaqaBrand],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            .frame(height: 250)
            .overlay(
                Image(systemName: "figure.run")
                    .font(.system(size: 80))
                    .foregroundColor(.white.opacity(0.3))
            )
    }

    // MARK: - Availability Status
    private var availabilityStatus: some View {
        HStack(spacing: 8) {
            Image(systemName: spotsAvailable ? "checkmark.circle.fill" : "xmark.circle.fill")
                .foregroundColor(spotsAvailable ? .success : .error)

            Text(spotsAvailable ? "\(spotsRemaining) spots available" : "Class is full")
                .font(.bodyMedium)
                .foregroundColor(.textPrimary)
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(spotsAvailable ? Color.success.opacity(0.1) : Color.error.opacity(0.1))
        .cornerRadius(12)
    }

    // MARK: - Book Button
    private var bookButton: some View {
        VStack(spacing: 0) {
            Divider()

            Button {
                showingBookingConfirmation = true
            } label: {
                Text(spotsAvailable ? "Book Class" : "Join Waitlist")
                    .primaryButtonStyle(isEnabled: !viewModel.bookingInProgress)
            }
            .disabled(viewModel.bookingInProgress)
            .padding()
            .background(Color.surface)
        }
    }

    private var spotsAvailable: Bool {
        gymClass.enrolled < gymClass.capacity
    }

    private var spotsRemaining: Int {
        max(0, gymClass.capacity - gymClass.enrolled)
    }
}

// MARK: - Detail Row
struct DetailRow: View {
    let icon: String
    let title: String
    let value: String

    var body: some View {
        HStack {
            Image(systemName: icon)
                .font(.body)
                .foregroundColor(.liyaqaBrand)
                .frame(width: 30)

            Text(title)
                .font(.bodyMedium)
                .foregroundColor(.textSecondary)

            Spacer()

            Text(value)
                .font(.bodyMedium)
                .foregroundColor(.textPrimary)
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
        ClassDetailView(gymClass: GymClass(
            id: "1",
            name: "HIIT Training",
            instructor: "John Doe",
            date: Date(),
            duration: 45,
            capacity: 20,
            enrolled: 15,
            imageUrl: nil
        ))
    }
}
