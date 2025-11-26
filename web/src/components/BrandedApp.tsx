import React from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { BrandingProvider, useBranding } from '../contexts/BrandingContext';
import { createTenantTheme } from '../hooks/useTenantBranding';
import CircularProgress from '@mui/material/CircularProgress';
import Box from '@mui/material/Box';

/**
 * Branded app wrapper that applies tenant theming
 */
export const BrandedApp: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <BrandingProvider>
      <ThemedApp>{children}</ThemedApp>
    </BrandingProvider>
  );
};

/**
 * Themed app component (internal)
 */
const ThemedApp: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { branding, loading, error } = useBranding();

  // Show loading state while fetching branding
  if (loading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="100vh"
      >
        <CircularProgress />
      </Box>
    );
  }

  // Show error state if branding fetch failed
  if (error) {
    console.error('Branding error:', error);
    // Continue with default theme if branding fails
  }

  // Create theme from branding
  const themeConfig = createTenantTheme(branding);
  const theme = createTheme(themeConfig);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {children}
    </ThemeProvider>
  );
};

/**
 * Example usage:
 *
 * import { BrandedApp } from './components/BrandedApp';
 * import App from './App';
 *
 * ReactDOM.render(
 *   <BrandedApp>
 *     <App />
 *   </BrandedApp>,
 *   document.getElementById('root')
 * );
 */
