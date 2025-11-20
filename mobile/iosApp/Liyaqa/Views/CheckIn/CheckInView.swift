import SwiftUI
import shared

struct CheckInView: View {
    @StateObject private var viewModel = CheckInViewModel()
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationView {
            ZStack {
                // Background
                Color.backgroundPrimary
                    .ignoresSafeArea()

                if viewModel.isLoading && viewModel.nextBooking == nil {
                    // Loading state
                    LoadingView()
                } else if let booking = viewModel.nextBooking {
                    // Check-in content
                    checkInContent(for: booking)
                } else {
                    // No bookings available
                    emptyStateView
                }
            }
            .navigationTitle("Check In")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.textSecondary)
                    }
                }
            }
            .onAppear {
                Task {
                    await viewModel.loadNextBooking()
                }
            }
            .onDisappear {
                viewModel.cleanup()
            }
        }
    }

    // MARK: - Check-In Content
    private func checkInContent(for booking: shared.Booking) -> some View {
        ScrollView {
            VStack(spacing: 30) {
                // Class info card
                classInfoCard(for: booking)

                // QR Code section
                qrCodeSection

                // Action buttons
                actionButtons

                // Check-in status
                if let status = viewModel.checkInStatus {
                    statusView(status)
                }

                Spacer(minLength: 20)
            }
            .padding()
        }
        .refreshable {
            await viewModel.loadNextBooking()
        }
    }

    // MARK: - Class Info Card
    private func classInfoCard(for booking: shared.Booking) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            // Class name
            Text(booking.schedule.classType.name)
                .font(.headlineLarge)
                .foregroundColor(.textPrimary)

            // Instructor
            HStack {
                Image(systemName: "person.fill")
                    .foregroundColor(.liyaqaBrand)
                Text(booking.schedule.instructor.name)
                    .font(.bodyMedium)
                    .foregroundColor(.textSecondary)
            }

            // Date and time
            HStack {
                Image(systemName: "calendar")
                    .foregroundColor(.liyaqaBrand)
                Text(booking.schedule.startDateTime.formatted(date: .abbreviated, time: .shortened))
                    .font(.bodyMedium)
                    .foregroundColor(.textSecondary)
            }

            // Duration
            HStack {
                Image(systemName: "clock.fill")
                    .foregroundColor(.liyaqaBrand)
                Text("\(booking.schedule.duration) minutes")
                    .font(.bodyMedium)
                    .foregroundColor(.textSecondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color.surfacePrimary)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.05), radius: 4, x: 0, y: 2)
    }

    // MARK: - QR Code Section
    private var qrCodeSection: some View {
        VStack(spacing: 16) {
            Text("Scan to Check In")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(.textPrimary)

            Text("Show this QR code at the gym entrance")
                .font(.bodyMedium)
                .foregroundColor(.textSecondary)
                .multilineTextAlignment(.center)

            // QR Code
            ZStack {
                if let qrImage = viewModel.qrCodeImage {
                    Image(uiImage: qrImage)
                        .interpolation(.none)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 280, height: 280)
                        .padding(20)
                        .background(Color.white)
                        .cornerRadius(16)
                        .shadow(color: Color.black.opacity(0.1), radius: 8, x: 0, y: 4)
                } else {
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color.surfacePrimary)
                        .frame(width: 280, height: 280)
                        .overlay(
                            ProgressView()
                                .scaleEffect(1.5)
                        )
                }
            }

            // Validity timer
            HStack(spacing: 6) {
                Image(systemName: "clock.fill")
                    .font(.caption)
                    .foregroundColor(.success)
                Text("Valid for: \(viewModel.validityTime)")
                    .font(.caption)
                    .foregroundColor(.textSecondary)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(Color.success.opacity(0.1))
            .cornerRadius(20)
        }
        .padding(.vertical)
    }

    // MARK: - Action Buttons
    private var actionButtons: some View {
        VStack(spacing: 16) {
            // Manual check-in button
            Button {
                viewModel.manualCheckIn()
            } label: {
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.body)
                    Text("Check In Now")
                        .font(.bodyLarge)
                        .fontWeight(.semibold)
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.liyaqaBrand)
                .foregroundColor(.white)
                .cornerRadius(12)
            }
            .disabled(viewModel.isLoading)

            // NFC check-in button (iOS 13+)
            if #available(iOS 13.0, *), viewModel.nfcSupported {
                Button {
                    viewModel.startNFCReading()
                } label: {
                    HStack {
                        Image(systemName: "wave.3.right")
                            .font(.body)
                        Text(viewModel.nfcReading ? "Reading NFC..." : "Check In with NFC")
                            .font(.bodyLarge)
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(viewModel.nfcReading ? Color.gray : Color.liyaqaBrandAccent)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
                .disabled(viewModel.isLoading || viewModel.nfcReading)
            }
        }
        .padding(.horizontal)
    }

    // MARK: - Status View
    private func statusView(_ status: CheckInStatus) -> some View {
        HStack(spacing: 12) {
            Image(systemName: status.success ? "checkmark.circle.fill" : "xmark.circle.fill")
                .font(.title3)
                .foregroundColor(status.success ? .success : .error)

            Text(status.message)
                .font(.bodyMedium)
                .foregroundColor(status.success ? .success : .error)
                .multilineTextAlignment(.leading)

            Spacer()
        }
        .padding()
        .background(status.success ? Color.success.opacity(0.1) : Color.error.opacity(0.1))
        .cornerRadius(12)
        .padding(.horizontal)
        .transition(.scale.combined(with: .opacity))
        .animation(.spring(), value: status.success)
    }

    // MARK: - Empty State
    private var emptyStateView: some View {
        VStack(spacing: 24) {
            Image(systemName: "calendar.badge.exclamationmark")
                .font(.system(size: 72))
                .foregroundColor(.liyaqaBrand)

            VStack(spacing: 8) {
                Text("No Upcoming Bookings")
                    .font(.headlineLarge)
                    .foregroundColor(.textPrimary)

                Text("You don't have any confirmed bookings available for check-in at this time.")
                    .font(.bodyMedium)
                    .foregroundColor(.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }

            Button {
                // Navigate to classes to book
                dismiss()
            } label: {
                Text("Browse Classes")
                    .font(.bodyLarge)
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.liyaqaBrand)
                    .foregroundColor(.white)
                    .cornerRadius(12)
            }
            .padding(.horizontal, 32)
        }
        .padding()
    }
}

// MARK: - Preview
#Preview {
    CheckInView()
}
