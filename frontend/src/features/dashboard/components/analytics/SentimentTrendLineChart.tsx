import React, { useState, useRef } from 'react';
import type { SentimentTrendPoint } from '../../types/DashboardAnalytics';

interface SentimentTrendLineChartProps {
  trend: SentimentTrendPoint[];
}

export const SentimentTrendLineChart: React.FC<SentimentTrendLineChartProps> = ({ trend }) => {
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  if (!trend || trend.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-64 bg-slate-900/40 border border-slate-800 rounded-2xl p-6">
        <span className="text-slate-500 text-sm">Sin datos de tendencias disponibles</span>
      </div>
    );
  }

  // Dimensiones fijas para el viewBox del SVG (se escala responsivamente con CSS)
  const width = 600;
  const height = 250;
  const paddingLeft = 45;
  const paddingRight = 20;
  const paddingTop = 20;
  const paddingBottom = 40;

  const plotWidth = width - paddingLeft - paddingRight;
  const plotHeight = height - paddingTop - paddingBottom;

  const totalPoints = trend.length;

  // Encontrar el valor máximo absoluto para escalar el eje Y
  const maxRawValue = Math.max(
    ...trend.map((p) => Math.max(p.positive, p.negative, p.neutral)),
    5 // Mínimo 5 para evitar escalar demasiado si los números son pequeños
  );
  
  // Redondear el máximo a un múltiplo agradable
  const yAxisSteps = 4;
  const maxValue = Math.ceil(maxRawValue / yAxisSteps) * yAxisSteps;

  // Formatear fechas para mostrar en el eje X
  const formatDate = (dateStr: string) => {
    try {
      const [, month, day] = dateStr.split('-');
      return `${day}/${month}`;
    } catch {
      return dateStr;
    }
  };

  // Calcular las coordenadas de cada punto
  const calculatePoints = (selector: (p: SentimentTrendPoint) => number) => {
    return trend.map((point, index) => {
      const x = paddingLeft + (totalPoints > 1 ? (index / (totalPoints - 1)) * plotWidth : plotWidth / 2);
      const val = selector(point);
      const y = paddingTop + plotHeight - (val / maxValue) * plotHeight;
      return { x, y, point };
    });
  };

  const posPoints = calculatePoints((p) => p.positive);
  const neuPoints = calculatePoints((p) => p.neutral);
  const negPoints = calculatePoints((p) => p.negative);

  // Generar string de trazado SVG
  const generatePath = (points: { x: number; y: number }[]) => {
    if (points.length === 0) return '';
    return `M ${points[0].x} ${points[0].y} ` + points.slice(1).map((p) => `L ${p.x} ${p.y}`).join(' ');
  };

  // Generar string de área sombreada
  const generateArea = (points: { x: number; y: number }[]) => {
    if (points.length === 0) return '';
    const startX = points[0].x;
    const endX = points[points.length - 1].x;
    const bottomY = paddingTop + plotHeight;
    return `${generatePath(points)} L ${endX} ${bottomY} L ${startX} ${bottomY} Z`;
  };

  return (
    <div ref={containerRef} className="relative bg-slate-900/60 backdrop-blur-xl border border-slate-800 p-6 rounded-2xl shadow-xl flex flex-col w-full">
      <div className="flex items-center justify-between mb-4">
        <h4 className="text-sm font-semibold text-slate-300">Histórico de Sentimientos</h4>
        <div className="flex gap-4 text-xs font-semibold">
          <div className="flex items-center gap-1.5 text-emerald-400">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
            Positivo
          </div>
          <div className="flex items-center gap-1.5 text-amber-400">
            <span className="w-2.5 h-2.5 rounded-full bg-amber-500" />
            Neutro
          </div>
          <div className="flex items-center gap-1.5 text-red-400">
            <span className="w-2.5 h-2.5 rounded-full bg-red-500" />
            Negativo
          </div>
        </div>
      </div>

      <div className="relative w-full overflow-hidden">
        <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-auto overflow-visible">
          {/* Definir Degradados y Filtros */}
          <defs>
            <linearGradient id="grad-pos" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#10b981" stopOpacity="0.25" />
              <stop offset="100%" stopColor="#10b981" stopOpacity="0.00" />
            </linearGradient>
            <linearGradient id="grad-neu" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#f59e0b" stopOpacity="0.20" />
              <stop offset="100%" stopColor="#f59e0b" stopOpacity="0.00" />
            </linearGradient>
            <linearGradient id="grad-neg" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#ef4444" stopOpacity="0.25" />
              <stop offset="100%" stopColor="#ef4444" stopOpacity="0.00" />
            </linearGradient>
          </defs>

          {/* Rejilla de Fondo Horizontal */}
          {Array.from({ length: yAxisSteps + 1 }).map((_, index) => {
            const val = (maxValue / yAxisSteps) * index;
            const y = paddingTop + plotHeight - (val / maxValue) * plotHeight;
            return (
              <g key={index}>
                <line
                  x1={paddingLeft}
                  y1={y}
                  x2={width - paddingRight}
                  y2={y}
                  className="stroke-slate-800/60"
                  strokeWidth="1"
                  strokeDasharray="4 4"
                />
                <text
                  x={paddingLeft - 8}
                  y={y + 4}
                  textAnchor="end"
                  className="fill-slate-500 text-[10px] font-medium"
                >
                  {val.toFixed(0)}
                </text>
              </g>
            );
          })}

          {/* Eje X Etiquetas de Fechas (Reducidas si son demasiadas) */}
          {trend.map((point, index) => {
            // Mostrar etiqueta en el eje X según densidad
            const showLabel =
              totalPoints <= 8 ||
              index === 0 ||
              index === totalPoints - 1 ||
              (totalPoints <= 15 && index % 2 === 0) ||
              (totalPoints > 15 && index % 4 === 0);

            if (!showLabel) return null;

            const x = paddingLeft + (totalPoints > 1 ? (index / (totalPoints - 1)) * plotWidth : plotWidth / 2);
            return (
              <text
                key={index}
                x={x}
                y={height - paddingBottom + 16}
                textAnchor="middle"
                className="fill-slate-500 text-[10px] font-medium"
              >
                {formatDate(point.date)}
              </text>
            );
          })}

          {/* Áreas Sombreadas (Debajo de los trazados) */}
          <path d={generateArea(posPoints)} fill="url(#grad-pos)" />
          <path d={generateArea(neuPoints)} fill="url(#grad-neu)" />
          <path d={generateArea(negPoints)} fill="url(#grad-neg)" />

          {/* Trazados de Líneas */}
          <path d={generatePath(posPoints)} fill="none" className="stroke-emerald-500" strokeWidth="2" strokeLinecap="round" />
          <path d={generatePath(neuPoints)} fill="none" className="stroke-amber-500" strokeWidth="2" strokeLinecap="round" />
          <path d={generatePath(negPoints)} fill="none" className="stroke-red-500" strokeWidth="2" strokeLinecap="round" />

          {/* Línea e Indicadores flotantes de Hover */}
          {hoveredIndex !== null && (
            <g>
              <line
                x1={posPoints[hoveredIndex].x}
                y1={paddingTop}
                x2={posPoints[hoveredIndex].x}
                y2={height - paddingBottom}
                className="stroke-slate-700"
                strokeWidth="1.5"
              />
              <circle cx={posPoints[hoveredIndex].x} cy={posPoints[hoveredIndex].y} r="4.5" className="fill-emerald-400 stroke-slate-900" strokeWidth="1.5" />
              <circle cx={neuPoints[hoveredIndex].x} cy={neuPoints[hoveredIndex].y} r="4.5" className="fill-amber-400 stroke-slate-900" strokeWidth="1.5" />
              <circle cx={negPoints[hoveredIndex].x} cy={negPoints[hoveredIndex].y} r="4.5" className="fill-red-400 stroke-slate-900" strokeWidth="1.5" />
            </g>
          )}

          {/* Zonas de Detección de Hover Invisibles */}
          {trend.map((_, index) => {
            const itemWidth = plotWidth / (totalPoints - 1 || 1);
            const x = paddingLeft + (totalPoints > 1 ? (index / (totalPoints - 1)) * plotWidth : plotWidth / 2) - itemWidth / 2;

            return (
              <rect
                key={index}
                x={x}
                y={paddingTop}
                width={itemWidth}
                height={plotHeight}
                fill="transparent"
                className="cursor-pointer"
                onMouseEnter={() => setHoveredIndex(index)}
                onMouseLeave={() => setHoveredIndex(null)}
              />
            );
          })}
        </svg>
      </div>

      {/* Tooltip Absoluto en HTML */}
      {hoveredIndex !== null && (
        <div
          className="absolute z-20 bg-slate-950/95 border border-slate-700 px-3 py-2.5 rounded-xl shadow-2xl text-[11px] text-slate-300 w-44 pointer-events-none transition-all duration-150"
          style={{
            left: `${Math.min(posPoints[hoveredIndex].x + 10, width - 190)}px`,
            top: `${yAxisSteps * 8}px`,
          }}
        >
          <div className="font-bold text-slate-100 border-b border-slate-800 pb-1 mb-1.5 flex justify-between">
            <span>Fecha:</span>
            <span>{trend[hoveredIndex].date}</span>
          </div>
          <div className="flex justify-between items-center text-emerald-400 mb-0.5">
            <span>Positivos:</span>
            <span className="font-bold">{trend[hoveredIndex].positive}</span>
          </div>
          <div className="flex justify-between items-center text-amber-400 mb-0.5">
            <span>Neutros:</span>
            <span className="font-bold">{trend[hoveredIndex].neutral}</span>
          </div>
          <div className="flex justify-between items-center text-red-400 mb-1">
            <span>Negativos:</span>
            <span className="font-bold">{trend[hoveredIndex].negative}</span>
          </div>
          <div className="flex justify-between items-center text-slate-500 pt-1 border-t border-slate-900 font-medium">
            <span>Análisis:</span>
            <span>{trend[hoveredIndex].analysisCount}</span>
          </div>
        </div>
      )}
    </div>
  );
};
