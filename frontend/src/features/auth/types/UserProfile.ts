export interface UserProfile {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: 'ROLE_USER' | 'ROLE_ADMIN';
}
