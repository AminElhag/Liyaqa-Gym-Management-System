import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/app/store/hooks';
import {
  platformLogin,
  platformLogout,
  fetchCurrentPlatformAdmin,
  clearPlatformError,
} from '../slices/platformAuthSlice';

export const usePlatformAuth = () => {
  const dispatch = useAppDispatch();
  const { admin, token, isAuthenticated, isLoading, error } = useAppSelector(
    (state) => state.platformAuth
  );

  const login = useCallback(
    async (email: string, password: string) => {
      try {
        await dispatch(platformLogin({ email, password })).unwrap();
        return { success: true };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    },
    [dispatch]
  );

  const logout = useCallback(async () => {
    await dispatch(platformLogout());
  }, [dispatch]);

  const loadCurrentAdmin = useCallback(async () => {
    try {
      await dispatch(fetchCurrentPlatformAdmin()).unwrap();
      return { success: true };
    } catch (err: any) {
      return { success: false, error: err.message };
    }
  }, [dispatch]);

  const clearError = useCallback(() => {
    dispatch(clearPlatformError());
  }, [dispatch]);

  return {
    admin,
    token,
    isAuthenticated,
    isLoading,
    error,
    login,
    logout,
    loadCurrentAdmin,
    clearError,
  };
};
