import React from 'react';
import { ArrowUpRight, ArrowDownRight, Minus } from 'lucide-react';

interface SentimentDeltaWidgetProps {
  delta: number;
  className?: string;
}

export const SentimentDeltaWidget: React.FC<SentimentDeltaWidgetProps> = ({ delta, className = '' }) => {
  const formattedDelta = delta.toFixed(1);
  const isPositive = delta > 0;
  const isNegative = delta < 0;

  if (isPositive) {
    return (
      <div
        className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-bold transition-all duration-300 hover:scale-105 shadow-md shadow-emerald-500/5 ${className}`}
        title="Variación con respecto al período anterior equivalente"
      >
        <ArrowUpRight className="w-3.5 h-3.5" />
        <span>+{formattedDelta}%</span>
      </div>
    );
  }

  if (isNegative) {
    return (
      <div
        className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-bold transition-all duration-300 hover:scale-105 shadow-md shadow-rose-500/5 ${className}`}
        title="Variación con respecto al período anterior equivalente"
      >
        <ArrowDownRight className="w-3.5 h-3.5" />
        <span>{formattedDelta}%</span>
      </div>
    );
  }

  return (
    <div
      className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-800 border border-slate-700/80 text-slate-400 text-xs font-semibold ${className}`}
      title="Sin variación con respecto al período anterior equivalente"
    >
      <Minus className="w-3 h-3" />
      <span>{formattedDelta}%</span>
    </div>
  );
};

export default SentimentDeltaWidget;
