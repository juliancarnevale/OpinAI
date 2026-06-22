import React from 'react';
import { Link } from 'react-router-dom';
import { Calendar, MessageSquare, ArrowRight } from 'lucide-react';
import type { AnalysisSummary } from '../types/Analysis';
import { AnalysisStatusBadge } from './AnalysisStatusBadge';
import { ROUTES } from '../../../config/constants';

interface AnalysisCardProps {
  analysis: AnalysisSummary;
}

export const AnalysisCard: React.FC<AnalysisCardProps> = ({ analysis }) => {
  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch (e) {
      return dateString;
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 hover:border-slate-700 transition duration-300 flex flex-col justify-between h-full group">
      <div className="space-y-4">
        <div className="flex justify-between items-start gap-4">
          <h4 className="text-lg font-bold text-white truncate group-hover:text-violet-400 transition w-full" title={analysis.title}>
            {analysis.title}
          </h4>
          <div className="shrink-0">
            <AnalysisStatusBadge status={analysis.status} />
          </div>
        </div>

        <div className="flex items-center space-x-6 text-sm text-slate-400">
          <div className="flex items-center space-x-1.5">
            <MessageSquare className="w-4 h-4 text-slate-500" />
            <span>{analysis.feedbackItemsCount} opiniones</span>
          </div>
          <div className="flex items-center space-x-1.5">
            <Calendar className="w-4 h-4 text-slate-500" />
            <span>{formatDate(analysis.createdAt)}</span>
          </div>
        </div>
      </div>

      <div className="mt-6 pt-4 border-t border-slate-800/80 flex justify-end">
        <Link
          to={ROUTES.ANALYSIS_DETAIL(analysis.id)}
          className="inline-flex items-center space-x-1 text-sm font-semibold text-violet-400 hover:text-violet-300 transition"
        >
          <span>Ver Detalles</span>
          <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition" />
        </Link>
      </div>
    </div>
  );
};
