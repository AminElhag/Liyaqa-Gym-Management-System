import { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
  Alert,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  TablePagination,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Search as SearchIcon,
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '@/app/store/hooks';
import { fetchMembers, createMember, updateMember, deleteMember, Member } from '@/features/members/membersSlice';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const memberSchema = z.object({
  firstName: z.string().min(2, 'First name must be at least 2 characters'),
  lastName: z.string().min(2, 'Last name must be at least 2 characters'),
  email: z.string().email('Invalid email address'),
  phone: z.string().min(10, 'Phone number must be at least 10 digits'),
  dateOfBirth: z.string().optional(),
  status: z.enum(['ACTIVE', 'SUSPENDED', 'INACTIVE']),
});

type MemberForm = z.infer<typeof memberSchema>;

export default function MembersPage() {
  const dispatch = useAppDispatch();
  const { members, isLoading, error, totalCount, currentPage, pageSize } = useAppSelector((state) => state.members);

  const [openDialog, setOpenDialog] = useState(false);
  const [editingMember, setEditingMember] = useState<Member | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<MemberForm>({
    resolver: zodResolver(memberSchema),
    defaultValues: {
      status: 'ACTIVE',
    },
  });

  useEffect(() => {
    dispatch(fetchMembers({ page: page + 1, pageSize: rowsPerPage, search: searchQuery }));
  }, [dispatch, page, rowsPerPage, searchQuery]);

  const handleOpenDialog = (member?: Member) => {
    if (member) {
      setEditingMember(member);
      reset({
        firstName: member.firstName,
        lastName: member.lastName,
        email: member.email,
        phone: member.phone,
        dateOfBirth: member.dateOfBirth,
        status: member.status,
      });
    } else {
      setEditingMember(null);
      reset({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        dateOfBirth: '',
        status: 'ACTIVE',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingMember(null);
    reset();
  };

  const onSubmit = async (data: MemberForm) => {
    try {
      if (editingMember) {
        await dispatch(updateMember({ id: editingMember.id, data })).unwrap();
      } else {
        await dispatch(createMember(data)).unwrap();
      }
      handleCloseDialog();
      dispatch(fetchMembers({ page: page + 1, pageSize: rowsPerPage, search: searchQuery }));
    } catch (err) {
      console.error('Failed to save member:', err);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await dispatch(deleteMember(id)).unwrap();
      setDeleteConfirmId(null);
      dispatch(fetchMembers({ page: page + 1, pageSize: rowsPerPage, search: searchQuery }));
    } catch (err) {
      console.error('Failed to delete member:', err);
    }
  };

  const handleChangePage = (_: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'SUSPENDED':
        return 'warning';
      case 'INACTIVE':
        return 'default';
      default:
        return 'default';
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Members</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          Add Member
        </Button>
      </Box>

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          placeholder="Search members by name or email..."
          value={searchQuery}
          onChange={(e) => {
            setSearchQuery(e.target.value);
            setPage(0);
          }}
          InputProps={{
            startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />,
          }}
        />
      </Paper>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Phone</TableCell>
              <TableCell>Join Date</TableCell>
              <TableCell>Status</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 5 }}>
                  <CircularProgress />
                </TableCell>
              </TableRow>
            ) : members.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 5 }}>
                  <Typography color="text.secondary">
                    No members found. Click "Add Member" to create your first member.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              members.map((member) => (
                <TableRow key={member.id} hover>
                  <TableCell>
                    {member.firstName} {member.lastName}
                  </TableCell>
                  <TableCell>{member.email}</TableCell>
                  <TableCell>{member.phone}</TableCell>
                  <TableCell>{new Date(member.joinDate).toLocaleDateString()}</TableCell>
                  <TableCell>
                    <Chip
                      label={member.status}
                      color={getStatusColor(member.status) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell align="right">
                    <IconButton
                      size="small"
                      onClick={() => handleOpenDialog(member)}
                      color="primary"
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton
                      size="small"
                      onClick={() => setDeleteConfirmId(member.id)}
                      color="error"
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
        <TablePagination
          rowsPerPageOptions={[5, 10, 25, 50]}
          component="div"
          count={totalCount}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      </TableContainer>

      {/* Add/Edit Member Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogTitle>
            {editingMember ? 'Edit Member' : 'Add New Member'}
          </DialogTitle>
          <DialogContent>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
              <TextField
                {...register('firstName')}
                label="First Name"
                fullWidth
                error={!!errors.firstName}
                helperText={errors.firstName?.message}
              />
              <TextField
                {...register('lastName')}
                label="Last Name"
                fullWidth
                error={!!errors.lastName}
                helperText={errors.lastName?.message}
              />
              <TextField
                {...register('email')}
                label="Email"
                type="email"
                fullWidth
                error={!!errors.email}
                helperText={errors.email?.message}
              />
              <TextField
                {...register('phone')}
                label="Phone"
                fullWidth
                error={!!errors.phone}
                helperText={errors.phone?.message}
              />
              <TextField
                {...register('dateOfBirth')}
                label="Date of Birth"
                type="date"
                fullWidth
                InputLabelProps={{ shrink: true }}
                error={!!errors.dateOfBirth}
                helperText={errors.dateOfBirth?.message}
              />
              <TextField
                {...register('status')}
                label="Status"
                select
                fullWidth
                error={!!errors.status}
                helperText={errors.status?.message}
                defaultValue="ACTIVE"
              >
                <MenuItem value="ACTIVE">Active</MenuItem>
                <MenuItem value="SUSPENDED">Suspended</MenuItem>
                <MenuItem value="INACTIVE">Inactive</MenuItem>
              </TextField>
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseDialog}>Cancel</Button>
            <Button type="submit" variant="contained">
              {editingMember ? 'Update' : 'Create'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={!!deleteConfirmId} onClose={() => setDeleteConfirmId(null)}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete this member? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteConfirmId(null)}>Cancel</Button>
          <Button
            onClick={() => deleteConfirmId && handleDelete(deleteConfirmId)}
            color="error"
            variant="contained"
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
