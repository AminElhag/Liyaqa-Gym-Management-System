import SwiftUI

struct InfoRow: View {
    let icon: String
    let label: String
    let value: String
    var iconColor: Color = .liyaqaBrand

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.body)
                .foregroundColor(iconColor)
                .frame(width: 24)

            Text(label)
                .font(.bodyMedium)
                .foregroundColor(.textSecondary)

            Spacer()

            Text(value)
                .font(.bodyMedium)
                .fontWeight(.medium)
                .foregroundColor(.textPrimary)
        }
        .padding(.vertical, 4)
    }
}

#Preview {
    VStack(spacing: 12) {
        InfoRow(icon: "clock", label: "Duration", value: "45 min")
        InfoRow(icon: "gauge", label: "Difficulty", value: "Intermediate")
        InfoRow(icon: "person.3", label: "Spots Available", value: "5/20")
        InfoRow(icon: "calendar", label: "Date", value: "Dec 25, 2024")
    }
    .padding()
}
