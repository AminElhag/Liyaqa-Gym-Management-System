import { Box, Typography, Paper } from '@mui/material';

export default function MembersPage() {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Members
      </Typography>
      <Paper sx={{ p: 3, mt: 3 }}>
        <Typography variant="body1" color="text.secondary">
          Member management coming soon...
        </Typography>
      </Paper>
    </Box>
  );
}
