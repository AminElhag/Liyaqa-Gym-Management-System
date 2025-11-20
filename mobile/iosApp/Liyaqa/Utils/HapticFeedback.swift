import UIKit

/// Provides haptic feedback for various user interactions
struct HapticFeedback {

    // MARK: - Notification Feedback

    /// Triggers success haptic feedback
    static func success() {
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.success)
    }

    /// Triggers error haptic feedback
    static func error() {
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.error)
    }

    /// Triggers warning haptic feedback
    static func warning() {
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.warning)
    }

    // MARK: - Impact Feedback

    /// Triggers light impact haptic feedback
    static func lightImpact() {
        let generator = UIImpactFeedbackGenerator(style: .light)
        generator.impactOccurred()
    }

    /// Triggers medium impact haptic feedback
    static func mediumImpact() {
        let generator = UIImpactFeedbackGenerator(style: .medium)
        generator.impactOccurred()
    }

    /// Triggers heavy impact haptic feedback
    static func heavyImpact() {
        let generator = UIImpactFeedbackGenerator(style: .heavy)
        generator.impactOccurred()
    }

    /// Triggers rigid impact haptic feedback (iOS 13+)
    @available(iOS 13.0, *)
    static func rigidImpact() {
        let generator = UIImpactFeedbackGenerator(style: .rigid)
        generator.impactOccurred()
    }

    /// Triggers soft impact haptic feedback (iOS 13+)
    @available(iOS 13.0, *)
    static func softImpact() {
        let generator = UIImpactFeedbackGenerator(style: .soft)
        generator.impactOccurred()
    }

    // MARK: - Selection Feedback

    /// Triggers selection change haptic feedback
    static func selectionChanged() {
        let generator = UISelectionFeedbackGenerator()
        generator.selectionChanged()
    }
}
