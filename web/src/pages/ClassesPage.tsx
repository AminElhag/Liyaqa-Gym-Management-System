import { Box, Typography, Paper } from '@mui/material';

export default function ClassesPage() {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Classes
      </Typography>
      <Paper sx={{ p: 3, mt: 3 }}>
        <Typography variant="body1" color="text.secondary">
          Class schedule and management coming soon...
        </Typography>
      </Paper>
    </Box>
  );
}
