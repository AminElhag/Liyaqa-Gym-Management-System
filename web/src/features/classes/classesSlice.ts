import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { apiClient } from '@/api/client';
import { API_ENDPOINTS } from '@/api/endpoints';

export interface Class {
  id: string;
  name: string;
  description?: string;
  trainerId: string;
  trainerName: string;
  startTime: string;
  endTime: string;
  capacity: number;
  bookedCount: number;
  status: 'SCHEDULED' | 'ONGOING' | 'COMPLETED' | 'CANCELLED';
  location?: string;
  date: string;
}

interface ClassesState {
  classes: Class[];
  selectedClass: Class | null;
  upcomingClasses: Class[];
  isLoading: boolean;
  isCreating: boolean;
  isUpdating: boolean;
  isDeleting: boolean;
  error: string | null;
  totalCount: number;
  currentPage: number;
  pageSize: number;
}

const initialState: ClassesState = {
  classes: [],
  selectedClass: null,
  upcomingClasses: [],
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
export const fetchClasses = createAsyncThunk(
  'classes/fetchClasses',
  async (params?: { page?: number; pageSize?: number; date?: string }) => {
    const response = await apiClient.get<{ data: Class[]; total: number }>(
      API_ENDPOINTS.classes.list,
      { params }
    );
    return response;
  }
);

export const fetchUpcomingClasses = createAsyncThunk(
  'classes/fetchUpcomingClasses',
  async () => {
    const response = await apiClient.get<Class[]>(API_ENDPOINTS.classes.upcoming);
    return response;
  }
);

export const fetchClassById = createAsyncThunk(
  'classes/fetchClassById',
  async (id: string) => {
    const response = await apiClient.get<Class>(API_ENDPOINTS.classes.detail(id));
    return response;
  }
);

export const createClass = createAsyncThunk(
  'classes/createClass',
  async (classData: Partial<Class>) => {
    const response = await apiClient.post<Class>(
      API_ENDPOINTS.classes.create,
      classData
    );
    return response;
  }
);

export const updateClass = createAsyncThunk(
  'classes/updateClass',
  async ({ id, data }: { id: string; data: Partial<Class> }) => {
    const response = await apiClient.put<Class>(
      API_ENDPOINTS.classes.update(id),
      data
    );
    return response;
  }
);

export const deleteClass = createAsyncThunk(
  'classes/deleteClass',
  async (id: string) => {
    await apiClient.delete(API_ENDPOINTS.classes.delete(id));
    return id;
  }
);

export const cancelClass = createAsyncThunk(
  'classes/cancelClass',
  async (id: string) => {
    const response = await apiClient.post<Class>(
      API_ENDPOINTS.classes.cancel(id)
    );
    return response;
  }
);

const classesSlice = createSlice({
  name: 'classes',
  initialState,
  reducers: {
    setSelectedClass: (state, action: PayloadAction<Class | null>) => {
      state.selectedClass = action.payload;
    },
    clearError: (state) => {
      state.error = null;
    },
    setPage: (state, action: PayloadAction<number>) => {
      state.currentPage = action.payload;
    },
  },
  extraReducers: (builder) => {
    // Fetch classes
    builder.addCase(fetchClasses.pending, (state) => {
      state.isLoading = true;
      state.error = null;
    });
    builder.addCase(fetchClasses.fulfilled, (state, action) => {
      state.isLoading = false;
      state.classes = action.payload.data;
      state.totalCount = action.payload.total;
    });
    builder.addCase(fetchClasses.rejected, (state, action) => {
      state.isLoading = false;
      state.error = action.error.message || 'Failed to fetch classes';
    });

    // Fetch upcoming classes
    builder.addCase(fetchUpcomingClasses.pending, (state) => {
      state.isLoading = true;
      state.error = null;
    });
    builder.addCase(fetchUpcomingClasses.fulfilled, (state, action) => {
      state.isLoading = false;
      state.upcomingClasses = action.payload;
    });
    builder.addCase(fetchUpcomingClasses.rejected, (state, action) => {
      state.isLoading = false;
      state.error = action.error.message || 'Failed to fetch upcoming classes';
    });

    // Fetch class by ID
    builder.addCase(fetchClassById.pending, (state) => {
      state.isLoading = true;
      state.error = null;
    });
    builder.addCase(fetchClassById.fulfilled, (state, action) => {
      state.isLoading = false;
      state.selectedClass = action.payload;
    });
    builder.addCase(fetchClassById.rejected, (state, action) => {
      state.isLoading = false;
      state.error = action.error.message || 'Failed to fetch class';
    });

    // Create class
    builder.addCase(createClass.pending, (state) => {
      state.isCreating = true;
      state.error = null;
    });
    builder.addCase(createClass.fulfilled, (state, action) => {
      state.isCreating = false;
      state.classes.unshift(action.payload);
      state.totalCount += 1;
    });
    builder.addCase(createClass.rejected, (state, action) => {
      state.isCreating = false;
      state.error = action.error.message || 'Failed to create class';
    });

    // Update class
    builder.addCase(updateClass.pending, (state) => {
      state.isUpdating = true;
      state.error = null;
    });
    builder.addCase(updateClass.fulfilled, (state, action) => {
      state.isUpdating = false;
      const index = state.classes.findIndex((c) => c.id === action.payload.id);
      if (index !== -1) {
        state.classes[index] = action.payload;
      }
      if (state.selectedClass?.id === action.payload.id) {
        state.selectedClass = action.payload;
      }
    });
    builder.addCase(updateClass.rejected, (state, action) => {
      state.isUpdating = false;
      state.error = action.error.message || 'Failed to update class';
    });

    // Delete class
    builder.addCase(deleteClass.pending, (state) => {
      state.isDeleting = true;
      state.error = null;
    });
    builder.addCase(deleteClass.fulfilled, (state, action) => {
      state.isDeleting = false;
      state.classes = state.classes.filter((c) => c.id !== action.payload);
      state.totalCount -= 1;
      if (state.selectedClass?.id === action.payload) {
        state.selectedClass = null;
      }
    });
    builder.addCase(deleteClass.rejected, (state, action) => {
      state.isDeleting = false;
      state.error = action.error.message || 'Failed to delete class';
    });

    // Cancel class
    builder.addCase(cancelClass.fulfilled, (state, action) => {
      const index = state.classes.findIndex((c) => c.id === action.payload.id);
      if (index !== -1) {
        state.classes[index] = action.payload;
      }
      if (state.selectedClass?.id === action.payload.id) {
        state.selectedClass = action.payload;
      }
    });
  },
});

export const { setSelectedClass, clearError, setPage } = classesSlice.actions;
export default classesSlice.reducer;
