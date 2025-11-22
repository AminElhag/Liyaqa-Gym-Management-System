import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { apiClient } from '@/api/client';

export interface DashboardStats {
  date: string;
  totalMembers: number;
  activeMembers: number;
  newMembersToday: number;
  todayCheckIns: number;
  currentOccupancy: number;
  todayRevenue: number;
  monthRevenue: number;
  activeSubscriptions: number;
  expiringSubscriptions: number;
  todayClassBookings: number;
  pendingPayments: number;
  trends: {
    memberGrowth: number;
    revenueGrowth: number;
    checkInGrowth: number;
  };
}

interface DashboardState {
  stats: DashboardStats | null;
  isLoading: boolean;
  error: string | null;
}

const initialState: DashboardState = {
  stats: null,
  isLoading: false,
  error: null,
};

export const fetchDashboardStats = createAsyncThunk(
  'dashboard/fetchStats',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.get<{ data: DashboardStats }>('/analytics/dashboard');
      return response.data.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch dashboard stats');
    }
  }
);

const dashboardSlice = createSlice({
  name: 'dashboard',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchDashboardStats.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchDashboardStats.fulfilled, (state, action) => {
        state.isLoading = false;
        state.stats = action.payload;
      })
      .addCase(fetchDashboardStats.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  },
});

export default dashboardSlice.reducer;
