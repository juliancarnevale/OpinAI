import type { AnalysisStatus } from '../../analysis/types/AnalysisStatus';

export interface RecentProject {
  id: string;
  name: string;
  description?: string;
  createdAt: string;
  feedbackItemsCount: number;
}

export interface RecentAnalysis {
  id: string;
  title: string;
  projectName: string;
  status: AnalysisStatus;
  createdAt: string;
}

export interface DashboardOverview {
  totalProjects: number;
  totalAnalyses: number;
  completedAnalyses: number;
  activeAnalyses: number;
  recentProjects: RecentProject[];
  recentAnalyses: RecentAnalysis[];
}
