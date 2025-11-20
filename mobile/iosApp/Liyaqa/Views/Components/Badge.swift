import SwiftUI

/// Badge component for displaying notification counts
struct Badge: View {
    let count: Int

    var body: some View {
        if count > 0 {
            Text(displayCount)
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(.white)
                .frame(minWidth: 16, minHeight: 16)
                .padding(.horizontal, count > 9 ? 4 : 2)
                .background(Color.error)
                .cornerRadius(8)
                .offset(x: 8, y: -8)
        }
    }

    private var displayCount: String {
        count > 99 ? "99+" : "\(count)"
    }
}

#Preview {
    HStack(spacing: 20) {
        Image(systemName: "bell")
            .overlay(Badge(count: 1))

        Image(systemName: "bell")
            .overlay(Badge(count: 12))

        Image(systemName: "bell")
            .overlay(Badge(count: 100))
    }
}
