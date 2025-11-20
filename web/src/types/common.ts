export type UserRole = 'ADMIN' | 'TRAINER' | 'MEMBER';

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  role: UserRole;
  dateOfBirth?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  profilePicture?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Member extends User {
  role: 'MEMBER';
  membershipNumber?: string;
  emergencyContact?: string;
  medicalNotes?: string;
}

export interface Trainer extends User {
  role: 'TRAINER';
  specialization?: string;
  bio?: string;
  certifications?: string[];
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface ApiError {
  message: string;
  status: number;
  errors?: Record<string, string[]>;
}
