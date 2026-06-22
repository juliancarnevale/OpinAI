import React from 'react';
import type { KeyIssueData } from '../../types/DashboardAnalytics';
import { AlertCircle } from 'lucide-react';

interface KeyIssuesListProps {
  issues: KeyIssueData[];
}

export const KeyIssuesList: React.FC<KeyIssuesListProps> = ({ issues }) => {
  if (!issues || issues.length === 0) {
    return (
      <div className="bg-slate-900/60 backdrop-blur-xl border border-slate-800 p-6 rounded-2xl shadow-xl flex flex-col items-center justify-center h-64 text-center">
        <AlertCircle className="w-8 h-8 text-slate-600 mb-2" />
        <span className="text-slate-500 text-sm">No se han identificado problemas clave todavía</span>
      </div>
    );
  }

  return (
    <div className="bg-slate-900/60 backdrop-blur-xl border border-slate-800 p-6 rounded-2xl shadow-xl flex flex-col w-full h-full">
      <h4 className="text-sm font-semibold text-slate-300 mb-5 flex items-center gap-2">
        <AlertCircle className="w-4 h-4 text-rose-500" />
        Temas Críticos Detectados (Key Issues)
      </h4>

      <div className="flex flex-col gap-4 overflow-y-auto pr-1 flex-1">
        {issues.map((item, idx) => (
          <div key={idx} className="flex flex-col">
            <div className="flex justify-between items-center text-xs font-semibold text-slate-300 mb-1.5">
              <span className="truncate max-w-[70%]">{item.issue}</span>
              <span className="text-slate-400">
                {item.percentage.toFixed(1)}% <span className="text-slate-600 font-normal">({item.count} análisis)</span>
              </span>
            </div>
            
            {/* Barra de progreso */}
            <div className="w-full bg-slate-950 rounded-full h-2 overflow-hidden border border-slate-800/80">
              <div
                className="bg-gradient-to-r from-rose-500 to-amber-500 h-full rounded-full transition-all duration-500"
                style={{ width: `${Math.min(item.percentage, 100)}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
