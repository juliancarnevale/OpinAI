import React from 'react';

export const DashboardSkeleton: React.FC = () => {
  return (
    <div className="space-y-8 animate-pulse">
      {/* Metrics Grid Shimmer */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="bg-slate-900 border border-slate-800/60 rounded-2xl p-6 flex items-center justify-between">
            <div className="space-y-3 flex-1">
              <div className="h-3 bg-slate-850 rounded w-2/3" />
              <div className="h-8 bg-slate-850 rounded w-1/2" />
            </div>
            <div className="w-12 h-12 bg-slate-850 rounded-xl shrink-0 ml-4" />
          </div>
        ))}
      </div>

      {/* Activity Lists Shimmer */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Projects list shimmer */}
        <div className="bg-slate-900 border border-slate-800/60 rounded-2xl p-6 space-y-6">
          <div className="flex justify-between border-b border-slate-800 pb-4">
            <div className="h-4 bg-slate-850 rounded w-1/3" />
            <div className="h-4 bg-slate-850 rounded w-1/6" />
          </div>
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="p-4 bg-slate-950/20 border border-slate-800/40 rounded-xl flex items-center justify-between">
                <div className="space-y-2.5 flex-1 pr-4">
                  <div className="h-4 bg-slate-850 rounded w-1/2" />
                  <div className="h-3 bg-slate-850 rounded w-1/3" />
                </div>
                <div className="w-8 h-8 bg-slate-850 rounded-lg shrink-0" />
              </div>
            ))}
          </div>
        </div>

        {/* Analyses list shimmer */}
        <div className="bg-slate-900 border border-slate-800/60 rounded-2xl p-6 space-y-6">
          <div className="flex justify-between border-b border-slate-800 pb-4">
            <div className="h-4 bg-slate-850 rounded w-1/3" />
          </div>
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="p-4 bg-slate-950/20 border border-slate-800/40 rounded-xl flex items-center justify-between">
                <div className="space-y-2.5 flex-1 pr-4">
                  <div className="h-4 bg-slate-850 rounded w-2/3" />
                  <div className="h-3 bg-slate-850 rounded w-1/4" />
                </div>
                <div className="w-8 h-8 bg-slate-850 rounded-lg shrink-0" />
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
