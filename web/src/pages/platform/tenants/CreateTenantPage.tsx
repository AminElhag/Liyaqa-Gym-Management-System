import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Box,
  Card,
  CardContent,
  Grid,
  TextField,
  Button,
  Typography,
  MenuItem,
  FormControlLabel,
  Switch,
  Alert,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useCreateTenant } from '@/features/platform/hooks/useCreateTenant';

const createTenantSchema = z.object({
  name: z.string().min(2, 'Name is required'),
  nameArabic: z.string().optional(),
  slug: z
    .string()
    .min(3, 'Slug must be at least 3 characters')
    .regex(/^[a-z0-9-]+$/, 'Only lowercase letters, numbers, and hyphens'),
  businessType: z.enum(['GYM', 'FITNESS_CENTER', 'SPORTS_CLUB']),
  plan: z.enum(['STARTER', 'PROFESSIONAL', 'ENTERPRISE']),
  billingCycle: z.enum(['MONTHLY', 'QUARTERLY', 'ANNUAL']),
  startWithTrial: z.boolean(),
  ownerName: z.string().min(2, 'Owner name is required'),
  ownerEmail: z.string().email('Invalid email address'),
  ownerPhone: z.string().min(10, 'Phone number must be at least 10 digits'),
  contactEmail: z.string().email('Invalid email address'),
  contactPhone: z.string().min(10, 'Phone number must be at least 10 digits'),
  city: z.string().min(2, 'City is required'),
  country: z.string().default('Saudi Arabia'),
  vatNumber: z.string().optional(),
  commercialRegistration: z.string().optional(),
});

type CreateTenantFormData = z.infer<typeof createTenantSchema>;

const getPlanLimitsText = (plan: string): string => {
  switch (plan) {
    case 'STARTER':
      return 'Up to 100 members, 5 staff accounts, Basic reporting';
    case 'PROFESSIONAL':
      return 'Up to 500 members, 20 staff accounts, Advanced reporting, Custom branding';
    case 'ENTERPRISE':
      return 'Unlimited members, Unlimited staff, Premium features, Dedicated support';
    default:
      return '';
  }
};

export const CreateTenantPage: React.FC = () => {
  const navigate = useNavigate();
  const { createTenant, isLoading, error } = useCreateTenant();

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<CreateTenantFormData>({
    resolver: zodResolver(createTenantSchema),
    defaultValues: {
      startWithTrial: true,
      billingCycle: 'MONTHLY',
      plan: 'STARTER',
      country: 'Saudi Arabia',
      businessType: 'GYM',
    },
  });

  const onSubmit = async (data: CreateTenantFormData) => {
    const result = await createTenant(data);
    if (result.success) {
      navigate(`/platform/tenants/${result.tenantId}`);
    }
  };

  const selectedPlan = watch('plan');

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Add New Tenant
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Create a new tenant organization with subscription plan
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <form onSubmit={handleSubmit(onSubmit)}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Business Information
                </Typography>

                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <TextField
                      {...register('name')}
                      label="Business Name (English)"
                      fullWidth
                      error={!!errors.name}
                      helperText={errors.name?.message}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      {...register('nameArabic')}
                      label="Business Name (Arabic)"
                      fullWidth
                      error={!!errors.nameArabic}
                      helperText={errors.nameArabic?.message}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      {...register('slug')}
                      label="Subdomain Slug"
                      fullWidth
                      error={!!errors.slug}
                      helperText={errors.slug?.message || 'Will be used as: slug.liyaqa.com'}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      {...register('businessType')}
                      select
                      label="Business Type"
                      fullWidth
                      defaultValue="GYM"
                      error={!!errors.businessType}
                      helperText={errors.businessType?.message}
                    >
                      <MenuItem value="GYM">Gym</MenuItem>
                      <MenuItem value="FITNESS_CENTER">Fitness Center</MenuItem>
                      <MenuItem value="SPORTS_CLUB">Sports Club</MenuItem>
                    </TextField>
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
                      {...register('country')}
                      label="Country"
                      fullWidth
                      error={!!errors.country}
                      helperText={errors.country?.message}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      {...register('vatNumber')}
                      label="VAT Number (Optional)"
                      fullWidth
                      error={!!errors.vatNumber}
                      helperText={errors.vatNumber?.message}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      {...register('commercialRegistration')}
                      label="Commercial Registration (Optional)"
                      fullWidth
                      error={!!errors.commercialRegistration}
                      helperText={errors.commercialRegistration?.message}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            <Card sx={{ mt: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Owner Account
                </Typography>

                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <TextField
                      {...register('ownerName')}
                      label="Owner Name"
                      fullWidth
                      error={!!errors.ownerName}
                      helperText={errors.ownerName?.message}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      {...register('ownerEmail')}
                      label="Owner Email"
                      type="email"
                      fullWidth
                      error={!!errors.ownerEmail}
                      helperText={errors.ownerEmail?.message}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      {...register('ownerPhone')}
                      label="Owner Phone"
                      fullWidth
                      error={!!errors.ownerPhone}
                      helperText={errors.ownerPhone?.message}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      {...register('contactEmail')}
                      label="Contact Email"
                      type="email"
                      fullWidth
                      error={!!errors.contactEmail}
                      helperText={errors.contactEmail?.message}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      {...register('contactPhone')}
                      label="Contact Phone"
                      fullWidth
                      error={!!errors.contactPhone}
                      helperText={errors.contactPhone?.message}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Subscription Plan
                </Typography>

                <TextField
                  {...register('plan')}
                  select
                  label="Plan"
                  fullWidth
                  margin="normal"
                  error={!!errors.plan}
                  helperText={errors.plan?.message}
                >
                  <MenuItem value="STARTER">Starter - SAR 500/month</MenuItem>
                  <MenuItem value="PROFESSIONAL">Professional - SAR 1,500/month</MenuItem>
                  <MenuItem value="ENTERPRISE">Enterprise - Custom</MenuItem>
                </TextField>

                <TextField
                  {...register('billingCycle')}
                  select
                  label="Billing Cycle"
                  fullWidth
                  margin="normal"
                  error={!!errors.billingCycle}
                  helperText={errors.billingCycle?.message}
                >
                  <MenuItem value="MONTHLY">Monthly</MenuItem>
                  <MenuItem value="QUARTERLY">Quarterly (5% off)</MenuItem>
                  <MenuItem value="ANNUAL">Annual (15% off)</MenuItem>
                </TextField>

                <FormControlLabel
                  control={<Switch {...register('startWithTrial')} defaultChecked />}
                  label="Start with 14-day trial"
                  sx={{ mt: 2 }}
                />

                {selectedPlan && (
                  <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
                    <Typography variant="subtitle2" gutterBottom>
                      Plan Limits:
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {getPlanLimitsText(selectedPlan)}
                    </Typography>
                  </Box>
                )}
              </CardContent>
            </Card>

            <Box sx={{ mt: 3 }}>
              <Button
                type="submit"
                variant="contained"
                fullWidth
                size="large"
                disabled={isLoading}
              >
                {isLoading ? 'Creating Tenant...' : 'Create Tenant'}
              </Button>

              <Button
                variant="outlined"
                fullWidth
                sx={{ mt: 1 }}
                onClick={() => navigate('/platform/tenants')}
                disabled={isLoading}
              >
                Cancel
              </Button>
            </Box>
          </Grid>
        </Grid>
      </form>
    </Box>
  );
};
