import SwiftUI

struct CustomButton: View {
    let title: String
    let icon: String?
    let style: ButtonStyle
    let isLoading: Bool
    let action: () -> Void

    enum ButtonStyle {
        case primary
        case secondary
        case outline
        case text
    }

    init(
        title: String,
        icon: String? = nil,
        style: ButtonStyle = .primary,
        isLoading: Bool = false,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.icon = icon
        self.style = style
        self.isLoading = isLoading
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: foregroundColor))
                } else {
                    if let icon = icon {
                        Image(systemName: icon)
                    }
                    Text(title)
                }
            }
            .font(.labelLarge)
            .foregroundColor(foregroundColor)
            .frame(maxWidth: style == .text ? nil : .infinity)
            .padding(.vertical, style == .text ? 8 : 16)
            .padding(.horizontal, style == .text ? 12 : 16)
            .background(backgroundColor)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(borderColor, lineWidth: style == .outline ? 2 : 0)
            )
        }
        .disabled(isLoading)
    }

    private var foregroundColor: Color {
        switch style {
        case .primary:
            return .onPrimary
        case .secondary:
            return .liyaqaBrand
        case .outline:
            return .liyaqaBrand
        case .text:
            return .liyaqaBrand
        }
    }

    private var backgroundColor: Color {
        switch style {
        case .primary:
            return .liyaqaBrand
        case .secondary:
            return .liyaqaBrand.opacity(0.1)
        case .outline:
            return .clear
        case .text:
            return .clear
        }
    }

    private var borderColor: Color {
        switch style {
        case .outline:
            return .liyaqaBrand
        default:
            return .clear
        }
    }
}

#Preview {
    VStack(spacing: 16) {
        CustomButton(title: "Primary Button", style: .primary) {
            print("Primary tapped")
        }

        CustomButton(title: "Secondary Button", icon: "star.fill", style: .secondary) {
            print("Secondary tapped")
        }

        CustomButton(title: "Outline Button", style: .outline) {
            print("Outline tapped")
        }

        CustomButton(title: "Text Button", style: .text) {
            print("Text tapped")
        }

        CustomButton(title: "Loading", style: .primary, isLoading: true) {
            print("Loading")
        }
    }
    .padding()
}
