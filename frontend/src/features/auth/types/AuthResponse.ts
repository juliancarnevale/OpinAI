import type { UserProfile } from './UserProfile';

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: UserProfile;
}
