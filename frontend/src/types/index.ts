export interface User {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: 'ROLE_USER' | 'ROLE_ADMIN';
}

export interface Project {
  id: string;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export type { AnalysisStatus } from '../features/analysis/types/AnalysisStatus';
import type { SentimentType, SentimentDistribution, AnalysisSummary, AnalysisDetail } from '../features/analysis/types/Analysis';
export type { SentimentType, SentimentDistribution, AnalysisSummary, AnalysisDetail };

// Alias for backward compatibility if any generic file imports Analysis
export type Analysis = AnalysisSummary;

export interface FeedbackItem {
  id: string;
  analysisId: string;
  content: string;
  sourceType: 'MANUAL' | 'CSV';
  externalMetadata?: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}
