import React, { useEffect, useState } from 'react';
import {
  Box,
  Stepper,
  Step,
  StepLabel,
  Card,
  Typography,
  Button,
  CircularProgress,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { OrganizationSetupStep } from './OrganizationSetupStep';
import { FirstBranchStep } from './FirstBranchStep';
import { MembershipPlansStep } from './MembershipPlansStep';
import { StaffInvitationStep } from './StaffInvitationStep';
import { PaymentSetupStep } from './PaymentSetupStep';
import { OnboardingCompleteStep } from './OnboardingCompleteStep';
import { api } from '@/services/api';

interface OnboardingStep {
  id: string;
  step: string;
  status: 'NOT_STARTED' | 'PENDING' | 'COMPLETED';
  completedAt: string | null;
  data: Record<string, any> | null;
}

interface OnboardingProgress {
  tenantId: string;
  steps: OnboardingStep[];
  completedSteps: number;
  totalSteps: number;
  percentComplete: number;
  currentStep: OnboardingStep | null;
  isComplete: boolean;
}

const STEP_COMPONENTS: Record<string, React.ComponentType<any>> = {
  ORGANIZATION_SETUP: OrganizationSetupStep,
  FIRST_BRANCH: FirstBranchStep,
  MEMBERSHIP_PLANS: MembershipPlansStep,
  STAFF_INVITED: StaffInvitationStep,
  PAYMENT_SETUP: PaymentSetupStep,
  COMPLETE: OnboardingCompleteStep,
};

const STEP_LABELS: Record<string, string> = {
  ACCOUNT_VERIFIED: 'Account Verified',
  ORGANIZATION_SETUP: 'Setup Organization',
  FIRST_BRANCH: 'Add Branch',
  MEMBERSHIP_PLANS: 'Create Plans',
  STAFF_INVITED: 'Invite Staff',
  PAYMENT_SETUP: 'Payment Setup',
  COMPLETE: 'Complete',
};

export const OnboardingWizard: React.FC = () => {
  const navigate = useNavigate();
  const [progress, setProgress] = useState<OnboardingProgress | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeStepIndex, setActiveStepIndex] = useState(0);

  useEffect(() => {
    loadProgress();
  }, []);

  const loadProgress = async () => {
    try {
      setLoading(true);
      const response = await api.get('/tenant/onboarding/progress');
      const progressData: OnboardingProgress = response.data;
      setProgress(progressData);

      // Find the active step index
      if (progressData.currentStep) {
        const index = progressData.steps.findIndex(
          (s) => s.step === progressData.currentStep?.step
        );
        setActiveStepIndex(Math.max(0, index));
      } else if (progressData.isComplete) {
        setActiveStepIndex(progressData.steps.length - 1);
      }
    } catch (error) {
      console.error('Failed to load onboarding progress:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleStepComplete = async () => {
    await loadProgress();
  };

  const handleSkipStep = async (step: string) => {
    try {
      await api.post(`/tenant/onboarding/skip/${step}`);
      await loadProgress();
    } catch (error) {
      console.error('Failed to skip step:', error);
    }
  };

  const handleFinishOnboarding = () => {
    navigate('/dashboard');
  };

  if (loading || !progress) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  const currentStep = progress.steps[activeStepIndex];
  const StepComponent = currentStep ? STEP_COMPONENTS[currentStep.step] : null;

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default', py: 4 }}>
      <Box sx={{ maxWidth: 1200, mx: 'auto', px: 3 }}>
        {/* Header */}
        <Box sx={{ mb: 4, textAlign: 'center' }}>
          <Typography variant="h3" gutterBottom>
            Welcome to Liyaqa!
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Let's get your gym management system set up in just a few steps
          </Typography>
        </Box>

        {/* Progress Stepper */}
        <Card sx={{ mb: 4, p: 3 }}>
          <Stepper activeStep={activeStepIndex} alternativeLabel>
            {progress.steps
              .filter((s) => s.step !== 'ACCOUNT_VERIFIED')
              .map((step) => (
                <Step key={step.step} completed={step.status === 'COMPLETED'}>
                  <StepLabel>{STEP_LABELS[step.step] || step.step}</StepLabel>
                </Step>
              ))}
          </Stepper>

          <Box sx={{ mt: 2, textAlign: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              {progress.completedSteps} of {progress.totalSteps} steps completed
              ({progress.percentComplete}%)
            </Typography>
          </Box>
        </Card>

        {/* Current Step Content */}
        <Card sx={{ p: 4 }}>
          {progress.isComplete ? (
            <OnboardingCompleteStep onFinish={handleFinishOnboarding} />
          ) : StepComponent ? (
            <StepComponent
              onComplete={handleStepComplete}
              onSkip={
                currentStep.step === 'STAFF_INVITED' ||
                currentStep.step === 'PAYMENT_SETUP'
                  ? () => handleSkipStep(currentStep.step)
                  : undefined
              }
              organizationId={
                progress.steps.find((s) => s.step === 'ORGANIZATION_SETUP')
                  ?.data?.organizationId
              }
            />
          ) : (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="h6" color="text.secondary">
                Step configuration not found
              </Typography>
            </Box>
          )}
        </Card>
      </Box>
    </Box>
  );
};
