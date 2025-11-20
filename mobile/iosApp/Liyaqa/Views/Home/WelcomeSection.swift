import SwiftUI

/// Welcome section displaying member name and current date
struct WelcomeSection: View {
    let memberName: String

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text("Hello, \(memberName)!")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.textPrimary)

                Text(Date().formatted(date: .long, time: .omitted))
                    .font(.subheadline)
                    .foregroundColor(.textSecondary)
            }
            Spacer()
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

#Preview {
    VStack(spacing: 20) {
        WelcomeSection(memberName: "Ahmed")
        WelcomeSection(memberName: "Sarah Johnson")
    }
    .padding()
}
