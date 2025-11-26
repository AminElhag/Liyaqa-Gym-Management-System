import { useState, useEffect } from 'react';
import axios from 'axios';

/**
 * Tenant branding interface
 */
export interface TenantBranding {
  tenantId: string;
  tenantName: string;
  tenantNameArabic?: string;
  logo?: string;
  favicon?: string;
  brandColors: BrandColors;
  customDomain?: string;
  emailFromName: string;
  emailFromAddress: string;
  smsFromName: string;
  supportEmail: string;
  supportPhone: string;
  socialLinks?: SocialLinks;
  mobileAppConfig?: MobileAppConfig;
}

export interface BrandColors {
  primaryColor: string;
  secondaryColor: string;
  accentColor: string;
}

export interface SocialLinks {
  facebook?: string;
  instagram?: string;
  twitter?: string;
  linkedin?: string;
  tiktok?: string;
  youtube?: string;
  website?: string;
}

export interface MobileAppConfig {
  appName: string;
  appNameArabic?: string;
  appIconUrl?: string;
  splashScreenUrl?: string;
  primaryColor: string;
  androidPackageName: string;
  iosAppId?: string;
  androidAppId?: string;
}

/**
 * Custom hook to fetch and apply tenant branding
 */
export const useTenantBranding = () => {
  const [branding, setBranding] = useState<TenantBranding | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchBranding = async () => {
      try {
        setLoading(true);
        setError(null);

        // Extract tenant slug from hostname (e.g., "gold-gym.liyaqa.com" -> "gold-gym")
        const hostname = window.location.hostname;
        const slug = hostname.split('.')[0];

        // Fetch branding from API
        const response = await axios.get<TenantBranding>(
          `/api/v1/public/branding/${slug}`
        );

        const brandingData = response.data;
        setBranding(brandingData);

        // Apply branding to document
        applyBrandingToDocument(brandingData);
      } catch (err) {
        console.error('Failed to fetch tenant branding:', err);
        setError('Failed to load branding');
      } finally {
        setLoading(false);
      }
    };

    fetchBranding();
  }, []);

  return { branding, loading, error };
};

/**
 * Apply branding to document (title, favicon, CSS variables)
 */
const applyBrandingToDocument = (branding: TenantBranding) => {
  // Update document title
  document.title = branding.tenantName;

  // Update favicon
  if (branding.favicon) {
    const favicon = document.querySelector("link[rel='icon']") as HTMLLinkElement;
    if (favicon) {
      favicon.href = branding.favicon;
    } else {
      const newFavicon = document.createElement('link');
      newFavicon.rel = 'icon';
      newFavicon.href = branding.favicon;
      document.head.appendChild(newFavicon);
    }
  }

  // Apply CSS variables for theme colors
  const root = document.documentElement;
  root.style.setProperty('--primary-color', branding.brandColors.primaryColor);
  root.style.setProperty('--secondary-color', branding.brandColors.secondaryColor);
  root.style.setProperty('--accent-color', branding.brandColors.accentColor);

  // Add meta tags
  updateMetaTag('application-name', branding.tenantName);
  updateMetaTag('apple-mobile-web-app-title', branding.tenantName);
  updateMetaTag('theme-color', branding.brandColors.primaryColor);
};

/**
 * Update or create a meta tag
 */
const updateMetaTag = (name: string, content: string) => {
  let meta = document.querySelector(`meta[name="${name}"]`) as HTMLMetaElement;
  if (meta) {
    meta.content = content;
  } else {
    meta = document.createElement('meta');
    meta.name = name;
    meta.content = content;
    document.head.appendChild(meta);
  }
};

/**
 * Create MUI theme from tenant branding
 */
export const createTenantTheme = (branding: TenantBranding | null) => {
  if (!branding) {
    return {
      palette: {
        primary: {
          main: '#1976d2',
        },
        secondary: {
          main: '#dc004e',
        },
      },
    };
  }

  return {
    palette: {
      primary: {
        main: branding.brandColors.primaryColor,
      },
      secondary: {
        main: branding.brandColors.secondaryColor,
      },
      error: {
        main: branding.brandColors.accentColor,
      },
    },
    typography: {
      fontFamily: [
        '-apple-system',
        'BlinkMacSystemFont',
        '"Segoe UI"',
        'Roboto',
        '"Helvetica Neue"',
        'Arial',
        'sans-serif',
      ].join(','),
    },
  };
};
