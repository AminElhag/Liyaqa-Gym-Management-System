import SwiftUI

/// Section displaying featured classes
struct FeaturedClassesSection: View {
    let classes: [FeaturedClass]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Featured Classes")
                    .font(.titleMedium)
                    .foregroundColor(.textPrimary)

                Spacer()

                NavigationLink {
                    ClassListView()
                } label: {
                    Text("See All")
                        .font(.bodySmall)
                        .foregroundColor(.liyaqaBrand)
                }
            }

            if classes.isEmpty {
                EmptyFeaturedClassesView()
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(classes) { gymClass in
                            FeaturedClassCard(gymClass: gymClass)
                        }
                    }
                }
            }
        }
    }
}

/// Individual featured class card
struct FeaturedClassCard: View {
    let gymClass: FeaturedClass

    var body: some View {
        NavigationLink {
            ClassDetailView(classId: gymClass.id)
        } label: {
            VStack(alignment: .leading, spacing: 12) {
                // Class image placeholder
                RoundedRectangle(cornerRadius: 12)
                    .fill(
                        LinearGradient(
                            colors: [
                                Color.liyaqaBrand.opacity(0.8),
                                Color.liyaqaBrand.opacity(0.4)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 250, height: 140)
                    .overlay(
                        VStack {
                            Spacer()
                            HStack {
                                Spacer()
                                if gymClass.isFull {
                                    Text("FULL")
                                        .font(.labelSmall)
                                        .fontWeight(.bold)
                                        .foregroundColor(.white)
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 5)
                                        .background(Color.error)
                                        .cornerRadius(6)
                                        .padding([.bottom, .trailing], 8)
                                } else if gymClass.availableSpots <= 3 {
                                    Text("\(gymClass.availableSpots) LEFT")
                                        .font(.labelSmall)
                                        .fontWeight(.bold)
                                        .foregroundColor(.white)
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 5)
                                        .background(Color.warning)
                                        .cornerRadius(6)
                                        .padding([.bottom, .trailing], 8)
                                }
                            }
                        }
                    )
                    .overlay(
                        Image(systemName: "figure.run")
                            .font(.system(size: 48))
                            .foregroundColor(.white.opacity(0.8))
                    )

                VStack(alignment: .leading, spacing: 6) {
                    Text(gymClass.name)
                        .font(.labelLarge)
                        .fontWeight(.semibold)
                        .foregroundColor(.textPrimary)
                        .lineLimit(1)

                    if let instructor = gymClass.instructorName {
                        HStack(spacing: 4) {
                            Image(systemName: "person.fill")
                                .font(.caption)
                            Text(instructor)
                                .font(.bodySmall)
                        }
                        .foregroundColor(.textSecondary)
                    }

                    HStack(spacing: 12) {
                        HStack(spacing: 4) {
                            Image(systemName: "clock")
                                .font(.caption)
                            Text(gymClass.formattedTime)
                                .font(.bodySmall)
                        }

                        HStack(spacing: 4) {
                            Image(systemName: "person.2")
                                .font(.caption)
                            Text("\(gymClass.capacity)")
                                .font(.bodySmall)
                        }
                    }
                    .foregroundColor(.textSecondary)
                }
            }
            .frame(width: 250)
            .padding()
            .cardStyle()
        }
    }
}

/// Empty state for featured classes
struct EmptyFeaturedClassesView: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "star.slash")
                .font(.system(size: 32))
                .foregroundColor(.textSecondary)

            Text("No featured classes available")
                .font(.bodyMedium)
                .foregroundColor(.textPrimary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 30)
        .cardStyle()
    }
}

/// Model for featured class
struct FeaturedClass: Identifiable {
    let id: String
    let name: String
    let instructorName: String?
    let startTime: Date
    let capacity: Int
    let availableSpots: Int
    let isFull: Bool

    var formattedTime: String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: startTime)
    }
}

#Preview {
    NavigationStack {
        ScrollView {
            VStack(spacing: 20) {
                FeaturedClassesSection(classes: [
                    FeaturedClass(
                        id: "1",
                        name: "Morning Yoga Flow",
                        instructorName: "Sarah Johnson",
                        startTime: Date().addingTimeInterval(3600),
                        capacity: 20,
                        availableSpots: 5,
                        isFull: false
                    ),
                    FeaturedClass(
                        id: "2",
                        name: "HIIT Training",
                        instructorName: "Mike Davis",
                        startTime: Date().addingTimeInterval(7200),
                        capacity: 15,
                        availableSpots: 2,
                        isFull: false
                    ),
                    FeaturedClass(
                        id: "3",
                        name: "Spin Class",
                        instructorName: "Emma Wilson",
                        startTime: Date().addingTimeInterval(10800),
                        capacity: 12,
                        availableSpots: 0,
                        isFull: true
                    )
                ])

                FeaturedClassesSection(classes: [])
            }
            .padding()
        }
    }
}
