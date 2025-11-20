import SwiftUI
import shared

struct ClassListView: View {
    @StateObject private var viewModel = ClassListViewModel()
    @State private var selectedDate = Date()

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Date picker (horizontal scroll)
                DatePickerRow(selectedDate: $selectedDate)
                    .onChange(of: selectedDate) { newDate in
                        Task {
                            await viewModel.loadSchedules(for: newDate)
                        }
                    }
                    .padding(.vertical, 8)

                // Filter chips
                FilterChipsRow(
                    selectedFilters: $viewModel.selectedFilters,
                    availableFilters: viewModel.availableFilters
                )
                .padding(.vertical, 8)

                Divider()

                // Class list
                classListContent
            }
            .navigationTitle("Classes")
            .navigationBarTitleDisplayMode(.large)
            .alert("Error", isPresented: $viewModel.showError) {
                Button("OK", role: .cancel) {}
            } message: {
                if let errorMessage = viewModel.errorMessage {
                    Text(errorMessage)
                }
            }
        }
        .task {
            await viewModel.loadSchedules(for: selectedDate)
        }
        .refreshable {
            await viewModel.refresh(for: selectedDate)
        }
        .tabItem {
            Label("Classes", systemImage: "calendar")
        }
    }

    // MARK: - Class List Content
    @ViewBuilder
    private var classListContent: some View {
        if viewModel.isLoading {
            VStack {
                Spacer()
                ProgressView()
                    .scaleEffect(1.5)
                    .progressViewStyle(CircularProgressViewStyle(tint: .liyaqaBrand))
                Text("Loading classes...")
                    .font(.bodyMedium)
                    .foregroundColor(.textSecondary)
                    .padding(.top, 12)
                Spacer()
            }
        } else if viewModel.schedules.isEmpty {
            EmptyStateView(
                message: "No classes scheduled for this date",
                icon: "calendar.badge.exclamationmark"
            )
        } else if viewModel.schedulesByTime.isEmpty {
            EmptyStateView(
                message: "No classes match your filters",
                icon: "line.3.horizontal.decrease.circle",
                actionTitle: "Clear Filters",
                action: {
                    viewModel.selectedFilters = ["All"]
                }
            )
        } else {
            List {
                ForEach(viewModel.schedulesByTime, id: \.id) { group in
                    Section(header: timeSlotHeader(group.timeSlot)) {
                        ForEach(group.schedules, id: \.id) { schedule in
                            NavigationLink {
                                ClassDetailView(schedule: schedule)
                            } label: {
                                ClassScheduleRow(schedule: schedule)
                            }
                            .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                        }
                    }
                }
            }
            .listStyle(.insetGrouped)
        }
    }

    // MARK: - Time Slot Header
    private func timeSlotHeader(_ timeSlot: String) -> some View {
        Text(timeSlot)
            .font(.system(size: 14, weight: .semibold))
            .foregroundColor(.textSecondary)
            .textCase(nil)
    }
}

#Preview {
    ClassListView()
}
