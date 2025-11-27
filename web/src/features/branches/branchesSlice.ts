import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { apiClient } from '../../api/client';

export interface Branch {
  id: string;
  organizationId: string;
  name: string;
  facilityType: string;
  address?: {
    street: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
  };
  contactInfo?: {
    email: string;
    phone: string;
  };
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

interface BranchesState {
  branches: Branch[];
  isLoading: boolean;
  error: string | null;
}

const initialState: BranchesState = {
  branches: [],
  isLoading: false,
  error: null,
};

// Async thunk to fetch branches
export const fetchBranches = createAsyncThunk(
  'branches/fetchBranches',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/branches');
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch branches');
    }
  }
);

const branchesSlice = createSlice({
  name: 'branches',
  initialState,
  reducers: {
    clearBranchesError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchBranches.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchBranches.fulfilled, (state, action: PayloadAction<any>) => {
        state.isLoading = false;
        // Handle different API response formats defensively
        if (Array.isArray(action.payload)) {
          // API returned array directly
          state.branches = action.payload;
        } else if (action.payload && typeof action.payload === 'object') {
          // API returned object with data property
          state.branches = Array.isArray(action.payload.data) ? action.payload.data : [];
        } else {
          // Fallback to empty array
          state.branches = [];
        }
      })
      .addCase(fetchBranches.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  },
});

export const { clearBranchesError } = branchesSlice.actions;
export default branchesSlice.reducer;