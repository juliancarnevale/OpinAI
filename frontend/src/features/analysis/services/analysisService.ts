import api from '../../../services/api';
import type { AnalysisSummary, AnalysisDetail } from '../types/Analysis';
import type { CreateAnalysisRequest } from '../types/CreateAnalysisRequest';

export const analysisService = {
  getAnalysesByProject: async (projectId: string): Promise<AnalysisSummary[]> => {
    const response = await api.get<AnalysisSummary[]>(`/projects/${projectId}/analyses`);
    return response.data;
  },

  getAnalysisById: async (id: string): Promise<AnalysisDetail> => {
    const response = await api.get<AnalysisDetail>(`/analyses/${id}`);
    return response.data;
  },

  createAnalysis: async (projectId: string, data: CreateAnalysisRequest): Promise<AnalysisSummary> => {
    const response = await api.post<AnalysisSummary>(`/projects/${projectId}/analyses`, data);
    return response.data;
  },

  deleteAnalysis: async (id: string): Promise<void> => {
    await api.delete(`/analyses/${id}`);
  },

  reprocessAnalysis: async (id: string): Promise<void> => {
    await api.post(`/analyses/${id}/reprocess`);
  },
};
