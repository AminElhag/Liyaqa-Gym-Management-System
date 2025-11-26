# Liyaqa Web Application

## White-Label Branding System

This web application supports tenant-specific branding through a dynamic theming system.

### Usage

Wrap your app with `BrandedApp` to automatically apply tenant branding:

```tsx
import React from 'react';
import ReactDOM from 'react-dom';
import { BrandedApp } from './components/BrandedApp';
import App from './App';

ReactDOM.render(
  <BrandedApp>
    <App />
  </BrandedApp>,
  document.getElementById('root')
);
```

### How it Works

1. **Automatic Slug Detection**: The branding hook extracts the tenant slug from the hostname (e.g., `gold-gym.liyaqa.com` → `gold-gym`)

2. **API Fetch**: Fetches branding from `/api/v1/public/branding/{slug}`

3. **Document Updates**:
   - Updates page title
   - Updates favicon
   - Sets CSS variables for theme colors
   - Adds meta tags

4. **MUI Theme**: Creates a Material-UI theme with tenant colors

### Using Branding in Components

```tsx
import { useBranding } from './contexts/BrandingContext';

function MyComponent() {
  const { branding, loading, error } = useBranding();

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h1>{branding?.tenantName}</h1>
      {branding?.logo && <img src={branding.logo} alt="Logo" />}
    </div>
  );
}
```

### CSS Variables

The following CSS variables are set automatically:

- `--primary-color`: Primary brand color
- `--secondary-color`: Secondary brand color
- `--accent-color`: Accent color

Use them in your CSS:

```css
.my-button {
  background-color: var(--primary-color);
  color: white;
}
```

### Custom Domain Support

For tenants with custom domains, the system will extract the subdomain or use domain lookup to find the correct tenant.

## Development

```bash
npm install
npm start
```

## Production Build

```bash
npm run build
```
