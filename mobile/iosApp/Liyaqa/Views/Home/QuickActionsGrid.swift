import SwiftUI

/// Grid of quick action buttons for common tasks
struct QuickActionsGrid: View {
    @State private var showingCheckIn = false

    var body: some View {
        VStack(spacing: 12) {
            HStack(spacing: 12) {
                QuickActionButton(
                    title: "Book Class",
                    icon: "calendar.badge.plus",
                    color: .liyaqaBrand
                ) {
                    NavigationLink(destination: ClassListView()) {
                        EmptyView()
                    }
                }

                QuickActionButton(
                    title: "Check In",
                    icon: "qrcode.viewfinder",
                    color: .success
                ) {
                    Button {
                        showingCheckIn = true
                    } label: {
                        EmptyView()
                    }
                }
            }

            HStack(spacing: 12) {
                QuickActionButton(
                    title: "View Schedule",
                    icon: "calendar",
                    color: .info
                ) {
                    NavigationLink(destination: ClassListView()) {
                        EmptyView()
                    }
                }

                QuickActionButton(
                    title: "My Bookings",
                    icon: "list.bullet.clipboard",
                    color: .warning
                ) {
                    // TODO: Navigate to bookings view
                    EmptyView()
                }
            }
        }
        .sheet(isPresented: $showingCheckIn) {
            CheckInView()
        }
    }
}

/// Individual quick action button
struct QuickActionButton<Content: View>: View {
    let title: String
    let icon: String
    let color: Color
    @ViewBuilder let destination: () -> Content

    var body: some View {
        destination()
            .opacity(0)
            .overlay(
                VStack(spacing: 8) {
                    Image(systemName: icon)
                        .font(.system(size: 32))
                        .foregroundColor(color)

                    Text(title)
                        .font(.labelMedium)
                        .foregroundColor(.textPrimary)
                        .multilineTextAlignment(.center)
                }
                .frame(maxWidth: .infinity, minHeight: 100)
                .padding()
                .cardStyle()
            )
    }
}

#Preview {
    NavigationStack {
        ScrollView {
            VStack(spacing: 20) {
                QuickActionsGrid()
            }
            .padding()
        }
    }
}
