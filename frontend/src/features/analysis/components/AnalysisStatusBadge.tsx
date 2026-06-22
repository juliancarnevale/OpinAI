import React from 'react';
import { Clock, RefreshCw, CheckCircle2, AlertCircle } from 'lucide-react';
import type { AnalysisStatus } from '../types/AnalysisStatus';

interface AnalysisStatusBadgeProps {
  status: AnalysisStatus;
  size?: 'sm' | 'md';
}

export const AnalysisStatusBadge: React.FC<AnalysisStatusBadgeProps> = ({ status, size = 'sm' }) => {
  const sizeClasses = size === 'sm' 
    ? 'px-2.5 py-0.5 text-xs' 
    : 'px-3 py-1 text-xs';

  const iconSize = size === 'sm' ? 'w-3 h-3' : 'w-3.5 h-3.5';

  switch (status) {
    case 'PENDING':
      return (
        <span className={`inline-flex items-center space-x-1.5 rounded-full font-medium bg-amber-500/10 text-amber-400 border border-amber-500/20 ${sizeClasses}`}>
          <Clock className={`${iconSize} animate-pulse`} />
          <span>Pendiente</span>
        </span>
      );
    case 'PROCESSING':
      return (
        <span className={`inline-flex items-center space-x-1.5 rounded-full font-medium bg-blue-500/10 text-blue-400 border border-blue-500/20 ${sizeClasses}`}>
          <RefreshCw className={`${iconSize} animate-spin`} />
          <span>Procesando</span>
        </span>
      );
    case 'COMPLETED':
      return (
        <span className={`inline-flex items-center space-x-1.5 rounded-full font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 ${sizeClasses}`}>
          <CheckCircle2 className={iconSize} />
          <span>Completado</span>
        </span>
      );
    case 'FAILED':
      return (
        <span className={`inline-flex items-center space-x-1.5 rounded-full font-medium bg-rose-500/10 text-rose-400 border border-rose-500/20 ${sizeClasses}`}>
          <AlertCircle className={iconSize} />
          <span>Fallido</span>
        </span>
      );
  }
};
