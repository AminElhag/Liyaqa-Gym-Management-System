import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/app/store/hooks';
import { createTenant } from '../slices/tenantsSlice';
import { CreateTenantRequest } from '../types';

export const useCreateTenant = () => {
  const dispatch = useAppDispatch();
  const { loading, error } = useAppSelector((state) => state.tenants);

  const create = useCallback(
    async (data: CreateTenantRequest) => {
      try {
        const result = await dispatch(createTenant(data)).unwrap();
        return { success: true, tenantId: result.id, tenant: result };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    },
    [dispatch]
  );

  return {
    createTenant: create,
    isLoading: loading,
    error,
  };
};
