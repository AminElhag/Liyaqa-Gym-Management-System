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
  Card,
  CardContent,
} from '@mui/material';
import { api } from '@/services/api';

const paymentSchema = z.object({
  paymentGateway: z.string().min(1, 'Payment gateway is required'),
  apiKey: z.string().optional(),
  merchantId: z.string().optional(),
});

type PaymentForm = z.infer<typeof paymentSchema>;

interface PaymentSetupStepProps {
  onComplete: () => void;
  onSkip?: () => void;
}

export const PaymentSetupStep: React.FC<PaymentSetupStepProps> = ({
  onComplete,
  onSkip,
}) => {
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<PaymentForm>({
    resolver: zodResolver(paymentSchema),
    defaultValues: {
      paymentGateway: 'STRIPE',
    },
  });

  const selectedGateway = watch('paymentGateway');

  const onSubmit = async (data: PaymentForm) => {
    try {
      setIsSubmitting(true);
      setError(null);
      await api.post('/tenant/onboarding/steps/payment-setup', data);
      onComplete();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to setup payment');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Payment Gateway Setup
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Configure your payment gateway to accept member payments
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Alert severity="info" sx={{ mb: 3 }}>
        You can skip this step and configure payment settings later. Manual payment
        collection will be available until payment gateway is configured.
      </Alert>

      <form onSubmit={handleSubmit(onSubmit)}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <TextField
              {...register('paymentGateway')}
              label="Payment Gateway"
              fullWidth
              select
              error={!!errors.paymentGateway}
              helperText={errors.paymentGateway?.message}
            >
              <MenuItem value="STRIPE">Stripe</MenuItem>
              <MenuItem value="MOYASAR">Moyasar (Saudi Arabia)</MenuItem>
              <MenuItem value="HYPERPAY">HyperPay</MenuItem>
              <MenuItem value="PAYFORT">PayFort</MenuItem>
              <MenuItem value="TAP">Tap Payments</MenuItem>
              <MenuItem value="MANUAL">Manual Payment (Cash/Bank Transfer)</MenuItem>
            </TextField>
          </Grid>

          {selectedGateway !== 'MANUAL' && (
            <>
              <Grid item xs={12}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      {selectedGateway} Configuration
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                      You'll need to obtain API credentials from your {selectedGateway}{' '}
                      account dashboard.
                    </Typography>

                    <Grid container spacing={2}>
                      <Grid item xs={12}>
                        <TextField
                          {...register('apiKey')}
                          label="API Key / Secret Key"
                          fullWidth
                          type="password"
                          error={!!errors.apiKey}
                          helperText={
                            errors.apiKey?.message ||
                            'Your secret API key from payment gateway'
                          }
                          placeholder="sk_test_..."
                        />
                      </Grid>

                      <Grid item xs={12}>
                        <TextField
                          {...register('merchantId')}
                          label="Merchant ID / Publishable Key"
                          fullWidth
                          error={!!errors.merchantId}
                          helperText={
                            errors.merchantId?.message ||
                            'Your merchant ID or publishable key'
                          }
                          placeholder="pk_test_..."
                        />
                      </Grid>
                    </Grid>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12}>
                <Alert severity="warning">
                  Test mode credentials are recommended for initial setup. You can
                  switch to live credentials later from settings.
                </Alert>
              </Grid>
            </>
          )}

          <Grid item xs={12}>
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
              {onSkip && (
                <Button
                  variant="outlined"
                  onClick={onSkip}
                  disabled={isSubmitting}
                >
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
          </Grid>
        </Grid>
      </form>
    </Box>
  );
};
