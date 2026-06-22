import React from 'react';
import type { SentimentDistribution, SentimentType } from '../types/Analysis';
import { SentimentDistributionChart } from './SentimentDistributionChart';
import {
  Sparkles,
  AlertTriangle,
  FileText,
  ThumbsUp,
  ThumbsDown,
  HelpCircle
} from 'lucide-react';

interface AnalysisAiResultProps {
  overallSentiment?: SentimentType;
  executiveSummary?: string;
  keyIssues?: string[];
  improvementOpportunities?: string[];
  sentimentDistribution?: SentimentDistribution;
}

export const AnalysisAiResult: React.FC<AnalysisAiResultProps> = ({
  overallSentiment,
  executiveSummary,
  keyIssues = [],
  improvementOpportunities = [],
  sentimentDistribution,
}) => {
  
  // Render badge for overall sentiment
  const renderSentimentBadge = () => {
    switch (overallSentiment) {
      case 'POSITIVE':
        return (
          <span className="inline-flex items-center space-x-1.5 px-3 py-1 bg-emerald-500/10 border border-emerald-500/25 text-emerald-400 text-xs font-bold rounded-lg uppercase tracking-wider">
            <ThumbsUp className="w-3.5 h-3.5" />
            <span>Predominantemente Positivo</span>
          </span>
        );
      case 'NEGATIVE':
        return (
          <span className="inline-flex items-center space-x-1.5 px-3 py-1 bg-rose-500/10 border border-rose-500/25 text-rose-400 text-xs font-bold rounded-lg uppercase tracking-wider">
            <ThumbsDown className="w-3.5 h-3.5" />
            <span>Predominantemente Negativo</span>
          </span>
        );
      case 'NEUTRAL':
        return (
          <span className="inline-flex items-center space-x-1.5 px-3 py-1 bg-slate-500/10 border border-slate-500/25 text-slate-400 text-xs font-bold rounded-lg uppercase tracking-wider">
            <HelpCircle className="w-3.5 h-3.5" />
            <span>Neutral / Mixto</span>
          </span>
        );
      default:
        return null;
    }
  };

  const hasIssues = keyIssues.length > 0;
  const hasOpportunities = improvementOpportunities.length > 0;
  const showListGrid = hasIssues || hasOpportunities;

  return (
    <div className="space-y-6">
      
      {/* Sentiment & Overview Banner */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl relative overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 bg-violet-600/5 rounded-full blur-3xl pointer-events-none" />
        
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-5 border-b border-slate-800/80">
          <div className="flex items-center space-x-2.5">
            <Sparkles className="w-5 h-5 text-violet-400 animate-pulse" />
            <h3 className="text-lg font-bold text-white">Resultados de Inteligencia Artificial</h3>
          </div>
          <div>{renderSentimentBadge()}</div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 pt-5">
          {/* Executive Summary */}
          {executiveSummary && (
            <div className="space-y-3">
              <div className="flex items-center space-x-2 text-slate-400">
                <FileText className="w-4.5 h-4.5 text-violet-400" />
                <h4 className="text-xs uppercase tracking-wider font-bold">Resumen Ejecutivo</h4>
              </div>
              <p className="text-slate-300 text-sm leading-relaxed bg-slate-950/20 p-4 rounded-xl border border-slate-800/60 font-medium">
                {executiveSummary}
              </p>
            </div>
          )}

          {/* Sentiment Chart */}
          {sentimentDistribution && (
            <div className="flex flex-col justify-center">
              <SentimentDistributionChart distribution={sentimentDistribution} />
            </div>
          )}
        </div>
      </div>

      {/* Issues & Opportunities Sections */}
      {showListGrid ? (
        <div className={`grid grid-cols-1 ${hasIssues && hasOpportunities ? 'md:grid-cols-2' : 'grid-cols-1'} gap-6`}>
          
          {/* Key Issues */}
          {hasIssues && (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl flex flex-col">
              <div className="flex items-center space-x-2.5 text-rose-400 border-b border-slate-800/80 pb-4 mb-4">
                <AlertTriangle className="w-5 h-5 shrink-0" />
                <h3 className="font-bold text-white">Problemas Clave Detectados</h3>
              </div>
              <ul className="space-y-3.5 flex-1">
                {keyIssues.map((issue, idx) => (
                  <li key={idx} className="flex items-start space-x-3 text-sm">
                    <span className="flex items-center justify-center w-5 h-5 rounded-full bg-rose-500/10 text-rose-400 text-xs font-bold shrink-0 mt-0.5 border border-rose-500/20">
                      {idx + 1}
                    </span>
                    <span className="text-slate-300 font-medium leading-relaxed">{issue}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* Improvement Opportunities */}
          {hasOpportunities && (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl flex flex-col">
              <div className="flex items-center space-x-2.5 text-emerald-400 border-b border-slate-800/80 pb-4 mb-4">
                <Sparkles className="w-5 h-5 shrink-0" />
                <h3 className="font-bold text-white">Oportunidades de Mejora</h3>
              </div>
              <ul className="space-y-3.5 flex-1">
                {improvementOpportunities.map((opportunity, idx) => (
                  <li key={idx} className="flex items-start space-x-3 text-sm">
                    <span className="flex items-center justify-center w-5 h-5 rounded-full bg-emerald-500/10 text-emerald-400 text-xs font-bold shrink-0 mt-0.5 border border-emerald-500/20">
                      {idx + 1}
                    </span>
                    <span className="text-slate-300 font-medium leading-relaxed">{opportunity}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

        </div>
      ) : (
        /* Empty states for both lists */
        <div className="bg-emerald-950/10 border border-emerald-900/35 text-emerald-400 p-5 rounded-2xl shadow-xl flex items-start space-x-4">
          <ThumbsUp className="w-6 h-6 shrink-0 text-emerald-500 mt-0.5" />
          <div>
            <h4 className="font-bold text-base text-white">Análisis limpio de conflictos</h4>
            <p className="text-sm text-emerald-350 mt-1">
              Gemini AI no ha detectado problemas críticos ni necesidades urgentes de mejora en este lote de opiniones. Las respuestas sugieren un desempeño estable y positivo en general.
            </p>
          </div>
        </div>
      )}

    </div>
  );
};
