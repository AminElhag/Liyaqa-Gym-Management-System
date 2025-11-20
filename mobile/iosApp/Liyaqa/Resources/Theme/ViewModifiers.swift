import SwiftUI

// MARK: - Card Modifier
struct CardModifier: ViewModifier {
    var backgroundColor: Color = .cardBackground
    var cornerRadius: CGFloat = 12
    var shadowRadius: CGFloat = 4

    func body(content: Content) -> some View {
        content
            .background(backgroundColor)
            .cornerRadius(cornerRadius)
            .shadow(color: Color.black.opacity(0.1), radius: shadowRadius, x: 0, y: 2)
    }
}

extension View {
    func cardStyle(
        backgroundColor: Color = .cardBackground,
        cornerRadius: CGFloat = 12,
        shadowRadius: CGFloat = 4
    ) -> some View {
        modifier(CardModifier(
            backgroundColor: backgroundColor,
            cornerRadius: cornerRadius,
            shadowRadius: shadowRadius
        ))
    }
}

// MARK: - Primary Button Modifier
struct PrimaryButtonModifier: ViewModifier {
    var isEnabled: Bool = true

    func body(content: Content) -> some View {
        content
            .font(.labelLarge)
            .foregroundColor(.onPrimary)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .background(isEnabled ? Color.liyaqaBrand : Color.gray)
            .cornerRadius(12)
    }
}

extension View {
    func primaryButtonStyle(isEnabled: Bool = true) -> some View {
        modifier(PrimaryButtonModifier(isEnabled: isEnabled))
    }
}

// MARK: - Secondary Button Modifier
struct SecondaryButtonModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(.labelLarge)
            .foregroundColor(.liyaqaBrand)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .background(Color.clear)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.liyaqaBrand, lineWidth: 2)
            )
    }
}

extension View {
    func secondaryButtonStyle() -> some View {
        modifier(SecondaryButtonModifier())
    }
}

// MARK: - Text Field Modifier
struct TextFieldModifier: ViewModifier {
    var icon: String?

    func body(content: Content) -> some View {
        HStack(spacing: 12) {
            if let icon = icon {
                Image(systemName: icon)
                    .foregroundColor(.textSecondary)
                    .frame(width: 20)
            }
            content
        }
        .padding()
        .background(Color.surfaceVariant.opacity(0.5))
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.outline.opacity(0.5), lineWidth: 1)
        )
    }
}

extension View {
    func textFieldStyle(icon: String? = nil) -> some View {
        modifier(TextFieldModifier(icon: icon))
    }
}

// MARK: - Loading Modifier
struct LoadingModifier: ViewModifier {
    var isLoading: Bool

    func body(content: Content) -> some View {
        ZStack {
            content
                .disabled(isLoading)
                .blur(radius: isLoading ? 2 : 0)

            if isLoading {
                ProgressView()
                    .scaleEffect(1.5)
                    .progressViewStyle(CircularProgressViewStyle(tint: .liyaqaBrand))
            }
        }
    }
}

extension View {
    func loading(_ isLoading: Bool) -> some View {
        modifier(LoadingModifier(isLoading: isLoading))
    }
}

// MARK: - Empty State Modifier
struct EmptyStateModifier: ViewModifier {
    var isEmpty: Bool
    var icon: String
    var title: String
    var message: String

    func body(content: Content) -> some View {
        ZStack {
            if isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: icon)
                        .font(.system(size: 64))
                        .foregroundColor(.textSecondary)

                    Text(title)
                        .font(.titleLarge)
                        .foregroundColor(.textPrimary)

                    Text(message)
                        .font(.bodyMedium)
                        .foregroundColor(.textSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                content
            }
        }
    }
}

extension View {
    func emptyState(
        isEmpty: Bool,
        icon: String,
        title: String,
        message: String
    ) -> some View {
        modifier(EmptyStateModifier(
            isEmpty: isEmpty,
            icon: icon,
            title: title,
            message: message
        ))
    }
}

// MARK: - Status Badge Modifier
struct StatusBadgeModifier: ViewModifier {
    enum Status {
        case active, inactive, warning, error

        var color: Color {
            switch self {
            case .active: return .success
            case .inactive: return .gray
            case .warning: return .warning
            case .error: return .error
            }
        }
    }

    var status: Status

    func body(content: Content) -> some View {
        content
            .font(.labelSmall)
            .foregroundColor(.white)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(status.color)
            .cornerRadius(12)
    }
}

extension View {
    func statusBadge(_ status: StatusBadgeModifier.Status) -> some View {
        modifier(StatusBadgeModifier(status: status))
    }
}

// MARK: - Shimmer Effect
struct ShimmerModifier: ViewModifier {
    @State private var phase: CGFloat = 0

    func body(content: Content) -> some View {
        content
            .overlay(
                LinearGradient(
                    gradient: Gradient(colors: [
                        .clear,
                        .white.opacity(0.3),
                        .clear
                    ]),
                    startPoint: .leading,
                    endPoint: .trailing
                )
                .offset(x: phase * 300)
            )
            .onAppear {
                withAnimation(
                    Animation.linear(duration: 1.5)
                        .repeatForever(autoreverses: false)
                ) {
                    phase = 1
                }
            }
    }
}

extension View {
    func shimmer() -> some View {
        modifier(ShimmerModifier())
    }
}
