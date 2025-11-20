import SwiftUI
import shared

/// Card displaying upcoming bookings
struct UpcomingBookingsCard: View {
    let bookings: [BookingWithDetails]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Upcoming Bookings")
                    .font(.titleMedium)
                    .foregroundColor(.textPrimary)

                Spacer()

                // TODO: Add navigation to all bookings
                Text("See All")
                    .font(.bodySmall)
                    .foregroundColor(.liyaqaBrand)
            }

            if bookings.isEmpty {
                EmptyBookingsView()
            } else {
                VStack(spacing: 8) {
                    ForEach(bookings.prefix(3)) { booking in
                        BookingRow(booking: booking)
                    }
                }
            }
        }
        .padding()
        .cardStyle()
    }
}

/// Individual booking row
struct BookingRow: View {
    let booking: BookingWithDetails

    var body: some View {
        HStack(spacing: 12) {
            // Class icon
            RoundedRectangle(cornerRadius: 8)
                .fill(Color.liyaqaBrand.opacity(0.2))
                .frame(width: 50, height: 50)
                .overlay(
                    Image(systemName: "figure.run")
                        .font(.title3)
                        .foregroundColor(.liyaqaBrand)
                )

            VStack(alignment: .leading, spacing: 4) {
                Text(booking.className)
                    .font(.labelLarge)
                    .foregroundColor(.textPrimary)

                if let instructor = booking.instructorName {
                    Text(instructor)
                        .font(.bodySmall)
                        .foregroundColor(.textSecondary)
                }

                HStack(spacing: 8) {
                    Image(systemName: "clock")
                        .font(.caption)
                    Text(booking.formattedTime)
                        .font(.bodySmall)
                }
                .foregroundColor(.textSecondary)
            }

            Spacer()

            // Status badge
            Text(booking.statusText)
                .font(.labelSmall)
                .foregroundColor(.white)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(statusColor)
                .cornerRadius(6)
        }
        .padding(.vertical, 8)
    }

    private var statusColor: Color {
        switch booking.status {
        case "CONFIRMED":
            return .success
        case "WAITLISTED":
            return .warning
        default:
            return .gray
        }
    }
}

/// Empty state for bookings
struct EmptyBookingsView: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "calendar.badge.exclamationmark")
                .font(.system(size: 32))
                .foregroundColor(.textSecondary)

            Text("No upcoming bookings")
                .font(.bodyMedium)
                .foregroundColor(.textPrimary)

            Text("Book a class to get started")
                .font(.bodySmall)
                .foregroundColor(.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 20)
    }
}

/// Model for booking with class details
struct BookingWithDetails: Identifiable {
    let id: String
    let className: String
    let instructorName: String?
    let startTime: Date
    let status: String
    let statusText: String

    var formattedTime: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: startTime)
    }
}

#Preview {
    ScrollView {
        VStack(spacing: 20) {
            // With bookings
            UpcomingBookingsCard(bookings: [
                BookingWithDetails(
                    id: "1",
                    className: "Yoga Flow",
                    instructorName: "Sarah Johnson",
                    startTime: Date().addingTimeInterval(3600),
                    status: "CONFIRMED",
                    statusText: "Confirmed"
                ),
                BookingWithDetails(
                    id: "2",
                    className: "HIIT Training",
                    instructorName: "Mike Davis",
                    startTime: Date().addingTimeInterval(7200),
                    status: "WAITLISTED",
                    statusText: "Waitlisted"
                )
            ])

            // Empty state
            UpcomingBookingsCard(bookings: [])
        }
        .padding()
    }
}
