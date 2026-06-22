import { CONSTANTS } from '../config/constants';

export const tokenStorage = {
  getToken(): string | null {
    return localStorage.getItem(CONSTANTS.TOKEN_STORAGE_KEY);
  },

  setToken(token: string): void {
    localStorage.setItem(CONSTANTS.TOKEN_STORAGE_KEY, token);
  },

  removeToken(): void {
    localStorage.removeItem(CONSTANTS.TOKEN_STORAGE_KEY);
  }
};
