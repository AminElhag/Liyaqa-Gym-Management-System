import SwiftUI

struct DatePickerRow: View {
    @Binding var selectedDate: Date
    @State private var dates: [Date] = []

    private let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "EEE\ndd"
        return formatter
    }()

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(dates, id: \.self) { date in
                        DateCell(
                            date: date,
                            isSelected: Calendar.current.isDate(date, inSameDayAs: selectedDate),
                            dateFormatter: dateFormatter
                        )
                        .onTapGesture {
                            withAnimation {
                                selectedDate = date
                            }
                        }
                        .id(date)
                    }
                }
                .padding(.horizontal)
            }
            .onAppear {
                generateDates()
                // Scroll to selected date
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                    withAnimation {
                        proxy.scrollTo(selectedDate, anchor: .center)
                    }
                }
            }
        }
        .frame(height: 80)
    }

    private func generateDates() {
        let calendar = Calendar.current
        var dateArray: [Date] = []

        // Generate dates for the next 30 days
        for i in 0..<30 {
            if let date = calendar.date(byAdding: .day, value: i, to: Date()) {
                dateArray.append(date)
            }
        }

        dates = dateArray
    }
}

// MARK: - Date Cell
struct DateCell: View {
    let date: Date
    let isSelected: Bool
    let dateFormatter: DateFormatter

    var body: some View {
        VStack(spacing: 4) {
            Text(dateFormatter.string(from: date))
                .font(.system(size: 14, weight: isSelected ? .bold : .regular))
                .foregroundColor(isSelected ? .white : .textPrimary)
                .multilineTextAlignment(.center)
                .lineLimit(2)

            if isToday {
                Circle()
                    .fill(isSelected ? Color.white : Color.liyaqaBrand)
                    .frame(width: 6, height: 6)
            }
        }
        .frame(width: 60)
        .padding(.vertical, 12)
        .background(isSelected ? Color.liyaqaBrand : Color.surfaceVariant.opacity(0.5))
        .cornerRadius(12)
    }

    private var isToday: Bool {
        Calendar.current.isDateInToday(date)
    }
}

#Preview {
    VStack {
        DatePickerRow(selectedDate: .constant(Date()))
        Spacer()
    }
}
