import React from 'react';
import { Link } from 'react-router-dom';
import type { RecentAnalysis } from '../types/DashboardOverview';
import { Database, ArrowRight, Clock, Folder } from 'lucide-react';
import { AnalysisStatusBadge } from '../../analysis/components/AnalysisStatusBadge';
import { ROUTES } from '../../../config/constants';

interface RecentAnalysesListProps {
  analyses: RecentAnalysis[];
}

export const RecentAnalysesList: React.FC<RecentAnalysesListProps> = ({ analyses }) => {
  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('es-ES', {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      });
    } catch (e) {
      return dateString;
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl flex flex-col h-full">
      <div className="flex items-center justify-between border-b border-slate-800/80 pb-4 mb-4 shrink-0">
        <div className="flex items-center space-x-2 text-violet-400">
          <Database className="w-5 h-5" />
          <h3 className="font-bold text-white text-base">Análisis Recientes</h3>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto space-y-4">
        {analyses.length === 0 ? (
          <div className="py-12 text-center text-slate-500 text-sm">
            No has iniciado análisis todavía.
          </div>
        ) : (
          analyses.map((analysis) => (
            <div
              key={analysis.id}
              className="p-4 bg-slate-950/40 border border-slate-850 rounded-xl hover:border-slate-800 hover:bg-slate-950/80 transition duration-200 flex items-center justify-between gap-4 group"
            >
              <div className="min-w-0 space-y-1.5 flex-1">
                <div className="flex flex-col sm:flex-row sm:items-center gap-2 justify-between">
                  <h4 className="font-bold text-white text-sm truncate group-hover:text-violet-400 transition" title={analysis.title}>
                    {analysis.title}
                  </h4>
                  <div className="shrink-0">
                    <AnalysisStatusBadge status={analysis.status} size="sm" />
                  </div>
                </div>

                <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-[11px] text-slate-500 font-semibold">
                  <div className="flex items-center space-x-1">
                    <Folder className="w-3.5 h-3.5 shrink-0 text-slate-500" />
                    <span className="truncate max-w-[120px]">{analysis.projectName}</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <Clock className="w-3.5 h-3.5 shrink-0 text-slate-500" />
                    <span>{formatDate(analysis.createdAt)}</span>
                  </div>
                </div>
              </div>

              <Link
                to={ROUTES.ANALYSIS_DETAIL(analysis.id)}
                className="p-2 bg-slate-900 border border-slate-800 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 hover:border-slate-700 transition shrink-0 group-hover:translate-x-0.5"
                title="Ver detalles de análisis"
              >
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
