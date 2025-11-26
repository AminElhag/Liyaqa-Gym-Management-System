import SwiftUI
import shared

/**
 * Branded theme for iOS app that applies tenant-specific colors.
 */
struct BrandedTheme {
    let primaryColor: Color
    let secondaryColor: Color
    let accentColor: Color
    let logo: String?
    let appName: String

    /**
     * Initialize theme from tenant branding
     */
    init(branding: TenantBranding?) {
        if let branding = branding {
            self.primaryColor = Color(hex: branding.brandColors.primaryColor) ?? .blue
            self.secondaryColor = Color(hex: branding.brandColors.secondaryColor) ?? .pink
            self.accentColor = Color(hex: branding.brandColors.accentColor) ?? .purple
            self.logo = branding.logo
            self.appName = branding.tenantName
        } else {
            // Default theme
            self.primaryColor = .blue
            self.secondaryColor = .pink
            self.accentColor = .purple
            self.logo = nil
            self.appName = "Liyaqa"
        }
    }

    /**
     * Initialize theme with custom colors for preview/testing
     */
    init(
        primaryColor: Color = .blue,
        secondaryColor: Color = .pink,
        accentColor: Color = .purple,
        logo: String? = nil,
        appName: String = "Liyaqa"
    ) {
        self.primaryColor = primaryColor
        self.secondaryColor = secondaryColor
        self.accentColor = accentColor
        self.logo = logo
        self.appName = appName
    }
}

/**
 * View modifier that applies tenant branding to a view
 */
struct BrandedThemeModifier: ViewModifier {
    let theme: BrandedTheme

    func body(content: Content) -> some View {
        content
            .tint(theme.primaryColor)
            .accentColor(theme.accentColor)
    }
}

extension View {
    /**
     * Apply branded theme to a view
     */
    func brandedTheme(_ theme: BrandedTheme) -> some View {
        self.modifier(BrandedThemeModifier(theme: theme))
    }
}

/**
 * Color extension to parse hex color strings
 */
extension Color {
    init?(hex: String) {
        var hexSanitized = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        hexSanitized = hexSanitized.replacingOccurrences(of: "#", with: "")

        var rgb: UInt64 = 0

        guard Scanner(string: hexSanitized).scanHexInt64(&rgb) else {
            return nil
        }

        let length = hexSanitized.count

        let r, g, b, a: Double
        if length == 6 {
            r = Double((rgb & 0xFF0000) >> 16) / 255.0
            g = Double((rgb & 0x00FF00) >> 8) / 255.0
            b = Double(rgb & 0x0000FF) / 255.0
            a = 1.0
        } else if length == 8 {
            r = Double((rgb & 0xFF000000) >> 24) / 255.0
            g = Double((rgb & 0x00FF0000) >> 16) / 255.0
            b = Double((rgb & 0x0000FF00) >> 8) / 255.0
            a = Double(rgb & 0x000000FF) / 255.0
        } else {
            return nil
        }

        self.init(.sRGB, red: r, green: g, blue: b, opacity: a)
    }
}

/**
 * Environment key for branded theme
 */
struct BrandedThemeKey: EnvironmentKey {
    static let defaultValue: BrandedTheme = BrandedTheme()
}

extension EnvironmentValues {
    var brandedTheme: BrandedTheme {
        get { self[BrandedThemeKey.self] }
        set { self[BrandedThemeKey.self] = newValue }
    }
}

/**
 * Preview provider for testing branded theme
 */
struct BrandedTheme_Previews: PreviewProvider {
    static var previews: some View {
        let customTheme = BrandedTheme(
            primaryColor: Color(hex: "#1976D2") ?? .blue,
            secondaryColor: Color(hex: "#DC004E") ?? .pink,
            accentColor: Color(hex: "#F50057") ?? .purple,
            appName: "Gold Gym"
        )

        VStack(spacing: 20) {
            Text("Primary Color")
                .foregroundColor(customTheme.primaryColor)

            Text("Secondary Color")
                .foregroundColor(customTheme.secondaryColor)

            Text("Accent Color")
                .foregroundColor(customTheme.accentColor)

            Button("Test Button") {
                print("Button tapped")
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
        .brandedTheme(customTheme)
        .environment(\.brandedTheme, customTheme)
    }
}
