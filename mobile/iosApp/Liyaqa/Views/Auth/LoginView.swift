import SwiftUI

struct LoginView: View {
    @StateObject private var viewModel = AuthViewModel()
    @EnvironmentObject var appState: AppState
    @State private var showingRegister = false
    @State private var showingForgotPassword = false
    @FocusState private var focusedField: Field?

    enum Field {
        case email, password
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Logo and Title
                VStack(spacing: 16) {
                    Image(systemName: "figure.run.circle.fill")
                        .font(.system(size: 80))
                        .foregroundColor(.liyaqaBrand)

                    Text("Welcome to Liyaqa")
                        .font(.headlineLarge)
                        .foregroundColor(.textPrimary)

                    Text("Your fitness journey starts here")
                        .font(.bodyMedium)
                        .foregroundColor(.textSecondary)
                }
                .padding(.top, 40)
                .padding(.bottom, 20)

                // Login Form
                VStack(spacing: 16) {
                    // Email Field
                    TextField("Email", text: $viewModel.email)
                        .textFieldStyle(icon: "envelope.fill")
                        .keyboardType(.emailAddress)
                        .textContentType(.emailAddress)
                        .autocapitalization(.none)
                        .focused($focusedField, equals: .email)
                        .submitLabel(.next)
                        .onSubmit {
                            focusedField = .password
                        }

                    // Password Field
                    SecureField("Password", text: $viewModel.password)
                        .textFieldStyle(icon: "lock.fill")
                        .textContentType(.password)
                        .focused($focusedField, equals: .password)
                        .submitLabel(.done)
                        .onSubmit {
                            Task {
                                await viewModel.login()
                            }
                        }

                    // Forgot Password
                    HStack {
                        Spacer()
                        Button {
                            showingForgotPassword = true
                        } label: {
                            Text("Forgot Password?")
                                .font(.bodySmall)
                                .foregroundColor(.liyaqaBrand)
                        }
                    }
                }
                .padding(.horizontal)

                // Error Message
                if let errorMessage = viewModel.errorMessage {
                    Text(errorMessage)
                        .font(.bodySmall)
                        .foregroundColor(.error)
                        .padding(.horizontal)
                }

                // Login Button
                Button {
                    Task {
                        await viewModel.login()
                        if viewModel.isAuthenticated {
                            appState.isAuthenticated = true
                        }
                    }
                } label: {
                    Text("Sign In")
                        .primaryButtonStyle(isEnabled: !viewModel.isLoading)
                }
                .disabled(viewModel.isLoading)
                .padding(.horizontal)

                // Biometric Login
                if BiometricAuthService.shared.biometricType != .none {
                    Button {
                        Task {
                            await viewModel.loginWithBiometrics()
                            if viewModel.isAuthenticated {
                                appState.isAuthenticated = true
                            }
                        }
                    } label: {
                        HStack {
                            Image(systemName: BiometricAuthService.shared.biometricType == .faceID ? "faceid" : "touchid")
                            Text("Sign in with \(BiometricAuthService.shared.biometricType == .faceID ? "Face ID" : "Touch ID")")
                        }
                        .secondaryButtonStyle()
                    }
                    .padding(.horizontal)
                }

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

                // Sign in with Apple
                SignInWithAppleButton()
                    .frame(height: 56)
                    .cornerRadius(12)
                    .padding(.horizontal)

                // Register Link
                HStack {
                    Text("Don't have an account?")
                        .font(.bodyMedium)
                        .foregroundColor(.textSecondary)

                    Button {
                        showingRegister = true
                    } label: {
                        Text("Sign Up")
                            .font(.labelLarge)
                            .foregroundColor(.liyaqaBrand)
                    }
                }
                .padding(.top, 8)

                Spacer()
            }
        }
        .background(Color.background)
        .loading(viewModel.isLoading)
        .sheet(isPresented: $showingRegister) {
            RegisterView()
        }
        .sheet(isPresented: $showingForgotPassword) {
            ForgotPasswordView()
        }
    }
}

// MARK: - Forgot Password View
struct ForgotPasswordView: View {
    @StateObject private var viewModel = AuthViewModel()
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                Text("Enter your email address and we'll send you a link to reset your password")
                    .font(.bodyMedium)
                    .foregroundColor(.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding()

                TextField("Email", text: $viewModel.email)
                    .textFieldStyle(icon: "envelope.fill")
                    .keyboardType(.emailAddress)
                    .textContentType(.emailAddress)
                    .autocapitalization(.none)
                    .padding(.horizontal)

                if let errorMessage = viewModel.errorMessage {
                    Text(errorMessage)
                        .font(.bodySmall)
                        .foregroundColor(errorMessage.contains("sent") ? .success : .error)
                        .padding(.horizontal)
                }

                Button {
                    Task {
                        await viewModel.resetPassword()
                    }
                } label: {
                    Text("Reset Password")
                        .primaryButtonStyle(isEnabled: !viewModel.isLoading)
                }
                .disabled(viewModel.isLoading)
                .padding(.horizontal)

                Spacer()
            }
            .padding(.top)
            .navigationTitle("Reset Password")
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
        }
    }
}

// MARK: - Sign in with Apple Button
struct SignInWithAppleButton: View {
    var body: some View {
        Button {
            // Implement Sign in with Apple
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
    LoginView()
        .environmentObject(AppState())
}
