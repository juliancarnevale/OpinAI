import React from 'react';
import { NssIndicator } from './NssIndicator';
import { SentimentDonutChart } from './SentimentDonutChart';
import { SentimentTrendLineChart } from './SentimentTrendLineChart';
import type { SentimentDistribution, SentimentTrendPoint } from '../../types/DashboardAnalytics';

interface AnalyticsChartsGridProps {
  netSentimentScore: number;
  globalSentiment: SentimentDistribution;
  sentimentTrend: SentimentTrendPoint[];
}

export const AnalyticsChartsGrid: React.FC<AnalyticsChartsGridProps> = ({
  netSentimentScore,
  globalSentiment,
  sentimentTrend,
}) => {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
      <div className="lg:col-span-3">
        <NssIndicator score={netSentimentScore} />
      </div>
      <div className="lg:col-span-3">
        <SentimentDonutChart distribution={globalSentiment} />
      </div>
      <div className="lg:col-span-6">
        <SentimentTrendLineChart trend={sentimentTrend} />
      </div>
    </div>
  );
};

export default AnalyticsChartsGrid;
