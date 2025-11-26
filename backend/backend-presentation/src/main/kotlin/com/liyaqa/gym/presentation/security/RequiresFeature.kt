package com.liyaqa.gym.presentation.security

import com.liyaqa.gym.domain.entities.tenant.PlatformFeature

/**
 * Annotation to mark endpoints that require specific platform features.
 *
 * When applied to a controller method, this annotation will check if the current
 * tenant has the required feature(s) enabled before allowing access.
 *
 * If the tenant does not have the required feature(s), a FeatureNotEnabledException
 * will be thrown.
 *
 * Usage:
 * ```kotlin
 * @RequiresFeature(PlatformFeature.ADVANCED_ANALYTICS)
 * @GetMapping("/api/v1/analytics/advanced")
 * fun getAdvancedAnalytics(): ResponseEntity<AdvancedAnalytics> {
 *     // This will only execute if tenant has ADVANCED_ANALYTICS feature
 * }
 *
 * @RequiresFeature(PlatformFeature.MULTI_BRANCH, PlatformFeature.CUSTOM_BRANDING)
 * @PostMapping("/api/v1/branches")
 * fun createBranch(): ResponseEntity<Branch> {
 *     // This requires both MULTI_BRANCH and CUSTOM_BRANDING features
 * }
 * ```
 *
 * @param features The required platform feature(s). All features must be enabled.
 * @param requireAll If true (default), all features must be enabled. If false, at least one must be enabled.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RequiresFeature(
    vararg val features: PlatformFeature,
    val requireAll: Boolean = true
)
