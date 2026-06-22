import React from 'react';

export const ReportHistorySkeleton: React.FC = () => {
  return (
    <div className="space-y-6 animate-pulse">
      {/* Generador Placeholder */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
        <div className="h-5 bg-slate-800 rounded w-1/4"></div>
        <div className="h-3 bg-slate-800 rounded w-1/3"></div>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="h-20 bg-slate-800 rounded-xl"></div>
          <div className="h-20 bg-slate-800 rounded-xl"></div>
        </div>
        <div className="h-10 bg-slate-800 rounded w-32"></div>
      </div>

      {/* Historial Placeholder */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="p-6 border-b border-slate-800">
          <div className="h-5 bg-slate-800 rounded w-1/5"></div>
        </div>
        <div className="p-6 space-y-4">
          <div className="h-4 bg-slate-800 rounded w-full"></div>
          <div className="h-4 bg-slate-800 rounded w-5/6"></div>
          <div className="h-4 bg-slate-800 rounded w-4/5"></div>
        </div>
      </div>
    </div>
  );
};
