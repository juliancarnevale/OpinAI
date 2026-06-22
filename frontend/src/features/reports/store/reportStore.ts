import { create } from 'zustand';
import { reportService } from '../services/reportService';
import type { Report } from '../types/Report';
import axios from 'axios';

interface ReportState {
  reports: Report[];
  isLoading: boolean;
  isGenerating: boolean;
  error: string | null;
  errorStatus: number | null;
  fetchReports: (projectId: string, signal?: AbortSignal) => Promise<void>;
  generateReport: (projectId: string, format: 'PDF' | 'CSV', signal?: AbortSignal) => Promise<void>;
  downloadReport: (reportId: string, fileName: string, signal?: AbortSignal) => Promise<void>;
  deleteReport: (projectId: string, reportId: string, signal?: AbortSignal) => Promise<void>;
  clearError: () => void;
}

export const useReportStore = create<ReportState>((set, get) => ({
  reports: [],
  isLoading: false,
  isGenerating: false,
  error: null,
  errorStatus: null,

  fetchReports: async (projectId, signal) => {
    set({ isLoading: true, error: null, errorStatus: null });
    try {
      const reports = await reportService.getReportsByProject(projectId, signal);
      set({ reports, isLoading: false });
    } catch (err: unknown) {
      if (axios.isCancel(err)) return;
      let message = 'Error al obtener el historial de reportes.';
      let status: number | null = null;
      if (axios.isAxiosError(err)) {
        status = err.response?.status || null;
        message = err.response?.data?.message || message;
      }
      set({ error: message, errorStatus: status, isLoading: false });
    }
  },

  generateReport: async (projectId, format, signal) => {
    set({ isGenerating: true, error: null, errorStatus: null });
    try {
      await reportService.createReport(projectId, format, signal);
      // Backend as source of truth
      await get().fetchReports(projectId, signal);
    } catch (err: unknown) {
      if (axios.isCancel(err)) return;
      let message = 'Error al generar el reporte.';
      let status: number | null = null;
      if (axios.isAxiosError(err)) {
        status = err.response?.status || null;
        message = err.response?.data?.message || message;
      }
      set({ error: message, errorStatus: status });
      throw err;
    } finally {
      set({ isGenerating: false });
    }
  },

  downloadReport: async (reportId, fileName, signal) => {
    try {
      await reportService.downloadReport(reportId, fileName, signal);
    } catch (err: unknown) {
      if (axios.isCancel(err)) return;
      let message = 'Error al descargar el archivo.';
      let status: number | null = null;
      if (axios.isAxiosError(err)) {
        status = err.response?.status || null;
      }
      set({ error: message, errorStatus: status });
    }
  },

  deleteReport: async (projectId, reportId, signal) => {
    set({ isLoading: true, error: null, errorStatus: null });
    try {
      await reportService.deleteReport(reportId, signal);
      // Backend as source of truth
      await get().fetchReports(projectId, signal);
    } catch (err: unknown) {
      if (axios.isCancel(err)) return;
      let message = 'Error al eliminar el reporte.';
      let status: number | null = null;
      if (axios.isAxiosError(err)) {
        status = err.response?.status || null;
        message = err.response?.data?.message || message;
      }
      set({ error: message, errorStatus: status, isLoading: false });
      throw err;
    }
  },

  clearError: () => set({ error: null, errorStatus: null })
}));
