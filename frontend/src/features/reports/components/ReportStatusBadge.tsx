import React from 'react';
import { Loader2, CheckCircle2, AlertTriangle } from 'lucide-react';

interface Props {
  status: 'GENERATING' | 'READY' | 'FAILED';
}

export const ReportStatusBadge: React.FC<Props> = ({ status }) => {
  const styles = {
    GENERATING: 'bg-amber-500/10 text-amber-400 border border-amber-500/20',
    READY: 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20',
    FAILED: 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
  };

  return (
    <span className={`inline-flex items-center space-x-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${styles[status]}`}>
      {status === 'GENERATING' && (
        <>
          <Loader2 className="w-3 h-3 animate-spin text-amber-400" />
          <span>Generando</span>
        </>
      )}
      {status === 'READY' && (
        <>
          <CheckCircle2 className="w-3 h-3 text-emerald-400" />
          <span>Listo</span>
        </>
      )}
      {status === 'FAILED' && (
        <>
          <AlertTriangle className="w-3 h-3 text-rose-400" />
          <span>Fallido</span>
        </>
      )}
    </span>
  );
};
