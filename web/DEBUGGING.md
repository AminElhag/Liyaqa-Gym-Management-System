# Web Application Debugging Guide

This guide covers debugging the React + Vite web application using both IntelliJ IDEA and browser-based tools.

## Option 1: Debugging in IntelliJ IDEA

### Prerequisites
- IntelliJ IDEA Ultimate (Community Edition has limited JavaScript debugging support)
- Chrome or Edge browser installed
- JetBrains IDE Support Chrome extension (optional but recommended)

### Setup Steps

#### 1. Create JavaScript Debug Configuration

1. Open IntelliJ IDEA and load this project
2. Go to **Run → Edit Configurations...**
3. Click the **+** button and select **JavaScript Debug**
4. Configure:
   - **Name**: `Debug Web App`
   - **URL**: `http://localhost:3000`
   - **Browser**: Chrome (recommended)
   - **Ensure breakpoints detected**: ✓ (checked)

#### 2. Start the Development Server

In the terminal, start the Vite dev server:

```bash
cd web
npm run dev
```

Wait for Vite to start and display "Local: http://localhost:3000"

#### 3. Start Debugging

1. Click the Debug icon (bug icon) next to your configuration
2. IntelliJ will open Chrome with remote debugging enabled
3. Set breakpoints in your TypeScript/JavaScript files by clicking in the gutter (left margin)
4. Interact with the app in the browser to trigger breakpoints

### IntelliJ Debugging Features

- **Breakpoints**: Click in the gutter next to line numbers
- **Conditional Breakpoints**: Right-click breakpoint → Add condition
- **Watch Variables**: Variables window shows current scope
- **Call Stack**: See the execution path
- **Console**: Evaluate expressions while paused
- **Step Controls**:
  - F8 (Step Over)
  - F7 (Step Into)
  - Shift+F8 (Step Out)
  - F9 (Resume)

### Troubleshooting IntelliJ Debugging

**Issue: Breakpoints not hitting**
- Ensure sourcemaps are enabled (already configured in `vite.config.ts`)
- Check that the URL matches exactly (including port)
- Try clearing browser cache and restart debug session
- Verify Chrome is launched with remote debugging enabled

**Issue: "Cannot find files in workspace"**
- Make sure you opened the root project directory in IntelliJ
- Check that TypeScript files are recognized (not marked as plain text)

---

## Option 2: Browser Developer Tools (Recommended)

Browser debugging is often faster and more reliable for React applications.

### Chrome DevTools

#### Setup

1. Start the dev server:
```bash
cd web
npm run dev
```

2. Open Chrome and navigate to `http://localhost:3000`

3. Open DevTools:
   - Windows/Linux: `F12` or `Ctrl+Shift+I`
   - Mac: `Cmd+Option+I`

#### Key Features

**Sources Tab**
- Press `Ctrl+P` (Cmd+P on Mac) to quickly find files
- Navigate to `webpack://` → `src/` to find your source files
- Click line numbers to set breakpoints
- Use conditional breakpoints: right-click line number → "Add conditional breakpoint"

**Console Tab**
- Execute JavaScript expressions in the current context
- Log statements from your code appear here
- Access React components: `$r` (selected component in React DevTools)

**Network Tab**
- Monitor API calls to `http://localhost:8080/api/v1/`
- Inspect request/response headers and payloads
- Check timing and status codes

**Application Tab**
- View LocalStorage (where auth tokens are stored)
- Inspect cookies
- View session storage

#### Chrome DevTools Debugging Shortcuts

- `F8`: Resume execution
- `F10`: Step over
- `F11`: Step into
- `Shift+F11`: Step out
- `Ctrl+Shift+E`: Run selected text in console

### React Developer Tools

**Installation**

Install the Chrome extension:
[React Developer Tools](https://chrome.google.com/webstore/detail/react-developer-tools/fmkadmapgofadopljbjfkapdkoienihi)

**Features**

1. **Components Tab**
   - Inspect React component tree
   - View props and state
   - Edit props/state in real-time
   - Find component source code
   - Highlight component on page (hover in tree)

2. **Profiler Tab**
   - Record performance profiles
   - Identify slow components
   - Analyze render timing

**Tips**
- Click the 🎯 icon to select a component on the page
- Right-click a component → "Show source" to jump to code
- Use the search bar to find components by name
- Enable "Highlight updates" to see which components re-render

### Redux DevTools

**Installation**

Install the Chrome extension:
[Redux DevTools](https://chrome.google.com/webstore/detail/redux-devtools/lmhkpmbekcpmknklioeibfkpmmfibljd)

**Features**

Your app uses Redux Toolkit, so Redux DevTools will show:

1. **Action History**
   - See all dispatched actions (`auth/login`, `auth/logout`, etc.)
   - Time-travel debugging: jump to any previous state
   - Inspect action payloads

2. **State Tree**
   - View entire Redux store
   - Inspect nested state (auth, user, etc.)
   - Monitor state changes in real-time

3. **Diff View**
   - See exactly what changed in state
   - Compare before/after for each action

**Usage**
- Open DevTools → Redux tab
- Interact with the app (login, etc.)
- Watch actions appear in the log
- Click actions to see state at that point
- Use slider to time-travel through state changes

---

## Debugging Common Issues

### Authentication Issues

**Check API Connection**
```javascript
// In browser console
console.log(import.meta.env.VITE_API_BASE_URL)
// Should output: http://localhost:8080/api/v1
```

**Inspect Auth Token**
```javascript
// In browser console
localStorage.getItem('access_token')
```

**Monitor Login Request**
1. Open Network tab
2. Filter: `auth/login`
3. Try logging in
4. Inspect request payload and response

### Component Not Rendering

**Set Breakpoint in Component**
1. Open Sources → `src/components/YourComponent.tsx`
2. Set breakpoint in render/return section
3. Refresh page
4. Check props and state when paused

**Use React DevTools**
1. Open Components tab
2. Search for your component
3. Verify props are correct
4. Check if component is mounted

### State Management Issues

**Redux State Debugging**
1. Open Redux DevTools
2. Check current state matches expectations
3. Verify actions are dispatched correctly
4. Use time-travel to find when state became incorrect

**Local Component State**
1. Open React DevTools
2. Find component in tree
3. Inspect hooks (useState, useEffect, etc.)
4. Edit state values to test behavior

---

## Recommended Debugging Workflow

### For Most Development: Browser DevTools
1. Use Chrome DevTools for breakpoints and step debugging
2. Use React DevTools to inspect component hierarchy
3. Use Redux DevTools to monitor state changes
4. Use Network tab for API issues

**Advantages:**
- Faster startup (no IDE configuration)
- More React-specific features
- Better hot reload support
- Access to specialized React/Redux tools

### For Complex Debugging: IntelliJ IDEA
- When you need IDE integration
- When working across multiple files
- When you want unified debugging for frontend + backend
- When you prefer IDE keyboard shortcuts

---

## Quick Reference

### Browser Console Snippets

```javascript
// Check environment variables
console.log(import.meta.env)

// Get auth state from Redux
window.__REDUX_DEVTOOLS_EXTENSION__ &&
  window.__REDUX_DEVTOOLS_EXTENSION__.send()

// Clear auth and reload
localStorage.clear(); location.reload()

// Get current user from localStorage
JSON.parse(localStorage.getItem('access_token'))

// Enable verbose axios logging (add to code)
axios.interceptors.request.use(req => {
  console.log('Request:', req);
  return req;
});
```

### Useful Breakpoint Locations

- **Login flow**: `web/src/features/auth/authSlice.ts:56` (login thunk)
- **API client**: `web/src/api/client.ts:77` (POST method)
- **Auth errors**: `web/src/api/client.ts:30` (401 handler)
- **Component rendering**: Any component's return statement

---

## Performance Profiling

### React Profiler

1. Open React DevTools → Profiler tab
2. Click record (●)
3. Interact with app
4. Click stop (■)
5. Analyze render times and component updates

### Chrome Performance Tab

1. Open DevTools → Performance tab
2. Click record
3. Perform actions you want to profile
4. Click stop
5. Analyze flame graph and timeline

---

## Tips & Best Practices

1. **Use `debugger` statements**: Add `debugger;` in code to automatically pause
2. **Console logging**: Use `console.log()`, `console.table()`, `console.group()`
3. **Network throttling**: DevTools → Network → Throttling to test slow connections
4. **Preserve log**: Check "Preserve log" in Console/Network tabs across page reloads
5. **Disable cache**: DevTools → Network → "Disable cache" when debugging
6. **Conditional breakpoints**: Right-click gutter → Add conditional breakpoint
7. **Logpoints**: Breakpoint that logs without pausing (Chrome 73+)

---

## Additional Resources

- [Chrome DevTools Documentation](https://developer.chrome.com/docs/devtools/)
- [React DevTools Guide](https://react.dev/learn/react-developer-tools)
- [Redux DevTools Extension](https://github.com/reduxjs/redux-devtools)
- [Vite Debugging Guide](https://vitejs.dev/guide/debugging.html)
