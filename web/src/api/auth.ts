import { apiClient } from './client';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: {
    id: string;
    email: string;
    role: string;
    organizationId: string;
    branchId: string | null;
    memberId: string | null;
    staffId: string | null;
    isEmailVerified: boolean;
    mustChangePassword: boolean;
  };
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
}

export const authApi = {
  login: (data: LoginRequest) =>
    apiClient.post<LoginResponse>('/auth/login', data),

  register: (data: RegisterRequest) =>
    apiClient.post<LoginResponse>('/auth/register', data),

  getCurrentUser: () =>
    apiClient.get<LoginResponse['user']>('/auth/me'),

  logout: () =>
    apiClient.post<void>('/auth/logout'),

  refreshToken: () =>
    apiClient.post<{ token: string }>('/auth/refresh'),
};
