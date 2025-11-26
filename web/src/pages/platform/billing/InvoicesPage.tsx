import React from 'react';
import {
  Box,
  Card,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Chip,
  IconButton,
} from '@mui/material';
import { Download, Visibility } from '@mui/icons-material';

export const InvoicesPage: React.FC = () => {
  // Mock data - will be replaced with actual API call
  const invoices = [
    {
      id: '1',
      invoiceNumber: 'INV-2024-001',
      tenantName: 'Elite Fitness Center',
      amount: 1500,
      status: 'PAID',
      dueDate: '2024-02-01',
      paidDate: '2024-01-28',
    },
    {
      id: '2',
      invoiceNumber: 'INV-2024-002',
      tenantName: 'Power Gym',
      amount: 500,
      status: 'PENDING',
      dueDate: '2024-02-05',
      paidDate: null,
    },
    {
      id: '3',
      invoiceNumber: 'INV-2024-003',
      tenantName: 'Fitness Pro',
      amount: 1500,
      status: 'OVERDUE',
      dueDate: '2024-01-25',
      paidDate: null,
    },
  ];

  const getStatusColor = (status: string): 'success' | 'warning' | 'error' => {
    switch (status) {
      case 'PAID':
        return 'success';
      case 'PENDING':
        return 'warning';
      case 'OVERDUE':
        return 'error';
      default:
        return 'warning';
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Invoices
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        View and manage all platform invoices
      </Typography>

      <Card>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Invoice #</TableCell>
              <TableCell>Tenant</TableCell>
              <TableCell>Amount</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Due Date</TableCell>
              <TableCell>Paid Date</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {invoices.map((invoice) => (
              <TableRow key={invoice.id} hover>
                <TableCell>
                  <Typography variant="body2" fontWeight="medium">
                    {invoice.invoiceNumber}
                  </Typography>
                </TableCell>
                <TableCell>{invoice.tenantName}</TableCell>
                <TableCell>SAR {invoice.amount.toLocaleString()}</TableCell>
                <TableCell>
                  <Chip label={invoice.status} color={getStatusColor(invoice.status)} size="small" />
                </TableCell>
                <TableCell>{new Date(invoice.dueDate).toLocaleDateString()}</TableCell>
                <TableCell>
                  {invoice.paidDate ? new Date(invoice.paidDate).toLocaleDateString() : '-'}
                </TableCell>
                <TableCell>
                  <IconButton>
                    <Visibility />
                  </IconButton>
                  <IconButton>
                    <Download />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Card>
    </Box>
  );
};
