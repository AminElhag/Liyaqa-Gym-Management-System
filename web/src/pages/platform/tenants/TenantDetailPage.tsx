import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Chip,
  Button,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Divider,
} from '@mui/material';
import { Edit, Block, CheckCircle, Cancel } from '@mui/icons-material';
import { useTenantDetail } from '@/features/platform/hooks/useTenantDetail';

export const TenantDetailPage: React.FC = () => {
  const { tenantId } = useParams<{ tenantId: string }>();
  const navigate = useNavigate();
  const { tenant, loading, suspendTenant, activateTenant, cancelTenant } = useTenantDetail(tenantId);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogAction, setDialogAction] = useState<'suspend' | 'activate' | 'cancel' | null>(null);

  const handleAction = async () => {
    if (!dialogAction) return;

    let result;
    switch (dialogAction) {
      case 'suspend':
        result = await suspendTenant();
        break;
      case 'activate':
        result = await activateTenant();
        break;
      case 'cancel':
        result = await cancelTenant();
        break;
    }

    if (result.success) {
      setDialogOpen(false);
      setDialogAction(null);
    }
  };

  const openDialog = (action: 'suspend' | 'activate' | 'cancel') => {
    setDialogAction(action);
    setDialogOpen(true);
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

  const getStatusColor = (status: string): 'success' | 'info' | 'warning' | 'error' => {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'TRIAL':
        return 'info';
      case 'SUSPENDED':
        return 'warning';
      case 'CANCELLED':
        return 'error';
      default:
        return 'info';
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Box>
          <Typography variant="h4" gutterBottom>
            {tenant.name}
          </Typography>
          <Chip label={tenant.status} color={getStatusColor(tenant.status)} sx={{ mr: 1 }} />
          <Chip label={tenant.subscriptionPlan} variant="outlined" />
        </Box>
        <Box>
          <Button
            variant="outlined"
            startIcon={<Edit />}
            onClick={() => navigate(`/platform/tenants/${tenant.id}/edit`)}
            sx={{ mr: 1 }}
          >
            Edit
          </Button>
          {tenant.status === 'ACTIVE' && (
            <Button
              variant="outlined"
              color="warning"
              startIcon={<Block />}
              onClick={() => openDialog('suspend')}
              sx={{ mr: 1 }}
            >
              Suspend
            </Button>
          )}
          {tenant.status === 'SUSPENDED' && (
            <Button
              variant="outlined"
              color="success"
              startIcon={<CheckCircle />}
              onClick={() => openDialog('activate')}
              sx={{ mr: 1 }}
            >
              Activate
            </Button>
          )}
          {tenant.status !== 'CANCELLED' && (
            <Button
              variant="outlined"
              color="error"
              startIcon={<Cancel />}
              onClick={() => openDialog('cancel')}
            >
              Cancel
            </Button>
          )}
        </Box>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Business Information
              </Typography>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Business Name
                </Typography>
                <Typography variant="body1">{tenant.name}</Typography>
              </Box>

              {tenant.nameArabic && (
                <Box sx={{ mb: 2 }}>
                  <Typography variant="caption" color="text.secondary">
                    Business Name (Arabic)
                  </Typography>
                  <Typography variant="body1">{tenant.nameArabic}</Typography>
                </Box>
              )}

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Subdomain
                </Typography>
                <Typography variant="body1">{tenant.slug}.liyaqa.com</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Business Type
                </Typography>
                <Typography variant="body1">{tenant.businessType}</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Location
                </Typography>
                <Typography variant="body1">
                  {tenant.city}, {tenant.country}
                </Typography>
              </Box>

              {tenant.vatNumber && (
                <Box sx={{ mb: 2 }}>
                  <Typography variant="caption" color="text.secondary">
                    VAT Number
                  </Typography>
                  <Typography variant="body1">{tenant.vatNumber}</Typography>
                </Box>
              )}

              {tenant.commercialRegistration && (
                <Box sx={{ mb: 2 }}>
                  <Typography variant="caption" color="text.secondary">
                    Commercial Registration
                  </Typography>
                  <Typography variant="body1">{tenant.commercialRegistration}</Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Subscription Details
              </Typography>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Plan
                </Typography>
                <Typography variant="body1">{tenant.subscriptionPlan}</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Billing Cycle
                </Typography>
                <Typography variant="body1">{tenant.billingCycle}</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Members
                </Typography>
                <Typography variant="body1">
                  {tenant.totalMembers} / {tenant.maxMembers}
                </Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Subscription Start
                </Typography>
                <Typography variant="body1">
                  {new Date(tenant.subscriptionStartDate).toLocaleDateString()}
                </Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Subscription End
                </Typography>
                <Typography variant="body1">
                  {new Date(tenant.subscriptionEndDate).toLocaleDateString()}
                </Typography>
              </Box>

              {tenant.trialEndsAt && (
                <Box sx={{ mb: 2 }}>
                  <Typography variant="caption" color="text.secondary">
                    Trial Ends
                  </Typography>
                  <Typography variant="body1" color="warning.main">
                    {new Date(tenant.trialEndsAt).toLocaleDateString()}
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Owner Contact
              </Typography>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Owner Name
                </Typography>
                <Typography variant="body1">{tenant.ownerName}</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Owner Email
                </Typography>
                <Typography variant="body1">{tenant.ownerEmail}</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Owner Phone
                </Typography>
                <Typography variant="body1">{tenant.ownerPhone}</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Contact Email
                </Typography>
                <Typography variant="body1">{tenant.contactEmail}</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Contact Phone
                </Typography>
                <Typography variant="body1">{tenant.contactPhone}</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)}>
        <DialogTitle>
          Confirm {dialogAction === 'suspend' ? 'Suspend' : dialogAction === 'activate' ? 'Activate' : 'Cancel'} Tenant
        </DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to {dialogAction} this tenant? This action will affect their access
            to the platform.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button
            onClick={handleAction}
            color={dialogAction === 'cancel' ? 'error' : dialogAction === 'suspend' ? 'warning' : 'success'}
            variant="contained"
          >
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
