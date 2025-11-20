import SwiftUI

struct ProfileView: View {
    @StateObject private var viewModel = ProfileViewModel()
    @EnvironmentObject var appState: AppState
    @State private var showingSettings = false
    @State private var showingEditProfile = false

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Profile Header
                profileHeader

                // Quick Actions
                quickActions

                // Membership Section
                membershipSection

                // Activity Stats
                activityStats

                // Payment History
                paymentHistorySection

                // Account Actions
                accountActions

                Spacer(minLength: 20)
            }
            .padding(.horizontal)
        }
        .background(Color.background)
        .navigationTitle("Profile")
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button {
                    showingSettings = true
                } label: {
                    Image(systemName: "gearshape.fill")
                        .foregroundColor(.textPrimary)
                }
            }
        }
        .task {
            await viewModel.loadProfile()
        }
        .sheet(isPresented: $showingSettings) {
            SettingsView()
        }
        .sheet(isPresented: $showingEditProfile) {
            EditProfileView()
        }
    }

    // MARK: - Profile Header
    private var profileHeader: some View {
        VStack(spacing: 16) {
            // Profile Image
            ZStack(alignment: .bottomTrailing) {
                Circle()
                    .fill(Color.liyaqaBrand.opacity(0.3))
                    .frame(width: 100, height: 100)
                    .overlay(
                        Group {
                            if let image = viewModel.profileImage {
                                Image(uiImage: image)
                                    .resizable()
                                    .scaledToFill()
                            } else {
                                Image(systemName: "person.fill")
                                    .font(.system(size: 40))
                                    .foregroundColor(.liyaqaBrand)
                            }
                        }
                    )
                    .clipShape(Circle())

                Button {
                    viewModel.showingImagePicker = true
                } label: {
                    Circle()
                        .fill(Color.liyaqaBrand)
                        .frame(width: 32, height: 32)
                        .overlay(
                            Image(systemName: "camera.fill")
                                .font(.system(size: 14))
                                .foregroundColor(.white)
                        )
                }
            }

            // Name and Email
            VStack(spacing: 4) {
                Text(appState.currentUser?.fullName ?? "Member Name")
                    .font(.titleLarge)
                    .foregroundColor(.textPrimary)

                Text(appState.currentUser?.email ?? "member@liyaqa.com")
                    .font(.bodyMedium)
                    .foregroundColor(.textSecondary)
            }

            // Edit Profile Button
            Button {
                showingEditProfile = true
            } label: {
                Text("Edit Profile")
                    .font(.labelMedium)
                    .foregroundColor(.liyaqaBrand)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 8)
                    .background(Color.liyaqaBrand.opacity(0.1))
                    .cornerRadius(20)
            }
        }
        .padding(.vertical)
    }

    // MARK: - Quick Actions
    private var quickActions: some View {
        HStack(spacing: 12) {
            QuickActionButton(icon: "calendar", title: "My Classes") {
                // Navigate to my classes
            }

            QuickActionButton(icon: "creditcard.fill", title: "Payments") {
                // Navigate to payments
            }

            QuickActionButton(icon: "qrcode", title: "QR Code") {
                // Show member QR code
            }
        }
    }

    // MARK: - Membership Section
    private var membershipSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Membership")
                .font(.titleMedium)
                .foregroundColor(.textPrimary)

            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Text("Status")
                        .font(.bodyMedium)
                        .foregroundColor(.textSecondary)

                    Spacer()

                    Text("Active")
                        .statusBadge(.active)
                }

                Divider()

                HStack {
                    Text("Plan")
                        .font(.bodyMedium)
                        .foregroundColor(.textSecondary)

                    Spacer()

                    Text("Premium Monthly")
                        .font(.bodyMedium)
                        .foregroundColor(.textPrimary)
                }

                Divider()

                HStack {
                    Text("Valid Until")
                        .font(.bodyMedium)
                        .foregroundColor(.textSecondary)

                    Spacer()

                    Text(Date().addingTimeInterval(30*24*60*60), style: .date)
                        .font(.bodyMedium)
                        .foregroundColor(.textPrimary)
                }

                Button {
                    // Navigate to renewal
                } label: {
                    Text("Renew Membership")
                        .font(.labelMedium)
                        .foregroundColor(.liyaqaBrand)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.liyaqaBrand.opacity(0.1))
                        .cornerRadius(8)
                }
                .padding(.top, 4)
            }
            .padding()
            .cardStyle()
        }
    }

    // MARK: - Activity Stats
    private var activityStats: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Activity")
                .font(.titleMedium)
                .foregroundColor(.textPrimary)

            HStack(spacing: 12) {
                StatBox(value: "24", label: "Classes", icon: "calendar")
                StatBox(value: "12", label: "Check-ins", icon: "checkmark.circle")
                StatBox(value: "5", label: "Streak", icon: "flame.fill")
            }
        }
    }

    // MARK: - Payment History Section
    private var paymentHistorySection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Payment History")
                    .font(.titleMedium)
                    .foregroundColor(.textPrimary)

                Spacer()

                Button {
                    // View all payments
                } label: {
                    Text("See All")
                        .font(.bodySmall)
                        .foregroundColor(.liyaqaBrand)
                }
            }

            if viewModel.paymentHistory.isEmpty {
                EmptyPaymentHistoryView()
            } else {
                VStack(spacing: 8) {
                    ForEach(viewModel.paymentHistory.prefix(3)) { payment in
                        PaymentRow(payment: payment)
                    }
                }
            }
        }
    }

    // MARK: - Account Actions
    private var accountActions: some View {
        VStack(spacing: 12) {
            ActionButton(
                icon: "doc.text.fill",
                title: "Terms of Service",
                color: .textPrimary
            ) {
                // Open terms
            }

            ActionButton(
                icon: "lock.shield.fill",
                title: "Privacy Policy",
                color: .textPrimary
            ) {
                // Open privacy policy
            }

            ActionButton(
                icon: "questionmark.circle.fill",
                title: "Help & Support",
                color: .textPrimary
            ) {
                // Open support
            }

            ActionButton(
                icon: "arrow.right.square.fill",
                title: "Logout",
                color: .error
            ) {
                viewModel.logout()
                appState.logout()
            }
        }
    }
}

// MARK: - Quick Action Button
struct QuickActionButton: View {
    let icon: String
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.title2)
                    .foregroundColor(.liyaqaBrand)

                Text(title)
                    .font(.labelSmall)
                    .foregroundColor(.textPrimary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .cardStyle()
        }
    }
}

// MARK: - Stat Box
struct StatBox: View {
    let value: String
    let label: String
    let icon: String

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundColor(.liyaqaBrand)

            Text(value)
                .font(.titleMedium)
                .foregroundColor(.textPrimary)

            Text(label)
                .font(.labelSmall)
                .foregroundColor(.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 16)
        .cardStyle()
    }
}

// MARK: - Payment Row
struct PaymentRow: View {
    let payment: Payment

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(payment.description)
                    .font(.bodyMedium)
                    .foregroundColor(.textPrimary)

                Text(payment.date, style: .date)
                    .font(.bodySmall)
                    .foregroundColor(.textSecondary)
            }

            Spacer()

            Text("$\(payment.amount, specifier: "%.2f")")
                .font(.bodyMedium)
                .foregroundColor(.textPrimary)
        }
        .padding()
        .cardStyle()
    }
}

// MARK: - Action Button
struct ActionButton: View {
    let icon: String
    let title: String
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(color)

                Text(title)
                    .font(.bodyMedium)
                    .foregroundColor(color)

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.textSecondary)
            }
            .padding()
            .cardStyle()
        }
    }
}

// MARK: - Empty Payment History
struct EmptyPaymentHistoryView: View {
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: "creditcard")
                .font(.system(size: 32))
                .foregroundColor(.textSecondary)

            Text("No payment history yet")
                .font(.bodyMedium)
                .foregroundColor(.textPrimary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 30)
        .cardStyle()
    }
}

#Preview {
    NavigationView {
        ProfileView()
            .environmentObject(AppState())
    }
}
