import React from 'react';
import { Box, Button, Typography } from '@mui/material';
import { CheckCircle as CheckCircleIcon } from '@mui/icons-material';

interface OnboardingCompleteStepProps {
  onFinish: () => void;
}

export const OnboardingCompleteStep: React.FC<OnboardingCompleteStepProps> = ({
  onFinish,
}) => {
  return (
    <Box sx={{ textAlign: 'center', py: 6 }}>
      <CheckCircleIcon
        sx={{ fontSize: 120, color: 'success.main', mb: 3 }}
      />

      <Typography variant="h3" gutterBottom>
        You're All Set!
      </Typography>

      <Typography variant="h6" color="text.secondary" sx={{ mb: 4 }}>
        Your gym management system is ready to use
      </Typography>

      <Box sx={{ mb: 4 }}>
        <Typography variant="body1" paragraph>
          Congratulations! You've successfully completed the onboarding process.
        </Typography>
        <Typography variant="body1" paragraph>
          You can now start managing your gym operations, adding members, scheduling
          classes, and much more.
        </Typography>
      </Box>

      <Box
        sx={{
          display: 'inline-flex',
          flexDirection: 'column',
          gap: 2,
          alignItems: 'flex-start',
          textAlign: 'left',
          mb: 4,
        }}
      >
        <Typography variant="body2" color="text.secondary">
          ✓ Organization profile configured
        </Typography>
        <Typography variant="body2" color="text.secondary">
          ✓ First branch location added
        </Typography>
        <Typography variant="body2" color="text.secondary">
          ✓ Membership plans created
        </Typography>
        <Typography variant="body2" color="text.secondary">
          ✓ System is ready for operations
        </Typography>
      </Box>

      <Button
        variant="contained"
        size="large"
        onClick={onFinish}
        sx={{ minWidth: 200 }}
      >
        Go to Dashboard
      </Button>

      <Typography variant="caption" display="block" sx={{ mt: 3 }} color="text.secondary">
        You can access all settings and configurations from the dashboard
      </Typography>
    </Box>
  );
};
