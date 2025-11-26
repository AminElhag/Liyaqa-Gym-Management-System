import Foundation
import SwiftUI
import shared

/**
 * ViewModel for managing tenant branding state in iOS app
 */
@MainActor
class BrandingViewModel: ObservableObject {
    @Published var branding: TenantBranding?
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?

    private let brandingRepository: TenantBrandingRepository

    init(brandingRepository: TenantBrandingRepository) {
        self.brandingRepository = brandingRepository
    }

    /**
     * Load tenant branding by slug
     */
    func loadBranding(tenantSlug: String, forceRefresh: Bool = false) async {
        isLoading = true
        errorMessage = nil

        do {
            let result = try await brandingRepository.getTenantBranding(
                tenantSlug: tenantSlug,
                forceRefresh: forceRefresh
            )

            if let branding = result.getOrNull() {
                self.branding = branding
            } else if let error = result.exceptionOrNull() {
                self.errorMessage = error.localizedDescription
            }
        } catch {
            self.errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    /**
     * Observe branding changes
     */
    func observeBranding(tenantSlug: String) {
        Task {
            for await result in brandingRepository.observeTenantBranding(tenantSlug: tenantSlug) {
                if let branding = result.getOrNull() {
                    self.branding = branding
                } else if let error = result.exceptionOrNull() {
                    self.errorMessage = error.localizedDescription
                }
            }
        }
    }

    /**
     * Get cached branding if available
     */
    func getCachedBranding() -> TenantBranding? {
        return brandingRepository.getCachedBranding()
    }

    /**
     * Clear branding cache
     */
    func clearCache() {
        brandingRepository.clearCache()
        branding = nil
    }

    /**
     * Get branded theme from current branding
     */
    var theme: BrandedTheme {
        return BrandedTheme(branding: branding)
    }
}

/**
 * Extension to convert KMP Result to Swift Result
 */
extension shared.Result {
    func getOrNull() -> T? {
        if self.isSuccess {
            return self.value
        }
        return nil
    }

    func exceptionOrNull() -> Error? {
        if self.isFailure {
            return self.error as? Error
        }
        return nil
    }
}
