import React from 'react';
import type { SentimentDistribution } from '../types/Analysis';

interface SentimentDistributionChartProps {
  distribution?: SentimentDistribution;
}

export const SentimentDistributionChart: React.FC<SentimentDistributionChartProps> = ({ distribution }) => {
  if (!distribution) return null;
  
  const { positive = 0, negative = 0, neutral = 0 } = distribution;
  const total = positive + negative + neutral;

  if (total === 0) {
    return (
      <div className="h-10 w-full bg-slate-800 rounded-xl flex items-center justify-center text-xs text-slate-500 border border-slate-800/80">
        No hay opiniones con sentimientos clasificados en este análisis.
      </div>
    );
  }

  const posPct = (positive / total) * 100;
  const neuPct = (neutral / total) * 100;
  const negPct = (negative / total) * 100;

  return (
    <div className="space-y-5">
      <h4 className="text-xs uppercase tracking-wider text-slate-400 font-bold">Distribución de Sentimientos</h4>

      {/* Segmented Proportional Bar */}
      <div className="h-8 w-full bg-slate-950 rounded-full overflow-hidden flex border border-slate-800 shadow-inner">
        {positive > 0 && (
          <div
            style={{ width: `${posPct}%` }}
            className="bg-gradient-to-r from-emerald-600 to-teal-500 hover:opacity-90 transition-all duration-500 ease-out flex items-center justify-center text-[11px] font-bold text-white shadow-sm cursor-help select-none shrink-0"
            title={`Positivo: ${positive} comentarios (${posPct.toFixed(1)}%)`}
          >
            {posPct > 8 && `${posPct.toFixed(0)}%`}
          </div>
        )}
        {neutral > 0 && (
          <div
            style={{ width: `${neuPct}%` }}
            className="bg-gradient-to-r from-slate-500 to-slate-400 hover:opacity-90 transition-all duration-500 ease-out flex items-center justify-center text-[11px] font-bold text-white cursor-help select-none shrink-0 border-l border-r border-slate-900/30"
            title={`Neutro: ${neutral} comentarios (${neuPct.toFixed(1)}%)`}
          >
            {neuPct > 8 && `${neuPct.toFixed(0)}%`}
          </div>
        )}
        {negative > 0 && (
          <div
            style={{ width: `${negPct}%` }}
            className="bg-gradient-to-r from-rose-600 to-pink-500 hover:opacity-90 transition-all duration-500 ease-out flex items-center justify-center text-[11px] font-bold text-white shadow-sm cursor-help select-none shrink-0"
            title={`Negativo: ${negative} comentarios (${negPct.toFixed(1)}%)`}
          >
            {negPct > 8 && `${negPct.toFixed(0)}%`}
          </div>
        )}
      </div>

      {/* Grid Legend */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div className="flex items-center space-x-3 bg-slate-950/45 p-2.5 rounded-xl border border-slate-800 hover:border-slate-700 transition">
          <span className="w-3.5 h-3.5 rounded-full bg-gradient-to-r from-emerald-500 to-teal-400 shrink-0" />
          <div className="min-w-0">
            <p className="text-[10px] text-slate-500 font-semibold uppercase tracking-wider">Opiniones Positivas</p>
            <p className="text-sm font-bold text-white mt-0.5">
              {positive} <span className="text-xs text-slate-400 font-normal">({posPct.toFixed(1)}%)</span>
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-3 bg-slate-950/45 p-2.5 rounded-xl border border-slate-800 hover:border-slate-700 transition">
          <span className="w-3.5 h-3.5 rounded-full bg-gradient-to-r from-slate-500 to-slate-400 shrink-0" />
          <div className="min-w-0">
            <p className="text-[10px] text-slate-500 font-semibold uppercase tracking-wider">Opiniones Neutras</p>
            <p className="text-sm font-bold text-white mt-0.5">
              {neutral} <span className="text-xs text-slate-400 font-normal">({neuPct.toFixed(1)}%)</span>
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-3 bg-slate-950/45 p-2.5 rounded-xl border border-slate-800 hover:border-slate-700 transition">
          <span className="w-3.5 h-3.5 rounded-full bg-gradient-to-r from-rose-500 to-pink-500 shrink-0" />
          <div className="min-w-0">
            <p className="text-[10px] text-slate-500 font-semibold uppercase tracking-wider">Opiniones Negativas</p>
            <p className="text-sm font-bold text-white mt-0.5">
              {negative} <span className="text-xs text-slate-400 font-normal">({negPct.toFixed(1)}%)</span>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
