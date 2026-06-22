import React, { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useAnalysisStore } from '../../../store/analysisStore';
import { useAnalysisPolling } from '../hooks/useAnalysisPolling';
import { AnalysisStatusBadge } from '../components/AnalysisStatusBadge';
import { AnalysisAiResult } from '../components/AnalysisAiResult';
import { ROUTES } from '../../../config/constants';
import {
  ArrowLeft,
  Calendar,
  MessageSquare,
  Trash2,
  Loader2,
  X,
  Database,
  AlertCircle,
  Hourglass,
  RefreshCw,
  AlertOctagon,
  Play,
  WifiOff
} from 'lucide-react';

export const AnalysisDetailPage: React.FC = () => {
  const { analysisId } = useParams<{ analysisId: string }>();
  const { deleteAnalysis, clearError } = useAnalysisStore();
  const navigate = useNavigate();

  const [confirmDelete, setConfirmDelete] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isReprocessing, setIsReprocessing] = useState(false);

  // Setup the polling hook
  const {
    analysis: selectedAnalysis,
    isLoading,
    error: pollingError,
    consecutiveErrors,
    isPolling,
    triggerReprocess,
  } = useAnalysisPolling({ analysisId });

  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch (e) {
      return dateString;
    }
  };

  const handleDelete = async () => {
    if (!analysisId || !selectedAnalysis) return;
    setIsDeleting(true);
    try {
      const projectId = selectedAnalysis.projectId;
      await deleteAnalysis(analysisId);
      navigate(ROUTES.PROJECT_ANALYSES(projectId));
    } catch (err) {
      // Handled by store state
    } finally {
      setIsDeleting(false);
      setConfirmDelete(false);
    }
  };

  const handleReprocess = async () => {
    setIsReprocessing(true);
    try {
      await triggerReprocess();
    } catch (err) {
      // Error is set in polling state
    } finally {
      setIsReprocessing(false);
    }
  };

  if (isLoading && !selectedAnalysis) {
    return (
      <div className="flex flex-col items-center justify-center py-32 space-y-4">
        <Loader2 className="w-12 h-12 text-violet-500 animate-spin" />
        <p className="text-slate-400 text-sm">Cargando los detalles del análisis...</p>
      </div>
    );
  }

  if (pollingError && !selectedAnalysis) {
    return (
      <div className="bg-rose-950/40 border border-rose-800 text-rose-200 p-6 rounded-xl text-center max-w-lg mx-auto space-y-4">
        <AlertCircle className="w-12 h-12 text-rose-500 mx-auto" />
        <div>
          <h3 className="font-bold text-lg">Error al cargar</h3>
          <p className="text-sm text-rose-300 mt-1">{pollingError}</p>
        </div>
        <Link
          to={ROUTES.PROJECTS}
          className="inline-flex items-center space-x-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-lg text-sm transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Volver a Proyectos</span>
        </Link>
      </div>
    );
  }

  if (!selectedAnalysis) return null;

  return (
    <div className="space-y-6">
      {/* Back button and delete action bar */}
      <div className="flex justify-between items-center">
        <Link
          to={ROUTES.PROJECT_ANALYSES(selectedAnalysis.projectId)}
          className="inline-flex items-center space-x-2 text-sm text-slate-400 hover:text-white transition w-fit"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Volver a la Lista</span>
        </Link>

        {confirmDelete ? (
          <div className="flex items-center space-x-2 bg-slate-900 border border-rose-900/50 p-1.5 rounded-lg">
            <span className="text-xs text-rose-400 px-2 font-medium">¿Seguro que deseas eliminar?</span>
            <button
              onClick={handleDelete}
              disabled={isDeleting}
              className="px-3 py-1.5 bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold rounded-lg transition cursor-pointer flex items-center space-x-1"
            >
              {isDeleting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              <span>Eliminar</span>
            </button>
            <button
              onClick={() => setConfirmDelete(false)}
              disabled={isDeleting}
              className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded-lg transition cursor-pointer"
            >
              Cancelar
            </button>
          </div>
        ) : (
          <button
            onClick={() => setConfirmDelete(true)}
            className="inline-flex items-center space-x-1.5 px-3.5 py-2 text-sm bg-rose-950/20 hover:bg-rose-950/40 text-rose-400 hover:text-rose-300 border border-rose-900/30 hover:border-rose-900/50 rounded-lg transition cursor-pointer font-medium"
          >
            <Trash2 className="w-4 h-4" />
            <span>Eliminar Análisis</span>
          </button>
        )}
      </div>

      {/* Network reconnection status alert */}
      {consecutiveErrors > 0 && isPolling && (
        <div className="bg-amber-950/30 border border-amber-800/40 text-amber-200 p-4 rounded-xl flex items-center space-x-3">
          <WifiOff className="w-5 h-5 text-amber-500 animate-pulse shrink-0" />
          <div>
            <p className="font-semibold text-sm">Problemas de conexión detectados</p>
            <p className="text-xs text-amber-400/85">
              Intentando reconectar con el servidor... Intento {consecutiveErrors}/3.
            </p>
          </div>
        </div>
      )}

      {/* Polling Error notification banner */}
      {pollingError && selectedAnalysis && (
        <div className="bg-rose-950/40 border border-rose-800 text-rose-200 p-4 rounded-xl flex items-start justify-between">
          <div className="flex space-x-3">
            <AlertCircle className="w-5 h-5 text-rose-500 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-sm">Problema detectado</p>
              <p className="text-xs text-rose-300 mt-1">{pollingError}</p>
            </div>
          </div>
          <button onClick={clearError} className="text-rose-400 hover:text-rose-200 cursor-pointer">
            <X className="w-5 h-5" />
          </button>
        </div>
      )}

      {/* Analysis Metadata Card */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-6">
        <div>
          <span className="text-xs font-semibold text-violet-400 bg-violet-500/10 border border-violet-500/20 px-2.5 py-1 rounded-md uppercase tracking-wider">
            {selectedAnalysis.projectName}
          </span>
          <h2 className="text-3xl font-extrabold text-white mt-3">{selectedAnalysis.title}</h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 pt-4 border-t border-slate-800/60">
          <div className="flex items-center space-x-3">
            <div className="p-3 bg-slate-950 rounded-xl border border-slate-800">
              <Database className="w-5 h-5 text-violet-500" />
            </div>
            <div>
              <p className="text-[10px] text-slate-500 uppercase tracking-wider font-bold">Estado del análisis</p>
              <div className="mt-1">
                <AnalysisStatusBadge status={selectedAnalysis.status} size="md" />
              </div>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <div className="p-3 bg-slate-950 rounded-xl border border-slate-800">
              <Calendar className="w-5 h-5 text-violet-500" />
            </div>
            <div>
              <p className="text-[10px] text-slate-500 uppercase tracking-wider font-bold">Fecha de Creación</p>
              <p className="text-sm font-bold text-white mt-1">{formatDate(selectedAnalysis.createdAt)}</p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <div className="p-3 bg-slate-950 rounded-xl border border-slate-800">
              <MessageSquare className="w-5 h-5 text-violet-500" />
            </div>
            <div>
              <p className="text-[10px] text-slate-500 uppercase tracking-wider font-bold">Total opiniones</p>
              <p className="text-sm font-bold text-white mt-1">
                {selectedAnalysis.feedbackItems.length} comentarios analizados
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Dynamic Main Body based on Analysis State */}
      {selectedAnalysis.status === 'PENDING' && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-xl text-center space-y-6 max-w-2xl mx-auto my-6 relative overflow-hidden">
          <div className="absolute top-0 right-0 w-32 h-32 bg-violet-600/5 rounded-full blur-2xl pointer-events-none" />
          <div className="p-4 bg-slate-950 rounded-full w-fit mx-auto border border-slate-800">
            <Hourglass className="w-10 h-10 text-violet-400 animate-[spin_4s_linear_infinite]" />
          </div>
          <div className="space-y-2">
            <h3 className="text-xl font-bold text-white">Análisis en Cola de Espera</h3>
            <p className="text-sm text-slate-400 max-w-md mx-auto leading-relaxed">
              Tu análisis está en la cola y comenzará en unos segundos, tan pronto como el motor de ejecución esté libre.
            </p>
          </div>
          <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden border border-slate-800">
            <div className="bg-gradient-to-r from-violet-600 to-fuchsia-600 h-full w-1/3 rounded-full opacity-60 animate-pulse" />
          </div>
          <p className="text-xs text-slate-500 font-medium">Actualizando estado de forma automática...</p>
        </div>
      )}

      {selectedAnalysis.status === 'PROCESSING' && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-xl text-center space-y-6 max-w-2xl mx-auto my-6 relative overflow-hidden">
          <div className="absolute top-0 right-0 w-32 h-32 bg-violet-600/5 rounded-full blur-2xl pointer-events-none" />
          <div className="p-4 bg-slate-950 rounded-full w-fit mx-auto border border-slate-800 relative">
            <div className="absolute inset-0 bg-violet-500/10 rounded-full animate-ping" />
            <RefreshCw className="w-10 h-10 text-violet-500 animate-spin" />
          </div>
          <div className="space-y-2">
            <h3 className="text-xl font-bold text-white">Analizando con Gemini AI</h3>
            <p className="text-sm text-slate-400 max-w-md mx-auto leading-relaxed">
              Nuestra inteligencia artificial está leyendo las opiniones. Extrayendo el sentimiento general y estructurando resúmenes ejecutivos.
            </p>
          </div>
          <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden border border-slate-800 relative">
            <div className="bg-gradient-to-r from-violet-500 via-fuchsia-500 to-violet-500 h-full rounded-full w-2/3 animate-pulse absolute top-0 left-0" />
          </div>
          <p className="text-xs text-slate-500 font-medium">Sondeando cambios cada 2 segundos...</p>
        </div>
      )}

      {selectedAnalysis.status === 'FAILED' && (
        <div className="bg-slate-900 border border-rose-950/60 rounded-2xl p-8 shadow-xl text-center space-y-6 max-w-2xl mx-auto my-6 relative overflow-hidden">
          <div className="absolute top-0 right-0 w-32 h-32 bg-rose-600/5 rounded-full blur-2xl pointer-events-none" />
          <div className="p-4 bg-rose-950/20 rounded-full w-fit mx-auto border border-rose-900/30">
            <AlertOctagon className="w-10 h-10 text-rose-500" />
          </div>
          <div className="space-y-2">
            <h3 className="text-xl font-bold text-white">El Análisis No Pudo Completarse</h3>
            <p className="text-sm text-rose-350 max-w-md mx-auto leading-relaxed">
              Ocurrió un error al procesar las opiniones de este análisis. Puedes intentar forzar un reprocesamiento del análisis con la IA.
            </p>
          </div>
          
          <div className="pt-2">
            <button
              onClick={handleReprocess}
              disabled={isReprocessing}
              className="inline-flex items-center space-x-2 px-5 py-2.5 bg-gradient-to-r from-rose-600 to-red-650 hover:from-rose-500 hover:to-red-650 text-white font-bold rounded-xl text-sm transition shadow-lg shadow-rose-950/20 hover:shadow-rose-950/35 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isReprocessing ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Play className="w-4 h-4" />
              )}
              <span>Reprocesar Análisis</span>
            </button>
          </div>
        </div>
      )}

      {selectedAnalysis.status === 'COMPLETED' && (
        <AnalysisAiResult
          overallSentiment={selectedAnalysis.overallSentiment}
          executiveSummary={selectedAnalysis.executiveSummary}
          keyIssues={selectedAnalysis.keyIssues}
          improvementOpportunities={selectedAnalysis.improvementOpportunities}
          sentimentDistribution={selectedAnalysis.sentimentDistribution}
        />
      )}

      {/* Opinions List Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        <div className="p-5 border-b border-slate-800 bg-slate-900/50 flex items-center justify-between">
          <h3 className="text-lg font-bold text-white">Listado de Opiniones Cargadas</h3>
          <span className="text-xs px-2.5 py-0.5 rounded-full bg-slate-950 text-slate-400 border border-slate-800 font-medium">
            Entrada Manual
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-sm">
            <thead>
              <tr className="border-b border-slate-800 bg-slate-950/20 text-slate-400 font-semibold">
                <th className="py-4 px-6 w-16 text-center">#</th>
                <th className="py-4 px-6">Contenido del Comentario</th>
                <th className="py-4 px-6 w-40 text-center">Método</th>
                <th className="py-4 px-6 w-48 text-right">Fecha de Carga</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {selectedAnalysis.feedbackItems.map((item, index) => (
                <tr key={item.id} className="hover:bg-slate-800/10 transition text-slate-300">
                  <td className="py-4 px-6 text-center font-mono text-xs text-slate-600">{index + 1}</td>
                  <td className="py-4 px-6 text-white font-medium leading-relaxed max-w-lg break-words">
                    {item.content}
                  </td>
                  <td className="py-4 px-6 text-center">
                    <span className="inline-block px-2.5 py-0.5 text-xs font-semibold rounded bg-violet-950/30 text-violet-400 border border-violet-900/20">
                      MANUAL
                    </span>
                  </td>
                  <td className="py-4 px-6 text-right text-xs text-slate-500 font-medium">
                    {formatDate(item.createdAt)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AnalysisDetailPage;
