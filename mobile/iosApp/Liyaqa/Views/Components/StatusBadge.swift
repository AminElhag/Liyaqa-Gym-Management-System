import SwiftUI

struct StatusBadge: View {
    let status: BookingStatusType

    var body: some View {
        Text(status.displayText)
            .font(.system(size: 11, weight: .semibold))
            .foregroundColor(.white)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(status.color)
            .cornerRadius(8)
    }
}

#Preview {
    VStack(spacing: 12) {
        StatusBadge(status: .available)
        StatusBadge(status: .almostFull)
        StatusBadge(status: .full)
        StatusBadge(status: .cancelled)
        StatusBadge(status: .booked)
    }
    .padding()
}
