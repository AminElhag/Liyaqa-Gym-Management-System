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
  MenuItem,
} from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { api } from '@/services/api';

const inviteSchema = z.object({
  name: z.string().min(2, 'Name is required'),
  email: z.string().email('Invalid email address'),
  role: z.string().min(1, 'Role is required'),
});

const invitationsSchema = z.object({
  invitations: z.array(inviteSchema).min(1, 'At least one invitation is required'),
});

type InvitationsForm = z.infer<typeof invitationsSchema>;

interface StaffInvitationStepProps {
  onComplete: () => void;
  onSkip?: () => void;
}

export const StaffInvitationStep: React.FC<StaffInvitationStepProps> = ({
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
  } = useForm<InvitationsForm>({
    resolver: zodResolver(invitationsSchema),
    defaultValues: {
      invitations: [{ name: '', email: '', role: 'STAFF' }],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'invitations',
  });

  const addInvite = () => {
    append({ name: '', email: '', role: 'STAFF' });
  };

  const onSubmit = async (data: InvitationsForm) => {
    try {
      setIsSubmitting(true);
      setError(null);
      await api.post('/tenant/onboarding/steps/staff-invited', data);
      onComplete();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to send invitations');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Invite Your Team
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Invite staff members to join your gym management system
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Alert severity="info" sx={{ mb: 3 }}>
        You can skip this step and invite team members later from the settings page.
      </Alert>

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
                <Typography variant="h6">Team Member {index + 1}</Typography>
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
                    {...register(`invitations.${index}.name`)}
                    label="Full Name"
                    fullWidth
                    error={!!errors.invitations?.[index]?.name}
                    helperText={errors.invitations?.[index]?.name?.message}
                  />
                </Grid>

                <Grid item xs={12} md={6}>
                  <TextField
                    {...register(`invitations.${index}.email`)}
                    label="Email Address"
                    type="email"
                    fullWidth
                    error={!!errors.invitations?.[index]?.email}
                    helperText={errors.invitations?.[index]?.email?.message}
                  />
                </Grid>

                <Grid item xs={12}>
                  <TextField
                    {...register(`invitations.${index}.role`)}
                    label="Role"
                    fullWidth
                    select
                    error={!!errors.invitations?.[index]?.role}
                    helperText={errors.invitations?.[index]?.role?.message}
                  >
                    <MenuItem value="ADMIN">Admin</MenuItem>
                    <MenuItem value="STAFF">Staff</MenuItem>
                    <MenuItem value="TRAINER">Trainer</MenuItem>
                    <MenuItem value="RECEPTIONIST">Receptionist</MenuItem>
                  </TextField>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        ))}

        <Button
          startIcon={<AddIcon />}
          onClick={addInvite}
          variant="outlined"
          sx={{ mb: 3 }}
        >
          Add Another Member
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
            {isSubmitting ? 'Sending Invitations...' : 'Continue'}
          </Button>
        </Box>
      </form>
    </Box>
  );
};
