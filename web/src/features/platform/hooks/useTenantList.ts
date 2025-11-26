import { useCallback, useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '@/app/store/hooks';
import { fetchTenants, setFilters, clearTenantsError } from '../slices/tenantsSlice';

interface UseTenantListFilters {
  status?: string;
  plan?: string;
  search?: string;
}

export const useTenantList = (filters?: UseTenantListFilters) => {
  const dispatch = useAppDispatch();
  const { tenants, loading, error, pagination, filters: currentFilters } = useAppSelector(
    (state) => state.tenants
  );

  // Update filters when they change
  useEffect(() => {
    if (filters) {
      dispatch(setFilters(filters));
    }
  }, [filters, dispatch]);

  const loadTenants = useCallback(
    async (page?: number) => {
      try {
        await dispatch(
          fetchTenants({
            page: page || pagination.page,
            limit: pagination.limit,
            ...currentFilters,
          })
        ).unwrap();
        return { success: true };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    },
    [dispatch, pagination.page, pagination.limit, currentFilters]
  );

  const changePage = useCallback(
    (newPage: number) => {
      loadTenants(newPage);
    },
    [loadTenants]
  );

  const clearError = useCallback(() => {
    dispatch(clearTenantsError());
  }, [dispatch]);

  return {
    tenants,
    loading,
    error,
    pagination,
    filters: currentFilters,
    loadTenants,
    changePage,
    clearError,
  };
};
