import SwiftUI

struct SettingsView: View {
    @StateObject private var viewModel = ProfileViewModel()
    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationView {
            List {
                // Account Section
                Section {
                    NavigationLink {
                        EditProfileView()
                    } label: {
                        SettingRow(
                            icon: "person.fill",
                            title: "Edit Profile",
                            iconColor: .liyaqaBrand
                        )
                    }

                    NavigationLink {
                        ChangePasswordView()
                    } label: {
                        SettingRow(
                            icon: "lock.fill",
                            title: "Change Password",
                            iconColor: .liyaqaBrand
                        )
                    }
                } header: {
                    Text("Account")
                }

                // Preferences Section
                Section {
                    Toggle(isOn: $viewModel.notificationsEnabled) {
                        SettingRow(
                            icon: "bell.fill",
                            title: "Push Notifications",
                            iconColor: .info
                        )
                    }
                    .onChange(of: viewModel.notificationsEnabled) { newValue in
                        viewModel.toggleNotifications(newValue)
                    }

                    Toggle(isOn: $viewModel.biometricEnabled) {
                        SettingRow(
                            icon: BiometricAuthService.shared.biometricType == .faceID ? "faceid" : "touchid",
                            title: BiometricAuthService.shared.biometricType == .faceID ? "Face ID" : "Touch ID",
                            iconColor: .success
                        )
                    }
                    .onChange(of: viewModel.biometricEnabled) { newValue in
                        viewModel.toggleBiometric(newValue)
                    }

                    Toggle(isOn: $viewModel.darkModeEnabled) {
                        SettingRow(
                            icon: "moon.fill",
                            title: "Dark Mode",
                            iconColor: .secondary
                        )
                    }
                    .onChange(of: viewModel.darkModeEnabled) { newValue in
                        viewModel.toggleDarkMode(newValue)
                        appState.toggleColorScheme()
                    }
                } header: {
                    Text("Preferences")
                }

                // HealthKit Section
                Section {
                    NavigationLink {
                        HealthKitSettingsView()
                    } label: {
                        SettingRow(
                            icon: "heart.fill",
                            title: "Health Integration",
                            iconColor: .error
                        )
                    }
                } header: {
                    Text("Health & Fitness")
                } footer: {
                    Text("Connect with Apple Health to sync your workout data")
                }

                // Support Section
                Section {
                    NavigationLink {
                        Text("FAQ") // TODO: Implement FAQ view
                    } label: {
                        SettingRow(
                            icon: "questionmark.circle.fill",
                            title: "FAQ",
                            iconColor: .info
                        )
                    }

                    NavigationLink {
                        Text("Contact Support") // TODO: Implement support view
                    } label: {
                        SettingRow(
                            icon: "envelope.fill",
                            title: "Contact Support",
                            iconColor: .liyaqaBrand
                        )
                    }

                    NavigationLink {
                        AboutView()
                    } label: {
                        SettingRow(
                            icon: "info.circle.fill",
                            title: "About",
                            iconColor: .secondary
                        )
                    }
                } header: {
                    Text("Support")
                }

                // Danger Zone
                Section {
                    Button(role: .destructive) {
                        // TODO: Implement account deletion
                    } label: {
                        SettingRow(
                            icon: "trash.fill",
                            title: "Delete Account",
                            iconColor: .error
                        )
                    }
                } header: {
                    Text("Danger Zone")
                } footer: {
                    Text("Deleting your account is permanent and cannot be undone")
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        dismiss()
                    } label: {
                        Text("Done")
                            .foregroundColor(.liyaqaBrand)
                    }
                }
            }
        }
    }
}

// MARK: - Setting Row
struct SettingRow: View {
    let icon: String
    let title: String
    let iconColor: Color

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.body)
                .foregroundColor(iconColor)
                .frame(width: 24)

            Text(title)
                .font(.bodyMedium)
                .foregroundColor(.textPrimary)
        }
    }
}

// MARK: - Edit Profile View
struct EditProfileView: View {
    @Environment(\.dismiss) var dismiss
    @State private var fullName = ""
    @State private var phoneNumber = ""
    @State private var email = ""

    var body: some View {
        NavigationView {
            Form {
                Section {
                    TextField("Full Name", text: $fullName)
                    TextField("Phone Number", text: $phoneNumber)
                        .keyboardType(.phonePad)
                    TextField("Email", text: $email)
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                } header: {
                    Text("Personal Information")
                }
            }
            .navigationTitle("Edit Profile")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }

                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        // TODO: Save profile changes
                        dismiss()
                    }
                    .fontWeight(.semibold)
                }
            }
        }
    }
}

// MARK: - Change Password View
struct ChangePasswordView: View {
    @Environment(\.dismiss) var dismiss
    @State private var currentPassword = ""
    @State private var newPassword = ""
    @State private var confirmPassword = ""

    var body: some View {
        Form {
            Section {
                SecureField("Current Password", text: $currentPassword)
                SecureField("New Password", text: $newPassword)
                SecureField("Confirm New Password", text: $confirmPassword)
            } header: {
                Text("Change Password")
            } footer: {
                Text("Password must be at least 8 characters long")
            }

            Section {
                Button {
                    // TODO: Change password
                    dismiss()
                } label: {
                    Text("Update Password")
                        .frame(maxWidth: .infinity)
                        .foregroundColor(.liyaqaBrand)
                }
            }
        }
        .navigationTitle("Change Password")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - HealthKit Settings View
struct HealthKitSettingsView: View {
    @State private var syncWorkouts = false
    @State private var syncHeartRate = false
    @State private var syncCalories = false

    var body: some View {
        Form {
            Section {
                Toggle("Sync Workouts", isOn: $syncWorkouts)
                Toggle("Sync Heart Rate", isOn: $syncHeartRate)
                Toggle("Sync Calories Burned", isOn: $syncCalories)
            } header: {
                Text("Data to Sync")
            } footer: {
                Text("Choose which data to sync with Apple Health")
            }

            Section {
                Button {
                    HealthKitService.shared.requestAuthorization()
                } label: {
                    Text("Request Health Permissions")
                        .frame(maxWidth: .infinity)
                        .foregroundColor(.liyaqaBrand)
                }
            }
        }
        .navigationTitle("Health Integration")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - About View
struct AboutView: View {
    var body: some View {
        List {
            Section {
                HStack {
                    Text("Version")
                    Spacer()
                    Text("1.0.0")
                        .foregroundColor(.textSecondary)
                }

                HStack {
                    Text("Build")
                    Spacer()
                    Text("1")
                        .foregroundColor(.textSecondary)
                }
            }

            Section {
                Link("Terms of Service", destination: URL(string: "https://liyaqa.com/terms")!)
                Link("Privacy Policy", destination: URL(string: "https://liyaqa.com/privacy")!)
            }

            Section {
                VStack(spacing: 8) {
                    Image(systemName: "figure.run.circle.fill")
                        .font(.system(size: 60))
                        .foregroundColor(.liyaqaBrand)

                    Text("Liyaqa")
                        .font(.titleMedium)
                        .foregroundColor(.textPrimary)

                    Text("Your fitness journey companion")
                        .font(.bodySmall)
                        .foregroundColor(.textSecondary)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical)
            }
        }
        .listStyle(.insetGrouped)
        .navigationTitle("About")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    SettingsView()
        .environmentObject(AppState())
}
