import { create } from 'zustand';
import { dashboardAnalyticsService } from '../services/dashboardAnalyticsService';
import type { DashboardAnalytics } from '../types/DashboardAnalytics';
import axios from 'axios';

interface AnalyticsFilters {
  projectId: string | null;
  timeframe: '7d' | '30d' | 'all';
}

interface DashboardAnalyticsState {
  analyticsData: DashboardAnalytics | null;
  isLoading: boolean;
  error: string | null;
  filters: AnalyticsFilters;
  
  // Acciones puras de mutación de filtros (No invocan side effects automáticamente)
  setProjectId: (projectId: string | null) => void;
  setTimeframe: (timeframe: '7d' | '30d' | 'all') => void;
  resetFilters: () => void;
  
  // Acción de comunicación externa controlada con cancelación de request
  fetchAnalytics: () => Promise<void>;
}

// Controller persistente para cancelar peticiones previas en vuelo
let activeAbortController: AbortController | null = null;

export const useDashboardAnalyticsStore = create<DashboardAnalyticsState>((set, get) => ({
  analyticsData: null,
  isLoading: false,
  error: null,
  filters: {
    projectId: null,
    timeframe: '30d',
  },

  setProjectId: (projectId) => {
    set((state) => ({ filters: { ...state.filters, projectId } }));
  },

  setTimeframe: (timeframe) => {
    set((state) => ({ filters: { ...state.filters, timeframe } }));
  },

  resetFilters: () => {
    set({ filters: { projectId: null, timeframe: '30d' } });
  },

  fetchAnalytics: async () => {
    // 1. Cancelar cualquier petición en vuelo previa
    if (activeAbortController) {
      activeAbortController.abort();
    }
    
    activeAbortController = new AbortController();
    set({ isLoading: true, error: null });

    const { projectId, timeframe } = get().filters;

    // 2. Calcular rango de fechas con constructores JS correctos
    let startDate: string | undefined;
    const endDate = new Date().toISOString();

    if (timeframe === '7d') {
      const d = new Date();
      d.setDate(d.getDate() - 7);
      startDate = d.toISOString();
    } else if (timeframe === '30d') {
      const d = new Date();
      d.setDate(d.getDate() - 30);
      startDate = d.toISOString();
    }

    try {
      const data = await dashboardAnalyticsService.getAnalytics(
        projectId || undefined,
        startDate,
        endDate,
        activeAbortController.signal
      );
      set({ analyticsData: data, isLoading: false });
    } catch (err: unknown) {
      if (axios.isCancel(err)) {
        // Ignorar silenciosamente si la petición fue abortada intencionalmente
        return;
      }
      let message = 'Error al obtener analíticas.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        message = err.response.data.message;
      } else if (err instanceof Error) {
        message = err.message;
      }
      set({ error: message, isLoading: false });
    }
  },
}));
