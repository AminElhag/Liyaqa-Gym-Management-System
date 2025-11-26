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

const branchSchema = z.object({
  branchName: z
    .string()
    .min(3, 'Branch name must be at least 3 characters')
    .max(100, 'Branch name must be less than 100 characters'),
  facilityType: z.enum(['MALE_ONLY', 'FEMALE_ONLY', 'FAMILY'], {
    errorMap: () => ({ message: 'Please select a facility type' }),
  }),
  street: z.string().min(3, 'Street address is required'),
  city: z.string().min(2, 'City is required'),
  state: z.string().min(2, 'State/Province is required'),
  postalCode: z.string().min(4, 'Postal code is required'),
  country: z.string().min(2, 'Country is required'),
});

type BranchForm = z.infer<typeof branchSchema>;

interface FirstBranchStepProps {
  onComplete: () => void;
  organizationId?: string;
}

export const FirstBranchStep: React.FC<FirstBranchStepProps> = ({
  onComplete,
  organizationId,
}) => {
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<BranchForm>({
    resolver: zodResolver(branchSchema),
    defaultValues: {
      country: 'Saudi Arabia',
      facilityType: 'FAMILY',
    },
  });

  const onSubmit = async (data: BranchForm) => {
    try {
      setIsSubmitting(true);
      setError(null);

      const payload = {
        branchName: data.branchName,
        facilityType: data.facilityType,
        address: {
          street: data.street,
          city: data.city,
          state: data.state,
          postalCode: data.postalCode,
          country: data.country,
        },
      };

      await api.post(
        `/tenant/onboarding/steps/first-branch?organizationId=${organizationId}`,
        payload
      );
      onComplete();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create branch');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Add Your First Branch
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Add your first gym location or branch
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <form onSubmit={handleSubmit(onSubmit)}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            <TextField
              {...register('branchName')}
              label="Branch Name"
              fullWidth
              error={!!errors.branchName}
              helperText={errors.branchName?.message}
              placeholder="e.g., Main Branch, Downtown Location"
            />
          </Grid>

          <Grid item xs={12} md={4}>
            <TextField
              {...register('facilityType')}
              label="Facility Type"
              fullWidth
              select
              error={!!errors.facilityType}
              helperText={errors.facilityType?.message}
            >
              <MenuItem value="FAMILY">Family (Mixed Gender)</MenuItem>
              <MenuItem value="MALE_ONLY">Male Only</MenuItem>
              <MenuItem value="FEMALE_ONLY">Female Only</MenuItem>
            </TextField>
          </Grid>

          <Grid item xs={12}>
            <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>
              Branch Address
            </Typography>
          </Grid>

          <Grid item xs={12}>
            <TextField
              {...register('street')}
              label="Street Address"
              fullWidth
              error={!!errors.street}
              helperText={errors.street?.message}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              {...register('city')}
              label="City"
              fullWidth
              error={!!errors.city}
              helperText={errors.city?.message}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              {...register('state')}
              label="State/Province"
              fullWidth
              error={!!errors.state}
              helperText={errors.state?.message}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              {...register('postalCode')}
              label="Postal Code"
              fullWidth
              error={!!errors.postalCode}
              helperText={errors.postalCode?.message}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              {...register('country')}
              label="Country"
              fullWidth
              error={!!errors.country}
              helperText={errors.country?.message}
            />
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
