import Foundation
import HealthKit

class HealthKitService {
    static let shared = HealthKitService()

    private let healthStore = HKHealthStore()

    private init() {}

    var isHealthKitAvailable: Bool {
        return HKHealthStore.isHealthDataAvailable()
    }

    func requestAuthorization() {
        guard isHealthKitAvailable else {
            print("HealthKit is not available on this device")
            return
        }

        let typesToRead: Set<HKObjectType> = [
            HKObjectType.workoutType(),
            HKObjectType.quantityType(forIdentifier: .activeEnergyBurned)!,
            HKObjectType.quantityType(forIdentifier: .heartRate)!,
            HKObjectType.quantityType(forIdentifier: .distanceWalkingRunning)!
        ]

        let typesToWrite: Set<HKSampleType> = [
            HKObjectType.workoutType(),
            HKObjectType.quantityType(forIdentifier: .activeEnergyBurned)!
        ]

        healthStore.requestAuthorization(toShare: typesToWrite, read: typesToRead) { success, error in
            if let error = error {
                print("Error requesting HealthKit authorization: \(error)")
            }

            if success {
                print("HealthKit authorization granted")
            }
        }
    }

    func saveWorkout(
        activityType: HKWorkoutActivityType,
        start: Date,
        end: Date,
        calories: Double,
        distance: Double? = nil
    ) async throws {
        guard isHealthKitAvailable else {
            throw HealthKitError.notAvailable
        }

        var samples: [HKSample] = []

        // Create workout
        let workout = HKWorkout(
            activityType: activityType,
            start: start,
            end: end,
            duration: end.timeIntervalSince(start),
            totalEnergyBurned: HKQuantity(unit: .kilocalorie(), doubleValue: calories),
            totalDistance: distance != nil ? HKQuantity(unit: .meter(), doubleValue: distance!) : nil,
            metadata: nil
        )

        // Create energy burned sample
        let energyType = HKQuantityType.quantityType(forIdentifier: .activeEnergyBurned)!
        let energyQuantity = HKQuantity(unit: .kilocalorie(), doubleValue: calories)
        let energySample = HKQuantitySample(
            type: energyType,
            quantity: energyQuantity,
            start: start,
            end: end
        )
        samples.append(energySample)

        // Create distance sample if available
        if let distance = distance {
            let distanceType = HKQuantityType.quantityType(forIdentifier: .distanceWalkingRunning)!
            let distanceQuantity = HKQuantity(unit: .meter(), doubleValue: distance)
            let distanceSample = HKQuantitySample(
                type: distanceType,
                quantity: distanceQuantity,
                start: start,
                end: end
            )
            samples.append(distanceSample)
        }

        try await healthStore.save([workout])
        try await healthStore.add(samples, to: workout)
    }

    func fetchRecentWorkouts(limit: Int = 10) async throws -> [HKWorkout] {
        guard isHealthKitAvailable else {
            throw HealthKitError.notAvailable
        }

        let workoutType = HKObjectType.workoutType()
        let sortDescriptor = NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: false)

        return try await withCheckedThrowingContinuation { continuation in
            let query = HKSampleQuery(
                sampleType: workoutType,
                predicate: nil,
                limit: limit,
                sortDescriptors: [sortDescriptor]
            ) { query, samples, error in
                if let error = error {
                    continuation.resume(throwing: error)
                    return
                }

                let workouts = samples as? [HKWorkout] ?? []
                continuation.resume(returning: workouts)
            }

            healthStore.execute(query)
        }
    }

    func getTodayCaloriesBurned() async throws -> Double {
        guard isHealthKitAvailable else {
            throw HealthKitError.notAvailable
        }

        let energyType = HKQuantityType.quantityType(forIdentifier: .activeEnergyBurned)!
        let now = Date()
        let startOfDay = Calendar.current.startOfDay(for: now)

        let predicate = HKQuery.predicateForSamples(
            withStart: startOfDay,
            end: now,
            options: .strictStartDate
        )

        return try await withCheckedThrowingContinuation { continuation in
            let query = HKStatisticsQuery(
                quantityType: energyType,
                quantitySamplePredicate: predicate,
                options: .cumulativeSum
            ) { query, statistics, error in
                if let error = error {
                    continuation.resume(throwing: error)
                    return
                }

                let sum = statistics?.sumQuantity()
                let calories = sum?.doubleValue(for: .kilocalorie()) ?? 0.0
                continuation.resume(returning: calories)
            }

            healthStore.execute(query)
        }
    }
}

// MARK: - HealthKit Error
enum HealthKitError: Error {
    case notAvailable
    case authorizationDenied
}
