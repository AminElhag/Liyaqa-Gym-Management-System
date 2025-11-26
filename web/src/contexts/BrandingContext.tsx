import React, { createContext, useContext, ReactNode } from 'react';
import { useTenantBranding, TenantBranding } from '../hooks/useTenantBranding';

/**
 * Branding context interface
 */
interface BrandingContextType {
  branding: TenantBranding | null;
  loading: boolean;
  error: string | null;
}

/**
 * Branding context
 */
const BrandingContext = createContext<BrandingContextType | undefined>(undefined);

/**
 * Branding provider component
 */
export const BrandingProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { branding, loading, error } = useTenantBranding();

  return (
    <BrandingContext.Provider value={{ branding, loading, error }}>
      {children}
    </BrandingContext.Provider>
  );
};

/**
 * Hook to use branding context
 */
export const useBranding = (): BrandingContextType => {
  const context = useContext(BrandingContext);
  if (context === undefined) {
    throw new Error('useBranding must be used within a BrandingProvider');
  }
  return context;
};

/**
 * Higher-order component to inject branding
 */
export const withBranding = <P extends object>(
  Component: React.ComponentType<P & BrandingContextType>
) => {
  return (props: P) => {
    const branding = useBranding();
    return <Component {...props} {...branding} />;
  };
};
