import React from 'react';

export const AnalyticsSkeleton: React.FC = () => {
  return (
    <div className="space-y-8 animate-pulse">
      {/* Cabecera Shimmer */}
      <div className="flex justify-between items-center pb-2">
        <div className="space-y-3 flex-1">
          <div className="h-7 bg-slate-850 rounded w-1/4" />
          <div className="h-3.5 bg-slate-850 rounded w-1/3" />
        </div>
        <div className="w-24 h-8 bg-slate-850 rounded-xl" />
      </div>

      {/* Filtros Shimmer */}
      <div className="h-20 bg-slate-900/40 border border-slate-800/80 rounded-2xl" />

      {/* Grid de KPI Cards Shimmer */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="h-24 bg-slate-900/40 border border-slate-800/80 rounded-2xl p-5 flex items-center justify-between">
            <div className="space-y-3 flex-1">
              <div className="h-2 bg-slate-850 rounded w-2/3" />
              <div className="h-6 bg-slate-850 rounded w-1/3" />
            </div>
            <div className="w-12 h-12 bg-slate-850 rounded-xl shrink-0" />
          </div>
        ))}
      </div>

      {/* Grid de Gráficos Shimmer (Gauge, Donut, Line Chart) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* NssIndicator skeleton (Gauge) */}
        <div className="lg:col-span-3 h-72 bg-slate-900/40 border border-slate-800/80 rounded-2xl" />

        {/* SentimentDonutChart skeleton */}
        <div className="lg:col-span-3 h-72 bg-slate-900/40 border border-slate-800/80 rounded-2xl" />

        {/* SentimentTrendLineChart skeleton */}
        <div className="lg:col-span-6 h-72 bg-slate-900/40 border border-slate-800/80 rounded-2xl" />
      </div>

      {/* Grid de Tablas Shimmer (Issues, Project Comparisons) */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="h-80 bg-slate-900/40 border border-slate-800/80 rounded-2xl" />
        <div className="h-80 bg-slate-900/40 border border-slate-800/80 rounded-2xl" />
      </div>
    </div>
  );
};

export default AnalyticsSkeleton;
