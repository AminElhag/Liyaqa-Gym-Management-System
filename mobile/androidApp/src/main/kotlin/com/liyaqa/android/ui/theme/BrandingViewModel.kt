package com.liyaqa.android.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liyaqa.gym.data.repositories.TenantBrandingRepository
import com.liyaqa.gym.domain.TenantBranding
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing tenant branding state
 */
@HiltViewModel
class BrandingViewModel @Inject constructor(
    private val brandingRepository: TenantBrandingRepository
) : ViewModel() {

    private val _brandingState = MutableStateFlow<BrandingState>(BrandingState.Loading)
    val brandingState: StateFlow<BrandingState> = _brandingState.asStateFlow()

    /**
     * Load tenant branding by slug
     */
    fun loadBranding(tenantSlug: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _brandingState.value = BrandingState.Loading

            brandingRepository.getTenantBranding(tenantSlug, forceRefresh)
                .fold(
                    onSuccess = { branding ->
                        _brandingState.value = BrandingState.Success(branding)
                    },
                    onFailure = { error ->
                        _brandingState.value = BrandingState.Error(error.message ?: "Failed to load branding")
                    }
                )
        }
    }

    /**
     * Observe branding changes
     */
    fun observeBranding(tenantSlug: String) {
        viewModelScope.launch {
            brandingRepository.observeTenantBranding(tenantSlug)
                .collect { result ->
                    result.fold(
                        onSuccess = { branding ->
                            _brandingState.value = BrandingState.Success(branding)
                        },
                        onFailure = { error ->
                            _brandingState.value = BrandingState.Error(error.message ?: "Failed to load branding")
                        }
                    )
                }
        }
    }

    /**
     * Get cached branding if available
     */
    fun getCachedBranding(): TenantBranding? {
        return brandingRepository.getCachedBranding()
    }

    /**
     * Clear branding cache
     */
    fun clearCache() {
        brandingRepository.clearCache()
        _brandingState.value = BrandingState.Loading
    }
}

/**
 * Sealed class representing branding state
 */
sealed class BrandingState {
    object Loading : BrandingState()
    data class Success(val branding: TenantBranding) : BrandingState()
    data class Error(val message: String) : BrandingState()
}
