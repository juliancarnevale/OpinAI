import React, { useState } from 'react';
import { X, Loader2 } from 'lucide-react';
import { useAnalysisStore } from '../../../store/analysisStore';
import { CreateAnalysisForm } from './CreateAnalysisForm';
import type { CreateAnalysisRequest } from '../types/CreateAnalysisRequest';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '../../../config/constants';

interface CreateAnalysisModalProps {
  isOpen: boolean;
  onClose: () => void;
  projectId: string;
}

export const CreateAnalysisModal: React.FC<CreateAnalysisModalProps> = ({ isOpen, onClose, projectId }) => {
  const { createAnalysis, isLoading } = useAnalysisStore();
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const navigate = useNavigate();

  if (!isOpen) return null;

  const handleSubmit = async (data: CreateAnalysisRequest) => {
    setErrorMsg(null);
    try {
      const created = await createAnalysis(projectId, data);
      onClose();
      // Redirigir al detalle del análisis recién creado utilizando ruta centralizada
      navigate(ROUTES.ANALYSIS_DETAIL(created.id));
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorMsg(err.message);
      } else {
        setErrorMsg('Error al guardar el análisis. Inténtelo de nuevo.');
      }
    }
  };

  const handleCloseAttempt = () => {
    if (!isLoading) {
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop - Evita cerrar al hacer clic si está cargando */}
      <div 
        className="absolute inset-0 bg-black/60 backdrop-blur-sm" 
        onClick={handleCloseAttempt} 
      />

      {/* Modal Content */}
      <div className="relative bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl w-full max-w-lg mx-4 z-10 overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex justify-between items-center pb-4 border-b border-slate-800 shrink-0">
          <div>
            <h3 className="text-xl font-bold text-white">Nuevo Análisis</h3>
            <p className="text-xs text-slate-400 mt-0.5">Ingresa las opiniones de tus clientes para comenzar.</p>
          </div>
          <button
            onClick={handleCloseAttempt}
            disabled={isLoading}
            className="text-slate-400 hover:text-white p-1 hover:bg-slate-800 rounded-lg transition disabled:opacity-30 cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Error local */}
        {errorMsg && (
          <div className="bg-rose-950/40 border border-rose-900/50 text-rose-300 p-3 rounded-lg text-xs mt-4 shrink-0">
            {errorMsg}
          </div>
        )}

        {/* Form Body */}
        <div className="flex-1 overflow-y-auto py-4">
          <CreateAnalysisForm onSubmit={handleSubmit} isLoading={isLoading} />
        </div>

        {/* Loading Overlay */}
        {isLoading && (
          <div className="absolute inset-0 bg-slate-950/70 flex flex-col items-center justify-center space-y-3 z-20">
            <Loader2 className="w-10 h-10 text-violet-500 animate-spin" />
            <p className="text-sm font-medium text-slate-300">Guardando y procesando opiniones...</p>
          </div>
        )}
      </div>
    </div>
  );
};
