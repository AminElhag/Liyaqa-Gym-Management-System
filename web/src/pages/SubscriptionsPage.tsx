import { Box, Typography, Paper } from '@mui/material';

export default function SubscriptionsPage() {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Subscriptions
      </Typography>
      <Paper sx={{ p: 3, mt: 3 }}>
        <Typography variant="body1" color="text.secondary">
          Subscription management coming soon...
        </Typography>
      </Paper>
    </Box>
  );
}
