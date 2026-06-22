import api from '../../../services/api';
import type { LoginRequest } from '../types/LoginRequest';
import type { RegisterRequest } from '../types/RegisterRequest';
import type { AuthResponse } from '../types/AuthResponse';
import type { UserProfile } from '../types/UserProfile';

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', credentials);
    return response.data;
  },

  async register(data: RegisterRequest): Promise<UserProfile> {
    const response = await api.post<UserProfile>('/auth/register', data);
    return response.data;
  },

  async getCurrentUser(): Promise<UserProfile> {
    const response = await api.get<UserProfile>('/auth/me');
    return response.data;
  }
};
