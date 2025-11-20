import SwiftUI
import shared

struct ClassScheduleRow: View {
    let schedule: shared.ClassSchedule

    var body: some View {
        HStack(spacing: 12) {
            // Class icon or image
            classIcon

            // Class details
            VStack(alignment: .leading, spacing: 4) {
                // Class name
                Text(schedule.classId)
                    .font(.headline)
                    .foregroundColor(.textPrimary)

                // Trainer and time
                HStack {
                    if let trainerName = schedule.instructorName {
                        Text(trainerName)
                            .font(.subheadline)
                            .foregroundColor(.textSecondary)
                    }

                    Spacer()

                    Text(schedule.timeRange)
                        .font(.subheadline)
                        .foregroundColor(.textSecondary)
                }

                // Capacity indicator
                HStack(spacing: 8) {
                    HStack(spacing: 4) {
                        Image(systemName: "person.fill")
                            .font(.caption)
                        Text("\(Int(schedule.bookedCount))/\(Int(schedule.capacity))")
                            .font(.caption)
                    }
                    .foregroundColor(.textSecondary)

                    Spacer()

                    // Status badge
                    StatusBadge(status: schedule.bookingStatusType)
                }
            }
        }
        .padding(.vertical, 8)
        .padding(.horizontal, 4)
    }

    // MARK: - Class Icon
    private var classIcon: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 8)
                .fill(schedule.classTypeEnum.color.opacity(0.2))
                .frame(width: 50, height: 50)

            Image(systemName: schedule.classTypeEnum.icon)
                .font(.title3)
                .foregroundColor(schedule.classTypeEnum.color)
        }
    }
}

#Preview {
    let mockSchedule = shared.ClassSchedule(
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
        notes: nil,
        createdAt: shared.Instant.Companion.shared.fromEpochMilliseconds(epochMilliseconds: 0),
        updatedAt: shared.Instant.Companion.shared.fromEpochMilliseconds(epochMilliseconds: 0)
    )

    return List {
        ClassScheduleRow(schedule: mockSchedule)
        ClassScheduleRow(schedule: mockSchedule)
    }
    .listStyle(.insetGrouped)
}
