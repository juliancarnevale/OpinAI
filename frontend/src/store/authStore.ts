import { create } from 'zustand';
import { authService } from '../features/auth/services/authService';
import type { LoginRequest } from '../features/auth/types/LoginRequest';
import type { RegisterRequest } from '../features/auth/types/RegisterRequest';
import type { UserProfile } from '../features/auth/types/UserProfile';
import { tokenStorage } from '../utils/tokenStorage';
import axios from 'axios';

interface AuthState {
  user: UserProfile | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<UserProfile>;
  logout: () => void;
  checkAuth: () => Promise<void>;
  clearError: () => void;
}

export const useAuthStore = create<AuthState>((set) => {
  // Inicializamos el token centralizadamente desde tokenStorage
  const initialToken = tokenStorage.getToken();

  return {
    user: null,
    token: initialToken,
    // Estado consistente: Inicializa en false hasta verificar el token con checkAuth()
    isAuthenticated: false,
    // Si hay un token guardado, iniciamos en estado de carga mientras checkAuth lo valida
    isLoading: !!initialToken,
    error: null,

    login: async (credentials) => {
      set({ isLoading: true, error: null });
      try {
        const response = await authService.login(credentials);
        tokenStorage.setToken(response.token);
        set({
          user: response.user,
          token: response.token,
          isAuthenticated: true,
          isLoading: false,
          error: null,
        });
      } catch (err: unknown) {
        let message = 'Error al iniciar sesión. Compruebe sus credenciales.';
        if (axios.isAxiosError(err) && err.response?.data?.message) {
          message = err.response.data.message;
        } else if (err instanceof Error) {
          message = err.message;
        }
        set({ error: message, isLoading: false, isAuthenticated: false });
        throw err;
      }
    },

    register: async (data) => {
      set({ isLoading: true, error: null });
      try {
        const userProfile = await authService.register(data);
        set({ isLoading: false, error: null });
        return userProfile;
      } catch (err: unknown) {
        let message = 'Error al registrar el usuario.';
        if (axios.isAxiosError(err) && err.response?.data?.message) {
          message = err.response.data.message;
        } else if (err instanceof Error) {
          message = err.message;
        }
        set({ error: message, isLoading: false });
        throw err;
      }
    },

    logout: () => {
      tokenStorage.removeToken();
      set({
        user: null,
        token: null,
        isAuthenticated: false,
        error: null,
      });
    },

    checkAuth: async () => {
      const token = tokenStorage.getToken();
      if (!token) {
        set({ user: null, token: null, isAuthenticated: false, isLoading: false });
        return;
      }

      set({ isLoading: true, error: null });
      try {
        const userProfile = await authService.getCurrentUser();
        set({
          user: userProfile,
          token: token,
          isAuthenticated: true,
          isLoading: false,
        });
      } catch (err: unknown) {
        let isSessionExpired = false;
        let message = 'Error de conexión con el servidor. Inténtelo de nuevo más tarde.';

        if (axios.isAxiosError(err)) {
          // Si el servidor responde explícitamente con 401 (No autorizado) o 403 (Prohibido)
          if (err.response?.status === 401 || err.response?.status === 403) {
            isSessionExpired = true;
            message = 'Su sesión ha expirado o es inválida. Inicie sesión de nuevo.';
          } else if (err.response) {
            message = err.response.data?.message || `Error del servidor (${err.response.status}).`;
          }
        } else if (err instanceof Error) {
          message = err.message;
        }

        if (isSessionExpired) {
          // Si la sesión expiró realmente, limpiamos los datos locales de forma segura
          tokenStorage.removeToken();
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: message,
          });
        } else {
          // Si es un error de red, timeout o error 5xx:
          // Mantenemos el token y la sesión local activa, e informamos del error de red
          set({
            isAuthenticated: true, // Mantenemos la sesión activa localmente
            isLoading: false,
            error: message,
          });
        }
      }
    },

    clearError: () => set({ error: null }),
  };
});
