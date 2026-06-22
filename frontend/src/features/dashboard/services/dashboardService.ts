import api from '../../../services/api';
import type { DashboardOverview } from '../types/DashboardOverview';

export const dashboardService = {
  getOverview: async (): Promise<DashboardOverview> => {
    const response = await api.get<DashboardOverview>('/dashboard/overview');
    return response.data;
  },
};
