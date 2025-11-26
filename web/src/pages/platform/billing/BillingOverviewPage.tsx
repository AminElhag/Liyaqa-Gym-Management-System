import React from 'react';
import { Box, Card, CardContent, Typography, Grid, Paper } from '@mui/material';
import { AttachMoney, Receipt, TrendingUp } from '@mui/icons-material';

export const BillingOverviewPage: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Billing Overview
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Monitor revenue, invoices, and billing metrics across all tenants
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={4}>
          <Paper sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <AttachMoney sx={{ fontSize: 40, color: 'primary.main', mr: 2 }} />
              <Box>
                <Typography variant="h4">SAR 125,450</Typography>
                <Typography variant="caption" color="text.secondary">
                  Total Revenue (This Month)
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>

        <Grid item xs={12} sm={6} md={4}>
          <Paper sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <Receipt sx={{ fontSize: 40, color: 'success.main', mr: 2 }} />
              <Box>
                <Typography variant="h4">234</Typography>
                <Typography variant="caption" color="text.secondary">
                  Invoices Generated
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>

        <Grid item xs={12} sm={6} md={4}>
          <Paper sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <TrendingUp sx={{ fontSize: 40, color: 'warning.main', mr: 2 }} />
              <Box>
                <Typography variant="h4">+15.3%</Typography>
                <Typography variant="caption" color="text.secondary">
                  Growth vs Last Month
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>

        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Revenue Breakdown
              </Typography>
              <Box sx={{ p: 3, textAlign: 'center', color: 'text.secondary' }}>
                <Typography>Revenue breakdown chart will be implemented here</Typography>
                <Typography variant="caption">
                  Shows revenue distribution by plan type and billing cycle
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Recent Invoices
              </Typography>
              <Box sx={{ p: 3, textAlign: 'center', color: 'text.secondary' }}>
                <Typography>Recent invoices list will be displayed here</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Outstanding Payments
              </Typography>
              <Box sx={{ p: 3, textAlign: 'center', color: 'text.secondary' }}>
                <Typography>Outstanding payments will be displayed here</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
