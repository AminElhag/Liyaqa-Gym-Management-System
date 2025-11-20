import Foundation
import Combine
import CoreImage
import UIKit
import CoreNFC
import shared

@MainActor
class CheckInViewModel: NSObject, ObservableObject {
    // MARK: - Published Properties
    @Published var qrCodeImage: UIImage?
    @Published var validityTime = "30s"
    @Published var checkInStatus: CheckInStatus?
    @Published var isLoading = false
    @Published var nextBooking: shared.Booking?
    @Published var nfcSupported = false
    @Published var nfcReading = false

    // MARK: - Private Properties
    private var timer: Timer?
    private var countdownTimer: Timer?
    private var remainingSeconds = 30
    private let checkInUseCase: CheckInUseCase
    private let bookingRepository: BookingRepository
    private var nfcSession: NFCNDEFReaderSession?
    private var qrDataCache: String?

    // MARK: - Initialization
    init(
        checkInUseCase: CheckInUseCase? = nil,
        bookingRepository: BookingRepository? = nil
    ) {
        self.checkInUseCase = checkInUseCase ?? KoinHelper.shared.getCheckInUseCase()
        self.bookingRepository = bookingRepository ?? KoinHelper.shared.getBookingRepository()
        super.init()

        // Check NFC availability
        if #available(iOS 13.0, *) {
            nfcSupported = NFCNDEFReaderSession.readingAvailable
        }
    }

    // MARK: - Public Methods

    /// Loads the next upcoming booking
    func loadNextBooking() async {
        isLoading = true
        defer { isLoading = false }

        guard let memberId = getMemberId() else {
            checkInStatus = .failure("Please log in to check in")
            return
        }

        do {
            let result = try await bookingRepository.getMyBookings(
                memberId: memberId,
                forceRefresh: true
            )

            if let bookings = try? result.getOrThrow() {
                // Find the next confirmed booking that can be checked in
                nextBooking = bookings
                    .filter { $0.status == .confirmed && $0.canCheckIn() }
                    .sorted { $0.schedule.startDateTime < $1.schedule.startDateTime }
                    .first

                if nextBooking != nil {
                    generateQRCode()
                } else {
                    checkInStatus = .failure("No upcoming bookings available for check-in")
                }
            }
        } catch {
            checkInStatus = .failure("Failed to load bookings: \(error.localizedDescription)")
        }
    }

    /// Generates a QR code with member ID, timestamp, and token
    func generateQRCode() {
        guard let booking = nextBooking else {
            checkInStatus = .failure("No booking available for check-in")
            return
        }

        // Create QR code data with booking ID, timestamp, and token
        let qrData = createQRData(bookingId: booking.id)
        qrDataCache = qrData

        // Generate QR code image using CoreImage
        let filter = CIFilter(name: "CIQRCodeGenerator")
        filter?.setValue(qrData.data(using: .utf8), forKey: "inputMessage")
        filter?.setValue("H", forKey: "inputCorrectionLevel")

        if let outputImage = filter?.outputImage {
            let context = CIContext()
            let transform = CGAffineTransform(scaleX: 10, y: 10)
            let scaledImage = outputImage.transformed(by: transform)

            if let cgImage = context.createCGImage(scaledImage, from: scaledImage.extent) {
                qrCodeImage = UIImage(cgImage: cgImage)
            }
        }

        // Start refresh timer (refresh every 30 seconds)
        startRefreshTimer()
    }

    /// Performs manual check-in for the next booking
    func manualCheckIn() {
        guard let booking = nextBooking else {
            checkInStatus = .failure("No booking available for check-in")
            HapticFeedback.error()
            return
        }

        Task {
            isLoading = true
            defer { isLoading = false }

            do {
                let result = try await checkInUseCase.invoke(bookingId: booking.id)

                if let _ = try? result.getOrThrow() {
                    checkInStatus = .success("Checked in successfully!")
                    HapticFeedback.success()

                    // Clear QR code after successful check-in
                    qrCodeImage = nil
                    timer?.invalidate()
                    countdownTimer?.invalidate()

                    // Reload bookings to get updated status
                    await loadNextBooking()
                } else if case .failure(let error) = result {
                    let errorMessage = extractErrorMessage(from: error)
                    checkInStatus = .failure(errorMessage)
                    HapticFeedback.error()
                }
            } catch {
                checkInStatus = .failure("Check-in failed: \(error.localizedDescription)")
                HapticFeedback.error()
            }
        }
    }

    /// Starts NFC reading session for check-in
    @available(iOS 13.0, *)
    func startNFCReading() {
        guard nfcSupported else {
            checkInStatus = .failure("NFC is not supported on this device")
            HapticFeedback.error()
            return
        }

        guard let booking = nextBooking else {
            checkInStatus = .failure("No booking available for check-in")
            HapticFeedback.error()
            return
        }

        nfcReading = true
        nfcSession = NFCNDEFReaderSession(delegate: self, queue: nil, invalidateAfterFirstRead: true)
        nfcSession?.alertMessage = "Hold your device near the NFC reader at the gym entrance"
        nfcSession?.begin()
    }

    /// Cleans up resources
    func cleanup() {
        timer?.invalidate()
        countdownTimer?.invalidate()
        timer = nil
        countdownTimer = nil
        if #available(iOS 13.0, *) {
            nfcSession?.invalidate()
            nfcSession = nil
        }
    }

    // MARK: - Private Methods

    /// Creates QR code data string
    private func createQRData(bookingId: String) -> String {
        let timestamp = Date().timeIntervalSince1970
        let token = generateSecurityToken()

        // Format: bookingId|timestamp|token
        return "\(bookingId)|\(Int(timestamp))|\(token)"
    }

    /// Generates a security token for QR code validation
    private func generateSecurityToken() -> String {
        // In production, this should be a proper JWT or signed token from the backend
        let randomData = Data((0..<16).map { _ in UInt8.random(in: 0...255) })
        return randomData.base64EncodedString()
    }

    /// Starts the QR code refresh timer
    private func startRefreshTimer() {
        // Invalidate existing timers
        timer?.invalidate()
        countdownTimer?.invalidate()

        // Reset countdown
        remainingSeconds = 30
        updateValidityTime()

        // Timer to regenerate QR code every 30 seconds
        timer = Timer.scheduledTimer(withTimeInterval: 30, repeats: true) { [weak self] _ in
            self?.generateQRCode()
        }

        // Timer to update countdown every second
        countdownTimer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] _ in
            guard let self = self else { return }
            self.remainingSeconds -= 1

            if self.remainingSeconds <= 0 {
                self.remainingSeconds = 30
            }

            self.updateValidityTime()
        }
    }

    /// Updates the validity time display
    private func updateValidityTime() {
        validityTime = "\(remainingSeconds)s"
    }

    /// Gets the current member ID from authentication
    private func getMemberId() -> String? {
        // TODO: Get actual member ID from authentication
        // For now, return a mock ID
        return "mock-member-id"
    }

    /// Extracts a user-friendly error message from the error
    private func extractErrorMessage(from error: Error) -> String {
        // Try to extract message from Kotlin exception
        if let kotlinError = error as? KotlinException {
            return kotlinError.message ?? "Check-in failed"
        }
        return error.localizedDescription
    }

    // MARK: - Deinit
    deinit {
        cleanup()
    }
}

// MARK: - NFCNDEFReaderSessionDelegate
@available(iOS 13.0, *)
extension CheckInViewModel: NFCNDEFReaderSessionDelegate {
    func readerSession(_ session: NFCNDEFReaderSession, didInvalidateWithError error: Error) {
        DispatchQueue.main.async {
            self.nfcReading = false

            // Only show error if it's not a user cancellation
            if let nfcError = error as? NFCReaderError,
               nfcError.code != .readerSessionInvalidationErrorUserCanceled {
                self.checkInStatus = .failure("NFC reading failed: \(error.localizedDescription)")
                HapticFeedback.error()
            }
        }
    }

    func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        guard let message = messages.first,
              let record = message.records.first,
              let payloadString = String(data: record.payload, encoding: .utf8) else {
            DispatchQueue.main.async {
                self.checkInStatus = .failure("Invalid NFC tag data")
                HapticFeedback.error()
                self.nfcReading = false
            }
            return
        }

        // Process the NFC tag data (should contain gym location identifier)
        // In production, validate the location against the booking's gym
        DispatchQueue.main.async {
            session.alertMessage = "NFC tag detected! Checking in..."
            session.invalidate()

            // Perform check-in
            self.manualCheckIn()
            self.nfcReading = false
        }
    }
}
