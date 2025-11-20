import SwiftUI

struct ClassListView: View {
    @StateObject private var viewModel = ClassViewModel()
    @State private var showingFilters = false

    var body: some View {
        VStack(spacing: 0) {
            // Search Bar
            searchBar

            // Filter Chips
            filterChips

            // Class List
            classListContent
        }
        .navigationTitle("Classes")
        .navigationBarTitleDisplayMode(.large)
        .task {
            await viewModel.loadClasses()
        }
        .refreshable {
            await viewModel.refresh()
        }
    }

    // MARK: - Search Bar
    private var searchBar: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.textSecondary)

            TextField("Search classes or instructors", text: $viewModel.searchText)
                .font(.bodyMedium)

            if !viewModel.searchText.isEmpty {
                Button {
                    viewModel.searchText = ""
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.textSecondary)
                }
            }
        }
        .padding()
        .background(Color.surfaceVariant.opacity(0.5))
        .cornerRadius(12)
        .padding(.horizontal)
        .padding(.top)
    }

    // MARK: - Filter Chips
    private var filterChips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(ClassViewModel.ClassFilter.allCases, id: \.self) { filter in
                    FilterChip(
                        title: filter.rawValue,
                        isSelected: viewModel.selectedFilter == filter
                    ) {
                        viewModel.selectedFilter = filter
                    }
                }
            }
            .padding(.horizontal)
        }
        .padding(.vertical, 12)
    }

    // MARK: - Class List Content
    private var classListContent: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(viewModel.filteredClasses) { gymClass in
                    NavigationLink {
                        ClassDetailView(gymClass: gymClass)
                    } label: {
                        ClassCard(gymClass: gymClass)
                    }
                    .buttonStyle(PlainButtonStyle())
                }
            }
            .padding(.horizontal)
            .padding(.bottom, 20)
        }
        .emptyState(
            isEmpty: viewModel.filteredClasses.isEmpty && !viewModel.isLoading,
            icon: "calendar.badge.exclamationmark",
            title: "No Classes Found",
            message: "Try adjusting your search or filters"
        )
        .loading(viewModel.isLoading)
    }
}

// MARK: - Filter Chip
struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.labelMedium)
                .foregroundColor(isSelected ? .white : .textPrimary)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? Color.liyaqaBrand : Color.surfaceVariant.opacity(0.5))
                .cornerRadius(20)
        }
    }
}

// MARK: - Class Card
struct ClassCard: View {
    let gymClass: GymClass

    var body: some View {
        HStack(spacing: 12) {
            // Class Image
            RoundedRectangle(cornerRadius: 8)
                .fill(Color.liyaqaBrand.opacity(0.3))
                .frame(width: 80, height: 80)
                .overlay(
                    Image(systemName: "figure.run")
                        .font(.title)
                        .foregroundColor(.liyaqaBrand)
                )

            // Class Info
            VStack(alignment: .leading, spacing: 6) {
                Text(gymClass.name)
                    .font(.titleSmall)
                    .foregroundColor(.textPrimary)

                HStack {
                    Image(systemName: "person.fill")
                        .font(.caption)
                    Text(gymClass.instructor)
                        .font(.bodySmall)
                }
                .foregroundColor(.textSecondary)

                HStack {
                    Image(systemName: "clock")
                        .font(.caption)
                    Text(gymClass.date, style: .time)
                        .font(.bodySmall)

                    Spacer()

                    Image(systemName: "timer")
                        .font(.caption)
                    Text("\(gymClass.duration) min")
                        .font(.bodySmall)
                }
                .foregroundColor(.textSecondary)

                // Capacity
                HStack {
                    ProgressView(value: Double(gymClass.enrolled), total: Double(gymClass.capacity))
                        .tint(.liyaqaBrand)

                    Text("\(gymClass.enrolled)/\(gymClass.capacity)")
                        .font(.labelSmall)
                        .foregroundColor(.textSecondary)
                }
            }
        }
        .padding()
        .cardStyle()
    }
}

#Preview {
    NavigationView {
        ClassListView()
    }
}
