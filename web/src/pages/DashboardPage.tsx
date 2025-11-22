import { useEffect } from 'react';
import { Box, Paper, Typography, CircularProgress, Alert } from '@mui/material';
import {
  People as PeopleIcon,
  CardMembership as SubscriptionIcon,
  CalendarToday as CalendarIcon,
  TrendingUp as TrendingUpIcon,
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '@/app/store/hooks';
import { fetchDashboardStats } from '@/features/dashboard/dashboardSlice';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  color: string;
  trend?: number;
}

function StatCard({ title, value, icon, color, trend }: StatCardProps) {
  return (
    <Paper
      sx={{
        p: 3,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
      }}
    >
      <Box>
        <Typography color="text.secondary" variant="subtitle2">
          {title}
        </Typography>
        <Typography variant="h4" sx={{ mt: 1 }}>
          {value}
        </Typography>
        {trend !== undefined && (
          <Typography
            variant="caption"
            sx={{
              color: trend >= 0 ? 'success.main' : 'error.main',
              mt: 0.5,
              display: 'block',
            }}
          >
            {trend >= 0 ? '↑' : '↓'} {Math.abs(trend).toFixed(1)}% from yesterday
          </Typography>
        )}
      </Box>
      <Box
        sx={{
          backgroundColor: color,
          borderRadius: 2,
          p: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {icon}
      </Box>
    </Paper>
  );
}

export default function DashboardPage() {
  const dispatch = useAppDispatch();
  const { stats, isLoading, error } = useAppSelector((state) => state.dashboard);
  const { user } = useAppSelector((state) => state.auth);

  useEffect(() => {
    dispatch(fetchDashboardStats());
  }, [dispatch]);

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box>
        <Typography variant="h4" gutterBottom>
          Dashboard
        </Typography>
        <Alert severity="error" sx={{ mt: 2 }}>
          {error}
        </Alert>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Welcome back, {user?.email || 'User'}! Here's your gym overview for today.
      </Typography>

      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: {
            xs: '1fr',
            sm: 'repeat(2, 1fr)',
            md: 'repeat(4, 1fr)',
          },
          gap: 3,
        }}
      >
        <StatCard
          title="Total Members"
          value={stats?.totalMembers || 0}
          icon={<PeopleIcon sx={{ color: 'white' }} />}
          color="primary.main"
          trend={stats?.trends.memberGrowth}
        />
        <StatCard
          title="Active Subscriptions"
          value={stats?.activeSubscriptions || 0}
          icon={<SubscriptionIcon sx={{ color: 'white' }} />}
          color="success.main"
        />
        <StatCard
          title="Today's Check-ins"
          value={stats?.todayCheckIns || 0}
          icon={<CalendarIcon sx={{ color: 'white' }} />}
          color="info.main"
          trend={stats?.trends.checkInGrowth}
        />
        <StatCard
          title="Today's Revenue"
          value={`SAR ${stats?.todayRevenue?.toFixed(2) || '0.00'}`}
          icon={<TrendingUpIcon sx={{ color: 'white' }} />}
          color="warning.main"
          trend={stats?.trends.revenueGrowth}
        />
      </Box>

      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: {
            xs: '1fr',
            md: 'repeat(2, 1fr)',
          },
          gap: 3,
          mt: 3,
        }}
      >
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Monthly Overview
          </Typography>
          <Box sx={{ mt: 2 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
              <Typography color="text.secondary">Month Revenue:</Typography>
              <Typography variant="h6">SAR {stats?.monthRevenue?.toFixed(2) || '0.00'}</Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
              <Typography color="text.secondary">Active Members:</Typography>
              <Typography variant="h6">{stats?.activeMembers || 0}</Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
              <Typography color="text.secondary">New Members Today:</Typography>
              <Typography variant="h6">{stats?.newMembersToday || 0}</Typography>
            </Box>
          </Box>
        </Paper>

        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Today's Activity
          </Typography>
          <Box sx={{ mt: 2 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
              <Typography color="text.secondary">Class Bookings:</Typography>
              <Typography variant="h6">{stats?.todayClassBookings || 0}</Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
              <Typography color="text.secondary">Current Occupancy:</Typography>
              <Typography variant="h6">{stats?.currentOccupancy || 0}</Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
              <Typography color="text.secondary">Expiring Subscriptions:</Typography>
              <Typography variant="h6" color={stats?.expiringSubscriptions ? 'warning.main' : 'inherit'}>
                {stats?.expiringSubscriptions || 0}
              </Typography>
            </Box>
          </Box>
        </Paper>
      </Box>
    </Box>
  );
}
