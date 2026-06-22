import React from 'react';
import { HelpCircle } from 'lucide-react';

interface NssIndicatorProps {
  score: number; // Rango [-100, +100]
}

export const NssIndicator: React.FC<NssIndicatorProps> = ({ score }) => {
  // Asegurar límites del score
  const safeScore = Math.max(-100, Math.min(100, score));

  // Determinar la categoría del score
  let tier = { label: 'Regular', color: 'text-amber-400', border: 'border-amber-500/20', bg: 'bg-amber-500/5' };
  if (safeScore >= 50) {
    tier = { label: 'Excelente', color: 'text-emerald-400', border: 'border-emerald-500/20', bg: 'bg-emerald-500/5' };
  } else if (safeScore >= 0) {
    tier = { label: 'Bueno', color: 'text-blue-400', border: 'border-blue-500/20', bg: 'bg-blue-500/5' };
  } else if (safeScore < -30) {
    tier = { label: 'Deficiente', color: 'text-rose-400', border: 'border-rose-500/20', bg: 'bg-rose-500/5' };
  }

  // Cálculos para el arco y la aguja del tacómetro
  const radius = 90;
  const cx = 120;
  const cy = 125;
  const arcLength = Math.PI * radius; // Longitud del arco semicircular (~282.74)

  // Mapear score [-100, 100] a porcentaje [0, 1]
  const pct = (safeScore + 100) / 200;
  const strokeDashoffset = arcLength * (1 - pct);

  // Calcular ángulo y coordenadas de la punta de la aguja (radio de la aguja: 75)
  const needleRadius = 75;
  const angleDegrees = 180 + pct * 180;
  const angleRad = (angleDegrees * Math.PI) / 180;
  const needleX = cx + needleRadius * Math.cos(angleRad);
  const needleY = cy + needleRadius * Math.sin(angleRad);

  return (
    <div className="bg-slate-900/50 backdrop-blur-xl border border-slate-800/80 rounded-2xl p-6 shadow-xl flex flex-col justify-between h-full relative group hover:border-slate-700/60 transition-all duration-300">
      {/* Encabezado */}
      <div className="flex justify-between items-start mb-4">
        <div>
          <h3 className="text-sm font-bold text-slate-350 tracking-wide uppercase flex items-center gap-1.5">
            Net Sentiment Score (NSS)
            <div className="relative group/tooltip">
              <HelpCircle className="w-3.5 h-3.5 text-slate-500 hover:text-slate-400 cursor-help transition-colors" />
              <div className="absolute left-1/2 -translate-x-1/2 bottom-full mb-2 w-48 p-2 bg-slate-950 border border-slate-800 text-[10px] text-slate-400 rounded-lg opacity-0 pointer-events-none group-hover/tooltip:opacity-100 transition-opacity duration-200 shadow-xl z-10 leading-normal">
                Métrica de satisfacción neta calculada como: ((Positivos - Negativos) / Total) * 100. Varía de -100 a +100.
              </div>
            </div>
          </h3>
          <p className="text-xs text-slate-500 mt-0.5">Indice de satisfacción global.</p>
        </div>
        <div className={`px-2.5 py-0.5 text-[10px] font-extrabold uppercase rounded-full border ${tier.border} ${tier.bg} ${tier.color}`}>
          {tier.label}
        </div>
      </div>

      {/* Tacómetro SVG */}
      <div className="flex justify-center items-center my-2 relative">
        <svg width="220" height="135" viewBox="0 0 240 145" className="overflow-visible">
          <defs>
            <linearGradient id="nssArcGradient" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#f43f5e" />   {/* Rose/Red */}
              <stop offset="30%" stopColor="#f59e0b" />  {/* Orange/Amber */}
              <stop offset="70%" stopColor="#3b82f6" />  {/* Blue */}
              <stop offset="100%" stopColor="#10b981" /> {/* Emerald/Green */}
            </linearGradient>
          </defs>

          {/* Arco de Fondo (Gris) */}
          <path
            d={`M ${cx - radius},${cy} A ${radius},${radius} 0 0,1 ${cx + radius},${cy}`}
            fill="none"
            stroke="#1e293b"
            strokeWidth="14"
            strokeLinecap="round"
          />

          {/* Arco con Gradiente de Valor */}
          <path
            d={`M ${cx - radius},${cy} A ${radius},${radius} 0 0,1 ${cx + radius},${cy}`}
            fill="none"
            stroke="url(#nssArcGradient)"
            strokeWidth="14"
            strokeLinecap="round"
            strokeDasharray={arcLength}
            strokeDashoffset={strokeDashoffset}
            className="transition-all duration-1000 ease-out"
          />

          {/* Divisiones (Ticks en -100, 0, 100) */}
          <line x1={cx - radius - 15} y1={cy} x2={cx - radius} y2={cy} stroke="#475569" strokeWidth="2" />
          <line x1={cx} y1={cy - radius + 15} x2={cx} y2={cy - radius} stroke="#475569" strokeWidth="2" />
          <line x1={cx + radius} y1={cy} x2={cx + radius + 15} y2={cy} stroke="#475569" strokeWidth="2" />

          {/* Textos auxiliares de límite */}
          <text x={cx - radius - 20} y={cy + 4} fill="#475569" fontSize="10" fontWeight="bold" textAnchor="end">-100</text>
          <text x={cx} y={cy - radius - 8} fill="#475569" fontSize="10" fontWeight="bold" textAnchor="middle">0</text>
          <text x={cx + radius + 20} y={cy + 4} fill="#475569" fontSize="10" fontWeight="bold" textAnchor="start">+100</text>

          {/* Aguja (Línea + Círculo central) */}
          <line
            x1={cx}
            y1={cy}
            x2={needleX}
            y2={needleY}
            stroke="#f1f5f9"
            strokeWidth="4.5"
            strokeLinecap="round"
            className="transition-all duration-1000 ease-out origin-center"
          />
          <circle cx={cx} cy={cy} r="8" fill="#f1f5f9" stroke="#0f172a" strokeWidth="2" />
          <circle cx={cx} cy={cy} r="3" fill="#0f172a" />
        </svg>

        {/* Score central numérico */}
        <div className="absolute bottom-2 flex flex-col items-center">
          <span className="text-3xl font-extrabold text-white tracking-tight">
            {safeScore > 0 ? `+${safeScore.toFixed(0)}` : safeScore.toFixed(0)}
          </span>
          <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mt-0.5">Score</span>
        </div>
      </div>
    </div>
  );
};

export default NssIndicator;
