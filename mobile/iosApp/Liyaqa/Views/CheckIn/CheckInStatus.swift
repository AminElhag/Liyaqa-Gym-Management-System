import Foundation

/// Represents the status of a check-in attempt
struct CheckInStatus {
    let success: Bool
    let message: String

    /// Creates a successful check-in status
    static func success(_ message: String = "Checked in successfully!") -> CheckInStatus {
        return CheckInStatus(success: true, message: message)
    }

    /// Creates a failed check-in status
    static func failure(_ message: String) -> CheckInStatus {
        return CheckInStatus(success: false, message: message)
    }
}
