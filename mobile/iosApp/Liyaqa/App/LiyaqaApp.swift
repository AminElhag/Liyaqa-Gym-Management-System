import SwiftUI
import shared

@main
struct LiyaqaApp: App {
    @StateObject private var appState = AppState()

    init() {
        // Initialize Koin DI from shared Kotlin module
        KoinHelperKt.doInitKoin()

        // Configure app appearance
        configureAppearance()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appState)
                .preferredColorScheme(appState.colorScheme)
        }
    }

    private func configureAppearance() {
        // Configure navigation bar appearance
        let appearance = UINavigationBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = UIColor(Color.surface)
        appearance.titleTextAttributes = [.foregroundColor: UIColor(Color.onSurface)]
        appearance.largeTitleTextAttributes = [.foregroundColor: UIColor(Color.onSurface)]

        UINavigationBar.appearance().standardAppearance = appearance
        UINavigationBar.appearance().scrollEdgeAppearance = appearance
        UINavigationBar.appearance().compactAppearance = appearance

        // Configure tab bar appearance
        let tabBarAppearance = UITabBarAppearance()
        tabBarAppearance.configureWithOpaqueBackground()
        tabBarAppearance.backgroundColor = UIColor(Color.surface)

        UITabBar.appearance().standardAppearance = tabBarAppearance
        if #available(iOS 15.0, *) {
            UITabBar.appearance().scrollEdgeAppearance = tabBarAppearance
        }
    }
}

// MARK: - App State
class AppState: ObservableObject {
    @Published var isAuthenticated = false
    @Published var currentUser: User?
    @Published var colorScheme: ColorScheme?

    init() {
        // Load saved preferences
        loadPreferences()
    }

    private func loadPreferences() {
        // Load dark mode preference
        if let savedScheme = UserDefaults.standard.string(forKey: "colorScheme") {
            colorScheme = savedScheme == "dark" ? .dark : .light
        }

        // Check if user is authenticated
        if let _ = KeychainHelper.shared.getToken() {
            isAuthenticated = true
            // Load user from storage if needed
        }
    }

    func login(user: User, token: String) {
        KeychainHelper.shared.saveToken(token)
        currentUser = user
        isAuthenticated = true
    }

    func logout() {
        KeychainHelper.shared.deleteToken()
        currentUser = nil
        isAuthenticated = false
    }

    func toggleColorScheme() {
        colorScheme = colorScheme == .dark ? .light : .dark
        UserDefaults.standard.set(colorScheme == .dark ? "dark" : "light", forKey: "colorScheme")
    }
}

// MARK: - Content View
struct ContentView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        Group {
            if appState.isAuthenticated {
                MainTabView()
            } else {
                NavigationView {
                    LoginView()
                }
            }
        }
    }
}

// MARK: - Main Tab View
struct MainTabView: View {
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            NavigationView {
                HomeView()
            }
            .tabItem {
                Label("Home", systemImage: "house.fill")
            }
            .tag(0)

            NavigationView {
                ClassListView()
            }
            .tabItem {
                Label("Classes", systemImage: "calendar")
            }
            .tag(1)

            NavigationView {
                CheckInView()
            }
            .tabItem {
                Label("Check In", systemImage: "qrcode.viewfinder")
            }
            .tag(2)

            NavigationView {
                ProfileView()
            }
            .tabItem {
                Label("Profile", systemImage: "person.fill")
            }
            .tag(3)
        }
        .accentColor(.liyaqaBrand)
    }
}

// MARK: - User Model
struct User: Codable {
    let id: String
    let email: String
    let fullName: String
    let phoneNumber: String?
    let profileImageUrl: String?
    let membershipStatus: String
    let membershipExpiry: Date?
}
