import React, { useState } from 'react';
import type { SentimentDistribution } from '../../types/DashboardAnalytics';

interface SentimentDonutChartProps {
  distribution: SentimentDistribution;
}

export const SentimentDonutChart: React.FC<SentimentDonutChartProps> = ({ distribution }) => {
  const { positive, negative, neutral } = distribution;
  const total = positive + negative + neutral;

  const [activeSegment, setActiveSegment] = useState<{ label: string; count: number; percentage: number } | null>(null);

  if (total === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-64 bg-slate-900/40 border border-slate-800 rounded-2xl p-6">
        <span className="text-slate-500 text-sm">Sin comentarios analizados en este periodo</span>
      </div>
    );
  }

  const posPct = (positive * 100) / total;
  const neuPct = (neutral * 100) / total;
  const negPct = (negative * 100) / total;

  const radius = 38;
  const strokeWidth = 8;
  const circumference = 2 * Math.PI * radius; // ~238.76

  // Calcular offsets acumulados
  let accumulatedPercent = 0;

  const segments = [
    { label: 'Positivo', count: positive, percentage: posPct, strokeColor: 'stroke-emerald-500 hover:stroke-emerald-400', colorClass: 'bg-emerald-500' },
    { label: 'Neutro', count: neutral, percentage: neuPct, strokeColor: 'stroke-amber-500 hover:stroke-amber-400', colorClass: 'bg-amber-500' },
    { label: 'Negativo', count: negative, percentage: negPct, strokeColor: 'stroke-red-500 hover:stroke-red-400', colorClass: 'bg-red-500' },
  ].filter(s => s.count > 0);

  return (
    <div className="bg-slate-900/60 backdrop-blur-xl border border-slate-800 p-6 rounded-2xl shadow-xl flex flex-col md:flex-row items-center gap-8">
      {/* Gráfico SVG */}
      <div className="relative w-48 h-48 flex items-center justify-center">
        <svg viewBox="0 0 100 100" className="w-full h-full transform -rotate-90">
          {/* Fondo */}
          <circle
            cx="50"
            cy="50"
            r={radius}
            fill="transparent"
            className="stroke-slate-950"
            strokeWidth={strokeWidth}
          />
          {/* Segmentos */}
          {segments.map((seg, idx) => {
            const strokeDasharray = `${(seg.percentage * circumference) / 100} ${circumference}`;
            const strokeDashoffset = -((accumulatedPercent * circumference) / 100);
            accumulatedPercent += seg.percentage;

            return (
              <circle
                key={idx}
                cx="50"
                cy="50"
                r={radius}
                fill="transparent"
                className={`transition-all duration-300 cursor-pointer ${seg.strokeColor}`}
                strokeWidth={activeSegment?.label === seg.label ? strokeWidth + 2 : strokeWidth}
                strokeDasharray={strokeDasharray}
                strokeDashoffset={strokeDashoffset}
                strokeLinecap="round"
                onMouseEnter={() => setActiveSegment({ label: seg.label, count: seg.count, percentage: seg.percentage })}
                onMouseLeave={() => setActiveSegment(null)}
              />
            );
          })}
        </svg>

        {/* Texto Central */}
        <div className="absolute inset-0 flex flex-col items-center justify-center text-center pointer-events-none">
          {activeSegment ? (
            <>
              <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">{activeSegment.label}</span>
              <span className="text-2xl font-bold text-slate-100">{activeSegment.percentage.toFixed(1)}%</span>
              <span className="text-[10px] text-slate-500">{activeSegment.count} opiniones</span>
            </>
          ) : (
            <>
              <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">Total</span>
              <span className="text-3xl font-extrabold text-slate-100">{total}</span>
              <span className="text-[10px] text-slate-500">Opiniones</span>
            </>
          )}
        </div>
      </div>

      {/* Leyenda */}
      <div className="flex-1 flex flex-col gap-3.5 w-full">
        <h4 className="text-sm font-semibold text-slate-300 mb-1">Distribución de Sentimientos</h4>
        {segments.map((seg, idx) => (
          <div
            key={idx}
            className={`flex items-center justify-between p-2.5 rounded-xl transition-all duration-300 border ${
              activeSegment?.label === seg.label
                ? 'bg-slate-950/80 border-slate-700'
                : 'bg-transparent border-transparent'
            }`}
            onMouseEnter={() => setActiveSegment({ label: seg.label, count: seg.count, percentage: seg.percentage })}
            onMouseLeave={() => setActiveSegment(null)}
          >
            <div className="flex items-center gap-3">
              <span className={`w-3 h-3 rounded-full ${seg.colorClass}`} />
              <span className="text-sm font-medium text-slate-300">{seg.label}</span>
            </div>
            <div className="text-right">
              <span className="text-sm font-bold text-slate-100">{seg.percentage.toFixed(1)}%</span>
              <span className="text-xs text-slate-500 block">{seg.count} opiniones</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
