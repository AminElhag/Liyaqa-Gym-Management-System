import SwiftUI

struct FilterChipsRow: View {
    @Binding var selectedFilters: Set<String>
    let availableFilters: [ClassType]
    let multiSelect: Bool

    init(
        selectedFilters: Binding<Set<String>>,
        availableFilters: [ClassType],
        multiSelect: Bool = false
    ) {
        self._selectedFilters = selectedFilters
        self.availableFilters = availableFilters
        self.multiSelect = multiSelect
    }

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(availableFilters, id: \.self) { filter in
                    FilterChip(
                        title: filter.rawValue,
                        icon: filter.icon,
                        isSelected: selectedFilters.contains(filter.rawValue)
                    ) {
                        toggleFilter(filter.rawValue)
                    }
                }
            }
            .padding(.horizontal)
        }
        .frame(height: 44)
    }

    private func toggleFilter(_ filter: String) {
        if multiSelect {
            if selectedFilters.contains(filter) {
                selectedFilters.remove(filter)
            } else {
                selectedFilters.insert(filter)
            }
        } else {
            // Single select mode
            if selectedFilters.contains(filter) && selectedFilters.count == 1 {
                // If clicking the only selected filter, do nothing (keep it selected)
                return
            } else {
                selectedFilters.removeAll()
                selectedFilters.insert(filter)
            }
        }
    }
}

// MARK: - Filter Chip (Enhanced Version)
struct FilterChip: View {
    let title: String
    let icon: String?
    let isSelected: Bool
    let action: () -> Void

    init(title: String, icon: String? = nil, isSelected: Bool, action: @escaping () -> Void) {
        self.title = title
        self.icon = icon
        self.isSelected = isSelected
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                if let icon = icon {
                    Image(systemName: icon)
                        .font(.system(size: 14))
                }

                Text(title)
                    .font(.labelMedium)
            }
            .foregroundColor(isSelected ? .white : .textPrimary)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(isSelected ? Color.liyaqaBrand : Color.surfaceVariant.opacity(0.5))
            .cornerRadius(20)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(isSelected ? Color.liyaqaBrand : Color.clear, lineWidth: 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
}

#Preview {
    VStack(spacing: 20) {
        FilterChipsRow(
            selectedFilters: .constant(["All"]),
            availableFilters: ClassType.allCases
        )

        FilterChipsRow(
            selectedFilters: .constant(["Yoga", "HIIT"]),
            availableFilters: ClassType.allCases,
            multiSelect: true
        )
    }
}
