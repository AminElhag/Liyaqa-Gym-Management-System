import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/app/store/hooks';
import { fetchPlatformMetrics, clearMetricsError } from '../slices/platformMetricsSlice';

export const usePlatformMetrics = () => {
  const dispatch = useAppDispatch();
  const { metrics, loading, error } = useAppSelector((state) => state.platformMetrics);

  const loadMetrics = useCallback(async () => {
    try {
      await dispatch(fetchPlatformMetrics()).unwrap();
      return { success: true };
    } catch (err: any) {
      return { success: false, error: err.message };
    }
  }, [dispatch]);

  const clearError = useCallback(() => {
    dispatch(clearMetricsError());
  }, [dispatch]);

  return {
    metrics,
    loading,
    error,
    loadMetrics,
    clearError,
  };
};
