import { useCallback, useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '@/app/store/hooks';
import {
  fetchTenantById,
  updateTenant,
  suspendTenant,
  activateTenant,
  cancelTenant,
  resetCurrentTenant,
  clearTenantsError,
} from '../slices/tenantsSlice';
import { UpdateTenantRequest } from '../types';

export const useTenantDetail = (tenantId?: string) => {
  const dispatch = useAppDispatch();
  const { currentTenant, loading, error } = useAppSelector((state) => state.tenants);

  useEffect(() => {
    if (tenantId) {
      dispatch(fetchTenantById(tenantId));
    }
    return () => {
      dispatch(resetCurrentTenant());
    };
  }, [tenantId, dispatch]);

  const update = useCallback(
    async (data: UpdateTenantRequest) => {
      if (!tenantId) return { success: false, error: 'No tenant ID provided' };
      try {
        const result = await dispatch(updateTenant({ tenantId, data })).unwrap();
        return { success: true, tenant: result };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    },
    [dispatch, tenantId]
  );

  const suspend = useCallback(async () => {
    if (!tenantId) return { success: false, error: 'No tenant ID provided' };
    try {
      const result = await dispatch(suspendTenant(tenantId)).unwrap();
      return { success: true, tenant: result };
    } catch (err: any) {
      return { success: false, error: err.message };
    }
  }, [dispatch, tenantId]);

  const activate = useCallback(async () => {
    if (!tenantId) return { success: false, error: 'No tenant ID provided' };
    try {
      const result = await dispatch(activateTenant(tenantId)).unwrap();
      return { success: true, tenant: result };
    } catch (err: any) {
      return { success: false, error: err.message };
    }
  }, [dispatch, tenantId]);

  const cancel = useCallback(async () => {
    if (!tenantId) return { success: false, error: 'No tenant ID provided' };
    try {
      const result = await dispatch(cancelTenant(tenantId)).unwrap();
      return { success: true, tenant: result };
    } catch (err: any) {
      return { success: false, error: err.message };
    }
  }, [dispatch, tenantId]);

  const clearError = useCallback(() => {
    dispatch(clearTenantsError());
  }, [dispatch]);

  return {
    tenant: currentTenant,
    loading,
    error,
    updateTenant: update,
    suspendTenant: suspend,
    activateTenant: activate,
    cancelTenant: cancel,
    clearError,
  };
};
