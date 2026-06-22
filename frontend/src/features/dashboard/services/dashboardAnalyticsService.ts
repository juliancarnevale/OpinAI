import api from '../../../services/api';
import type { DashboardAnalytics } from '../types/DashboardAnalytics';

export const dashboardAnalyticsService = {
  getAnalytics: async (
    projectId?: string,
    startDate?: string,
    endDate?: string,
    signal?: AbortSignal
  ): Promise<DashboardAnalytics> => {
    const params = new URLSearchParams();
    if (projectId) params.append('projectId', projectId);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);

    const response = await api.get<DashboardAnalytics>('/dashboard/analytics', {
      params,
      signal,
    });
    return response.data;
  },
};
