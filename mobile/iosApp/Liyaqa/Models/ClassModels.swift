import Foundation
import SwiftUI
import shared

// MARK: - ClassSchedule Extensions
extension shared.ClassSchedule: Identifiable {}

extension shared.ClassSchedule {
    var timeRange: String {
        let start = convertLocalDateTimeToDate(startDateTime)
        let end = convertLocalDateTimeToDate(endDateTime)

        let formatter = DateFormatter()
        formatter.timeStyle = .short

        return "\(formatter.string(from: start)) - \(formatter.string(from: end))"
    }

    var formattedDate: String {
        let date = convertLocalDateTimeToDate(startDateTime)
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        return formatter.string(from: date)
    }

    var duration: String {
        let start = convertLocalDateTimeToDate(startDateTime)
        let end = convertLocalDateTimeToDate(endDateTime)
        let minutes = Int(end.timeIntervalSince(start) / 60)
        return "\(minutes) min"
    }

    var bookingStatusType: BookingStatusType {
        if isCancelled {
            return .cancelled
        } else if isFull() {
            return .full
        } else if availableSpots() <= 3 {
            return .almostFull
        } else {
            return .available
        }
    }

    var classTypeEnum: ClassType {
        // Default to general if no specific mapping exists
        // You can enhance this based on actual class names or categories
        return .general
    }

    private func convertLocalDateTimeToDate(_ localDateTime: shared.LocalDateTime) -> Date {
        let calendar = Calendar.current
        var components = DateComponents()
        components.year = Int(localDateTime.year)
        components.month = Int(localDateTime.monthNumber)
        components.day = Int(localDateTime.dayOfMonth)
        components.hour = Int(localDateTime.hour)
        components.minute = Int(localDateTime.minute)
        components.second = Int(localDateTime.second)

        return calendar.date(from: components) ?? Date()
    }
}

// MARK: - Booking Extensions
extension shared.Booking: Identifiable {}

// MARK: - ClassType Enum
enum ClassType: String, CaseIterable {
    case all = "All"
    case yoga = "Yoga"
    case cardio = "Cardio"
    case strength = "Strength"
    case hiit = "HIIT"
    case pilates = "Pilates"
    case cycling = "Cycling"
    case general = "General"

    var icon: String {
        switch self {
        case .all: return "calendar"
        case .yoga: return "figure.mind.and.body"
        case .cardio: return "figure.run"
        case .strength: return "dumbbell.fill"
        case .hiit: return "flame.fill"
        case .pilates: return "figure.flexibility"
        case .cycling: return "figure.indoor.cycle"
        case .general: return "figure.walk"
        }
    }

    var color: Color {
        switch self {
        case .all: return .gray
        case .yoga: return Color(hex: "9C27B0")
        case .cardio: return Color(hex: "F44336")
        case .strength: return Color(hex: "FF9800")
        case .hiit: return Color(hex: "E91E63")
        case .pilates: return Color(hex: "673AB7")
        case .cycling: return Color(hex: "2196F3")
        case .general: return .liyaqaBrand
        }
    }
}

// MARK: - BookingStatusType Enum
enum BookingStatusType {
    case available
    case almostFull
    case full
    case cancelled
    case booked

    var displayText: String {
        switch self {
        case .available: return "Available"
        case .almostFull: return "Almost Full"
        case .full: return "Full"
        case .cancelled: return "Cancelled"
        case .booked: return "Booked"
        }
    }

    var color: Color {
        switch self {
        case .available: return .success
        case .almostFull: return .warning
        case .full: return .error
        case .cancelled: return .gray
        case .booked: return .liyaqaBrand
        }
    }
}

// MARK: - SchedulesByTime
struct SchedulesByTime: Identifiable {
    let id = UUID()
    let timeSlot: String
    let schedules: [shared.ClassSchedule]
}

// MARK: - Helper Models
struct ActivityStats {
    let classesAttended: Int
    let classesBooked: Int
    let totalWorkouts: Int
    let currentStreak: Int

    static var empty: ActivityStats {
        ActivityStats(classesAttended: 0, classesBooked: 0, totalWorkouts: 0, currentStreak: 0)
    }
}

struct BookingWithDetails: Identifiable {
    let id: String
    let className: String
    let instructorName: String?
    let startTime: Date
    let status: String
    let statusText: String
}

struct FeaturedClass: Identifiable {
    let id: String
    let name: String
    let instructorName: String?
    let startTime: Date
    let capacity: Int
    let availableSpots: Int
    let isFull: Bool
}
