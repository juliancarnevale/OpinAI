import { create } from 'zustand';
import { analysisService } from '../features/analysis/services/analysisService';
import type { AnalysisSummary, AnalysisDetail } from '../features/analysis/types/Analysis';
import type { CreateAnalysisRequest } from '../features/analysis/types/CreateAnalysisRequest';
import axios from 'axios';

interface AnalysisState {
  analyses: AnalysisSummary[];
  selectedAnalysis: AnalysisDetail | null;
  isLoading: boolean;
  error: string | null;
  fetchProjectAnalyses: (projectId: string) => Promise<void>;
  fetchAnalysis: (id: string) => Promise<AnalysisDetail>;
  createAnalysis: (projectId: string, data: CreateAnalysisRequest) => Promise<AnalysisSummary>;
  deleteAnalysis: (id: string) => Promise<void>;
  reprocessAnalysis: (id: string) => Promise<void>;
  clearSelectedAnalysis: () => void;
  clearError: () => void;
}

export const useAnalysisStore = create<AnalysisState>((set) => ({
  analyses: [],
  selectedAnalysis: null,
  isLoading: false,
  error: null,

  fetchProjectAnalyses: async (projectId) => {
    set({ isLoading: true, error: null });
    try {
      const analyses = await analysisService.getAnalysesByProject(projectId);
      set({ analyses, isLoading: false });
    } catch (err: unknown) {
      let message = 'Error al obtener la lista de análisis.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        message = err.response.data.message;
      } else if (err instanceof Error) {
        message = err.message;
      }
      set({ error: message, isLoading: false });
    }
  },

  fetchAnalysis: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const selectedAnalysis = await analysisService.getAnalysisById(id);
      set({ selectedAnalysis, isLoading: false });
      return selectedAnalysis;
    } catch (err: unknown) {
      let message = 'Error al obtener los detalles del análisis.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        message = err.response.data.message;
      } else if (err instanceof Error) {
        message = err.message;
      }
      set({ error: message, isLoading: false });
      throw err;
    }
  },

  createAnalysis: async (projectId, data) => {
    set({ isLoading: true, error: null });
    try {
      const newAnalysis = await analysisService.createAnalysis(projectId, data);
      set((state) => ({
        analyses: [newAnalysis, ...state.analyses],
        isLoading: false,
      }));
      return newAnalysis;
    } catch (err: unknown) {
      let message = 'Error al crear el análisis.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        message = err.response.data.message;
      } else if (err instanceof Error) {
        message = err.message;
      }
      set({ error: message, isLoading: false });
      throw err;
    }
  },

  deleteAnalysis: async (id) => {
    set({ isLoading: true, error: null });
    try {
      await analysisService.deleteAnalysis(id);
      set((state) => ({
        analyses: state.analyses.filter((a) => a.id !== id),
        selectedAnalysis: state.selectedAnalysis?.id === id ? null : state.selectedAnalysis,
        isLoading: false,
      }));
    } catch (err: unknown) {
      let message = 'Error al eliminar el análisis.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        message = err.response.data.message;
      } else if (err instanceof Error) {
        message = err.message;
      }
      set({ error: message, isLoading: false });
      throw err;
    }
  },

  reprocessAnalysis: async (id) => {
    set({ isLoading: true, error: null });
    try {
      await analysisService.reprocessAnalysis(id);
      set((state) => {
        const updatedSelected = state.selectedAnalysis && state.selectedAnalysis.id === id
          ? {
              ...state.selectedAnalysis,
              status: 'PENDING' as const,
              overallSentiment: undefined,
              executiveSummary: undefined,
              keyIssues: undefined,
              improvementOpportunities: undefined,
              sentimentDistribution: undefined,
            }
          : state.selectedAnalysis;

        const updatedList = state.analyses.map((a) =>
          a.id === id ? { ...a, status: 'PENDING' as const } : a
        );

        return {
          selectedAnalysis: updatedSelected,
          analyses: updatedList,
          isLoading: false,
        };
      });
    } catch (err: unknown) {
      let message = 'Error al solicitar el reprocesamiento del análisis.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        message = err.response.data.message;
      } else if (err instanceof Error) {
        message = err.message;
      }
      set({ error: message, isLoading: false });
      throw err;
    }
  },

  clearSelectedAnalysis: () => set({ selectedAnalysis: null }),
  clearError: () => set({ error: null }),
}));
