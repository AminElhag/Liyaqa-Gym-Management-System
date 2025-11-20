import SwiftUI

struct EmptyStateView: View {
    let message: String
    var icon: String = "calendar.badge.exclamationmark"
    var actionTitle: String? = nil
    var action: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: icon)
                .font(.system(size: 64))
                .foregroundColor(.textSecondary)

            Text(message)
                .font(.bodyMedium)
                .foregroundColor(.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            if let actionTitle = actionTitle, let action = action {
                Button(action: action) {
                    Text(actionTitle)
                        .font(.labelMedium)
                        .foregroundColor(.liyaqaBrand)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(Color.liyaqaBrand.opacity(0.1))
                        .cornerRadius(8)
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding()
    }
}

#Preview {
    VStack {
        EmptyStateView(
            message: "No classes scheduled for this date",
            icon: "calendar.badge.exclamationmark"
        )

        Divider()

        EmptyStateView(
            message: "No results found",
            icon: "magnifyingglass",
            actionTitle: "Clear Filters",
            action: { print("Clear filters") }
        )
    }
}
