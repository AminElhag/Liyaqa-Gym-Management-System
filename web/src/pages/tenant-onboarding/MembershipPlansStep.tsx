import React, { useState } from 'react';
import { useFieldArray, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  Grid,
  IconButton,
  Card,
  CardContent,
} from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { api } from '@/services/api';

const planSchema = z.object({
  name: z.string().min(2, 'Plan name is required'),
  nameArabic: z.string().min(2, 'Arabic name is required'),
  durationMonths: z.number().min(1, 'Duration must be at least 1 month'),
  price: z.number().min(0, 'Price must be positive'),
  description: z.string().optional(),
});

const plansSchema = z.object({
  plans: z.array(planSchema).min(1, 'At least one plan is required'),
});

type PlansForm = z.infer<typeof plansSchema>;

interface MembershipPlansStepProps {
  onComplete: () => void;
  onSkip?: () => void;
}

export const MembershipPlansStep: React.FC<MembershipPlansStepProps> = ({
  onComplete,
  onSkip,
}) => {
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<PlansForm>({
    resolver: zodResolver(plansSchema),
    defaultValues: {
      plans: [
        {
          name: 'Monthly',
          nameArabic: 'شهري',
          durationMonths: 1,
          price: 200,
          description: 'Monthly membership',
        },
      ],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'plans',
  });

  const addPlan = () => {
    append({
      name: '',
      nameArabic: '',
      durationMonths: 1,
      price: 0,
      description: '',
    });
  };

  const onSubmit = async (data: PlansForm) => {
    try {
      setIsSubmitting(true);
      setError(null);
      await api.post('/tenant/onboarding/steps/membership-plans', data);
      onComplete();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create plans');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Create Membership Plans
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Set up your membership plans and pricing
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <form onSubmit={handleSubmit(onSubmit)}>
        {fields.map((field, index) => (
          <Card key={field.id} sx={{ mb: 3 }}>
            <CardContent>
              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  mb: 2,
                }}
              >
                <Typography variant="h6">Plan {index + 1}</Typography>
                {fields.length > 1 && (
                  <IconButton
                    color="error"
                    onClick={() => remove(index)}
                    size="small"
                  >
                    <DeleteIcon />
                  </IconButton>
                )}
              </Box>

              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <TextField
                    {...register(`plans.${index}.name`)}
                    label="Plan Name (English)"
                    fullWidth
                    error={!!errors.plans?.[index]?.name}
                    helperText={errors.plans?.[index]?.name?.message}
                  />
                </Grid>

                <Grid item xs={12} md={6}>
                  <TextField
                    {...register(`plans.${index}.nameArabic`)}
                    label="Plan Name (Arabic)"
                    fullWidth
                    error={!!errors.plans?.[index]?.nameArabic}
                    helperText={errors.plans?.[index]?.nameArabic?.message}
                  />
                </Grid>

                <Grid item xs={12} md={6}>
                  <TextField
                    {...register(`plans.${index}.durationMonths`, {
                      valueAsNumber: true,
                    })}
                    label="Duration (Months)"
                    type="number"
                    fullWidth
                    error={!!errors.plans?.[index]?.durationMonths}
                    helperText={errors.plans?.[index]?.durationMonths?.message}
                  />
                </Grid>

                <Grid item xs={12} md={6}>
                  <TextField
                    {...register(`plans.${index}.price`, {
                      valueAsNumber: true,
                    })}
                    label="Price (SAR)"
                    type="number"
                    fullWidth
                    error={!!errors.plans?.[index]?.price}
                    helperText={errors.plans?.[index]?.price?.message}
                  />
                </Grid>

                <Grid item xs={12}>
                  <TextField
                    {...register(`plans.${index}.description`)}
                    label="Description (Optional)"
                    fullWidth
                    multiline
                    rows={2}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        ))}

        <Button
          startIcon={<AddIcon />}
          onClick={addPlan}
          variant="outlined"
          sx={{ mb: 3 }}
        >
          Add Another Plan
        </Button>

        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
          {onSkip && (
            <Button variant="outlined" onClick={onSkip} disabled={isSubmitting}>
              Skip for Now
            </Button>
          )}
          <Button
            type="submit"
            variant="contained"
            size="large"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Saving...' : 'Continue'}
          </Button>
        </Box>
      </form>
    </Box>
  );
};
