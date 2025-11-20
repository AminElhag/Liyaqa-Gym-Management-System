import { Box, Paper, Typography } from '@mui/material';
import {
  People as PeopleIcon,
  CardMembership as SubscriptionIcon,
  CalendarToday as CalendarIcon,
  TrendingUp as TrendingUpIcon,
} from '@mui/icons-material';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  color: string;
}

function StatCard({ title, value, icon, color }: StatCardProps) {
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
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Welcome to Liyaqa Gym Management System
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
          value="0"
          icon={<PeopleIcon sx={{ color: 'white' }} />}
          color="primary.main"
        />
        <StatCard
          title="Active Subscriptions"
          value="0"
          icon={<SubscriptionIcon sx={{ color: 'white' }} />}
          color="success.main"
        />
        <StatCard
          title="Classes Today"
          value="0"
          icon={<CalendarIcon sx={{ color: 'white' }} />}
          color="info.main"
        />
        <StatCard
          title="Revenue This Month"
          value="$0"
          icon={<TrendingUpIcon sx={{ color: 'white' }} />}
          color="warning.main"
        />
      </Box>
    </Box>
  );
}
