import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { apiClient } from '@/api/client';
import { API_ENDPOINTS } from '@/api/endpoints';

export interface Member {
  id: string;
  branchId?: string;
  name?: string;
  nameArabic?: string;
  email: string;
  phone: string;
  nationalId?: string;
  gender?: string;
  dateOfBirth?: string;
  age?: number;
  status: 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';
  profilePhotoUrl?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
  joinDate?: string;
  // Legacy fields for backward compatibility
  firstName?: string;
  lastName?: string;
  subscription?: {
    id: string;
    planName: string;
    status: string;
    startDate: string;
    endDate: string;
  };
}

interface MembersState {
  members: Member[];
  selectedMember: Member | null;
  isLoading: boolean;
  isCreating: boolean;
  isUpdating: boolean;
  isDeleting: boolean;
  error: string | null;
  totalCount: number;
  currentPage: number;
  pageSize: number;
}

const initialState: MembersState = {
  members: [],
  selectedMember: null,
  isLoading: false,
  isCreating: false,
  isUpdating: false,
  isDeleting: false,
  error: null,
  totalCount: 0,
  currentPage: 1,
  pageSize: 10,
};

// Async thunks
export const fetchMembers = createAsyncThunk(
  'members/fetchMembers',
  async (params?: { page?: number; pageSize?: number; search?: string }) => {
    const response = await apiClient.get<{ data: Member[]; total: number }>(
      API_ENDPOINTS.members.list,
      { params }
    );
    return response;
  }
);

export const fetchMemberById = createAsyncThunk(
  'members/fetchMemberById',
  async (id: string) => {
    const response = await apiClient.get<Member>(API_ENDPOINTS.members.detail(id));
    return response;
  }
);

export const createMember = createAsyncThunk(
  'members/createMember',
  async (memberData: Partial<Member>) => {
    const response = await apiClient.post<Member>(
      API_ENDPOINTS.members.create,
      memberData
    );
    return response;
  }
);

export const updateMember = createAsyncThunk(
  'members/updateMember',
  async ({ id, data }: { id: string; data: Partial<Member> }) => {
    const response = await apiClient.put<Member>(
      API_ENDPOINTS.members.update(id),
      data
    );
    return response;
  }
);

export const deleteMember = createAsyncThunk(
  'members/deleteMember',
  async (id: string) => {
    await apiClient.delete(API_ENDPOINTS.members.delete(id));
    return id;
  }
);

export const suspendMember = createAsyncThunk(
  'members/suspendMember',
  async (id: string) => {
    const response = await apiClient.post<Member>(
      API_ENDPOINTS.members.suspend(id)
    );
    return response;
  }
);

const membersSlice = createSlice({
  name: 'members',
  initialState,
  reducers: {
    setSelectedMember: (state, action: PayloadAction<Member | null>) => {
      state.selectedMember = action.payload;
    },
    clearError: (state) => {
      state.error = null;
    },
    setPage: (state, action: PayloadAction<number>) => {
      state.currentPage = action.payload;
    },
  },
  extraReducers: (builder) => {
    // Fetch members
    builder.addCase(fetchMembers.pending, (state) => {
      state.isLoading = true;
      state.error = null;
    });
    builder.addCase(fetchMembers.fulfilled, (state, action) => {
      state.isLoading = false;
      // Handle different API response formats defensively
      if (Array.isArray(action.payload)) {
        // API returned array directly
        state.members = action.payload;
        state.totalCount = action.payload.length;
      } else if (action.payload && typeof action.payload === 'object') {
        // API returned object with data property
        state.members = Array.isArray(action.payload.data) ? action.payload.data : [];
        state.totalCount = action.payload.total || state.members.length;
      } else {
        // Fallback to empty array
        state.members = [];
        state.totalCount = 0;
      }
    });
    builder.addCase(fetchMembers.rejected, (state, action) => {
      state.isLoading = false;
      state.error = action.error.message || 'Failed to fetch members';
    });

    // Fetch member by ID
    builder.addCase(fetchMemberById.pending, (state) => {
      state.isLoading = true;
      state.error = null;
    });
    builder.addCase(fetchMemberById.fulfilled, (state, action) => {
      state.isLoading = false;
      state.selectedMember = action.payload;
    });
    builder.addCase(fetchMemberById.rejected, (state, action) => {
      state.isLoading = false;
      state.error = action.error.message || 'Failed to fetch member';
    });

    // Create member
    builder.addCase(createMember.pending, (state) => {
      state.isCreating = true;
      state.error = null;
    });
    builder.addCase(createMember.fulfilled, (state, action) => {
      state.isCreating = false;
      state.members.unshift(action.payload);
      state.totalCount += 1;
    });
    builder.addCase(createMember.rejected, (state, action) => {
      state.isCreating = false;
      state.error = action.error.message || 'Failed to create member';
    });

    // Update member
    builder.addCase(updateMember.pending, (state) => {
      state.isUpdating = true;
      state.error = null;
    });
    builder.addCase(updateMember.fulfilled, (state, action) => {
      state.isUpdating = false;
      const index = state.members.findIndex((m) => m.id === action.payload.id);
      if (index !== -1) {
        state.members[index] = action.payload;
      }
      if (state.selectedMember?.id === action.payload.id) {
        state.selectedMember = action.payload;
      }
    });
    builder.addCase(updateMember.rejected, (state, action) => {
      state.isUpdating = false;
      state.error = action.error.message || 'Failed to update member';
    });

    // Delete member
    builder.addCase(deleteMember.pending, (state) => {
      state.isDeleting = true;
      state.error = null;
    });
    builder.addCase(deleteMember.fulfilled, (state, action) => {
      state.isDeleting = false;
      state.members = state.members.filter((m) => m.id !== action.payload);
      state.totalCount -= 1;
      if (state.selectedMember?.id === action.payload) {
        state.selectedMember = null;
      }
    });
    builder.addCase(deleteMember.rejected, (state, action) => {
      state.isDeleting = false;
      state.error = action.error.message || 'Failed to delete member';
    });

    // Suspend member
    builder.addCase(suspendMember.fulfilled, (state, action) => {
      const index = state.members.findIndex((m) => m.id === action.payload.id);
      if (index !== -1) {
        state.members[index] = action.payload;
      }
      if (state.selectedMember?.id === action.payload.id) {
        state.selectedMember = action.payload;
      }
    });
  },
});

export const { setSelectedMember, clearError, setPage } = membersSlice.actions;
export default membersSlice.reducer;
