import React from 'react';
import { useDashboardAnalyticsStore } from '../../store/dashboardAnalyticsStore';
import { useProjectStore } from '../../../../store/projectStore';
import { FolderKanban, Calendar } from 'lucide-react';

export const AnalyticsFilters: React.FC = () => {
  const { filters, setProjectId, setTimeframe } = useDashboardAnalyticsStore();
  const { projects } = useProjectStore();

  return (
    <div className="flex flex-col sm:flex-row gap-4 bg-slate-900/60 backdrop-blur-xl border border-slate-800 p-4 rounded-2xl shadow-xl">
      {/* Filtro por Proyecto */}
      <div className="flex-1">
        <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2 flex items-center gap-2">
          <FolderKanban className="w-3.5 h-3.5 text-indigo-400" />
          Filtrar por Proyecto
        </label>
        <div className="relative">
          <select
            value={filters.projectId || ''}
            onChange={(e) => setProjectId(e.target.value ? e.target.value : null)}
            className="w-full bg-slate-950/80 border border-slate-800 focus:border-indigo-500 rounded-xl px-4 py-2.5 text-sm text-slate-200 focus:outline-none appearance-none cursor-pointer transition-all duration-300"
          >
            <option value="">Todos los proyectos</option>
            {projects.map((proj) => (
              <option key={proj.id} value={proj.id}>
                {proj.name}
              </option>
            ))}
          </select>
          <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-4 text-slate-500">
            <svg className="fill-current h-4 w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
              <path d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z" />
            </svg>
          </div>
        </div>
      </div>

      {/* Rango de Tiempo */}
      <div className="sm:w-64">
        <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2 flex items-center gap-2">
          <Calendar className="w-3.5 h-3.5 text-pink-400" />
          Rango Temporal
        </label>
        <div className="relative">
          <select
            value={filters.timeframe}
            onChange={(e) => setTimeframe(e.target.value as '7d' | '30d' | 'all')}
            className="w-full bg-slate-950/80 border border-slate-800 focus:border-pink-500 rounded-xl px-4 py-2.5 text-sm text-slate-200 focus:outline-none appearance-none cursor-pointer transition-all duration-300"
          >
            <option value="7d">Últimos 7 días</option>
            <option value="30d">Últimos 30 días</option>
            <option value="all">Histórico completo</option>
          </select>
          <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-4 text-slate-500">
            <svg className="fill-current h-4 w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
              <path d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z" />
            </svg>
          </div>
        </div>
      </div>
    </div>
  );
};
