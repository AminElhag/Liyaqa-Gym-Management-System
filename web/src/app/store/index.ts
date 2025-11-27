import { configureStore } from '@reduxjs/toolkit';
import authReducer from '@/features/auth/authSlice';
import membersReducer from '@/features/members/membersSlice';
import branchesReducer from '@/features/branches/branchesSlice';
import classesReducer from '@/features/classes/classesSlice';
import dashboardReducer from '@/features/dashboard/dashboardSlice';
import platformAuthReducer from '@/features/platform/slices/platformAuthSlice';
import platformMetricsReducer from '@/features/platform/slices/platformMetricsSlice';
import tenantsReducer from '@/features/platform/slices/tenantsSlice';
import { apiSlice } from './apiSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    members: membersReducer,
    branches: branchesReducer,
    classes: classesReducer,
    dashboard: dashboardReducer,
    platformAuth: platformAuthReducer,
    platformMetrics: platformMetricsReducer,
    tenants: tenantsReducer,
    [apiSlice.reducerPath]: apiSlice.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignore these action types
        ignoredActions: ['persist/PERSIST'],
      },
    }).concat(apiSlice.middleware),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
