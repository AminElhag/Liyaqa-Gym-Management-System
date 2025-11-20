import Foundation
import SwiftUI

// MARK: - Navigation Route
enum NavigationRoute: Hashable {
    case home
    case classList
    case classDetail(GymClass)
    case checkIn
    case profile
    case settings
    case editProfile
    case payments
    case subscription

    static func == (lhs: NavigationRoute, rhs: NavigationRoute) -> Bool {
        switch (lhs, rhs) {
        case (.home, .home),
             (.classList, .classList),
             (.checkIn, .checkIn),
             (.profile, .profile),
             (.settings, .settings),
             (.editProfile, .editProfile),
             (.payments, .payments),
             (.subscription, .subscription):
            return true
        case let (.classDetail(lhsClass), .classDetail(rhsClass)):
            return lhsClass.id == rhsClass.id
        default:
            return false
        }
    }

    func hash(into hasher: inout Hasher) {
        switch self {
        case .home:
            hasher.combine("home")
        case .classList:
            hasher.combine("classList")
        case .classDetail(let gymClass):
            hasher.combine("classDetail")
            hasher.combine(gymClass.id)
        case .checkIn:
            hasher.combine("checkIn")
        case .profile:
            hasher.combine("profile")
        case .settings:
            hasher.combine("settings")
        case .editProfile:
            hasher.combine("editProfile")
        case .payments:
            hasher.combine("payments")
        case .subscription:
            hasher.combine("subscription")
        }
    }
}

// MARK: - Navigation Router
class NavigationRouter: ObservableObject {
    @Published var path = NavigationPath()

    func navigate(to route: NavigationRoute) {
        path.append(route)
    }

    func navigateBack() {
        if !path.isEmpty {
            path.removeLast()
        }
    }

    func navigateToRoot() {
        path.removeLast(path.count)
    }

    func pop(to route: NavigationRoute) {
        while !path.isEmpty {
            if let lastRoute = path.codable as? NavigationRoute,
               lastRoute == route {
                break
            }
            path.removeLast()
        }
    }
}

// MARK: - Deep Link Handler
class DeepLinkHandler: ObservableObject {
    @Published var activeDeepLink: DeepLink?

    enum DeepLink {
        case classDetail(String)
        case profile
        case checkIn
        case payment(String)
    }

    func handle(url: URL) {
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: true),
              let host = components.host else {
            return
        }

        switch host {
        case "class":
            if let classId = components.queryItems?.first(where: { $0.name == "id" })?.value {
                activeDeepLink = .classDetail(classId)
            }
        case "profile":
            activeDeepLink = .profile
        case "checkin":
            activeDeepLink = .checkIn
        case "payment":
            if let paymentId = components.queryItems?.first(where: { $0.name == "id" })?.value {
                activeDeepLink = .payment(paymentId)
            }
        default:
            break
        }
    }
}
