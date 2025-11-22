import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Alert,
  Typography,
  Box,
} from '@mui/material';
import { useAppDispatch } from '@/app/store/hooks';
import { changePassword } from '@/features/auth/authSlice';

interface ChangePasswordModalProps {
  open: boolean;
  onClose?: () => void;
  required?: boolean; // If true, user cannot close the modal
}

export const ChangePasswordModal: React.FC<ChangePasswordModalProps> = ({
  open,
  onClose,
  required = false,
}) => {
  const dispatch = useAppDispatch();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // Validation
    if (!currentPassword || !newPassword || !confirmPassword) {
      setError('All fields are required');
      return;
    }

    if (newPassword.length < 8) {
      setError('New password must be at least 8 characters');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('New passwords do not match');
      return;
    }

    if (currentPassword === newPassword) {
      setError('New password must be different from current password');
      return;
    }

    setIsLoading(true);

    try {
      const result = await dispatch(
        changePassword({
          currentPassword,
          newPassword,
        })
      );

      if (changePassword.fulfilled.match(result)) {
        setSuccess(true);
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');

        // Close modal after 2 seconds
        setTimeout(() => {
          if (onClose && !required) {
            onClose();
          }
          setSuccess(false);
        }, 2000);
      } else {
        setError(
          result.error?.message || 'Failed to change password. Please try again.'
        );
      }
    } catch (err: any) {
      setError(err?.message || 'An unexpected error occurred');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Dialog
      open={open}
      onClose={required ? undefined : onClose}
      maxWidth="sm"
      fullWidth
      disableEscapeKeyDown={required}
    >
      <DialogTitle>
        {required ? 'Password Change Required' : 'Change Password'}
      </DialogTitle>
      <DialogContent>
        {required && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            You must change your password before continuing. This is required for
            security purposes.
          </Alert>
        )}

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" sx={{ mb: 2 }}>
            Password changed successfully!
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit}>
          <TextField
            label="Current Password"
            type="password"
            fullWidth
            margin="normal"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            disabled={isLoading || success}
            required
          />

          <TextField
            label="New Password"
            type="password"
            fullWidth
            margin="normal"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            disabled={isLoading || success}
            helperText="Must be at least 8 characters"
            required
          />

          <TextField
            label="Confirm New Password"
            type="password"
            fullWidth
            margin="normal"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            disabled={isLoading || success}
            required
          />
        </Box>
      </DialogContent>
      <DialogActions>
        {!required && (
          <Button onClick={onClose} disabled={isLoading || success}>
            Cancel
          </Button>
        )}
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={isLoading || success}
        >
          {isLoading ? 'Changing...' : 'Change Password'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
