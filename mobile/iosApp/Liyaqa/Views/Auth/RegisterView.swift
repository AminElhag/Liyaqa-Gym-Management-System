import SwiftUI

struct RegisterView: View {
    @StateObject private var viewModel = AuthViewModel()
    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) var dismiss
    @FocusState private var focusedField: Field?

    enum Field {
        case fullName, email, phoneNumber, password
    }

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    // Header
                    VStack(spacing: 8) {
                        Text("Create Account")
                            .font(.headlineLarge)
                            .foregroundColor(.textPrimary)

                        Text("Join Liyaqa and start your fitness journey")
                            .font(.bodyMedium)
                            .foregroundColor(.textSecondary)
                            .multilineTextAlignment(.center)
                    }
                    .padding(.top, 20)

                    // Registration Form
                    VStack(spacing: 16) {
                        // Full Name
                        TextField("Full Name", text: $viewModel.fullName)
                            .textFieldStyle(icon: "person.fill")
                            .textContentType(.name)
                            .focused($focusedField, equals: .fullName)
                            .submitLabel(.next)
                            .onSubmit {
                                focusedField = .email
                            }

                        // Email
                        TextField("Email", text: $viewModel.email)
                            .textFieldStyle(icon: "envelope.fill")
                            .keyboardType(.emailAddress)
                            .textContentType(.emailAddress)
                            .autocapitalization(.none)
                            .focused($focusedField, equals: .email)
                            .submitLabel(.next)
                            .onSubmit {
                                focusedField = .phoneNumber
                            }

                        // Phone Number
                        TextField("Phone Number (Optional)", text: $viewModel.phoneNumber)
                            .textFieldStyle(icon: "phone.fill")
                            .keyboardType(.phonePad)
                            .textContentType(.telephoneNumber)
                            .focused($focusedField, equals: .phoneNumber)
                            .submitLabel(.next)
                            .onSubmit {
                                focusedField = .password
                            }

                        // Password
                        SecureField("Password", text: $viewModel.password)
                            .textFieldStyle(icon: "lock.fill")
                            .textContentType(.newPassword)
                            .focused($focusedField, equals: .password)
                            .submitLabel(.done)
                            .onSubmit {
                                Task {
                                    await viewModel.register()
                                }
                            }

                        // Password Requirements
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Password must:")
                                .font(.labelSmall)
                                .foregroundColor(.textSecondary)

                            HStack {
                                Image(systemName: viewModel.password.count >= 8 ? "checkmark.circle.fill" : "circle")
                                    .foregroundColor(viewModel.password.count >= 8 ? .success : .textSecondary)
                                    .font(.caption)

                                Text("Be at least 8 characters long")
                                    .font(.labelSmall)
                                    .foregroundColor(.textSecondary)
                            }
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 4)
                    }
                    .padding(.horizontal)

                    // Error Message
                    if let errorMessage = viewModel.errorMessage {
                        Text(errorMessage)
                            .font(.bodySmall)
                            .foregroundColor(.error)
                            .padding(.horizontal)
                    }

                    // Terms and Conditions
                    Text("By signing up, you agree to our [Terms of Service](https://liyaqa.com/terms) and [Privacy Policy](https://liyaqa.com/privacy)")
                        .font(.bodySmall)
                        .foregroundColor(.textSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)

                    // Register Button
                    Button {
                        Task {
                            await viewModel.register()
                            if viewModel.isAuthenticated {
                                appState.isAuthenticated = true
                                dismiss()
                            }
                        }
                    } label: {
                        Text("Create Account")
                            .primaryButtonStyle(isEnabled: !viewModel.isLoading)
                    }
                    .disabled(viewModel.isLoading)
                    .padding(.horizontal)

                    // Divider
                    HStack {
                        Rectangle()
                            .fill(Color.outline.opacity(0.3))
                            .frame(height: 1)
                        Text("OR")
                            .font(.bodySmall)
                            .foregroundColor(.textSecondary)
                        Rectangle()
                            .fill(Color.outline.opacity(0.3))
                            .frame(height: 1)
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 8)

                    // Sign up with Apple
                    SignUpWithAppleButton()
                        .frame(height: 56)
                        .cornerRadius(12)
                        .padding(.horizontal)

                    // Login Link
                    HStack {
                        Text("Already have an account?")
                            .font(.bodyMedium)
                            .foregroundColor(.textSecondary)

                        Button {
                            dismiss()
                        } label: {
                            Text("Sign In")
                                .font(.labelLarge)
                                .foregroundColor(.liyaqaBrand)
                        }
                    }
                    .padding(.top, 8)
                    .padding(.bottom, 20)
                }
            }
            .background(Color.background)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "xmark")
                            .foregroundColor(.textPrimary)
                    }
                }
            }
            .loading(viewModel.isLoading)
        }
    }
}

// MARK: - Sign up with Apple Button
struct SignUpWithAppleButton: View {
    var body: some View {
        Button {
            // Implement Sign up with Apple
        } label: {
            HStack {
                Image(systemName: "applelogo")
                    .font(.title3)
                Text("Continue with Apple")
                    .font(.labelLarge)
            }
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .background(Color.black)
            .cornerRadius(12)
        }
    }
}

#Preview {
    RegisterView()
        .environmentObject(AppState())
}
