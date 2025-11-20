import { apiClient } from './client';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    role: 'ADMIN' | 'TRAINER' | 'MEMBER';
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
