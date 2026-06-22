import { create } from 'zustand';
import { dashboardService } from '../features/dashboard/services/dashboardService';
import type { DashboardOverview } from '../features/dashboard/types/DashboardOverview';
import axios from 'axios';

interface DashboardState {
  overviewData: DashboardOverview | null;
  isLoading: boolean;
  error: string | null;
  fetchDashboardOverview: () => Promise<void>;
}

export const useDashboardStore = create<DashboardState>((set) => ({
  overviewData: null,
  isLoading: false,
  error: null,

  fetchDashboardOverview: async () => {
    set({ isLoading: true, error: null });
    try {
      const overviewData = await dashboardService.getOverview();
      set({ overviewData, isLoading: false });
    } catch (err: unknown) {
      let message = 'Error al obtener la información del dashboard.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        message = err.response.data.message;
      } else if (err instanceof Error) {
        message = err.message;
      }
      set({ error: message, isLoading: false });
    }
  },
}));
