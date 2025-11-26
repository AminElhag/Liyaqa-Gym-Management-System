import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { apiClient } from '@/api/client';
import { API_ENDPOINTS } from '@/api/endpoints';
import { PlatformMetricsState, PlatformMetrics } from '../types';

const initialState: PlatformMetricsState = {
  metrics: null,
  loading: false,
  error: null,
};

export const fetchPlatformMetrics = createAsyncThunk<PlatformMetrics>(
  'platformMetrics/fetch',
  async () => {
    const response = await apiClient.get<PlatformMetrics>(API_ENDPOINTS.platform.metrics);
    return response;
  }
);

const platformMetricsSlice = createSlice({
  name: 'platformMetrics',
  initialState,
  reducers: {
    clearMetricsError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchPlatformMetrics.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchPlatformMetrics.fulfilled, (state, action) => {
        state.loading = false;
        state.metrics = action.payload;
      })
      .addCase(fetchPlatformMetrics.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch platform metrics';
      });
  },
});

export const { clearMetricsError } = platformMetricsSlice.actions;
export default platformMetricsSlice.reducer;
