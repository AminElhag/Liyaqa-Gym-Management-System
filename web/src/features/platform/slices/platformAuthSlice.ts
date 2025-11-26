import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { apiClient } from '@/api/client';
import { API_ENDPOINTS } from '@/api/endpoints';
import { PlatformAuthState, PlatformAdmin } from '../types';

const initialState: PlatformAuthState = {
  admin: null,
  token: localStorage.getItem('platform_access_token'),
  isAuthenticated: !!localStorage.getItem('platform_access_token'),
  isLoading: false,
  error: null,
};

interface PlatformLoginCredentials {
  email: string;
  password: string;
}

interface PlatformAuthResponse {
  admin: PlatformAdmin;
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export const platformLogin = createAsyncThunk<PlatformAuthResponse, PlatformLoginCredentials>(
  'platformAuth/login',
  async (credentials) => {
    const response = await apiClient.post<PlatformAuthResponse>(
      API_ENDPOINTS.platform.auth.login,
      credentials
    );
    return response;
  }
);

export const platformLogout = createAsyncThunk('platformAuth/logout', async () => {
  try {
    await apiClient.post(API_ENDPOINTS.platform.auth.logout);
  } catch (error) {
    console.error('Platform logout API error:', error);
  }
});

export const fetchCurrentPlatformAdmin = createAsyncThunk<PlatformAdmin>(
  'platformAuth/fetchCurrentAdmin',
  async () => {
    const response = await apiClient.get<PlatformAdmin>(API_ENDPOINTS.platform.auth.me);
    return response;
  }
);

const platformAuthSlice = createSlice({
  name: 'platformAuth',
  initialState,
  reducers: {
    setPlatformCredentials: (
      state,
      action: PayloadAction<{ admin: PlatformAdmin; accessToken: string; refreshToken?: string }>
    ) => {
      state.admin = action.payload.admin;
      state.token = action.payload.accessToken;
      state.isAuthenticated = true;
      localStorage.setItem('platform_access_token', action.payload.accessToken);
      if (action.payload.refreshToken) {
        localStorage.setItem('platform_refresh_token', action.payload.refreshToken);
      }
    },
    clearPlatformError: (state) => {
      state.error = null;
    },
    setPlatformLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      // Login
      .addCase(platformLogin.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(platformLogin.fulfilled, (state, action) => {
        state.isLoading = false;
        state.isAuthenticated = true;
        state.admin = action.payload.admin;
        state.token = action.payload.accessToken;
        localStorage.setItem('platform_access_token', action.payload.accessToken);
        localStorage.setItem('platform_refresh_token', action.payload.refreshToken);
      })
      .addCase(platformLogin.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Platform login failed';
      })
      // Logout
      .addCase(platformLogout.fulfilled, (state) => {
        state.admin = null;
        state.token = null;
        state.isAuthenticated = false;
        state.error = null;
        localStorage.removeItem('platform_access_token');
        localStorage.removeItem('platform_refresh_token');
      })
      // Fetch current admin
      .addCase(fetchCurrentPlatformAdmin.pending, (state) => {
        state.isLoading = true;
      })
      .addCase(fetchCurrentPlatformAdmin.fulfilled, (state, action) => {
        state.isLoading = false;
        state.admin = action.payload;
        state.isAuthenticated = true;
      })
      .addCase(fetchCurrentPlatformAdmin.rejected, (state) => {
        state.isLoading = false;
        state.isAuthenticated = false;
        state.admin = null;
        state.token = null;
        localStorage.removeItem('platform_access_token');
        localStorage.removeItem('platform_refresh_token');
      });
  },
});

export const { setPlatformCredentials, clearPlatformError, setPlatformLoading } = platformAuthSlice.actions;
export default platformAuthSlice.reducer;
