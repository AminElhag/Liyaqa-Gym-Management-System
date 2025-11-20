import { Routes, Route, Navigate } from 'react-router-dom';
import { useAppSelector } from '@app/store/hooks';

// Layout
import MainLayout from '@components/layout/MainLayout';

// Pages
import LoginPage from '@pages/LoginPage';
import DashboardPage from '@pages/DashboardPage';
import MembersPage from '@pages/MembersPage';
import SubscriptionsPage from '@pages/SubscriptionsPage';
import ClassesPage from '@pages/ClassesPage';
import AnalyticsPage from '@pages/AnalyticsPage';
import NotFoundPage from '@pages/NotFoundPage';

// Protected Route Component
interface ProtectedRouteProps {
  children: React.ReactNode;
}

function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isAuthenticated } = useAppSelector((state) => state.auth);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

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

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
