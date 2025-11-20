import SwiftUI

// MARK: - Typography
extension Font {
    // MARK: - Display
    static let displayLarge = Font.system(size: 57, weight: .regular)
    static let displayMedium = Font.system(size: 45, weight: .regular)
    static let displaySmall = Font.system(size: 36, weight: .regular)

    // MARK: - Headline
    static let headlineLarge = Font.system(size: 32, weight: .regular)
    static let headlineMedium = Font.system(size: 28, weight: .regular)
    static let headlineSmall = Font.system(size: 24, weight: .regular)

    // MARK: - Title
    static let titleLarge = Font.system(size: 22, weight: .medium)
    static let titleMedium = Font.system(size: 16, weight: .medium)
    static let titleSmall = Font.system(size: 14, weight: .medium)

    // MARK: - Body
    static let bodyLarge = Font.system(size: 16, weight: .regular)
    static let bodyMedium = Font.system(size: 14, weight: .regular)
    static let bodySmall = Font.system(size: 12, weight: .regular)

    // MARK: - Label
    static let labelLarge = Font.system(size: 14, weight: .medium)
    static let labelMedium = Font.system(size: 12, weight: .medium)
    static let labelSmall = Font.system(size: 11, weight: .medium)
}

// MARK: - Text Styles
extension Text {
    func displayLarge() -> Text {
        self.font(.displayLarge)
    }

    func displayMedium() -> Text {
        self.font(.displayMedium)
    }

    func displaySmall() -> Text {
        self.font(.displaySmall)
    }

    func headlineLarge() -> Text {
        self.font(.headlineLarge)
    }

    func headlineMedium() -> Text {
        self.font(.headlineMedium)
    }

    func headlineSmall() -> Text {
        self.font(.headlineSmall)
    }

    func titleLarge() -> Text {
        self.font(.titleLarge)
    }

    func titleMedium() -> Text {
        self.font(.titleMedium)
    }

    func titleSmall() -> Text {
        self.font(.titleSmall)
    }

    func bodyLarge() -> Text {
        self.font(.bodyLarge)
    }

    func bodyMedium() -> Text {
        self.font(.bodyMedium)
    }

    func bodySmall() -> Text {
        self.font(.bodySmall)
    }

    func labelLarge() -> Text {
        self.font(.labelLarge)
    }

    func labelMedium() -> Text {
        self.font(.labelMedium)
    }

    func labelSmall() -> Text {
        self.font(.labelSmall)
    }
}
