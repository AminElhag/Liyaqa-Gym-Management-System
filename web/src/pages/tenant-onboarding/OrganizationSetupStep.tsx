import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  MenuItem,
  Grid,
} from '@mui/material';
import { api } from '@/services/api';

const organizationSchema = z.object({
  organizationName: z
    .string()
    .min(3, 'Organization name must be at least 3 characters')
    .max(100, 'Organization name must be less than 100 characters'),
  timezone: z.string().min(1, 'Timezone is required'),
  defaultCurrency: z.string().length(3, 'Currency must be 3 characters'),
  defaultLanguage: z.enum(['ar', 'en'], {
    errorMap: () => ({ message: 'Language must be ar or en' }),
  }),
});

type OrganizationForm = z.infer<typeof organizationSchema>;

interface OrganizationSetupStepProps {
  onComplete: () => void;
}

export const OrganizationSetupStep: React.FC<OrganizationSetupStepProps> = ({
  onComplete,
}) => {
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<OrganizationForm>({
    resolver: zodResolver(organizationSchema),
    defaultValues: {
      timezone: 'Asia/Riyadh',
      defaultCurrency: 'SAR',
      defaultLanguage: 'ar',
    },
  });

  const onSubmit = async (data: OrganizationForm) => {
    try {
      setIsSubmitting(true);
      setError(null);
      await api.post('/tenant/onboarding/steps/organization', data);
      onComplete();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to setup organization');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Setup Your Organization
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Tell us about your gym or fitness center
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <form onSubmit={handleSubmit(onSubmit)}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <TextField
              {...register('organizationName')}
              label="Organization Name"
              fullWidth
              error={!!errors.organizationName}
              helperText={errors.organizationName?.message}
              placeholder="e.g., Gold Gym"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              {...register('timezone')}
              label="Timezone"
              fullWidth
              select
              error={!!errors.timezone}
              helperText={errors.timezone?.message}
            >
              <MenuItem value="Asia/Riyadh">Asia/Riyadh (GMT+3)</MenuItem>
              <MenuItem value="Asia/Dubai">Asia/Dubai (GMT+4)</MenuItem>
              <MenuItem value="Asia/Kuwait">Asia/Kuwait (GMT+3)</MenuItem>
              <MenuItem value="Asia/Bahrain">Asia/Bahrain (GMT+3)</MenuItem>
            </TextField>
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              {...register('defaultCurrency')}
              label="Currency"
              fullWidth
              select
              error={!!errors.defaultCurrency}
              helperText={errors.defaultCurrency?.message}
            >
              <MenuItem value="SAR">SAR (Saudi Riyal)</MenuItem>
              <MenuItem value="AED">AED (UAE Dirham)</MenuItem>
              <MenuItem value="KWD">KWD (Kuwaiti Dinar)</MenuItem>
              <MenuItem value="BHD">BHD (Bahraini Dinar)</MenuItem>
            </TextField>
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              {...register('defaultLanguage')}
              label="Default Language"
              fullWidth
              select
              error={!!errors.defaultLanguage}
              helperText={errors.defaultLanguage?.message}
            >
              <MenuItem value="ar">Arabic</MenuItem>
              <MenuItem value="en">English</MenuItem>
            </TextField>
          </Grid>

          <Grid item xs={12}>
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
              <Button
                type="submit"
                variant="contained"
                size="large"
                disabled={isSubmitting}
              >
                {isSubmitting ? 'Saving...' : 'Continue'}
              </Button>
            </Box>
          </Grid>
        </Grid>
      </form>
    </Box>
  );
};
