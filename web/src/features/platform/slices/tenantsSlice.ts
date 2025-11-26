import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { apiClient } from '@/api/client';
import { API_ENDPOINTS } from '@/api/endpoints';
import { TenantsState, Tenant, CreateTenantRequest, UpdateTenantRequest } from '../types';

const initialState: TenantsState = {
  tenants: [],
  currentTenant: null,
  loading: false,
  error: null,
  pagination: {
    page: 1,
    limit: 20,
    total: 0,
    totalPages: 0,
  },
  filters: {
    status: '',
    plan: '',
    search: '',
  },
};

interface FetchTenantsParams {
  page?: number;
  limit?: number;
  status?: string;
  plan?: string;
  search?: string;
}

interface FetchTenantsResponse {
  tenants: Tenant[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
}

export const fetchTenants = createAsyncThunk<FetchTenantsResponse, FetchTenantsParams>(
  'tenants/fetchAll',
  async (params) => {
    const queryParams = new URLSearchParams();
    if (params.page) queryParams.append('page', params.page.toString());
    if (params.limit) queryParams.append('limit', params.limit.toString());
    if (params.status) queryParams.append('status', params.status);
    if (params.plan) queryParams.append('plan', params.plan);
    if (params.search) queryParams.append('search', params.search);

    const url = `${API_ENDPOINTS.platform.tenants}?${queryParams.toString()}`;
    const response = await apiClient.get<FetchTenantsResponse>(url);
    return response;
  }
);

export const fetchTenantById = createAsyncThunk<Tenant, string>(
  'tenants/fetchById',
  async (tenantId) => {
    const response = await apiClient.get<Tenant>(API_ENDPOINTS.platform.tenantDetail(tenantId));
    return response;
  }
);

export const createTenant = createAsyncThunk<Tenant, CreateTenantRequest>(
  'tenants/create',
  async (data) => {
    const response = await apiClient.post<Tenant>(API_ENDPOINTS.platform.tenants, data);
    return response;
  }
);

export const updateTenant = createAsyncThunk<
  Tenant,
  { tenantId: string; data: UpdateTenantRequest }
>(
  'tenants/update',
  async ({ tenantId, data }) => {
    const response = await apiClient.put<Tenant>(
      API_ENDPOINTS.platform.tenantDetail(tenantId),
      data
    );
    return response;
  }
);

export const suspendTenant = createAsyncThunk<Tenant, string>(
  'tenants/suspend',
  async (tenantId) => {
    const response = await apiClient.post<Tenant>(
      API_ENDPOINTS.platform.suspendTenant(tenantId)
    );
    return response;
  }
);

export const activateTenant = createAsyncThunk<Tenant, string>(
  'tenants/activate',
  async (tenantId) => {
    const response = await apiClient.post<Tenant>(
      API_ENDPOINTS.platform.activateTenant(tenantId)
    );
    return response;
  }
);

export const cancelTenant = createAsyncThunk<Tenant, string>(
  'tenants/cancel',
  async (tenantId) => {
    const response = await apiClient.post<Tenant>(
      API_ENDPOINTS.platform.cancelTenant(tenantId)
    );
    return response;
  }
);

const tenantsSlice = createSlice({
  name: 'tenants',
  initialState,
  reducers: {
    setFilters: (state, action: PayloadAction<Partial<TenantsState['filters']>>) => {
      state.filters = { ...state.filters, ...action.payload };
    },
    clearTenantsError: (state) => {
      state.error = null;
    },
    resetCurrentTenant: (state) => {
      state.currentTenant = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch tenants
      .addCase(fetchTenants.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchTenants.fulfilled, (state, action) => {
        state.loading = false;
        state.tenants = action.payload.tenants;
        state.pagination = action.payload.pagination;
      })
      .addCase(fetchTenants.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch tenants';
      })
      // Fetch tenant by ID
      .addCase(fetchTenantById.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchTenantById.fulfilled, (state, action) => {
        state.loading = false;
        state.currentTenant = action.payload;
      })
      .addCase(fetchTenantById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch tenant';
      })
      // Create tenant
      .addCase(createTenant.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createTenant.fulfilled, (state, action) => {
        state.loading = false;
        state.tenants.unshift(action.payload);
        state.currentTenant = action.payload;
      })
      .addCase(createTenant.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to create tenant';
      })
      // Update tenant
      .addCase(updateTenant.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateTenant.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.tenants.findIndex((t) => t.id === action.payload.id);
        if (index !== -1) {
          state.tenants[index] = action.payload;
        }
        state.currentTenant = action.payload;
      })
      .addCase(updateTenant.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to update tenant';
      })
      // Suspend tenant
      .addCase(suspendTenant.fulfilled, (state, action) => {
        const index = state.tenants.findIndex((t) => t.id === action.payload.id);
        if (index !== -1) {
          state.tenants[index] = action.payload;
        }
        if (state.currentTenant?.id === action.payload.id) {
          state.currentTenant = action.payload;
        }
      })
      // Activate tenant
      .addCase(activateTenant.fulfilled, (state, action) => {
        const index = state.tenants.findIndex((t) => t.id === action.payload.id);
        if (index !== -1) {
          state.tenants[index] = action.payload;
        }
        if (state.currentTenant?.id === action.payload.id) {
          state.currentTenant = action.payload;
        }
      })
      // Cancel tenant
      .addCase(cancelTenant.fulfilled, (state, action) => {
        const index = state.tenants.findIndex((t) => t.id === action.payload.id);
        if (index !== -1) {
          state.tenants[index] = action.payload;
        }
        if (state.currentTenant?.id === action.payload.id) {
          state.currentTenant = action.payload;
        }
      });
  },
});

export const { setFilters, clearTenantsError, resetCurrentTenant } = tenantsSlice.actions;
export default tenantsSlice.reducer;
