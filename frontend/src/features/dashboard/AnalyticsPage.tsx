import React, { useEffect } from 'react';
import { useDashboardAnalyticsStore } from './store/dashboardAnalyticsStore';
import { useProjectStore } from '../../store/projectStore';
import { AnalyticsFilters } from './components/analytics/AnalyticsFilters';
import { KeyIssuesList } from './components/analytics/KeyIssuesList';
import { ProjectComparisonsTable } from './components/analytics/ProjectComparisonsTable';
import { AnalyticsKpisGrid } from './components/analytics/AnalyticsKpisGrid';
import { AnalyticsChartsGrid } from './components/analytics/AnalyticsChartsGrid';
import { AnalyticsSkeleton } from './components/analytics/AnalyticsSkeleton';
import { RefreshCw, AlertCircle, BarChart2 } from 'lucide-react';

export const AnalyticsPage: React.FC = () => {
  const { fetchProjects } = useProjectStore();
  const {
    analyticsData,
    isLoading,
    error,
    filters,
    fetchAnalytics,
  } = useDashboardAnalyticsStore();

  // 1. Carga inicial de datos
  useEffect(() => {
    fetchProjects();
  }, [fetchProjects]);

  // 2. Recargar analíticas ante cambios en los filtros
  useEffect(() => {
    fetchAnalytics();
  }, [filters.projectId, filters.timeframe, fetchAnalytics]);

  const handleRefresh = () => {
    fetchAnalytics();
  };

  if (isLoading && !analyticsData) {
    return <AnalyticsSkeleton />;
  }

  if (error && !analyticsData) {
    return (
      <div className="bg-rose-950/40 border border-rose-800 text-rose-200 p-6 rounded-2xl text-center max-w-lg mx-auto space-y-4 my-12">
        <AlertCircle className="w-12 h-12 text-rose-500 mx-auto" />
        <div>
          <h3 className="font-bold text-lg">Error al cargar analíticas</h3>
          <p className="text-sm text-rose-300 mt-1">{error}</p>
        </div>
        <button
          onClick={handleRefresh}
          className="inline-flex items-center space-x-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-lg text-sm transition cursor-pointer"
        >
          <RefreshCw className="w-4 h-4" />
          <span>Reintentar</span>
        </button>
      </div>
    );
  }

  if (!analyticsData) return null;

  // Si el usuario no tiene proyectos, o si no hay comentarios analizados
  const hasData = analyticsData.completedAnalyses > 0;

  return (
    <div className="space-y-8">
      {/* Cabecera */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight flex items-center gap-2">
            <BarChart2 className="w-8 h-8 text-indigo-500" />
            Panel de Analíticas
          </h1>
          <p className="text-slate-400 mt-1 text-sm">
            Explora las métricas de satisfacción, opiniones y temas críticos de tus proyectos.
          </p>
        </div>

        <button
          onClick={handleRefresh}
          disabled={isLoading}
          className="inline-flex items-center space-x-1.5 px-4 py-2 bg-slate-900 border border-slate-800 hover:border-slate-700 hover:bg-slate-950 text-xs font-semibold text-slate-350 hover:text-white rounded-xl transition cursor-pointer disabled:opacity-50"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
          <span>Actualizar</span>
        </button>
      </div>

      {/* Selector de Filtros */}
      <AnalyticsFilters />

      {!hasData ? (
        <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-12 text-center max-w-xl mx-auto space-y-4 my-8">
          <BarChart2 className="w-16 h-16 text-slate-700 mx-auto" />
          <div>
            <h3 className="font-bold text-xl text-slate-200">Sin analíticas disponibles</h3>
            <p className="text-sm text-slate-400 mt-2">
              Para ver gráficos y análisis de sentimientos, necesitas crear un proyecto y completar al menos un análisis de opiniones asíncrono con Gemini.
            </p>
          </div>
        </div>
      ) : (
        <>
          {/* Tarjetas de Métricas KPI */}
          <AnalyticsKpisGrid
            totalProjects={analyticsData.totalProjects}
            totalAnalyses={analyticsData.totalAnalyses}
            totalFeedbacks={analyticsData.totalFeedbacks}
            positiveRate={analyticsData.positiveRate}
            positiveRateDelta={analyticsData.positiveRateDelta}
            timeframe={filters.timeframe}
          />

          {/* Gráficas Principales */}
          <AnalyticsChartsGrid
            netSentimentScore={analyticsData.netSentimentScore}
            globalSentiment={analyticsData.globalSentiment}
            sentimentTrend={analyticsData.sentimentTrend}
          />

          {/* Detalles e Issues */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 min-h-[350px]">
            <KeyIssuesList issues={analyticsData.topIssues} />
            <ProjectComparisonsTable comparisons={analyticsData.projectSentimentComparisons} />
          </div>
        </>
      )}
    </div>
  );
};

export default AnalyticsPage;
