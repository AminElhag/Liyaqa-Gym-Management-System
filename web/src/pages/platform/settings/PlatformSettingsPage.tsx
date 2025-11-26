import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  TextField,
  Button,
  Switch,
  FormControlLabel,
  Divider,
} from '@mui/material';

export const PlatformSettingsPage: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Platform Settings
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Configure platform-wide settings and preferences
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                General Settings
              </Typography>
              <Divider sx={{ mb: 3 }} />

              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <TextField
                    label="Platform Name"
                    fullWidth
                    defaultValue="Liyaqa Gym Management"
                  />
                </Grid>

                <Grid item xs={12}>
                  <TextField
                    label="Support Email"
                    type="email"
                    fullWidth
                    defaultValue="support@liyaqa.com"
                  />
                </Grid>

                <Grid item xs={12}>
                  <TextField
                    label="Support Phone"
                    fullWidth
                    defaultValue="+966 50 123 4567"
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Subscription Plans
              </Typography>
              <Divider sx={{ mb: 3 }} />

              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <TextField
                    label="Starter Plan Price (SAR/month)"
                    type="number"
                    fullWidth
                    defaultValue="500"
                  />
                </Grid>

                <Grid item xs={12} md={6}>
                  <TextField
                    label="Professional Plan Price (SAR/month)"
                    type="number"
                    fullWidth
                    defaultValue="1500"
                  />
                </Grid>

                <Grid item xs={12} md={6}>
                  <TextField
                    label="Trial Period (days)"
                    type="number"
                    fullWidth
                    defaultValue="14"
                  />
                </Grid>

                <Grid item xs={12} md={6}>
                  <TextField
                    label="Quarterly Discount (%)"
                    type="number"
                    fullWidth
                    defaultValue="5"
                  />
                </Grid>

                <Grid item xs={12} md={6}>
                  <TextField
                    label="Annual Discount (%)"
                    type="number"
                    fullWidth
                    defaultValue="15"
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Notifications
              </Typography>
              <Divider sx={{ mb: 3 }} />

              <FormControlLabel
                control={<Switch defaultChecked />}
                label="Send email notifications for new tenant registrations"
              />
              <FormControlLabel
                control={<Switch defaultChecked />}
                label="Send alerts for expiring subscriptions"
              />
              <FormControlLabel
                control={<Switch defaultChecked />}
                label="Send payment failure notifications"
              />
              <FormControlLabel
                control={<Switch />}
                label="Send weekly revenue reports"
              />
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                System Information
              </Typography>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Platform Version
                </Typography>
                <Typography variant="body1">v1.0.0</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Database Status
                </Typography>
                <Typography variant="body1" color="success.main">
                  Connected
                </Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Total Storage Used
                </Typography>
                <Typography variant="body1">12.5 GB</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Last Backup
                </Typography>
                <Typography variant="body1">2 hours ago</Typography>
              </Box>
            </CardContent>
          </Card>

          <Button variant="contained" fullWidth size="large">
            Save Settings
          </Button>
        </Grid>
      </Grid>
    </Box>
  );
};
