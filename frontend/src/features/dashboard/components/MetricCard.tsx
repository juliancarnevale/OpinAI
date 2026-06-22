import React from 'react';
import type { LucideIcon } from 'lucide-react';

interface MetricCardProps {
  title: string;
  value: number | string;
  icon: LucideIcon;
  iconColor: string;
  iconBg: string;
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  icon: Icon,
  iconColor,
  iconBg,
}) => {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl hover:border-slate-700 transition duration-300 flex items-center justify-between group">
      <div className="space-y-2">
        <p className="text-xs font-bold text-slate-500 uppercase tracking-wider">{title}</p>
        <p className="text-3xl font-extrabold text-white tracking-tight">{value}</p>
      </div>
      <div className={`p-4 rounded-xl ${iconBg} border border-white/5 transition-transform duration-300 group-hover:scale-105 shrink-0`}>
        <Icon className={`w-6 h-6 ${iconColor}`} />
      </div>
    </div>
  );
};
