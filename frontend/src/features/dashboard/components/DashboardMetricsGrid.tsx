import React from 'react';
import { MetricCard } from './MetricCard';
import { FolderKanban, Database, CheckCircle2, RefreshCw } from 'lucide-react';

interface DashboardMetricsGridProps {
  totalProjects: number;
  totalAnalyses: number;
  completedAnalyses: number;
  activeAnalyses: number;
}

export const DashboardMetricsGrid: React.FC<DashboardMetricsGridProps> = ({
  totalProjects,
  totalAnalyses,
  completedAnalyses,
  activeAnalyses,
}) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <MetricCard
        title="Total Proyectos"
        value={totalProjects}
        icon={FolderKanban}
        iconColor="text-violet-400"
        iconBg="bg-violet-500/10"
      />
      <MetricCard
        title="Total Análisis"
        value={totalAnalyses}
        icon={Database}
        iconColor="text-blue-400"
        iconBg="bg-blue-500/10"
      />
      <MetricCard
        title="Análisis Completados"
        value={completedAnalyses}
        icon={CheckCircle2}
        iconColor="text-emerald-400"
        iconBg="bg-emerald-500/10"
      />
      <MetricCard
        title="Análisis Activos"
        value={activeAnalyses}
        icon={RefreshCw}
        iconColor="text-amber-400"
        iconBg="bg-amber-500/10"
      />
    </div>
  );
};
