export interface SentimentDistribution {
  positive: number;
  negative: number;
  neutral: number;
}

export interface SentimentTrendPoint {
  date: string;
  positive: number;
  negative: number;
  neutral: number;
  analysisCount: number;
}

export interface ProjectSentiment {
  projectId: string;
  projectName: string;
  totalFeedbacks: number;
  sentimentDistribution: SentimentDistribution;
}

export interface KeyIssueData {
  issue: string;
  count: number;
  percentage: number;
}

export interface ImprovementOpportunityData {
  opportunity: string;
  count: number;
}

export interface DashboardAnalytics {
  totalProjects: number;
  totalAnalyses: number;
  completedAnalyses: number;
  activeAnalyses: number;
  totalFeedbacks: number;
  positiveRate: number; // Métrica derivada (%)
  netSentimentScore: number; // NSS (%)
  positiveRateDelta: number; // Delta (%)
  globalSentiment: SentimentDistribution;
  sentimentTrend: SentimentTrendPoint[];
  projectSentimentComparisons: ProjectSentiment[];
  topIssues: KeyIssueData[];
  topOpportunities: ImprovementOpportunityData[];
}
