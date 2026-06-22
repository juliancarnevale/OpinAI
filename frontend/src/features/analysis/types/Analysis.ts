import type { FeedbackItem } from './FeedbackItem';
import type { AnalysisStatus } from './AnalysisStatus';

export type SentimentType = 'POSITIVE' | 'NEUTRAL' | 'NEGATIVE';

export interface SentimentDistribution {
  positive: number;
  negative: number;
  neutral: number;
}

export interface AnalysisSummary {
  id: string;
  projectId: string;
  title: string;
  status: AnalysisStatus;
  createdAt: string;
  updatedAt: string;
  feedbackItemsCount: number;
}

export interface AnalysisDetail {
  id: string;
  projectId: string;
  projectName: string;
  title: string;
  status: AnalysisStatus;
  createdAt: string;
  updatedAt: string;
  feedbackItemsCount: number;
  overallSentiment?: SentimentType;
  executiveSummary?: string;
  keyIssues?: string[];
  improvementOpportunities?: string[];
  sentimentDistribution?: SentimentDistribution;
  feedbackItems: FeedbackItem[];
}
