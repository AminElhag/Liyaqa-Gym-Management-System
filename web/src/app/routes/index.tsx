import { Routes, Route, Navigate } from 'react-router-dom';

// Layout
import MainLayout from '@components/layout/MainLayout';

// Auth Components
import { LoginPage, RegisterPage } from '@pages/auth';
import { ProtectedRoute } from '@components/auth';

// Pages
import DashboardPage from '@pages/DashboardPage';
import MembersPage from '@pages/MembersPage';
import SubscriptionsPage from '@pages/SubscriptionsPage';
import ClassesPage from '@pages/ClassesPage';
import AnalyticsPage from '@pages/AnalyticsPage';
import NotFoundPage from '@pages/NotFoundPage';

export function AppRoutes() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Protected routes */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="members" element={<MembersPage />} />
        <Route path="subscriptions" element={<SubscriptionsPage />} />
        <Route path="classes" element={<ClassesPage />} />
        <Route path="analytics" element={<AnalyticsPage />} />
      </Route>

      {/* 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
