import { Box, Typography, Paper } from '@mui/material';

export default function AnalyticsPage() {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Analytics
      </Typography>
      <Paper sx={{ p: 3, mt: 3 }}>
        <Typography variant="body1" color="text.secondary">
          Analytics and reports coming soon...
        </Typography>
      </Paper>
    </Box>
  );
}
