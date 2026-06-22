import api from '../../../services/api';
import type { Report } from '../types/Report';

export const reportService = {
  // Generar reporte
  createReport: async (projectId: string, format: 'PDF' | 'CSV', signal?: AbortSignal): Promise<Report> => {
    const response = await api.post<Report>(`/projects/${projectId}/reports`, null, {
      params: { format },
      signal
    });
    return response.data;
  },

  // Listar historial
  getReportsByProject: async (projectId: string, signal?: AbortSignal): Promise<Report[]> => {
    const response = await api.get<Report[]>(`/projects/${projectId}/reports`, { signal });
    return response.data;
  },

  // Eliminar reporte
  deleteReport: async (reportId: string, signal?: AbortSignal): Promise<void> => {
    await api.delete(`/reports/${reportId}`, { signal });
  },

  // Descargar archivo binario
  downloadReport: async (reportId: string, fileName: string, signal?: AbortSignal): Promise<void> => {
    const response = await api.get(`/reports/${reportId}/download`, {
      responseType: 'blob',
      signal
    });

    const contentType = response.headers['content-type'];
    const blob = new Blob([response.data], { type: typeof contentType === 'string' ? contentType : undefined });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();

    // Limpieza de memoria
    link.parentNode?.removeChild(link);
    window.URL.revokeObjectURL(url);
  }
};
