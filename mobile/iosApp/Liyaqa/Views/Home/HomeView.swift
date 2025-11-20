import SwiftUI

struct HomeView: View {
    @StateObject private var viewModel = HomeViewModel()
    @State private var showingNotifications = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Welcome section
                    WelcomeSection(memberName: viewModel.memberName)

                    // Quick actions
                    QuickActionsGrid()

                    // Upcoming bookings
                    if !viewModel.upcomingBookings.isEmpty {
                        UpcomingBookingsCard(bookings: viewModel.upcomingBookings)
                    }

                    // Activity summary
                    if let stats = viewModel.activityStats {
                        ActivitySummaryCard(stats: stats)
                    }

                    // Featured classes
                    FeaturedClassesSection(classes: viewModel.featuredClasses)
                }
                .padding()
            }
            .navigationTitle("Home")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        showingNotifications = true
                    } label: {
                        Image(systemName: "bell")
                            .overlay(
                                viewModel.unreadCount > 0 ?
                                Badge(count: viewModel.unreadCount) : nil
                            )
                    }
                }
            }
            .refreshable {
                await viewModel.refresh()
            }
        }
        .tabItem {
            Label("Home", systemImage: "house")
        }
        .onAppear {
            viewModel.loadData()
        }
        .sheet(isPresented: $showingNotifications) {
            NotificationsView()
        }
        .loading(viewModel.isLoading)
    }
}

// MARK: - Notifications View (Placeholder)
struct NotificationsView: View {
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationStack {
            VStack {
                Text("Notifications")
                    .font(.largeTitle)
                Text("No new notifications")
                    .foregroundColor(.secondary)
            }
            .navigationTitle("Notifications")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
    }
}

// MARK: - Membership Card
struct MembershipCard: View {
    let membership: MembershipInfo

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Membership Status")
                        .font(.labelMedium)
                        .foregroundColor(.textSecondary)

                    Text(membership.plan)
                        .font(.titleMedium)
                        .foregroundColor(.textPrimary)
                }

                Spacer()

                statusBadge
            }

            Divider()

            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Expires")
                        .font(.labelSmall)
                        .foregroundColor(.textSecondary)

                    Text(membership.expiryDate, style: .date)
                        .font(.bodyMedium)
                        .foregroundColor(.textPrimary)
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 4) {
                    Text("Days Left")
                        .font(.labelSmall)
                        .foregroundColor(.textSecondary)

                    Text("\(membership.daysRemaining)")
                        .font(.titleMedium)
                        .foregroundColor(membership.daysRemaining < 7 ? .error : .liyaqaBrand)
                }
            }

            if membership.status == .expiringSoon {
                Button {
                    // Navigate to renewal
                } label: {
                    Text("Renew Membership")
                        .font(.labelMedium)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.liyaqaBrand)
                        .cornerRadius(8)
                }
            }
        }
        .padding()
        .cardStyle()
    }

    private var statusBadge: some View {
        Text(statusText)
            .statusBadge(statusType)
    }

    private var statusText: String {
        switch membership.status {
        case .active: return "Active"
        case .expiringSoon: return "Expiring Soon"
        case .expired: return "Expired"
        }
    }

    private var statusType: StatusBadgeModifier.Status {
        switch membership.status {
        case .active: return .active
        case .expiringSoon: return .warning
        case .expired: return .error
        }
    }
}

// MARK: - Quick Stats View
struct QuickStatsView: View {
    let stats: MemberStats

    var body: some View {
        VStack(spacing: 12) {
            HStack(spacing: 12) {
                StatCard(title: "Classes Attended", value: "\(stats.classesAttended)", icon: "checkmark.circle.fill", color: .success)
                StatCard(title: "Classes Booked", value: "\(stats.classesBooked)", icon: "calendar", color: .liyaqaBrand)
            }

            HStack(spacing: 12) {
                StatCard(title: "Total Workouts", value: "\(stats.totalWorkouts)", icon: "figure.run", color: .info)
                StatCard(title: "Current Streak", value: "\(stats.currentStreak) days", icon: "flame.fill", color: .warning)
            }
        }
    }
}

// MARK: - Stat Card
struct StatCard: View {
    let title: String
    let value: String
    let icon: String
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(color)

            Text(value)
                .font(.titleMedium)
                .foregroundColor(.textPrimary)

            Text(title)
                .font(.labelSmall)
                .foregroundColor(.textSecondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .cardStyle()
    }
}

// MARK: - Class Card Compact
struct ClassCardCompact: View {
    let gymClass: GymClass

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Class Image
            RoundedRectangle(cornerRadius: 8)
                .fill(Color.liyaqaBrand.opacity(0.3))
                .frame(width: 200, height: 100)
                .overlay(
                    Image(systemName: "figure.run")
                        .font(.largeTitle)
                        .foregroundColor(.liyaqaBrand)
                )

            VStack(alignment: .leading, spacing: 4) {
                Text(gymClass.name)
                    .font(.labelLarge)
                    .foregroundColor(.textPrimary)

                Text(gymClass.instructor)
                    .font(.bodySmall)
                    .foregroundColor(.textSecondary)

                HStack {
                    Image(systemName: "clock")
                        .font(.caption)
                    Text(gymClass.date, style: .time)
                        .font(.bodySmall)
                }
                .foregroundColor(.textSecondary)
            }
        }
        .frame(width: 200)
        .padding()
        .cardStyle()
    }
}

// MARK: - Activity Row
struct ActivityRow: View {
    let activity: Activity

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: activity.icon)
                .font(.title3)
                .foregroundColor(.liyaqaBrand)
                .frame(width: 40, height: 40)
                .background(Color.liyaqaBrand.opacity(0.1))
                .cornerRadius(8)

            VStack(alignment: .leading, spacing: 2) {
                Text(activity.title)
                    .font(.bodyMedium)
                    .foregroundColor(.textPrimary)

                Text(activity.subtitle)
                    .font(.bodySmall)
                    .foregroundColor(.textSecondary)
            }

            Spacer()

            Text(activity.date, style: .relative)
                .font(.labelSmall)
                .foregroundColor(.textSecondary)
        }
        .padding()
        .cardStyle()
    }
}

// MARK: - Empty States
struct EmptyUpcomingClassesView: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "calendar.badge.plus")
                .font(.system(size: 40))
                .foregroundColor(.textSecondary)

            Text("No upcoming classes")
                .font(.bodyMedium)
                .foregroundColor(.textPrimary)

            Text("Book a class to get started")
                .font(.bodySmall)
                .foregroundColor(.textSecondary)

            NavigationLink {
                ClassListView()
            } label: {
                Text("Browse Classes")
                    .font(.labelMedium)
                    .foregroundColor(.liyaqaBrand)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 10)
                    .background(Color.liyaqaBrand.opacity(0.1))
                    .cornerRadius(8)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 40)
        .cardStyle()
    }
}

struct EmptyActivityView: View {
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: "clock.arrow.circlepath")
                .font(.system(size: 32))
                .foregroundColor(.textSecondary)

            Text("No recent activity")
                .font(.bodyMedium)
                .foregroundColor(.textPrimary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 30)
        .cardStyle()
    }
}

#Preview {
    HomeView()
}
