package com.liyaqa.gym.presentation.security

import com.liyaqa.gym.domain.entities.tenant.FeatureNotEnabledException
import com.liyaqa.gym.domain.entities.tenant.TenantContextHolder
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Aspect for checking feature flags before method execution.
 *
 * This aspect intercepts methods annotated with @RequiresFeature and verifies
 * that the current tenant has access to the required platform features.
 *
 * The aspect runs after security checks but before the actual method execution.
 */
@Aspect
@Component
@Order(100) // Run after security filters
class FeatureCheckAspect {

    private val logger = LoggerFactory.getLogger(FeatureCheckAspect::class.java)

    /**
     * Check feature access before method execution.
     *
     * This method is called before any method annotated with @RequiresFeature.
     * It verifies that the current tenant has the required feature(s) enabled.
     *
     * @param joinPoint The join point representing the method being called
     * @param requiresFeature The RequiresFeature annotation with feature requirements
     * @throws FeatureNotEnabledException if the tenant doesn't have the required feature(s)
     */
    @Before("@annotation(requiresFeature)")
    fun checkMethodFeature(joinPoint: JoinPoint, requiresFeature: RequiresFeature) {
        val features = requiresFeature.features
        val requireAll = requiresFeature.requireAll

        if (features.isEmpty()) {
            logger.warn("@RequiresFeature annotation used without specifying any features on ${joinPoint.signature}")
            return
        }

        val tenantContext = TenantContextHolder.get()

        if (tenantContext == null) {
            logger.warn("Feature check called without tenant context on ${joinPoint.signature}")
            // Let it proceed - if tenant context is required, the filter will have blocked it
            return
        }

        logger.debug(
            "Checking features ${features.joinToString(", ")} for tenant ${tenantContext.tenantSlug}"
        )

        // Check feature access
        val hasAccess = if (requireAll) {
            tenantContext.hasAllFeatures(*features)
        } else {
            tenantContext.hasAnyFeature(*features)
        }

        if (!hasAccess) {
            val missingFeatures = features.filter { !tenantContext.hasFeature(it) }
            logger.warn(
                "Tenant ${tenantContext.tenantSlug} attempted to access feature-restricted endpoint. " +
                "Required: ${features.joinToString(", ")}, Missing: ${missingFeatures.joinToString(", ")}"
            )

            // Throw exception for the first missing feature
            throw FeatureNotEnabledException(
                tenantContext.tenantId,
                missingFeatures.first()
            )
        }

        logger.debug(
            "Feature check passed for tenant ${tenantContext.tenantSlug}"
        )
    }

    /**
     * Check feature access for class-level annotations.
     *
     * This method is called before any method in a class annotated with @RequiresFeature.
     *
     * @param joinPoint The join point representing the method being called
     * @param requiresFeature The RequiresFeature annotation with feature requirements
     * @throws FeatureNotEnabledException if the tenant doesn't have the required feature(s)
     */
    @Before("@within(requiresFeature) && !@annotation(com.liyaqa.gym.presentation.security.RequiresFeature)")
    fun checkClassFeature(joinPoint: JoinPoint, requiresFeature: RequiresFeature) {
        // Use the same logic as method-level checks
        checkMethodFeature(joinPoint, requiresFeature)
    }
}
