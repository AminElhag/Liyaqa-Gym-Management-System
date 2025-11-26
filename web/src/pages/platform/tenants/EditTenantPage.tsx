import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
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
  Alert,
  CircularProgress,
} from '@mui/material';
import { useTenantDetail } from '@/features/platform/hooks/useTenantDetail';

const updateTenantSchema = z.object({
  name: z.string().min(2, 'Name is required').optional(),
  nameArabic: z.string().optional(),
  businessType: z.enum(['GYM', 'FITNESS_CENTER', 'SPORTS_CLUB']).optional(),
  contactEmail: z.string().email('Invalid email address').optional(),
  contactPhone: z.string().min(10, 'Phone number must be at least 10 digits').optional(),
  city: z.string().min(2, 'City is required').optional(),
  country: z.string().optional(),
  vatNumber: z.string().optional(),
  commercialRegistration: z.string().optional(),
});

type UpdateTenantFormData = z.infer<typeof updateTenantSchema>;

export const EditTenantPage: React.FC = () => {
  const { tenantId } = useParams<{ tenantId: string }>();
  const navigate = useNavigate();
  const { tenant, loading, updateTenant, error } = useTenantDetail(tenantId);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<UpdateTenantFormData>({
    resolver: zodResolver(updateTenantSchema),
  });

  useEffect(() => {
    if (tenant) {
      reset({
        name: tenant.name,
        nameArabic: tenant.nameArabic,
        businessType: tenant.businessType,
        contactEmail: tenant.contactEmail,
        contactPhone: tenant.contactPhone,
        city: tenant.city,
        country: tenant.country,
        vatNumber: tenant.vatNumber,
        commercialRegistration: tenant.commercialRegistration,
      });
    }
  }, [tenant, reset]);

  const onSubmit = async (data: UpdateTenantFormData) => {
    const result = await updateTenant(data);
    if (result.success) {
      navigate(`/platform/tenants/${tenantId}`);
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  if (!tenant) {
    return (
      <Box>
        <Alert severity="error">Tenant not found</Alert>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Edit Tenant
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Update tenant information
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
                      value={tenant.slug}
                      label="Subdomain Slug"
                      fullWidth
                      disabled
                      helperText="Slug cannot be changed"
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      {...register('businessType')}
                      select
                      label="Business Type"
                      fullWidth
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
                      label="VAT Number"
                      fullWidth
                      error={!!errors.vatNumber}
                      helperText={errors.vatNumber?.message}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      {...register('commercialRegistration')}
                      label="Commercial Registration"
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
                  Contact Information
                </Typography>

                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <TextField
                      value={tenant.ownerName}
                      label="Owner Name"
                      fullWidth
                      disabled
                      helperText="Owner cannot be changed"
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      value={tenant.ownerEmail}
                      label="Owner Email"
                      fullWidth
                      disabled
                      helperText="Owner email cannot be changed"
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
                  Subscription Information
                </Typography>

                <Box sx={{ mb: 2 }}>
                  <Typography variant="caption" color="text.secondary">
                    Current Plan
                  </Typography>
                  <Typography variant="body1">{tenant.subscriptionPlan}</Typography>
                </Box>

                <Box sx={{ mb: 2 }}>
                  <Typography variant="caption" color="text.secondary">
                    Status
                  </Typography>
                  <Typography variant="body1">{tenant.status}</Typography>
                </Box>

                <Box sx={{ mb: 2 }}>
                  <Typography variant="caption" color="text.secondary">
                    Members
                  </Typography>
                  <Typography variant="body1">
                    {tenant.totalMembers} / {tenant.maxMembers}
                  </Typography>
                </Box>

                <Alert severity="info" sx={{ mt: 2 }}>
                  To change subscription plan or billing cycle, please contact platform
                  administrator.
                </Alert>
              </CardContent>
            </Card>

            <Box sx={{ mt: 3 }}>
              <Button type="submit" variant="contained" fullWidth size="large" disabled={loading}>
                {loading ? 'Updating...' : 'Update Tenant'}
              </Button>

              <Button
                variant="outlined"
                fullWidth
                sx={{ mt: 1 }}
                onClick={() => navigate(`/platform/tenants/${tenantId}`)}
                disabled={loading}
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
