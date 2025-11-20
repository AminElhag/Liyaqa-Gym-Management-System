import SwiftUI

/// Card displaying member activity statistics
struct ActivitySummaryCard: View {
    let stats: ActivityStats

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Activity Summary")
                .font(.titleMedium)
                .foregroundColor(.textPrimary)

            VStack(spacing: 12) {
                HStack(spacing: 12) {
                    ActivityStatItem(
                        title: "Classes\nAttended",
                        value: "\(stats.classesAttended)",
                        icon: "checkmark.circle.fill",
                        color: .success
                    )

                    ActivityStatItem(
                        title: "Classes\nBooked",
                        value: "\(stats.classesBooked)",
                        icon: "calendar",
                        color: .liyaqaBrand
                    )
                }

                HStack(spacing: 12) {
                    ActivityStatItem(
                        title: "Total\nWorkouts",
                        value: "\(stats.totalWorkouts)",
                        icon: "figure.run",
                        color: .info
                    )

                    ActivityStatItem(
                        title: "Current\nStreak",
                        value: "\(stats.currentStreak)",
                        icon: "flame.fill",
                        color: .warning
                    )
                }
            }
        }
        .padding()
        .cardStyle()
    }
}

/// Individual activity stat item
struct ActivityStatItem: View {
    let title: String
    let value: String
    let icon: String
    let color: Color

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(color)

            Text(value)
                .font(.titleMedium)
                .fontWeight(.semibold)
                .foregroundColor(.textPrimary)

            Text(title)
                .font(.labelSmall)
                .foregroundColor(.textSecondary)
                .multilineTextAlignment(.center)
                .lineLimit(2)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(color.opacity(0.1))
        .cornerRadius(12)
    }
}

/// Model for activity statistics
struct ActivityStats {
    let classesAttended: Int
    let classesBooked: Int
    let totalWorkouts: Int
    let currentStreak: Int

    static let empty = ActivityStats(
        classesAttended: 0,
        classesBooked: 0,
        totalWorkouts: 0,
        currentStreak: 0
    )
}

#Preview {
    ScrollView {
        VStack(spacing: 20) {
            ActivitySummaryCard(stats: ActivityStats(
                classesAttended: 42,
                classesBooked: 8,
                totalWorkouts: 156,
                currentStreak: 7
            ))

            ActivitySummaryCard(stats: .empty)
        }
        .padding()
    }
}
