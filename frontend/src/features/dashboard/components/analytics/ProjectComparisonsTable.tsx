import React from 'react';
import type { ProjectSentiment } from '../../types/DashboardAnalytics';
import { BarChart3 } from 'lucide-react';

interface ProjectComparisonsTableProps {
  comparisons: ProjectSentiment[];
}

export const ProjectComparisonsTable: React.FC<ProjectComparisonsTableProps> = ({ comparisons }) => {
  if (!comparisons || comparisons.length === 0) {
    return (
      <div className="bg-slate-900/60 backdrop-blur-xl border border-slate-800 p-6 rounded-2xl shadow-xl flex flex-col items-center justify-center h-64 text-center">
        <BarChart3 className="w-8 h-8 text-slate-600 mb-2" />
        <span className="text-slate-500 text-sm">No hay datos de comparación disponibles</span>
      </div>
    );
  }

  return (
    <div className="bg-slate-900/60 backdrop-blur-xl border border-slate-800 p-6 rounded-2xl shadow-xl flex flex-col w-full">
      <h4 className="text-sm font-semibold text-slate-300 mb-5 flex items-center gap-2">
        <BarChart3 className="w-4 h-4 text-indigo-500" />
        Comparativa de Proyectos
      </h4>

      <div className="overflow-x-auto">
        <table className="w-full border-collapse text-left">
          <thead>
            <tr className="border-b border-slate-800/80 text-xs font-bold text-slate-400 uppercase tracking-wider">
              <th className="pb-3 pr-4">Proyecto</th>
              <th className="pb-3 px-4 text-center">Comentarios</th>
              <th className="pb-3 pl-4 min-w-[160px] w-1/2">Distribución de Sentimiento</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/40">
            {comparisons.map((item) => {
              const { positive, negative, neutral } = item.sentimentDistribution;
              const total = positive + negative + neutral;
              
              const posPct = total > 0 ? (positive * 100) / total : 0;
              const neuPct = total > 0 ? (neutral * 100) / total : 0;
              const negPct = total > 0 ? (negative * 100) / total : 0;

              return (
                <tr key={item.projectId} className="hover:bg-slate-950/20 transition-all duration-300">
                  <td className="py-4 pr-4 font-semibold text-slate-200 text-sm">{item.projectName}</td>
                  <td className="py-4 px-4 text-center text-slate-400 font-bold text-sm">
                    {item.totalFeedbacks}
                  </td>
                  <td className="py-4 pl-4 align-middle">
                    {total > 0 ? (
                      <div className="flex flex-col gap-1 w-full">
                        {/* Barra de progreso apilada */}
                        <div className="flex w-full bg-slate-950 h-3 rounded-full overflow-hidden border border-slate-800">
                          {positive > 0 && (
                            <div
                              className="bg-emerald-500 h-full transition-all duration-300"
                              style={{ width: `${posPct}%` }}
                              title={`Positivo: ${posPct.toFixed(1)}%`}
                            />
                          )}
                          {neutral > 0 && (
                            <div
                              className="bg-amber-500 h-full transition-all duration-300"
                              style={{ width: `${neuPct}%` }}
                              title={`Neutro: ${neuPct.toFixed(1)}%`}
                            />
                          )}
                          {negative > 0 && (
                            <div
                              className="bg-red-500 h-full transition-all duration-300"
                              style={{ width: `${negPct}%` }}
                              title={`Negativo: ${negPct.toFixed(1)}%`}
                            />
                          )}
                        </div>
                        {/* Detalles numéricos */}
                        <div className="flex justify-between text-[10px] text-slate-500 font-semibold px-0.5">
                          <span className="text-emerald-500">{posPct.toFixed(0)}% Pos</span>
                          <span className="text-amber-500">{neuPct.toFixed(0)}% Neu</span>
                          <span className="text-red-500">{negPct.toFixed(0)}% Neg</span>
                        </div>
                      </div>
                    ) : (
                      <span className="text-slate-600 text-xs italic">Sin opiniones completadas</span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
