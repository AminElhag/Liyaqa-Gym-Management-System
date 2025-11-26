import React from 'react';
import { Box, Card, CardContent, Typography, Grid } from '@mui/material';

export const PlatformAnalyticsPage: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Platform Analytics
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Comprehensive analytics and insights across all tenants
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Tenant Growth Trend
              </Typography>
              <Box sx={{ p: 3, textAlign: 'center', color: 'text.secondary' }}>
                <Typography>Tenant growth chart will be implemented here</Typography>
                <Typography variant="caption">
                  Shows new tenants, churned tenants, and net growth over time
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Revenue by Plan
              </Typography>
              <Box sx={{ p: 3, textAlign: 'center', color: 'text.secondary' }}>
                <Typography>Revenue distribution by plan type</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Member Distribution
              </Typography>
              <Box sx={{ p: 3, textAlign: 'center', color: 'text.secondary' }}>
                <Typography>Total members across all tenants</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Subscription Status Overview
              </Typography>
              <Box sx={{ p: 3, textAlign: 'center', color: 'text.secondary' }}>
                <Typography>Subscription status breakdown across all tenants</Typography>
                <Typography variant="caption">Active, Trial, Suspended, Cancelled</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Geographic Distribution
              </Typography>
              <Box sx={{ p: 3, textAlign: 'center', color: 'text.secondary' }}>
                <Typography>Tenants distribution by city/region</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Churn Analysis
              </Typography>
              <Box sx={{ p: 3, textAlign: 'center', color: 'text.secondary' }}>
                <Typography>Churn rate and reasons over time</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
