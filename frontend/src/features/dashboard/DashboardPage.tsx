import React, { useEffect } from 'react';
import { useDashboardStore } from '../../store/dashboardStore';
import { useAuthStore } from '../../store/authStore';
import { DashboardMetricsGrid } from './components/DashboardMetricsGrid';
import { RecentProjectsList } from './components/RecentProjectsList';
import { RecentAnalysesList } from './components/RecentAnalysesList';
import { DashboardSkeleton } from './components/DashboardSkeleton';
import { DashboardEmptyState } from './components/DashboardEmptyState';
import { RefreshCw, AlertCircle } from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { user } = useAuthStore();
  const { overviewData, isLoading, error, fetchDashboardOverview } = useDashboardStore();

  useEffect(() => {
    fetchDashboardOverview();
  }, [fetchDashboardOverview]);

  const handleRefresh = () => {
    fetchDashboardOverview();
  };

  if (isLoading && !overviewData) {
    return <DashboardSkeleton />;
  }

  if (error && !overviewData) {
    return (
      <div className="bg-rose-950/40 border border-rose-800 text-rose-200 p-6 rounded-2xl text-center max-w-lg mx-auto space-y-4 my-12">
        <AlertCircle className="w-12 h-12 text-rose-500 mx-auto" />
        <div>
          <h3 className="font-bold text-lg">Error al cargar el dashboard</h3>
          <p className="text-sm text-rose-300 mt-1">{error}</p>
        </div>
        <button
          onClick={handleRefresh}
          className="inline-flex items-center space-x-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-lg text-sm transition"
        >
          <RefreshCw className="w-4 h-4" />
          <span>Reintentar</span>
        </button>
      </div>
    );
  }

  if (!overviewData) return null;

  // Render empty state if there are no projects created
  if (overviewData.totalProjects === 0) {
    return <DashboardEmptyState />;
  }

  return (
    <div className="space-y-8">
      {/* Welcome header and refresh action */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">
            ¡Hola, {user?.firstName || 'Usuario'}!
          </h1>
          <p className="text-slate-400 mt-1 text-sm">
            Aquí tienes un resumen general de la actividad de tu cuenta en OpinAI.
          </p>
        </div>

        <button
          onClick={handleRefresh}
          disabled={isLoading}
          className="inline-flex items-center space-x-1.5 px-4 py-2 bg-slate-900 border border-slate-800 hover:border-slate-700 hover:bg-slate-950 text-xs font-semibold text-slate-350 hover:text-white rounded-xl transition cursor-pointer disabled:opacity-50"
          title="Refrescar métricas"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
          <span>Actualizar</span>
        </button>
      </div>

      {/* KPI Cards Metrics Grid */}
      <DashboardMetricsGrid
        totalProjects={overviewData.totalProjects}
        totalAnalyses={overviewData.totalAnalyses}
        completedAnalyses={overviewData.completedAnalyses}
        activeAnalyses={overviewData.activeAnalyses}
      />

      {/* Recent Activity Grid split in columns */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 min-h-[350px]">
        <RecentProjectsList projects={overviewData.recentProjects} />
        <RecentAnalysesList analyses={overviewData.recentAnalyses} />
      </div>
    </div>
  );
};

export default DashboardPage;
