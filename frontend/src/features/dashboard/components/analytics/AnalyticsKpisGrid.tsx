import React from 'react';
import { Briefcase, BarChart2, MessageSquare, Heart } from 'lucide-react';
import { SentimentDeltaWidget } from './SentimentDeltaWidget';

interface AnalyticsKpisGridProps {
  totalProjects: number;
  totalAnalyses: number;
  totalFeedbacks: number;
  positiveRate: number;
  positiveRateDelta: number;
  timeframe: string;
}

export const AnalyticsKpisGrid: React.FC<AnalyticsKpisGridProps> = ({
  totalProjects,
  totalAnalyses,
  totalFeedbacks,
  positiveRate,
  positiveRateDelta,
  timeframe,
}) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
      {/* Proyectos */}
      <div className="bg-slate-900/50 border border-slate-800/80 p-5 rounded-2xl flex items-center gap-4 shadow-lg">
        <div className="p-3.5 bg-blue-500/10 border border-blue-500/20 rounded-xl">
          <Briefcase className="w-6 h-6 text-blue-400" />
        </div>
        <div>
          <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block">Proyectos Totales</span>
          <span className="text-2xl font-extrabold text-white">{totalProjects}</span>
        </div>
      </div>

      {/* Análisis Ejecutados */}
      <div className="bg-slate-900/50 border border-slate-800/80 p-5 rounded-2xl flex items-center gap-4 shadow-lg">
        <div className="p-3.5 bg-indigo-500/10 border border-indigo-500/20 rounded-xl">
          <BarChart2 className="w-6 h-6 text-indigo-400" />
        </div>
        <div>
          <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block">Análisis Ejecutados</span>
          <span className="text-2xl font-extrabold text-white">{totalAnalyses}</span>
        </div>
      </div>

      {/* Total Feedbacks */}
      <div className="bg-slate-900/50 border border-slate-800/80 p-5 rounded-2xl flex items-center gap-4 shadow-lg">
        <div className="p-3.5 bg-pink-500/10 border border-pink-500/20 rounded-xl">
          <MessageSquare className="w-6 h-6 text-pink-400" />
        </div>
        <div>
          <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block">Opiniones Procesadas</span>
          <span className="text-2xl font-extrabold text-white">{totalFeedbacks}</span>
        </div>
      </div>

      {/* Tasa de Positividad */}
      <div className="bg-slate-900/50 border border-slate-800/80 p-5 rounded-2xl flex items-center justify-between shadow-lg">
        <div className="flex items-center gap-4">
          <div className="p-3.5 bg-emerald-500/10 border border-emerald-500/20 rounded-xl">
            <Heart className="w-6 h-6 text-emerald-400" />
          </div>
          <div>
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block">Tasa de Positividad</span>
            <span className="text-2xl font-extrabold text-white">
              {positiveRate.toFixed(1)}%
            </span>
          </div>
        </div>
        {timeframe !== 'all' && (
          <SentimentDeltaWidget delta={positiveRateDelta} />
        )}
      </div>
    </div>
  );
};

export default AnalyticsKpisGrid;
