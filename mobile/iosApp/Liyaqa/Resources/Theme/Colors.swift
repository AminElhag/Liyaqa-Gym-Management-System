import SwiftUI

// MARK: - Color Extensions
extension Color {
    // MARK: - Light Theme Colors
    static let primary = Color(hex: "6750A4")
    static let onPrimary = Color.white
    static let primaryContainer = Color(hex: "EADDFF")
    static let onPrimaryContainer = Color(hex: "21005D")

    static let secondary = Color(hex: "625B71")
    static let onSecondary = Color.white
    static let secondaryContainer = Color(hex: "E8DEF8")
    static let onSecondaryContainer = Color(hex: "1D192B")

    static let tertiary = Color(hex: "7D5260")
    static let onTertiary = Color.white
    static let tertiaryContainer = Color(hex: "FFD8E4")
    static let onTertiaryContainer = Color(hex: "31111D")

    static let error = Color(hex: "B3261E")
    static let onError = Color.white
    static let errorContainer = Color(hex: "F9DEDC")
    static let onErrorContainer = Color(hex: "410E0B")

    static let background = Color(hex: "FFFBFE")
    static let onBackground = Color(hex: "1C1B1F")
    static let surface = Color(hex: "FFFBFE")
    static let onSurface = Color(hex: "1C1B1F")

    static let surfaceVariant = Color(hex: "E7E0EC")
    static let onSurfaceVariant = Color(hex: "49454F")
    static let outline = Color(hex: "79747E")
    static let outlineVariant = Color(hex: "CAC4D0")

    // MARK: - Dark Theme Colors
    static let primaryDark = Color(hex: "D0BCFF")
    static let onPrimaryDark = Color(hex: "381E72")
    static let primaryContainerDark = Color(hex: "4F378B")
    static let onPrimaryContainerDark = Color(hex: "EADDFF")

    static let secondaryDark = Color(hex: "CCC2DC")
    static let onSecondaryDark = Color(hex: "332D41")
    static let secondaryContainerDark = Color(hex: "4A4458")
    static let onSecondaryContainerDark = Color(hex: "E8DEF8")

    static let tertiaryDark = Color(hex: "EFB8C8")
    static let onTertiaryDark = Color(hex: "492532")
    static let tertiaryContainerDark = Color(hex: "633B48")
    static let onTertiaryContainerDark = Color(hex: "FFD8E4")

    static let errorDark = Color(hex: "F2B8B5")
    static let onErrorDark = Color(hex: "601410")
    static let errorContainerDark = Color(hex: "8C1D18")
    static let onErrorContainerDark = Color(hex: "F9DEDC")

    static let backgroundDark = Color(hex: "1C1B1F")
    static let onBackgroundDark = Color(hex: "E6E1E5")
    static let surfaceDark = Color(hex: "1C1B1F")
    static let onSurfaceDark = Color(hex: "E6E1E5")

    static let surfaceVariantDark = Color(hex: "49454F")
    static let onSurfaceVariantDark = Color(hex: "CAC4D0")
    static let outlineDark = Color(hex: "938F99")
    static let outlineVariantDark = Color(hex: "49454F")

    // MARK: - Custom Brand Colors
    static let liyaqaBrand = Color(hex: "00BFA5")
    static let liyaqaBrandVariant = Color(hex: "00897B")
    static let liyaqaAccent = Color(hex: "FF6F00")

    // MARK: - Status Colors
    static let success = Color(hex: "4CAF50")
    static let warning = Color(hex: "FF9800")
    static let info = Color(hex: "2196F3")

    // MARK: - Helper Initializer
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }

        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue:  Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

// MARK: - Semantic Colors
extension Color {
    static var cardBackground: Color {
        Color(uiColor: UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(Color.surfaceDark)
                : UIColor(Color.surface)
        })
    }

    static var textPrimary: Color {
        Color(uiColor: UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(Color.onSurfaceDark)
                : UIColor(Color.onSurface)
        })
    }

    static var textSecondary: Color {
        Color(uiColor: UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(Color.onSurfaceVariantDark)
                : UIColor(Color.onSurfaceVariant)
        })
    }
}
